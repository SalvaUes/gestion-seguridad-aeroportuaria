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

    @Query("SELECT t FROM Turno t WHERE t.agente = :agente AND t.finTurno > :rangoInicio AND t.inicioTurno < :rangoFin")
    List<Turno> findByAgenteAndFechasSolapadas(
            @Param("agente") Agente agente,
            @Param("rangoInicio") LocalDateTime rangoInicio,
            @Param("rangoFin") LocalDateTime rangoFin
    );

    @Query("SELECT t FROM Turno t JOIN FETCH t.agente WHERE t.finTurno > :rangoInicio AND t.inicioTurno < :rangoFin ORDER BY t.inicioTurno ASC")
    List<Turno> findByFechasSolapadasFetchingAgente(
            @Param("rangoInicio") LocalDateTime rangoInicio,
            @Param("rangoFin") LocalDateTime rangoFin
    );

    List<Turno> findByAgenteOrderByInicioTurnoAsc(Agente agente);

    // --- NUEVO MÉTODO PARA OBTENER TODOS CON FETCH ---
    @Query("SELECT t FROM Turno t JOIN FETCH t.agente ORDER BY t.inicioTurno ASC")
    List<Turno> findAllFetchingAgenteOrderByInicioTurnoAsc();
    // --- FIN NUEVO MÉTODO ---
}