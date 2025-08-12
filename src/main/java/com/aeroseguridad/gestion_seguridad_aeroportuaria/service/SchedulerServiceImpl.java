package com.aeroseguridad.gestion_seguridad_aeroportuaria.service;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.dto.ScheduleRequest;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.dto.ScheduleResult;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.*;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SchedulerServiceImpl implements SchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerServiceImpl.class);

    private final VueloRepository vueloRepository;
    private final AgenteRepository agenteRepository;
    private final TurnoRepository turnoRepository;
    private final PermisoRepository permisoRepository;

    @Autowired
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
            Vuelo managedVuelo = vueloRepository.findByIdWithFullDetails(vuelo.getIdVuelo()).orElse(vuelo);
            if (managedVuelo.getNecesidades() == null || managedVuelo.getNecesidades().isEmpty()) continue;
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
                        // --- INICIO DE LA NUEVA LÓGICA DE INTELIGENCIA DE CONFLICTOS ---
                        newAssignment.setEstado(EstadoAsignacion.CONFLICTO_NO_CUBIERTO);

                        // Analizamos la causa raíz del fallo para obtener un diagnóstico preciso.
                        Map.Entry<TipoConflicto, String> causa = analizarCausaDeFallo(necesidad, agentesActivos, currentAssignmentsInLoop, turnosPorAgente, permisosAprobadosPorAgente);
                        newAssignment.setTipoConflicto(causa.getKey());
                        newAssignment.setDetalleConflicto(causa.getValue());

                        result.getConflicts().add(newAssignment);
                        // --- FIN DE LA NUEVA LÓGICA ---
                    }
                    managedVuelo.addAssignment(newAssignment);
                }
            }
            vuelosToSave.add(managedVuelo);
        }

        vueloRepository.saveAll(vuelosToSave);
        return CompletableFuture.completedFuture(result);
    }

    private Optional<Agente> findBestFitAgentFor(NecesidadVuelo necesidad, List<Agente> todosAgentes, List<Assignment> currentAssignmentsInLoop, Map<Long, List<Turno>> turnosPorAgente, Map<Long, List<Permiso>> permisosPorAgente) {
        Vuelo vuelo = necesidad.getVuelo();
        PosicionSeguridad posicion = necesidad.getPosicion();
        LocalDateTime inicioServicio = necesidad.getInicioCobertura();
        LocalDateTime finServicio = necesidad.getFinCobertura();
        Set<Long> agentesYaAsignadosIds = currentAssignmentsInLoop.stream().filter(a -> a.getAgente() != null).map(a -> a.getAgente().getIdAgente()).collect(Collectors.toSet());

        return todosAgentes.stream()
            .filter(agente -> !agentesYaAsignadosIds.contains(agente.getIdAgente()))
            .filter(agente -> !isAgentOnLeave(agente, inicioServicio, finServicio, permisosPorAgente.get(agente.getIdAgente())))
            .filter(agente -> isAgentOnShift(agente, inicioServicio, finServicio, turnosPorAgente.get(agente.getIdAgente())))
            .filter(agente -> agente.getAerolineasPermitidas() != null && agente.getAerolineasPermitidas().contains(vuelo.getAerolinea()))
            .filter(agente -> {
                Genero generoRequerido = posicion.getGeneroRequerido();
                return generoRequerido == Genero.OTRO || agente.getGenero() == generoRequerido;
            })
            .filter(agente -> {
                if (!posicion.isRequiereEntrenamientoEspecial()) return true;
                return agente.getPosicionesHabilitadas() != null && agente.getPosicionesHabilitadas().contains(posicion);
            })
            .findFirst();
    }

    private Map.Entry<TipoConflicto, String> analizarCausaDeFallo(NecesidadVuelo necesidad, List<Agente> todosAgentes, List<Assignment> currentAssignmentsInLoop, Map<Long, List<Turno>> turnosPorAgente, Map<Long, List<Permiso>> permisosPorAgente) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        if (todosAgentes.isEmpty()) {
            return new AbstractMap.SimpleEntry<>(TipoConflicto.SIN_PERSONAL_DISPONIBLE, "No hay agentes activos en el sistema.");
        }

        Set<Long> agentesYaAsignadosIds = currentAssignmentsInLoop.stream().filter(a -> a.getAgente() != null).map(a -> a.getAgente().getIdAgente()).collect(Collectors.toSet());
        List<Agente> candidatos = todosAgentes.stream().filter(agente -> !agentesYaAsignadosIds.contains(agente.getIdAgente())).collect(Collectors.toList());
        long totalCandidatosInicial = candidatos.size();
        if (totalCandidatosInicial == 0) return new AbstractMap.SimpleEntry<>(TipoConflicto.SIN_PERSONAL_DISPONIBLE, "Todos los agentes ya están asignados a otra posición en este vuelo.");

        long countOnLeave = candidatos.stream().filter(agente -> isAgentOnLeave(agente, necesidad.getInicioCobertura(), necesidad.getFinCobertura(), permisosPorAgente.get(agente.getIdAgente()))).count();
        candidatos.removeIf(agente -> isAgentOnLeave(agente, necesidad.getInicioCobertura(), necesidad.getFinCobertura(), permisosPorAgente.get(agente.getIdAgente())));
        if (candidatos.isEmpty()) return new AbstractMap.SimpleEntry<>(TipoConflicto.CON_PERMISO_APROBADO, String.format("%d de %d agentes tenían permiso/ausencia.", countOnLeave, totalCandidatosInicial));

        long countOffShift = candidatos.stream().filter(agente -> !isAgentOnShift(agente, necesidad.getInicioCobertura(), necesidad.getFinCobertura(), turnosPorAgente.get(agente.getIdAgente()))).count();
        candidatos.removeIf(agente -> !isAgentOnShift(agente, necesidad.getInicioCobertura(), necesidad.getFinCobertura(), turnosPorAgente.get(agente.getIdAgente())));
        if (candidatos.isEmpty()) return new AbstractMap.SimpleEntry<>(TipoConflicto.FUERA_DE_TURNO, String.format("El turno de %d agentes no cubría el horario de %s a %s.", countOffShift, necesidad.getInicioCobertura().format(timeFormatter), necesidad.getFinCobertura().format(timeFormatter)));

        long countNoAirlinePermission = candidatos.stream().filter(agente -> agente.getAerolineasPermitidas() == null || !agente.getAerolineasPermitidas().contains(necesidad.getVuelo().getAerolinea())).count();
        candidatos.removeIf(agente -> agente.getAerolineasPermitidas() == null || !agente.getAerolineasPermitidas().contains(necesidad.getVuelo().getAerolinea()));
        if (candidatos.isEmpty()) return new AbstractMap.SimpleEntry<>(TipoConflicto.SIN_PERMISO_AEROLINEA, String.format("%d agentes no tenían permiso para la aerolínea %s.", countNoAirlinePermission, necesidad.getVuelo().getAerolinea().getNombre()));

        PosicionSeguridad posicion = necesidad.getPosicion();
        long countNoGender = candidatos.stream().filter(agente -> !(posicion.getGeneroRequerido() == Genero.OTRO || agente.getGenero() == posicion.getGeneroRequerido())).count();
        candidatos.removeIf(agente -> !(posicion.getGeneroRequerido() == Genero.OTRO || agente.getGenero() == posicion.getGeneroRequerido()));
        if (candidatos.isEmpty()) return new AbstractMap.SimpleEntry<>(TipoConflicto.NO_CUMPLE_GENERO, String.format("%d agentes no cumplían el requisito de género '%s'.", countNoGender, posicion.getGeneroRequerido()));

        long countNoSkill = candidatos.stream().filter(agente -> posicion.isRequiereEntrenamientoEspecial() && (agente.getPosicionesHabilitadas() == null || !agente.getPosicionesHabilitadas().contains(posicion))).count();
        candidatos.removeIf(agente -> posicion.isRequiereEntrenamientoEspecial() && (agente.getPosicionesHabilitadas() == null || !agente.getPosicionesHabilitadas().contains(posicion)));
        if (candidatos.isEmpty()) return new AbstractMap.SimpleEntry<>(TipoConflicto.SIN_HABILIDAD_REQUERIDA, String.format("%d agentes no tenían la habilidad '%s' requerida.", countNoSkill, posicion.getNombrePosicion()));
        
        return new AbstractMap.SimpleEntry<>(TipoConflicto.SIN_PERSONAL_DISPONIBLE, "No se encontró una causa específica del conflicto.");
    }

    private boolean isAgentOnShift(Agente agente, LocalDateTime inicioServicio, LocalDateTime finServicio, List<Turno> turnosDelAgente) {
        if (turnosDelAgente == null || turnosDelAgente.isEmpty()) return false;
        return turnosDelAgente.stream().anyMatch(turno -> !turno.getInicioTurno().isAfter(inicioServicio) && !turno.getFinTurno().isBefore(finServicio));
    }

    private boolean isAgentOnLeave(Agente agente, LocalDateTime inicioServicio, LocalDateTime finServicio, List<Permiso> permisosDelAgente) {
        if (permisosDelAgente == null || permisosDelAgente.isEmpty()) return false;
        return permisosDelAgente.stream().anyMatch(permiso -> permiso.getFechaInicio().isBefore(finServicio) && permiso.getFechaFin().isAfter(inicioServicio));
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
            .filter(agente -> findBestFitAgentFor(necesidad, List.of(agente), List.of(), turnosPorAgente, permisosAprobadosPorAgente).isPresent())
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