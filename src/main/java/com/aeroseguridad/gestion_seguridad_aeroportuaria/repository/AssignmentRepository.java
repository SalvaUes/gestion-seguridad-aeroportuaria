package com.aeroseguridad.gestion_seguridad_aeroportuaria.repository;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Assignment;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Vuelo; // <-- AÑADIR IMPORT
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List; // <-- AÑADIR IMPORT

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    // --- NUEVO MÉTODO: Para encontrar asignaciones de un vuelo específico ---
    List<Assignment> findByVuelo(Vuelo vuelo);
    
    // --- NUEVO MÉTODO: Para borrar todas las asignaciones de un vuelo ---
    void deleteAllByVuelo(Vuelo vuelo);
}