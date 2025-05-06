package com.aeroseguridad.gestion_seguridad_aeroportuaria.repository;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.EstadoSolicitudPermiso;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PermisoRepository extends JpaRepository<Permiso, Long> {

    // Busca permisos APROBADOS para un agente que se solapen con un rango de tiempo
    // Útil para calcular disponibilidad real
    @Query("SELECT p FROM Permiso p WHERE p.agente = :agente " +
           "AND p.estadoSolicitud = :estado " +
           "AND p.fechaFin > :rangoInicio AND p.fechaInicio < :rangoFin")
    List<Permiso> findByAgenteAndEstadoAndFechasSolapadas(
            @Param("agente") Agente agente,
            @Param("estado") EstadoSolicitudPermiso estado,
            @Param("rangoInicio") LocalDateTime rangoInicio,
            @Param("rangoFin") LocalDateTime rangoFin
    );

    // Busca todos los permisos en un rango de fechas (con agente)
    // Útil para la vista principal
    @Query("SELECT p FROM Permiso p JOIN FETCH p.agente " +
           "WHERE p.fechaFin > :rangoInicio AND p.fechaInicio < :rangoFin " +
           "ORDER BY p.fechaInicio ASC")
    List<Permiso> findByFechasSolapadasFetchingAgente(
            @Param("rangoInicio") LocalDateTime rangoInicio,
            @Param("rangoFin") LocalDateTime rangoFin
    );

    // Busca por agente
    List<Permiso> findByAgenteOrderByFechaInicioAsc(Agente agente);

    // Busca por estado
    List<Permiso> findByEstadoSolicitudOrderByFechaInicioAsc(EstadoSolicitudPermiso estado);

}