package com.aeroseguridad.gestion_seguridad_aeroportuaria.repository;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Vuelo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VueloRepository extends JpaRepository<Vuelo, Long> {

    // --- QUERY CORREGIDA DEFINITIVA: CAST en parámetro + lower() en ambos lados ---
    @Query("SELECT v FROM Vuelo v JOIN FETCH v.aerolinea " +
           // Aplicar lower() a la columna (que es VARCHAR)
           // Aplicar CAST al resultado de concat('%', :numeroVuelo, '%') ANTES de lower() y LIKE
           "WHERE lower(v.numeroVuelo) LIKE lower(CAST(concat('%', :numeroVuelo, '%') AS STRING))")
    List<Vuelo> findByNumeroVueloContainingIgnoreCaseFetchingAerolinea(@Param("numeroVuelo") String numeroVuelo);
    // --- FIN QUERY CORREGIDA ---


    // Para obtener TODOS con JOIN FETCH
    @Query("SELECT v FROM Vuelo v JOIN FETCH v.aerolinea")
    List<Vuelo> findAllFetchingAerolinea();


    // Busca vuelos que OCURREN (salida O llegada) dentro del rango Y opcionalmente filtrados por número de vuelo
    // Trae la aerolínea asociada.
    // --- QUERY CORREGIDA DEFINITIVA: CAST en parámetro + lower() en ambos lados ---
    @Query("SELECT v FROM Vuelo v JOIN FETCH v.aerolinea " +
           "WHERE (v.fechaHoraSalida BETWEEN :inicioRango AND :finRango OR v.fechaHoraLlegada BETWEEN :inicioRango AND :finRango) " +
           // Aplicar lower() a la columna y CAST + lower() al parámetro concatenado
           "AND (:numeroVuelo IS NULL OR lower(v.numeroVuelo) LIKE lower(CAST(concat('%', :numeroVuelo, '%') AS STRING)))")
    List<Vuelo> findByDateRangeAndNumeroVueloFetchingAerolinea(
            @Param("inicioRango") LocalDateTime inicioRango,
            @Param("finRango") LocalDateTime finRango,
            @Param("numeroVuelo") String numeroVuelo
    );
    // --- FIN QUERY CORREGIDA ---

}