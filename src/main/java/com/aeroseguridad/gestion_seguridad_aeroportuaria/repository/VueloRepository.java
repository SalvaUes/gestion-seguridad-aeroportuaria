package com.aeroseguridad.gestion_seguridad_aeroportuaria.repository;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Vuelo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VueloRepository extends JpaRepository<Vuelo, Long> {

    // --- MÉTODOS EXISTENTES QUE YA CORREGIMOS ---

    @Query("SELECT v FROM Vuelo v JOIN FETCH v.aerolinea ORDER BY v.fechaHoraLlegada DESC")
    List<Vuelo> findAllFetchingAerolinea();

    @Query("SELECT v FROM Vuelo v JOIN FETCH v.aerolinea WHERE lower(v.numeroVuelo) LIKE lower(concat('%', :searchTerm, '%')) ORDER BY v.fechaHoraLlegada DESC")
    List<Vuelo> findByNumeroVueloContainingIgnoreCaseFetchingAerolinea(@Param("searchTerm") String searchTerm);

    @Query("SELECT v FROM Vuelo v JOIN FETCH v.aerolinea WHERE v.fechaHoraLlegada BETWEEN :inicio AND :fin OR v.fechaHoraSalida BETWEEN :inicio AND :fin")
    List<Vuelo> findVuelosInPeriodFetchingAerolinea(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    // --- MÉTODO NUEVO PARA CORREGIR EL ERROR ACTUAL ---

    /**
     * Resuelve el error 'findByDateRangeAndNumeroVueloFetchingAerolinea is undefined'.
     * Busca vuelos en un rango de fechas y, opcionalmente, filtra por número de vuelo.
     * La consulta maneja el caso en que el número de vuelo sea nulo o vacío.
     */
    @Query("SELECT v FROM Vuelo v JOIN FETCH v.aerolinea " +
           "WHERE (v.fechaHoraLlegada BETWEEN :startDate AND :endDate OR v.fechaHoraSalida BETWEEN :startDate AND :endDate) " +
           "AND (:numeroVuelo IS NULL OR :numeroVuelo = '' OR lower(v.numeroVuelo) LIKE lower(concat('%', :numeroVuelo, '%'))) " +
           "ORDER BY v.fechaHoraLlegada DESC")
    List<Vuelo> findByDateRangeAndNumeroVueloFetchingAerolinea(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("numeroVuelo") String numeroVuelo
    );
    
    // --- MÉTODO EXISTENTE PARA OBTENER DETALLES COMPLETOS ---

    @Query("SELECT v FROM Vuelo v " +
           "LEFT JOIN FETCH v.aerolinea " +
           "LEFT JOIN FETCH v.necesidades n " +
           "LEFT JOIN FETCH n.posicion " +
           "LEFT JOIN FETCH v.assignments a " +
           "LEFT JOIN FETCH a.agente " +
           "LEFT JOIN FETCH a.posicionSeguridad " +
           "WHERE v.idVuelo = :id")
    Optional<Vuelo> findByIdWithFullDetails(@Param("id") Long id);
}