package com.aeroseguridad.gestion_seguridad_aeroportuaria.repository;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Rol; // <-- Importar Rol
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgenteRepository extends JpaRepository<Agente, Long> {

    // --- MÉTODOS EXISTENTES (SIN CAMBIOS) ---

    Optional<Agente> findByEmail(String email);

    @Query("SELECT a FROM Agente a LEFT JOIN FETCH a.posicionesHabilitadas WHERE a.activo = true AND (lower(a.nombre) LIKE lower(concat('%', :searchTerm, '%')) OR lower(a.apellido) LIKE lower(concat('%', :searchTerm, '%')))")
    List<Agente> searchActivosByNombreOrApellidoFetchingPosiciones(@Param("searchTerm") String searchTerm);

    @Query("SELECT a FROM Agente a LEFT JOIN FETCH a.posicionesHabilitadas WHERE a.activo = true ORDER BY a.apellido ASC, a.nombre ASC")
    List<Agente> findActivosFetchingPosiciones();

    @Query("SELECT a FROM Agente a LEFT JOIN FETCH a.posicionesHabilitadas WHERE a.activo = true AND lower(a.numeroCarnet) = lower(:numeroCarnet)")
    Optional<Agente> findActivoByNumeroCarnetIgnoreCaseFetchingPosiciones(@Param("numeroCarnet") String numeroCarnet);

    @Query("SELECT a FROM Agente a LEFT JOIN FETCH a.posicionesHabilitadas WHERE a.idAgente = :idAgente")
    Optional<Agente> findByIdFetchingPosiciones(@Param("idAgente") Long idAgente);

    // --- NUEVOS MÉTODOS REQUERIDOS PARA EL ORGANIGRAMA ---

    /**
     * Encuentra todo el personal que coincide con un rol específico.
     * @param rol El rol a buscar (AGENTE, SUPERVISOR, COORDINADOR).
     * @return Una lista del personal con ese rol.
     */
    List<Agente> findByRol(Rol rol);

    /**
     * Encuentra todo el personal con un rol específico que actualmente no tiene un superior asignado.
     * Esencial para poblar los ComboBox con personal "disponible".
     * @param rol El rol a buscar.
     * @return Una lista del personal libre con ese rol.
     */
    List<Agente> findByRolAndSuperiorIsNull(Rol rol);
}