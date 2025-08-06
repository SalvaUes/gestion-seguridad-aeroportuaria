package com.aeroseguridad.gestion_seguridad_aeroportuaria.repository;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PlantillaVuelo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantillaVueloRepository extends JpaRepository<PlantillaVuelo, Long> {

    // --- CAMBIO: La consulta ahora es más completa para evitar todos los errores de carga perezosa ---
    /**
     * Busca todas las plantillas y carga explícitamente (EAGER) todas las entidades
     * relacionadas que se necesitan para la visualización: Aerolinea, la colección de
     * NecesidadesEstandar y la Posicion dentro de cada necesidad.
     * Se usa LEFT JOIN para incluir plantillas que quizás no tengan necesidades aún.
     * Se usa DISTINCT para evitar duplicados en el resultado por los joins.
     * @return Una lista completa de PlantillaVuelo listas para ser mostradas.
     */
    @Query("SELECT DISTINCT p FROM PlantillaVuelo p " +
           "JOIN FETCH p.aerolinea " +
           "LEFT JOIN FETCH p.necesidadesEstandar n " +
           "LEFT JOIN FETCH n.posicion")
    List<PlantillaVuelo> findAllWithAerolinea();
}