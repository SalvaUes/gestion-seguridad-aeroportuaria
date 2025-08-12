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
                    Optional<Agente> bestFitAgent = findBestFitAgentFor(necesidad, agentesActivos, new ArrayList<>(managedVuelo.getAssignments()), turnosPorAgente, permisosAprobadosPorAgente);
                    
                    Assignment newAssignment = new Assignment();
                    newAssignment.setPosicionSeguridad(necesidad.getPosicion());
                    newAssignment.setFechaAsignacion(managedVuelo.getFechaHoraLlegada().toLocalDate());

                    if (bestFitAgent.isPresent()) {
                        newAssignment.setAgente(bestFitAgent.get());
                        newAssignment.setEstado(EstadoAsignacion.ASIGNADO);
                        result.getAssignments().add(newAssignment);
                    } else {
                        newAssignment.setEstado(EstadoAsignacion.CONFLICTO_NO_CUBIERTO);
                        result.getConflicts().add(newAssignment);
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
        Set<Long> agentesYaAsignadosIds = currentAssignmentsInLoop.stream()
                .filter(a -> a.getAgente() != null)
                .map(a -> a.getAgente().getIdAgente())
                .collect(Collectors.toSet());

        return todosAgentes.stream()
            .filter(agente -> !agentesYaAsignadosIds.contains(agente.getIdAgente()))
            
            // --- REFACTORIZADO: Lógica de filtro de aerolínea simplificada y más eficiente ---
            .filter(agente -> agente.getAerolineasPermitidas() != null && agente.getAerolineasPermitidas().contains(vuelo.getAerolinea()))
            
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
            turno.getInicioTurno().isBefore(finServicio) && turno.getFinTurno().isAfter(inicioServicio)
        );
    }

    private boolean isAgentOnLeave(Agente agente, LocalDateTime inicioServicio, LocalDateTime finServicio, List<Permiso> permisosDelAgente) {
        if (permisosDelAgente == null || permisosDelAgente.isEmpty()) return false;
        return permisosDelAgente.stream().anyMatch(permiso -> 
            permiso.getFechaInicio().isBefore(finServicio) && permiso.getFechaFin().isAfter(inicioServicio)
        );
    }

    @Override
    @Transactional
    public List<Agente> findCandidates(Long idVuelo, NecesidadVuelo necesidad, List<Assignment> currentAssignments) {
        Vuelo managedVuelo = vueloRepository.findByIdWithFullDetails(idVuelo)
                .orElseThrow(() -> new EntityNotFoundException("Vuelo no encontrado con ID: " + idVuelo));
        
        necesidad.setVuelo(managedVuelo);

        List<Agente> agentesActivos = agenteRepository.findActivosWithDetails();
        
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

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public void updateAssignmentsForVuelo(Vuelo vueloDetached, List<Assignment> detachedAssignments) {
        Vuelo managedVuelo = vueloRepository.findByIdWithFullDetails(vueloDetached.getIdVuelo())
                .orElseThrow(() -> new EntityNotFoundException("Vuelo no encontrado con ID: " + vueloDetached.getIdVuelo()));

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