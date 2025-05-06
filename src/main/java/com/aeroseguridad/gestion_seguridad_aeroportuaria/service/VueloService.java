package com.aeroseguridad.gestion_seguridad_aeroportuaria.service;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Vuelo;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.NecesidadVueloRepository; // Importar si se maneja borrado de necesidades aquí
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.VueloRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VueloService {

    private final VueloRepository vueloRepository;
    // Inyectar NecesidadVueloRepository si se decide borrar necesidades al borrar vuelo
    private final NecesidadVueloRepository necesidadVueloRepository;

    @Transactional(readOnly = true)
    public List<Vuelo> findAllForView() {
        return vueloRepository.findAllFetchingAerolinea();
    }

    @Transactional(readOnly = true)
    public List<Vuelo> findByNumeroVueloForView(String numeroVuelo) {
        if (numeroVuelo == null || numeroVuelo.isEmpty()) {
            return vueloRepository.findAllFetchingAerolinea();
        } else {
            return vueloRepository.findByNumeroVueloContainingIgnoreCaseFetchingAerolinea(numeroVuelo);
        }
    }

    // --- MÉTODO MODIFICADO/NUEVO para usar la consulta de rango de fechas ---
    @Transactional(readOnly = true)
    public List<Vuelo> findVuelosByDateRangeAndNumeroVueloForView(LocalDateTime inicioRango, LocalDateTime finRango, String numeroVuelo) {
        // Si numeroVuelo es vacío, lo tratamos como null para la consulta
        String searchTerm = (numeroVuelo == null || numeroVuelo.trim().isEmpty()) ? null : numeroVuelo.trim();
        return vueloRepository.findByDateRangeAndNumeroVueloFetchingAerolinea(inicioRango, finRango, searchTerm);
    }
    // --- FIN MÉTODO ---

    @Transactional(readOnly = true)
    public Optional<Vuelo> findById(Long id) {
        return vueloRepository.findById(id);
    }

    @Transactional
    public Vuelo save(Vuelo vuelo) {
        // Aquí iría lógica de negocio/validación antes de guardar, si aplica
        // La validación @AssertTrue de fechas se ejecutará por JPA/Hibernate al hacer flush
        return vueloRepository.save(vuelo);
    }

    @Transactional
    public void deleteById(Long id) {
        // --- LÓGICA OPCIONAL: Borrar necesidades asociadas ANTES de borrar el vuelo ---
        // Esto evita DataIntegrityViolationException si hay necesidades
        if (vueloRepository.existsById(id)) {
             necesidadVueloRepository.deleteByVueloIdVuelo(id); // Borra todas las necesidades del vuelo
             vueloRepository.deleteById(id); // Ahora borra el vuelo
        }
        // --- FIN LÓGICA OPCIONAL ---
        // Si no se añade lo anterior, el deleteById podría fallar si hay necesidades
        // vueloRepository.deleteById(id); // Método original
    }

    public long count() {
        return vueloRepository.count();
    }
}