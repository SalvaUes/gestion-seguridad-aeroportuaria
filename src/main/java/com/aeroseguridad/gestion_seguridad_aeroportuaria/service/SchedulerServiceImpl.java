package com.aeroseguridad.gestion_seguridad_aeroportuaria.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.dto.ScheduleRequest;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.dto.ScheduleResult;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Aerolinea;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Assignment;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.EstadoAsignacion;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.EstadoSolicitudPermiso;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Genero;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.NecesidadVuelo;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Permiso;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PosicionSeguridad;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.TipoConflicto;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Turno;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Vuelo;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.AgenteRepository;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.PermisoRepository;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.TurnoRepository;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.VueloRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional(readOnly = true)
public class SchedulerServiceImpl implements SchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerServiceImpl.class);

    private final VueloRepository vueloRepository;
    private final AgenteRepository agenteRepository;
    private final TurnoRepository turnoRepository;
    private final PermisoRepository permisoRepository;

    public SchedulerServiceImpl(VueloRepository vueloRepository, AgenteRepository agenteRepository, TurnoRepository turnoRepository, PermisoRepository permisoRepository) {
        this.vueloRepository = vueloRepository;
        this.agenteRepository = agenteRepository;
        this.turnoRepository = turnoRepository;
        this.permisoRepository = permisoRepository;
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public Future<ScheduleResult> generateSchedule(ScheduleRequest request) {
        logger.info("Generando horario para el período: {} a {}", request.getStartDate(), request.getEndDate());

        LocalDateTime inicioPeriodo = request.getStartDate().atStartOfDay();
        LocalDateTime finPeriodo = request.getEndDate().atTime(23, 59, 59);

        List<Vuelo> vuelosEnPeriodo = vueloRepository.findVuelosInPeriodFetchingAerolinea(inicioPeriodo, finPeriodo);
        List<Agente> agentesActivos = agenteRepository.findActivosWithDetails();
        Map<Long, List<Turno>> turnosPorAgente = turnoRepository.findByFechasSolapadasFetchingAgente(inicioPeriodo, finPeriodo).stream()
                .collect(Collectors.groupingBy(t -> t.getAgente().getIdAgente()));
        Map<Long, List<Permiso>> permisosAprobadosPorAgente = permisoRepository.findByFechasSolapadasFetchingAgente(inicioPeriodo, finPeriodo).stream()
                .filter(p -> p.getEstadoSolicitud() == EstadoSolicitudPermiso.APROBADO)
                .collect(Collectors.groupingBy(p -> p.getAgente().getIdAgente()));

        ScheduleResult result = new ScheduleResult();
        List<Vuelo> vuelosToSave = new ArrayList<>();

        for (Vuelo vuelo : vuelosEnPeriodo) {
            processVuelo(vuelo, agentesActivos, turnosPorAgente, permisosAprobadosPorAgente, result);
            vuelosToSave.add(vuelo);
        }

        vueloRepository.saveAll(vuelosToSave);
        return CompletableFuture.completedFuture(result);
    }

    private void processVuelo(Vuelo vuelo, List<Agente> agentesActivos, Map<Long, List<Turno>> turnosPorAgente, Map<Long, List<Permiso>> permisosAprobadosPorAgente, ScheduleResult result) {
        Vuelo managedVuelo = vueloRepository.findByIdWithFullDetails(vuelo.getIdVuelo()).orElse(vuelo);
        if (managedVuelo.getNecesidades() == null || managedVuelo.getNecesidades().isEmpty()) {
            return;
        }
        managedVuelo.getAssignments().clear();

        for (NecesidadVuelo necesidad : managedVuelo.getNecesidades()) {
            for (int i = 0; i < necesidad.getCantidadAgentes(); i++) {
                List<Assignment> currentAssignmentsInLoop = new ArrayList<>(managedVuelo.getAssignments());
                Optional<Agente> bestFitAgent = findBestFitAgentFor(necesidad, agentesActivos, currentAssignmentsInLoop, turnosPorAgente, permisosAprobadosPorAgente);

                Assignment newAssignment = new Assignment();
                newAssignment.setVuelo(managedVuelo);
                newAssignment.setPosicionSeguridad(necesidad.getPosicion());
                newAssignment.setFechaAsignacion(managedVuelo.getFechaHoraLlegada().toLocalDate());

                if (bestFitAgent.isPresent()) {
                    newAssignment.setAgente(bestFitAgent.get());
                    newAssignment.setEstado(EstadoAsignacion.ASIGNADO);
                    result.getAssignments().add(newAssignment);
                } else {
                    newAssignment.setEstado(EstadoAsignacion.CONFLICTO_NO_CUBIERTO);
                    Map.Entry<TipoConflicto, String> causa = analizarCausaDeFallo(necesidad, agentesActivos, currentAssignmentsInLoop, turnosPorAgente, permisosAprobadosPorAgente);
                    newAssignment.setTipoConflicto(causa.getKey());
                    newAssignment.setDetalleConflicto(causa.getValue());
                    result.getConflicts().add(newAssignment);
                }
                managedVuelo.addAssignment(newAssignment);
            }
        }
    }

    private Optional<Agente> findBestFitAgentFor(NecesidadVuelo necesidad, List<Agente> todosAgentes, List<Assignment> currentAssignmentsInLoop, Map<Long, List<Turno>> turnosPorAgente, Map<Long, List<Permiso>> permisosPorAgente) {
        Set<Long> agentesYaAsignadosIds = currentAssignmentsInLoop.stream()
                .filter(a -> a.getAgente() != null)
                .map(a -> a.getAgente().getIdAgente())
                .collect(Collectors.toSet());

        return todosAgentes.stream()
            .filter(agente -> !agentesYaAsignadosIds.contains(agente.getIdAgente()))
            .filter(agente -> isAgentAvailable(agente, necesidad, turnosPorAgente.get(agente.getIdAgente()), permisosPorAgente.get(agente.getIdAgente())))
            .findFirst();
    }

    private Map.Entry<TipoConflicto, String> analizarCausaDeFallo(NecesidadVuelo necesidad, List<Agente> todosAgentes, List<Assignment> currentAssignmentsInLoop, Map<Long, List<Turno>> turnosPorAgente, Map<Long, List<Permiso>> permisosPorAgente) {
        if (todosAgentes.isEmpty()) {
            return new AbstractMap.SimpleEntry<>(TipoConflicto.SIN_PERSONAL_DISPONIBLE, "No hay agentes activos en el sistema.");
        }

        Set<Long> agentesYaAsignadosIds = currentAssignmentsInLoop.stream()
                .filter(a -> a.getAgente() != null)
                .map(a -> a.getAgente().getIdAgente())
                .collect(Collectors.toSet());

        List<Agente> candidatos = todosAgentes.stream()
                .filter(agente -> !agentesYaAsignadosIds.contains(agente.getIdAgente()))
                .collect(Collectors.toList());

        if (candidatos.isEmpty()) {
            return new AbstractMap.SimpleEntry<>(TipoConflicto.SIN_PERSONAL_DISPONIBLE, "Todos los agentes ya están asignados a otra posición en este vuelo.");
        }

        for (Agente agente : candidatos) {
            if (isAgentOnLeave(agente, necesidad.getInicioCobertura(), necesidad.getFinCobertura(), permisosPorAgente.get(agente.getIdAgente()))) {
                return new AbstractMap.SimpleEntry<>(TipoConflicto.CON_PERMISO_APROBADO, String.format("El agente %s tiene un permiso aprobado que se solapa.", agente.getNombreCompleto()));
            }
            if (!isAgentOnShift(agente, necesidad.getInicioCobertura(), necesidad.getFinCobertura(), turnosPorAgente.get(agente.getIdAgente()))) {
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                return new AbstractMap.SimpleEntry<>(TipoConflicto.FUERA_DE_TURNO, String.format("El turno del agente %s no cubre el horario de %s a %s.", agente.getNombreCompleto(), necesidad.getInicioCobertura().format(timeFormatter), necesidad.getFinCobertura().format(timeFormatter)));
            }
            if (!hasAirlinePermission(agente, necesidad.getVuelo().getAerolinea())) {
                return new AbstractMap.SimpleEntry<>(TipoConflicto.SIN_PERMISO_AEROLINEA, String.format("El agente %s no tiene permiso para la aerolínea %s.", agente.getNombreCompleto(), necesidad.getVuelo().getAerolinea().getNombre()));
            }
            if (!meetsGenderRequirement(agente, necesidad.getPosicion())) {
                return new AbstractMap.SimpleEntry<>(TipoConflicto.NO_CUMPLE_GENERO, String.format("El agente %s no cumple el requisito de género '%s'.", agente.getNombreCompleto(), necesidad.getPosicion().getGeneroRequerido()));
            }
            if (!hasRequiredSkill(agente, necesidad.getPosicion())) {
                return new AbstractMap.SimpleEntry<>(TipoConflicto.SIN_HABILIDAD_REQUERIDA, String.format("El agente %s no tiene la habilidad '%s' requerida.", agente.getNombreCompleto(), necesidad.getPosicion().getNombrePosicion()));
            }
        }
        
        return new AbstractMap.SimpleEntry<>(TipoConflicto.SIN_PERSONAL_DISPONIBLE, "No se encontró una causa específica del conflicto, pero ningún agente cumplió todos los criterios.");
    }

    private boolean isAgentAvailable(Agente agente, NecesidadVuelo necesidad, List<Turno> turnos, List<Permiso> permisos) {
        return !isAgentOnLeave(agente, necesidad.getInicioCobertura(), necesidad.getFinCobertura(), permisos) &&
               isAgentOnShift(agente, necesidad.getInicioCobertura(), necesidad.getFinCobertura(), turnos) &&
               hasAirlinePermission(agente, necesidad.getVuelo().getAerolinea()) &&
               meetsGenderRequirement(agente, necesidad.getPosicion()) &&
               hasRequiredSkill(agente, necesidad.getPosicion());
    }

    private boolean isAgentOnShift(Agente agente, LocalDateTime inicioServicio, LocalDateTime finServicio, List<Turno> turnosDelAgente) {
        if (turnosDelAgente == null || turnosDelAgente.isEmpty()) return false;
        return turnosDelAgente.stream().anyMatch(turno -> !turno.getInicioTurno().isAfter(inicioServicio) && !turno.getFinTurno().isBefore(finServicio));
    }

    private boolean isAgentOnLeave(Agente agente, LocalDateTime inicioServicio, LocalDateTime finServicio, List<Permiso> permisosDelAgente) {
        if (permisosDelAgente == null || permisosDelAgente.isEmpty()) return false;
        return permisosDelAgente.stream().anyMatch(permiso -> permiso.getFechaInicio().isBefore(finServicio) && permiso.getFechaFin().isAfter(inicioServicio));
    }

    private boolean hasAirlinePermission(Agente agente, Aerolinea aerolinea) {
        return agente.getAerolineasPermitidas() != null && agente.getAerolineasPermitidas().contains(aerolinea);
    }

    private boolean meetsGenderRequirement(Agente agente, PosicionSeguridad posicion) {
        Genero generoRequerido = posicion.getGeneroRequerido();
        return generoRequerido == Genero.OTRO || agente.getGenero() == generoRequerido;
    }

    private boolean hasRequiredSkill(Agente agente, PosicionSeguridad posicion) {
        if (!posicion.isRequiereEntrenamientoEspecial()) return true;
        return agente.getPosicionesHabilitadas() != null && agente.getPosicionesHabilitadas().contains(posicion);
    }

    @Override
    @Transactional
    public List<Agente> findCandidates(Long idVuelo, NecesidadVuelo necesidad, List<Assignment> currentAssignments) {
        // Esta implementación se mantiene, ya que sigue siendo útil para la UI si se necesita.
        Vuelo managedVuelo = vueloRepository.findByIdWithFullDetails(idVuelo).orElseThrow(() -> new EntityNotFoundException("Vuelo no encontrado con ID: " + idVuelo));
        necesidad.setVuelo(managedVuelo);
        List<Agente> agentesActivos = agenteRepository.findActivosWithDetails();
        Map<Long, List<Turno>> turnosPorAgente = turnoRepository.findByFechasSolapadasFetchingAgente(necesidad.getInicioCobertura(), necesidad.getFinCobertura()).stream().collect(Collectors.groupingBy(t -> t.getAgente().getIdAgente()));
        Map<Long, List<Permiso>> permisosAprobadosPorAgente = permisoRepository.findByFechasSolapadasFetchingAgente(necesidad.getInicioCobertura(), necesidad.getFinCobertura()).stream().filter(p -> p.getEstadoSolicitud() == EstadoSolicitudPermiso.APROBADO).collect(Collectors.groupingBy(p -> p.getAgente().getIdAgente()));
        List<Agente> agentesYaAsignados = currentAssignments.stream().filter(a -> a.getAgente() != null).map(Assignment::getAgente).collect(Collectors.toList());
        
        return agentesActivos.stream()
            .filter(agente -> !agentesYaAsignados.contains(agente))
            .filter(agente -> isAgentAvailable(agente, necesidad, turnosPorAgente.get(agente.getIdAgente()), permisosAprobadosPorAgente.get(agente.getIdAgente())))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public void updateAssignmentsForVuelo(Vuelo vueloDetached, List<Assignment> detachedAssignments) {
        // Esta implementación se mantiene, ya que es para la edición manual de asignaciones.
        Vuelo managedVuelo = vueloRepository.findByIdWithFullDetails(vueloDetached.getIdVuelo()).orElseThrow(() -> new EntityNotFoundException("Vuelo no encontrado con ID: " + vueloDetached.getIdVuelo()));
        managedVuelo.getAssignments().clear();
        for (Assignment detachedAssignment : detachedAssignments) {
            Assignment newManagedAssignment = new Assignment();
            newManagedAssignment.setPosicionSeguridad(detachedAssignment.getPosicionSeguridad());
            newManagedAssignment.setAgente(detachedAssignment.getAgente());
            newManagedAssignment.setEstado(detachedAssignment.getEstado());
            newManagedAssignment.setFechaAsignacion(detachedAssignment.getFechaAsignacion());
            managedVuelo.addAssignment(newManagedAssignment);
        }
        vueloRepository.save(managedVuelo);
        logger.info("Se guardaron {} asignaciones para el vuelo {}", managedVuelo.getAssignments().size(), managedVuelo.getNumeroVuelo());
    }
}