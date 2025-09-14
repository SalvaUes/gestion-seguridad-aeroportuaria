package com.aeroseguridad.gestion_seguridad_aeroportuaria.service;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.NecesidadVuelo;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Vuelo; // Necesario
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.NecesidadVueloRepository;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.VueloRepository; // Para validar vuelo existente
import jakarta.persistence.EntityNotFoundException; // Para errores
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Asegúrate de tener este import

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NecesidadVueloService {

    private static final Logger log = LoggerFactory.getLogger(NecesidadVueloService.class);

    private final NecesidadVueloRepository necesidadVueloRepository;
    private final VueloRepository vueloRepository; // Para verificar que el vuelo existe

    // Busca todas las necesidades para un ID de vuelo dado
    @Transactional(readOnly = true)
    public List<NecesidadVuelo> findByVueloId(Long vueloId) {
        if (vueloId == null) {
            throw new IllegalArgumentException("El ID de vuelo no puede ser nulo");
        }
        // Verifica si el vuelo existe antes de buscar sus necesidades
        if (!vueloRepository.existsById(vueloId)) {
            throw new EntityNotFoundException("Vuelo no encontrado con ID: " + vueloId);
        }
        // Usa el método del repo que trae las posiciones
        return necesidadVueloRepository.findByVueloIdVueloFetchingPosicion(vueloId);
    }

    @Transactional(readOnly = true)
    public Optional<NecesidadVuelo> findById(Long id) {
        return necesidadVueloRepository.findById(id);
    }

    @Transactional
    public NecesidadVuelo save(NecesidadVuelo necesidadVuelo) {
        if (necesidadVuelo == null) {
            throw new IllegalArgumentException("La entidad NecesidadVuelo no puede ser nula");
        }
        if (necesidadVuelo.getVuelo() == null || necesidadVuelo.getVuelo().getIdVuelo() == null) {
            throw new IllegalArgumentException("La necesidad debe estar asociada a un vuelo válido existente.");
        }
        Long vueloId = necesidadVuelo.getVuelo().getIdVuelo();
        if (!vueloRepository.existsById(vueloId)) {
            throw new IllegalArgumentException("La necesidad debe estar asociada a un vuelo válido existente.");
        }
        // Podríamos añadir validación similar para PosicionSeguridad si fuera necesario
        return necesidadVueloRepository.save(necesidadVuelo);
    }

    @Transactional
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de la necesidad no puede ser nulo");
        }
        if (!necesidadVueloRepository.existsById(id)) {
            throw new EntityNotFoundException("Necesidad de Vuelo no encontrada con ID: " + id);
        }
        // Considerar si hay lógica adicional antes de borrar (ej. si ya hay asignaciones hechas)
        necesidadVueloRepository.deleteById(id);
        log.info("Necesidad de Vuelo con ID {} eliminada", id);
    }

    // --- MÉTODO AÑADIDO ---
    /**
     * Elimina todas las necesidades asociadas a un ID de vuelo específico.
     * Es importante que este método sea transaccional.
     * @param vueloId El ID del Vuelo cuyas necesidades se eliminarán.
     */
    @Transactional // Asegura que la operación de borrado sea atómica
    public void deleteByVueloId(Long vueloId) {
        if (vueloId == null) {
            throw new IllegalArgumentException("El ID de vuelo no puede ser nulo");
        }
        // Opcional: Verificar si el vuelo existe antes de intentar borrar sus necesidades
        if (!vueloRepository.existsById(vueloId)) {
            log.warn("Intento de borrar necesidades para un Vuelo ID que no existe: {}", vueloId);
            return; // Salir si el vuelo no existe
        }
        // Delega la llamada al método del repositorio que borra por ID de vuelo
        necesidadVueloRepository.deleteByVueloIdVuelo(vueloId);
        log.info("Necesidades para Vuelo ID {} eliminadas (si existían)", vueloId);
    }
    // --- FIN MÉTODO AÑADIDO ---


    public long count() {
        return necesidadVueloRepository.count();
    }

    public long countByVuelo(Vuelo vuelo) {
        if (vuelo == null || vuelo.getIdVuelo() == null) {
            return 0L;
        }
        // Necesitaría un método en el repositorio: countByVuelo(Vuelo vuelo)
        // Por ahora, no implementado directamente.
        return findByVueloId(vuelo.getIdVuelo()).size(); // Forma simple pero menos eficiente
    }
}