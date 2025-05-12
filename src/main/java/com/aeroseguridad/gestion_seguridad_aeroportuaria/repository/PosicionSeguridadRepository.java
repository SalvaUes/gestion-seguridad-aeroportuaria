package com.aeroseguridad.gestion_seguridad_aeroportuaria.repository;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PosicionSeguridad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Importar
import org.springframework.data.repository.query.Param; // Importar
import org.springframework.stereotype.Repository;

import java.util.List; // Importar

@Repository
public interface PosicionSeguridadRepository extends JpaRepository<PosicionSeguridad, Long> {

    // --- NUEVO: Encontrar todas las posiciones activas ---
    List<PosicionSeguridad> findByActivoTrueOrderByNombrePosicionAsc();

    // --- NUEVO: Buscar por nombre entre posiciones activas (ignorando may/min) ---
    // Puedes ajustar si solo quieres buscar por nombre exacto o si el filtro es más complejo
    @Query("SELECT p FROM PosicionSeguridad p WHERE p.activo = true AND lower(p.nombrePosicion) LIKE lower(concat('%', :nombre, '%')) ORDER BY p.nombrePosicion ASC")
    List<PosicionSeguridad> findActivasByNombrePosicionContainingIgnoreCase(@Param("nombre") String nombre);
}