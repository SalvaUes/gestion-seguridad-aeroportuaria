package com.aeroseguridad.gestion_seguridad_aeroportuaria.repository;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional; // Importar Optional

@Repository
public interface AgenteRepository extends JpaRepository<Agente, Long> {

    // Busca agentes ACTIVOS por nombre o apellido (ignorando may/min) con JOIN FETCH
    @Query("SELECT a FROM Agente a LEFT JOIN FETCH a.posicionesHabilitadas WHERE a.activo = true AND (lower(a.nombre) LIKE lower(concat('%', :searchTerm, '%')) OR lower(a.apellido) LIKE lower(concat('%', :searchTerm, '%')))")
    List<Agente> searchActivosByNombreOrApellidoFetchingPosiciones(@Param("searchTerm") String searchTerm);

    // Para cargar todos los ACTIVOS con sus posiciones
    @Query("SELECT a FROM Agente a LEFT JOIN FETCH a.posicionesHabilitadas WHERE a.activo = true ORDER BY a.apellido ASC, a.nombre ASC")
    List<Agente> findActivosFetchingPosiciones();

    // --- NUEVO: Método para buscar por número de carnet (ignorando may/min) ---
    // Usualmente el carnet es único, así que devuelve Optional<Agente>
    // Hace JOIN FETCH con posicionesHabilitadas para tener el objeto completo si se necesita después
    @Query("SELECT a FROM Agente a LEFT JOIN FETCH a.posicionesHabilitadas WHERE a.activo = true AND lower(a.numeroCarnet) = lower(:numeroCarnet)")
    Optional<Agente> findActivoByNumeroCarnetIgnoreCaseFetchingPosiciones(@Param("numeroCarnet") String numeroCarnet);

    // Si necesitas buscar también inactivos por carnet (menos común para UI):
    // Optional<Agente> findByNumeroCarnetIgnoreCase(String numeroCarnet);

    // Para buscar un agente por ID con sus posiciones (útil al editar)
    @Query("SELECT a FROM Agente a LEFT JOIN FETCH a.posicionesHabilitadas WHERE a.idAgente = :idAgente")
    Optional<Agente> findByIdFetchingPosiciones(@Param("idAgente") Long idAgente);
}