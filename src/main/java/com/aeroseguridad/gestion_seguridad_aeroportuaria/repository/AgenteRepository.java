package com.aeroseguridad.gestion_seguridad_aeroportuaria.repository;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgenteRepository extends JpaRepository<Agente, Long>, JpaSpecificationExecutor<Agente> {

    // --- Métodos Derivados por Nombre (para roles y jerarquía) ---
    // Spring Data JPA crea estas consultas automáticamente a partir del nombre del método.

    List<Agente> findByRol(Rol rol);

    List<Agente> findByRolAndSuperiorIsNull(Rol rol);

    List<Agente> findBySuperior(Agente superior);

    // --- Métodos con Consultas Explícitas (para optimización y búsquedas complejas) ---

    /**
     * Busca un agente por su ID y carga sus posiciones habilitadas en la misma consulta
     * para evitar errores de LazyInitializationException.
     */
    @Query("SELECT a FROM Agente a LEFT JOIN FETCH a.posicionesHabilitadas WHERE a.idAgente = :id")
    Optional<Agente> findByIdFetchingPosiciones(@Param("id") Long id);

    /**
     * Busca un agente ACTIVO por su número de carnet (ignorando mayúsculas/minúsculas)
     * y carga sus posiciones habilitadas.
     */
    @Query("SELECT a FROM Agente a LEFT JOIN FETCH a.posicionesHabilitadas WHERE a.activo = true AND lower(a.numeroCarnet) = lower(:numeroCarnet)")
    Optional<Agente> findActivoByNumeroCarnetIgnoreCaseFetchingPosiciones(@Param("numeroCarnet") String numeroCarnet);


    // --- Método Existente para el SchedulerService (se mantiene por consistencia) ---

    /**
     * Carga agentes activos junto con sus detalles (posiciones y permisos)
     * para ser usado por el motor de asignación automática de horarios.
     */
    @Query("SELECT a FROM Agente a " +
           "LEFT JOIN FETCH a.posicionesHabilitadas " +
           "LEFT JOIN FETCH a.permisosAerolinea pa " +
           "LEFT JOIN FETCH pa.aerolinea " +
           "WHERE a.activo = true")
    List<Agente> findActivosWithDetails();
}