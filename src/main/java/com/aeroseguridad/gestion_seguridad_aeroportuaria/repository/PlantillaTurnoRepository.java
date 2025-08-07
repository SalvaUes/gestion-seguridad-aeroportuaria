package com.aeroseguridad.gestion_seguridad_aeroportuaria.repository;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PlantillaTurno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PlantillaTurnoRepository extends JpaRepository<PlantillaTurno, Long> {

    /**
     * Encuentra todas las plantillas activas para un agente en un rango de fechas.
     * Una plantilla está activa si su rango de vigencia se solapa con el rango de búsqueda.
     */
    @Query("SELECT pt FROM PlantillaTurno pt " +
           "WHERE pt.agente.idAgente = :agenteId " +
           "AND pt.fechaInicioVigencia <= :finPeriodo " +
           "AND (pt.fechaFinVigencia IS NULL OR pt.fechaFinVigencia >= :inicioPeriodo)")
    List<PlantillaTurno> findActiveTemplatesForAgentInPeriod(Long agenteId, LocalDate inicioPeriodo, LocalDate finPeriodo);
}