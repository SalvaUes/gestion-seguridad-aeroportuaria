package com.aeroseguridad.gestion_seguridad_aeroportuaria.service;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.*;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.exception.ConflictoAsignacionException;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.AssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PCAServiceImpl implements PCAService {

    private final AssignmentRepository assignmentRepository;

    @Override
    public void verificarConflictoPorCambioDeTurno(Turno turnoOriginal, Turno turnoModificado) {
        if (turnoOriginal == null) return;
        if (turnoModificado == null) { // Borrado
            verificarConflictos(turnoOriginal.getAgente(), turnoOriginal.getInicioTurno(), turnoOriginal.getFinTurno(), "La eliminación de este turno");
        } else { // Modificación
            if (turnoModificado.getInicioTurno().isAfter(turnoOriginal.getInicioTurno())) {
                verificarConflictos(turnoOriginal.getAgente(), turnoOriginal.getInicioTurno(), turnoModificado.getInicioTurno(), "La modificación de este turno");
            }
            if (turnoModificado.getFinTurno().isBefore(turnoOriginal.getFinTurno())) {
                verificarConflictos(turnoOriginal.getAgente(), turnoModificado.getFinTurno(), turnoOriginal.getFinTurno(), "La modificación de este turno");
            }
        }
    }

    @Override
    public void verificarConflictoPorPermiso(Permiso permiso) {
        verificarConflictos(permiso.getAgente(), permiso.getFechaInicio(), permiso.getFechaFin(), "La aprobación de este permiso");
    }
    
    @Override
    public void verificarConflictoPorCambioDeHabilidad(Agente agente, PosicionSeguridad habilidad) {
        List<Assignment> conflictos = assignmentRepository.findFutureAssignmentsByAgentAndRequiredPosition(agente, habilidad, LocalDateTime.now());
        if (!conflictos.isEmpty()) {
            lanzarExcepcion(conflictos, "Quitar la habilidad '" + habilidad.getNombrePosicion() + "'");
        }
    }

    @Override
    public void verificarConflictoPorRevocacionPermisoAerolinea(Agente agente, Long aerolineaId) {
        List<Assignment> conflictos = assignmentRepository.findFutureAssignmentsByAgentAndAirline(agente, aerolineaId, LocalDateTime.now());
        if (!conflictos.isEmpty()) {
            lanzarExcepcion(conflictos, "Revocar el permiso para esta aerolínea");
        }
    }

    @Override
    public void verificarConflictoPorDesactivacionAgente(Agente agente) {
        List<Assignment> conflictos = assignmentRepository.findFutureAssignmentsByAgent(agente, LocalDateTime.now());
        if (!conflictos.isEmpty()) {
            lanzarExcepcion(conflictos, "Desactivar a este agente");
        }
    }

    private void verificarConflictos(Agente agente, LocalDateTime inicioIntervalo, LocalDateTime finIntervalo, String causa) {
        List<Assignment> conflictos = assignmentRepository.findConflictingAssignmentsForAgent(agente, inicioIntervalo, finIntervalo);
        if (!conflictos.isEmpty()) {
            lanzarExcepcion(conflictos, causa);
        }
    }
    
    private void lanzarExcepcion(List<Assignment> conflictos, String causa) {
        String detallesVuelos = conflictos.stream()
                .map(a -> "Vuelo " + a.getVuelo().getNumeroVuelo() + " (" + a.getPosicionSeguridad().getNombrePosicion() + ")")
                .collect(Collectors.joining(", "));
        String mensaje = String.format("%s dejará sin cobertura las siguientes asignaciones: %s.", causa, detallesVuelos);
        throw new ConflictoAsignacionException(mensaje);
    }
}