package com.aeroseguridad.gestion_seguridad_aeroportuaria.repository;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Aerolinea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional; // Importante añadir esta importación

@Repository
public interface AerolineaRepository extends JpaRepository<Aerolinea, Long> {

    /**
     * Busca todas las aerolíneas y las ordena alfabéticamente por su nombre.
     * Usado para poblar los ComboBox y CheckboxGroup en la UI.
     */
    List<Aerolinea> findAllByOrderByNombreAsc();

    /**
     * MÉTODO NUEVO: Busca una aerolínea por su código IATA, ignorando mayúsculas/minúsculas.
     * Resuelve el error en AerolineaService.
     * Devuelve un Optional porque la búsqueda podría no encontrar resultados.
     */
    Optional<Aerolinea> findByCodigoIataIgnoreCase(String codigoIata);

}