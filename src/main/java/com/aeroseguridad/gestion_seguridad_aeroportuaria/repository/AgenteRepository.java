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

    List<Agente> findByRol(Rol rol);
    List<Agente> findByRolAndSuperiorIsNull(Rol rol);
    List<Agente> findBySuperior(Agente superior);

    @Query("SELECT a FROM Agente a LEFT JOIN FETCH a.posicionesHabilitadas WHERE a.idAgente = :id")
    Optional<Agente> findByIdFetchingPosiciones(@Param("id") Long id);

    @Query("SELECT a FROM Agente a LEFT JOIN FETCH a.posicionesHabilitadas WHERE a.activo = true AND lower(a.numeroCarnet) = lower(:numeroCarnet)")
    Optional<Agente> findActivoByNumeroCarnetIgnoreCaseFetchingPosiciones(@Param("numeroCarnet") String numeroCarnet);

    @Query("SELECT DISTINCT a FROM Agente a " +
           "LEFT JOIN FETCH a.posicionesHabilitadas " +
           "LEFT JOIN FETCH a.aerolineasPermitidas " +
           "WHERE a.activo = true")
    List<Agente> findActivosWithDetails();

    @Query("SELECT DISTINCT a FROM Agente a LEFT JOIN FETCH a.plantillas " +
           "WHERE a.activo = true AND (" +
           "lower(a.nombre) LIKE lower(concat('%', :filtro, '%')) OR " +
           "lower(a.apellido) LIKE lower(concat('%', :filtro, '%')) OR " +
           "a.numeroCarnet LIKE concat('%', :filtro, '%'))")
    List<Agente> findActivosByFiltroTexto(@Param("filtro") String filtro);
    
    @Query("SELECT DISTINCT a FROM Agente a LEFT JOIN FETCH a.plantillas WHERE a.activo = true")
    List<Agente> findAllActivosWithPlantillas();
}