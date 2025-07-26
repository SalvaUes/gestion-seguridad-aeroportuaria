package com.aeroseguridad.gestion_seguridad_aeroportuaria.repository;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AgenteRepository extends JpaRepository<Agente, Long>, JpaSpecificationExecutor<Agente> {

    // --- CONSULTA CORREGIDA ---
    // Se añade "LEFT JOIN FETCH a.permisosAerolinea" para cargar los permisos proactivamente
    // y "DISTINCT" para evitar duplicados.
    @Query("SELECT DISTINCT a FROM Agente a LEFT JOIN FETCH a.posicionesHabilitadas LEFT JOIN FETCH a.permisosAerolinea WHERE a.activo = true ORDER BY a.apellido, a.nombre")
    List<Agente> findActivosFetchingPosiciones();

    @Query("SELECT a FROM Agente a LEFT JOIN FETCH a.posicionesHabilitadas WHERE a.activo = true AND (lower(a.nombre) LIKE lower(concat('%', :searchTerm, '%')) OR lower(a.apellido) LIKE lower(concat('%', :searchTerm, '%')))")
    List<Agente> searchActivosByNombreOrApellidoFetchingPosiciones(@Param("searchTerm") String searchTerm);

    @Query("SELECT a FROM Agente a LEFT JOIN FETCH a.posicionesHabilitadas WHERE a.idAgente = :id")
    Optional<Agente> findByIdFetchingPosiciones(@Param("id") Long id);

    @Query("SELECT a FROM Agente a LEFT JOIN FETCH a.posicionesHabilitadas WHERE a.activo = true AND lower(a.numeroCarnet) = lower(:numeroCarnet)")
    Optional<Agente> findActivoByNumeroCarnetIgnoreCaseFetchingPosiciones(@Param("numeroCarnet") String numeroCarnet);
    
    List<Agente> findByRol(Rol rol);
    
    List<Agente> findByRolAndSuperiorIsNull(Rol rol);

    List<Agente> findBySuperior(Agente superior);
}