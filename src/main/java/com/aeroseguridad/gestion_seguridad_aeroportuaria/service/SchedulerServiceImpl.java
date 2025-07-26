package com.aeroseguridad.gestion_seguridad_aeroportuaria.service;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.dto.ScheduleRequest;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.dto.ScheduleResult;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.*;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
public class SchedulerServiceImpl implements SchedulerService {
    
    private static final Logger logger = LoggerFactory.getLogger(SchedulerServiceImpl.class);

    private final VueloRepository vueloRepository;
    private final AgenteRepository agenteRepository;
    private final AssignmentRepository assignmentRepository;
    private final TurnoRepository turnoRepository;
    private final PermisoRepository permisoRepository;

    @Autowired
    public SchedulerServiceImpl(VueloRepository vueloRepository, AgenteRepository agenteRepository, AssignmentRepository assignmentRepository, TurnoRepository turnoRepository, PermisoRepository permisoRepository) {
        this.vueloRepository = vueloRepository;
        this.agenteRepository = agenteRepository;
        this.assignmentRepository = assignmentRepository;
        this.turnoRepository = turnoRepository;
        this.permisoRepository = permisoRepository;
    }

    @Override
    @Transactional
    public Future<ScheduleResult> generateSchedule(ScheduleRequest request) {
        logger.info("Generando horario para el período: {} a {}", request.getStartDate(), request.getEndDate());
        
        LocalDateTime inicioPeriodo = request.getStartDate().atStartOfDay();
        LocalDateTime finPeriodo = request.getEndDate().atTime(23, 59, 59);
        
        List<Vuelo> vuelosEnPeriodo = vueloRepository.findVuelosInPeriodFetchingAerolinea(inicioPeriodo, finPeriodo);
        List<Agente> agentesActivos = agenteRepository.findActivosFetchingPosiciones();
        
        List<Turno> todosLosTurnos = turnoRepository.findByFechasSolapadasFetchingAgente(inicioPeriodo, finPeriodo);
        List<Permiso> todosLosPermisos = permisoRepository.findByFechasSolapadasFetchingAgente(inicioPeriodo, finPeriodo);

        Map<Long, List<Turno>> turnosPorAgente = todosLosTurnos.stream()
                .collect(Collectors.groupingBy(t -> t.getAgente().getIdAgente()));
        
        Map<Long, List<Permiso>> permisosAprobadosPorAgente = todosLosPermisos.stream()
                .filter(p -> p.getEstadoSolicitud() == EstadoSolicitudPermiso.APROBADO)
                .collect(Collectors.groupingBy(p -> p.getAgente().getIdAgente()));
        
        ScheduleResult result = new ScheduleResult();
        List<Assignment> assignmentsToSave = new ArrayList<>();
        List<Assignment> assignmentsInThisRun = new ArrayList<>();

        for (Vuelo vuelo : vuelosEnPeriodo) {
            if (vuelo.getNecesidades() == null || vuelo.getNecesidades().isEmpty()) continue;

            for (NecesidadVuelo necesidad : vuelo.getNecesidades()) {
                PosicionSeguridad posicionRequerida = necesidad.getPosicion();
                int cantidadNecesaria = necesidad.getCantidadAgentes();
                for (int i = 0; i < cantidadNecesaria; i++) {
                    logger.debug("Buscando agente para Vuelo {}, Posición {} ({}/{})", vuelo.getNumeroVuelo(), posicionRequerida.getNombrePosicion(), i + 1, cantidadNecesaria);
                    
                    Optional<Agente> bestFitAgent = findBestFitAgentFor(necesidad, agentesActivos, assignmentsInThisRun, turnosPorAgente, permisosAprobadosPorAgente);
                    
                    Assignment newAssignment = new Assignment();
                    newAssignment.setVuelo(vuelo);
                    newAssignment.setPosicionSeguridad(posicionRequerida);
                    newAssignment.setFechaAsignacion(vuelo.getFechaHoraLlegada().toLocalDate());

                    if (bestFitAgent.isPresent()) {
                        Agente agenteAsignado = bestFitAgent.get();
                        newAssignment.setAgente(agenteAsignado);
                        newAssignment.setEstado("ASIGNADO");
                        assignmentsToSave.add(newAssignment);
                        result.getAssignments().add(newAssignment);
                        assignmentsInThisRun.add(newAssignment); 
                        logger.info("Asignado: Agente {} -> Vuelo {}, Posición {}", agenteAsignado.getNombreCompleto(), vuelo.getNumeroVuelo(), posicionRequerida.getNombrePosicion());
                    } else {
                        newAssignment.setEstado("CONFLICTO_NO_CUBIERTO");
                        result.getConflicts().add(newAssignment);
                        assignmentsInThisRun.add(newAssignment);
                        logger.warn("CONFLICTO: No se encontró agente para Vuelo {}, Posición {}", vuelo.getNumeroVuelo(), posicionRequerida.getNombrePosicion());
                        break; 
                    }
                }
            }
        }
        
        assignmentRepository.saveAll(assignmentsToSave);
        return CompletableFuture.completedFuture(result);
    }
    
