package com.aeroseguridad.gestion_seguridad_aeroportuaria.service;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Aerolinea; // Importa la Entidad
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.AerolineaRepository; // Importa el Repositorio
import lombok.RequiredArgsConstructor; // Importa la anotación de Lombok para la inyección
import org.springframework.stereotype.Service; // Importa la anotación @Service

import java.util.List;
import java.util.Optional;

@Service // 1. Marca esta clase como un "Servicio" de Spring. Spring la gestionará.
@RequiredArgsConstructor // 2. (Lombok) Genera un constructor con los campos 'final' requeridos. ¡Magia para la inyección!
public class AerolineaService {

    // --- Dependencias ---
    private final AerolineaRepository aerolineaRepository; // 3. Declara la dependencia (el Repositorio) como 'final'

    // 4. Gracias a @RequiredArgsConstructor, Lombok genera esto automáticamente:
    // public AerolineaService(AerolineaRepository aerolineaRepository) {
    //    this.aerolineaRepository = aerolineaRepository;
    // }
    // Spring usará este constructor para "inyectar" una instancia de AerolineaRepository aquí.



    // --- Métodos de Servicio ---

    /**
     * Obtiene todas las aerolíneas de la base de datos.
     * @return Una lista de todas las entidades Aerolinea.
     */
    public List<Aerolinea> findAll() {
        return aerolineaRepository.findAll(); // 5. Delega la llamada al método findAll() del repositorio.
    }




    /**
     * Busca una aerolínea por su ID.
     * @param id El ID de la aerolínea a buscar.
     * @return Un Optional que contiene la Aerolinea si se encuentra, o vacío si no.
     */
    public Optional<Aerolinea> findById(Long id) {
        return aerolineaRepository.findById(id); // 6. Delega la llamada al método findById() del repositorio.
    }

    /**
     * Guarda una nueva aerolínea o actualiza una existente.
     * @param aerolinea La entidad Aerolinea a guardar.
     * @return La entidad Aerolinea guardada (puede tener el ID generado si es nueva).
     */
    public Aerolinea save(Aerolinea aerolinea) {
        return aerolineaRepository.save(aerolinea); // 7. Delega la llamada al método save() del repositorio.
    }

    /**
     * Elimina una aerolínea por su ID.
     * @param id El ID de la aerolínea a eliminar.
     */
    public void deleteById(Long id) {
        aerolineaRepository.deleteById(id); // 8. Delega la llamada al método deleteById() del repositorio.
    }

    /**
     * Cuenta el número total de aerolíneas.
     * @return El número total de aerolíneas.
     */
    public long count() {
        return aerolineaRepository.count(); // 9. Delega la llamada al método count() del repositorio.
    }

    
    // por ejemplo, buscar por código IATA
    /**
     * Busca una aerolínea por su código IATA (ignorando mayúsculas/minúsculas).
     * @param codigoIata El código IATA de la aerolínea.
     */
    public Optional<Aerolinea> findByCodigoIata(String codigoIata) {
        // Llama al método definido en el repositorio
        return aerolineaRepository.findByCodigoIataIgnoreCase(codigoIata);
    }
}