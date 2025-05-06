package com.aeroseguridad.gestion_seguridad_aeroportuaria.repository;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.NecesidadVuelo;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Vuelo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying; // <<<--- IMPORTAR
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NecesidadVueloRepository extends JpaRepository<NecesidadVuelo, Long> {

    // Busca todas las necesidades para un vuelo específico (Objeto)
    List<NecesidadVuelo> findByVuelo(Vuelo vuelo);

    // Busca todas las necesidades para un ID de vuelo específico con JOIN FETCH
    @Query("SELECT n FROM NecesidadVuelo n JOIN FETCH n.posicion WHERE n.vuelo.idVuelo = :idVuelo ORDER BY n.inicioCobertura ASC")
    List<NecesidadVuelo> findByVueloIdVueloFetchingPosicion(@Param("idVuelo") Long idVuelo);

    // --- MÉTODO NUEVO PARA BORRAR POR ID DE VUELO ---
    @Modifying // Indica que esta query modifica datos (DELETE)
    void deleteByVueloIdVuelo(Long idVuelo); // Spring Data genera el DELETE FROM ... WHERE vuelo_id = ?
    // --- FIN MÉTODO NUEVO ---

}