    private Optional<Agente> findBestFitAgentFor(NecesidadVuelo necesidad, List<Agente> todosAgentes, List<Assignment> currentAssignmentsInLoop, Map<Long, List<Turno>> turnosPorAgente, Map<Long, List<Permiso>> permisosPorAgente) {
        Vuelo vuelo = necesidad.getVuelo();
        PosicionSeguridad posicion = necesidad.getPosicion();
        LocalDateTime inicioServicio = necesidad.getInicioCobertura();
        LocalDateTime finServicio = necesidad.getFinCobertura();

        return todosAgentes.stream()
            .filter(agente -> currentAssignmentsInLoop.stream().noneMatch(a -> a.getAgente() != null && a.getAgente().equals(agente)))
            .filter(agente -> {
                if (agente.getPermisosAerolinea() == null) return false;
                return agente.getPermisosAerolinea().stream()
                        .anyMatch(permiso -> permiso.getAerolinea().getIdAerolinea().equals(vuelo.getAerolinea().getIdAerolinea()));
            })
            .filter(agente -> {
                Genero generoRequerido = posicion.getGeneroRequerido();
                return generoRequerido == Genero.OTRO || agente.getGenero() == generoRequerido;
            })
            .filter(agente -> {
                if (!posicion.isRequiereEntrenamientoEspecial()) return true;
                return agente.getPosicionesHabilitadas() != null && agente.getPosicionesHabilitadas().contains(posicion);
            })
            .filter(agente -> isAgentOnShift(agente, inicioServicio, finServicio, turnosPorAgente.get(agente.getIdAgente())))
            .filter(agente -> !isAgentOnLeave(agente, inicioServicio, finServicio, permisosPorAgente.get(agente.getIdAgente())))
            .findFirst(); 
    }

    private boolean isAgentOnShift(Agente agente, LocalDateTime inicioServicio, LocalDateTime finServicio, List<Turno> turnosDelAgente) {
        if (turnosDelAgente == null || turnosDelAgente.isEmpty()) return false;
        return turnosDelAgente.stream().anyMatch(turno -> 
            !turno.getInicioTurno().isAfter(inicioServicio) && !turno.getFinTurno().isBefore(finServicio)
        );
    }

    private boolean isAgentOnLeave(Agente agente, LocalDateTime inicioServicio, LocalDateTime finServicio, List<Permiso> permisosDelAgente) {
        if (permisosDelAgente == null || permisosDelAgente.isEmpty()) return false;
        return permisosDelAgente.stream().anyMatch(permiso -> 
            permiso.getFechaInicio().isBefore(finServicio) && permiso.getFechaFin().isAfter(inicioServicio)
        );
    }

    // --- NUEVO MÉTODO: Implementación para encontrar candidatos ---
    @Override
    public List<Agente> findCandidates(NecesidadVuelo necesidad, List<Assignment> currentAssignments) {
        List<Agente> agentesActivos = agenteRepository.findActivosFetchingPosiciones();
        Map<Long, List<Turno>> turnosPorAgente = turnoRepository.findByFechasSolapadasFetchingAgente(necesidad.getInicioCobertura(), necesidad.getFinCobertura()).stream().collect(Collectors.groupingBy(t -> t.getAgente().getIdAgente()));
        Map<Long, List<Permiso>> permisosAprobadosPorAgente = permisoRepository.findByFechasSolapadasFetchingAgente(necesidad.getInicioCobertura(), necesidad.getFinCobertura()).stream().filter(p -> p.getEstadoSolicitud() == EstadoSolicitudPermiso.APROBADO).collect(Collectors.groupingBy(p -> p.getAgente().getIdAgente()));

        List<Agente> agentesYaAsignados = currentAssignments.stream()
                .filter(a -> a.getAgente() != null)
                .map(Assignment::getAgente)
                .collect(Collectors.toList());

        return agentesActivos.stream()
            .filter(agente -> !agentesYaAsignados.contains(agente))
            .filter(agente -> findBestFitAgentFor(necesidad, List.of(agente), List.of(), turnosPorAgente, permisosAprobadosPorAgente).isPresent())
            .collect(Collectors.toList());
    }

    // --- NUEVO MÉTODO: Implementación para guardar los cambios ---
    @Override
    @Transactional
    public void updateAssignmentsForVuelo(Vuelo vuelo, List<Assignment> newAssignments) {
        logger.info("Actualizando asignaciones para el vuelo: {}", vuelo.getNumeroVuelo());
        assignmentRepository.deleteAllByVuelo(vuelo);
        assignmentRepository.saveAll(newAssignments);
        logger.info("Se guardaron {} nuevas asignaciones.", newAssignments.size());
    }
}