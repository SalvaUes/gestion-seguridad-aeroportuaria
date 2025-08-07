package com.aeroseguridad.gestion_seguridad_aeroportuaria.repository;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Assignment;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.EstadoAsignacion;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PosicionSeguridad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    /**
     * Busca asignaciones para un agente específico que se solapan con un intervalo de tiempo dado.
     * Crucial para el PCAService.
     */
    @Query("SELECT a FROM Assignment a " +
           "JOIN a.vuelo v " +
           "WHERE a.agente = :agente " +
            // --- CORRECCIÓN DE NOMBRES DE CAMPO ---
           "AND v.fechaHoraLlegada < :finIntervalo " +
           "AND v.finOperacionSeguridad > :inicioIntervalo")
    List<Assignment> findConflictingAssignmentsForAgent(
            @Param("agente") Agente agente,
            @Param("inicioIntervalo") LocalDateTime inicioIntervalo,
            @Param("finIntervalo") LocalDateTime finIntervalo);

    /**
     * Encuentra todas las asignaciones que no están en estado 'ASIGNADO'.
     * Útil para el futuro panel de control de conflictos.
     */
    List<Assignment> findByEstadoIsNot(EstadoAsignacion estado);
    
    /**
     * Busca asignaciones futuras de un agente que requieran una habilidad específica
     */
    @Query("SELECT a FROM Assignment a JOIN a.vuelo v JOIN a.posicionSeguridad p " +
            // --- CORRECCIÓN DE NOMBRE DE CAMPO ---
           "WHERE a.agente = :agente AND v.fechaHoraLlegada > :now " +
           "AND p = :posicionRequerida")
    List<Assignment> findFutureAssignmentsByAgentAndRequiredPosition(
            @Param("agente") Agente agente,
            @Param("posicionRequerida") PosicionSeguridad posicionRequerida,
            @Param("now") LocalDateTime now
    );
    
    /**
     * Busca asignaciones futuras de un agente para una aerolínea específica
     */
    @Query("SELECT a FROM Assignment a JOIN a.vuelo v " +
            // --- CORRECCIÓN DE NOMBRE DE CAMPO ---
           "WHERE a.agente = :agente AND v.fechaHoraLlegada > :now " +
           "AND v.aerolinea.idAerolinea = :aerolineaId")
    List<Assignment> findFutureAssignmentsByAgentAndAirline(
            @Param("agente") Agente agente,
            @Param("aerolineaId") Long aerolineaId,
            @Param("now") LocalDateTime now
    );

    /**
     * Busca todas las asignaciones futuras de un agente
     */
    @Query("SELECT a FROM Assignment a JOIN a.vuelo v " +
            // --- CORRECCIÓN DE NOMBRE DE CAMPO ---
           "WHERE a.agente = :agente AND v.fechaHoraLlegada > :now")
    List<Assignment> findFutureAssignmentsByAgent(@Param("agente") Agente agente, @Param("now") LocalDateTime now);
}