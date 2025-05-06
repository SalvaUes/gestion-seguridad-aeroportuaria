package com.aeroseguridad.gestion_seguridad_aeroportuaria.service;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.NecesidadVuelo;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Vuelo; // Necesario
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.NecesidadVueloRepository;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.VueloRepository; // Para validar vuelo existente
import jakarta.persistence.EntityNotFoundException; // Para errores
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Asegúrate de tener este import

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NecesidadVueloService {

    private final NecesidadVueloRepository necesidadVueloRepository;
    private final VueloRepository vueloRepository; // Para verificar que el vuelo existe

    // Busca todas las necesidades para un ID de vuelo dado
    @Transactional(readOnly = true)
    public List<NecesidadVuelo> findByVueloId(Long vueloId) {
        // Verifica si el vuelo existe antes de buscar sus necesidades
         Vuelo vuelo = vueloRepository.findById(vueloId)
                 .orElseThrow(() -> new EntityNotFoundException("Vuelo no encontrado con ID: " + vueloId));
        // Usa el método del repo que trae las posiciones
        return necesidadVueloRepository.findByVueloIdVueloFetchingPosicion(vueloId); //
    }

    @Transactional(readOnly = true)
    public Optional<NecesidadVuelo> findById(Long id) {
        return necesidadVueloRepository.findById(id); //
    }

    @Transactional
    public NecesidadVuelo save(NecesidadVuelo necesidadVuelo) {
        // Validaciones de negocio podrían ir aquí:
        // - ¿Las horas de cobertura están dentro de un margen razonable de las horas del vuelo?
        // - ¿Ya existe un requerimiento idéntico (mismo vuelo/posición)? (La BD lo evitará por unique constraint)
        // - Asegurarse que vuelo y posicion no sean nulos (ya cubierto por @NotNull)

        // Asegurarse que el vuelo asociado existe
        if (necesidadVuelo.getVuelo() == null || necesidadVuelo.getVuelo().getIdVuelo() == null ||
            !vueloRepository.existsById(necesidadVuelo.getVuelo().getIdVuelo())) {
             throw new IllegalArgumentException("La necesidad debe estar asociada a un vuelo válido existente."); //
        }
         // Podríamos añadir validación similar para PosicionSeguridad si fuera necesario

        return necesidadVueloRepository.save(necesidadVuelo); //
    }

    @Transactional
    public void deleteById(Long id) {
        if (!necesidadVueloRepository.existsById(id)) {
             throw new EntityNotFoundException("Necesidad de Vuelo no encontrada con ID: " + id); //
        }
        // Considerar si hay lógica adicional antes de borrar (ej. si ya hay asignaciones hechas)
        necesidadVueloRepository.deleteById(id); //
    }

    // --- MÉTODO AÑADIDO ---
    /**
     * Elimina todas las necesidades asociadas a un ID de vuelo específico.
     * Es importante que este método sea transaccional.
     * @param vueloId El ID del Vuelo cuyas necesidades se eliminarán.
     */
    @Transactional // Asegura que la operación de borrado sea atómica
    public void deleteByVueloId(Long vueloId) {
        // Opcional: Verificar si el vuelo existe antes de intentar borrar sus necesidades
        if (!vueloRepository.existsById(vueloId)) {
            // Puedes lanzar una excepción o simplemente registrar un warning/info
             System.out.println("Advertencia: Intento de borrar necesidades para un Vuelo ID que no existe: " + vueloId);
             // No lanzamos excepción aquí para permitir que el borrado del vuelo (si existe) continúe.
             // O podrías lanzar: throw new EntityNotFoundException("Vuelo no encontrado con ID: " + vueloId);
             return; // Salir si el vuelo no existe
        }
        // Delega la llamada al método del repositorio que borra por ID de vuelo
        necesidadVueloRepository.deleteByVueloIdVuelo(vueloId); //
        System.out.println("Necesidades para Vuelo ID " + vueloId + " eliminadas (si existían)."); // Log informativo
    }
    // --- FIN MÉTODO AÑADIDO ---


    public long count() {
        return necesidadVueloRepository.count(); //
    }

     public long countByVuelo(Vuelo vuelo) {
        // Necesitaría un método en el repositorio: countByVuelo(Vuelo vuelo)
        // Por ahora, no implementado directamente.
        return findByVueloId(vuelo.getIdVuelo()).size(); // Forma simple pero menos eficiente
    }
}