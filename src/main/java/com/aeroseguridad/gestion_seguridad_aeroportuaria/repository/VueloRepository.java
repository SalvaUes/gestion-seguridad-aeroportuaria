package com.aeroseguridad.gestion_seguridad_aeroportuaria.repository;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Vuelo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime; // Importar LocalDateTime
import java.util.List;

@Repository
public interface VueloRepository extends JpaRepository<Vuelo, Long> {

    // Busca vuelos cuyo número de vuelo contenga la cadena dada (ignorando may/min)
    // y trae la aerolínea asociada en la misma consulta.
    @Query("SELECT v FROM Vuelo v JOIN FETCH v.aerolinea WHERE lower(v.numeroVuelo) LIKE lower(concat('%', :numeroVuelo, '%'))")
    List<Vuelo> findByNumeroVueloContainingIgnoreCaseFetchingAerolinea(@Param("numeroVuelo") String numeroVuelo);

    // Para obtener TODOS con JOIN FETCH
    @Query("SELECT v FROM Vuelo v JOIN FETCH v.aerolinea")
    List<Vuelo> findAllFetchingAerolinea();

    // --- NUEVO MÉTODO PARA BUSCAR POR RANGO DE FECHAS Y TEXTO (OPCIONAL) ---
    // Busca vuelos que OCURREN (salida O llegada) dentro del rango Y opcionalmente filtrados por número de vuelo
    // Trae la aerolínea asociada.
    @Query("SELECT v FROM Vuelo v JOIN FETCH v.aerolinea " +
           "WHERE (v.fechaHoraSalida BETWEEN :inicioRango AND :finRango OR v.fechaHoraLlegada BETWEEN :inicioRango AND :finRango) " +
           "AND (:numeroVuelo IS NULL OR lower(v.numeroVuelo) LIKE lower(concat('%', :numeroVuelo, '%')))")
    List<Vuelo> findByDateRangeAndNumeroVueloFetchingAerolinea(
            @Param("inicioRango") LocalDateTime inicioRango,
            @Param("finRango") LocalDateTime finRango,
            @Param("numeroVuelo") String numeroVuelo // Puede ser null o vacío si no se filtra por texto
    );

    // --- FIN NUEVO MÉTODO ---


    // Los métodos heredados como findById, save, deleteById siguen funcionando igual.
}