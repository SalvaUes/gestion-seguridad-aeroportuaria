package com.aeroseguridad.gestion_seguridad_aeroportuaria.repository;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgenteRepository extends JpaRepository<Agente, Long> {

    // Busca agentes ACTIVOS por nombre o apellido (ignorando may/min) con JOIN FETCH
    @Query("SELECT a FROM Agente a LEFT JOIN FETCH a.posicionesHabilitadas WHERE a.activo = true AND (lower(a.nombre) LIKE lower(concat('%', :searchTerm, '%')) OR lower(a.apellido) LIKE lower(concat('%', :searchTerm, '%')))")
    List<Agente> searchActivosByNombreOrApellidoFetchingPosiciones(@Param("searchTerm") String searchTerm);

    // Para cargar todos los ACTIVOS con sus posiciones
    @Query("SELECT a FROM Agente a LEFT JOIN FETCH a.posicionesHabilitadas WHERE a.activo = true")
    List<Agente> findActivosFetchingPosiciones();

     // Métodos originales (opcional mantenerlos si se usan en otro lugar)
     @Query("SELECT a FROM Agente a LEFT JOIN FETCH a.posicionesHabilitadas WHERE lower(a.nombre) LIKE lower(concat('%', :searchTerm, '%')) OR lower(a.apellido) LIKE lower(concat('%', :searchTerm, '%'))")
     List<Agente> searchByNombreOrApellidoFetchingPosiciones(@Param("searchTerm") String searchTerm);

     @Query("SELECT a FROM Agente a LEFT JOIN FETCH a.posicionesHabilitadas")
     List<Agente> findAllFetchingPosiciones();

     // Método opcional para contar solo activos
     // long countByActivoTrue();
}