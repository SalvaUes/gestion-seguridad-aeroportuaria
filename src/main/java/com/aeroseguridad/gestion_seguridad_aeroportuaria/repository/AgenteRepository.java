// RUTA: src/main/java/com/aeroseguridad/gestion_seguridad_aeroportuaria/repository/AgenteRepository.java
package com.aeroseguridad.gestion_seguridad_aeroportuaria.repository;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Rol; // Añadido por si se necesita
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // <-- NUEVO: Para queries dinámicas
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

// MODIFICADO: Se añade JpaSpecificationExecutor
public interface AgenteRepository extends JpaRepository<Agente, Long>, JpaSpecificationExecutor<Agente> {

    // Los métodos existentes permanecen, aunque algunos quedarán obsoletos por la nueva funcionalidad.

    @Query("SELECT a FROM Agente a LEFT JOIN FETCH a.posicionesHabilitadas WHERE a.activo = true ORDER BY a.apellido, a.nombre")
    List<Agente> findActivosFetchingPosiciones();

    @Query("SELECT a FROM Agente a LEFT JOIN FETCH a.posicionesHabilitadas WHERE a.activo = true AND (lower(a.nombre) LIKE lower(concat('%', :searchTerm, '%')) OR lower(a.apellido) LIKE lower(concat('%', :searchTerm, '%')))")
    List<Agente> searchActivosByNombreOrApellidoFetchingPosiciones(@Param("searchTerm") String searchTerm);

    @Query("SELECT a FROM Agente a LEFT JOIN FETCH a.posicionesHabilitadas WHERE a.idAgente = :id")
    Optional<Agente> findByIdFetchingPosiciones(@Param("id") Long id);

    @Query("SELECT a FROM Agente a LEFT JOIN FETCH a.posicionesHabilitadas WHERE a.activo = true AND lower(a.numeroCarnet) = lower(:numeroCarnet)")
    Optional<Agente> findActivoByNumeroCarnetIgnoreCaseFetchingPosiciones(@Param("numeroCarnet") String numeroCarnet);
    
    // NUEVOS MÉTODOS PARA EL ORGANIGRAMA
    List<Agente> findByRol(Rol rol);
    
    List<Agente> findByRolAndSuperiorIsNull(Rol rol);

    List<Agente> findBySuperior(Agente superior);
}