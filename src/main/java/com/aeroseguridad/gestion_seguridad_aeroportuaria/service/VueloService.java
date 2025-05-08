// service/VueloService.java (MODIFICADO)
package com.aeroseguridad.gestion_seguridad_aeroportuaria.service;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Vuelo;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.NecesidadVueloRepository;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.VueloRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils; // <-- Importar StringUtils

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VueloService {

    private final VueloRepository vueloRepository;
    private final NecesidadVueloRepository necesidadVueloRepository;

    @Transactional(readOnly = true)
    public List<Vuelo> findAllForView() {
        return vueloRepository.findAllFetchingAerolinea();
    }

    // --- MÉTODO findByNumeroVueloForView MODIFICADO ---
    @Transactional(readOnly = true)
    public List<Vuelo> findByNumeroVueloForView(String numeroVuelo) {
        // Usa StringUtils.hasText para convertir null/vacío/blancos a null real
        String searchTerm = StringUtils.hasText(numeroVuelo) ? numeroVuelo.trim() : null;
        if (searchTerm == null) {
            return vueloRepository.findAllFetchingAerolinea();
        } else {
            // Pasa el searchTerm (que puede ser null o texto trimado)
            return vueloRepository.findByNumeroVueloContainingIgnoreCaseFetchingAerolinea(searchTerm);
        }
    }
    // --- FIN MÉTODO MODIFICADO ---

    // --- MÉTODO findVuelosByDateRangeAndNumeroVueloForView MODIFICADO ---
    @Transactional(readOnly = true)
    public List<Vuelo> findVuelosByDateRangeAndNumeroVueloForView(LocalDateTime inicioRango, LocalDateTime finRango, String numeroVuelo) {
        // Usa StringUtils.hasText para convertir null/vacío/blancos a null real
        String searchTerm = StringUtils.hasText(numeroVuelo) ? numeroVuelo.trim() : null;

        // Pasa el searchTerm (que puede ser null o texto trimado) al repositorio
        return vueloRepository.findByDateRangeAndNumeroVueloFetchingAerolinea(inicioRango, finRango, searchTerm);
    }
    // --- FIN MÉTODO MODIFICADO ---

    @Transactional(readOnly = true)
    public Optional<Vuelo> findById(Long id) {
        return vueloRepository.findById(id);
    }

    @Transactional
    public Vuelo save(Vuelo vuelo) {
        return vueloRepository.save(vuelo);
    }

    @Transactional
    public void deleteById(Long id) {
         if (vueloRepository.existsById(id)) {
             necesidadVueloRepository.deleteByVueloIdVuelo(id);
             vueloRepository.deleteById(id);
        }
    }

    public long count() {
        return vueloRepository.count();
    }
}