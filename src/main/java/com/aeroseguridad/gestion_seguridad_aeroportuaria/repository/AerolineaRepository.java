package com.aeroseguridad.gestion_seguridad_aeroportuaria.repository;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Aerolinea; // Importamos la entidad Aerolinea
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;



@Repository // Indica que es un componente Repositorio de Spring
public interface AerolineaRepository extends JpaRepository<Aerolinea, Long> { // 2. Extiende JpaRepository

    
    /**
     * Busca una aerolínea por su código IATA (ignora mayúsculas/minúsculas).
     * Spring Data JPA implementará este método automáticamente.
     * @param codigoIata El código IATA a buscar.
     */
    Optional<Aerolinea> findByCodigoIataIgnoreCase(String codigoIata); // Usa IgnoreCase para flexibilidad
    

}