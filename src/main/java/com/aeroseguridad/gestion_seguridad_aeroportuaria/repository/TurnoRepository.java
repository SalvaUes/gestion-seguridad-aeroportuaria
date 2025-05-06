package com.aeroseguridad.gestion_seguridad_aeroportuaria.repository;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TurnoRepository extends JpaRepository<Turno, Long> {

    // Busca turnos para un agente específico dentro de un rango de fechas/horas
    // Útil para ver el horario de UN agente o verificar solapamientos al crear
    @Query("SELECT t FROM Turno t WHERE t.agente = :agente AND t.finTurno > :rangoInicio AND t.inicioTurno < :rangoFin")
    List<Turno> findByAgenteAndFechasSolapadas(
            @Param("agente") Agente agente,
            @Param("rangoInicio") LocalDateTime rangoInicio,
            @Param("rangoFin") LocalDateTime rangoFin
    );

    // Busca TODOS los turnos dentro de un rango de fechas/horas
    // Útil para vistas de calendario/planificación general. Incluye el Agente.
    @Query("SELECT t FROM Turno t JOIN FETCH t.agente WHERE t.finTurno > :rangoInicio AND t.inicioTurno < :rangoFin ORDER BY t.inicioTurno ASC")
    List<Turno> findByFechasSolapadasFetchingAgente(
            @Param("rangoInicio") LocalDateTime rangoInicio,
            @Param("rangoFin") LocalDateTime rangoFin
    );

     // Busca turnos para un agente específico (quizás para una lista simple)
    List<Turno> findByAgenteOrderByInicioTurnoAsc(Agente agente);

}