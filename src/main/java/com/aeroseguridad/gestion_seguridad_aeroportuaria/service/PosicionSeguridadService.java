package com.aeroseguridad.gestion_seguridad_aeroportuaria.service;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PosicionSeguridad;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.PosicionSeguridadRepository;
// Quita imports no usados si los hay (ConstraintViolationException, DataIntegrity)
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service // <<<--- ¡ASEGÚRATE QUE ESTA ANOTACIÓN @Service ESTÉ PRESENTE! ---
@RequiredArgsConstructor
public class PosicionSeguridadService {

    private final PosicionSeguridadRepository posicionSeguridadRepository;

    @Transactional(readOnly = true)
    public List<PosicionSeguridad> findAll() {
        return posicionSeguridadRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<PosicionSeguridad> findById(Long id) {
        return posicionSeguridadRepository.findById(id);
    }

    @Transactional
    public PosicionSeguridad save(PosicionSeguridad posicion) {
        // La validación y unicidad se manejan en la capa de persistencia/entidad
        return posicionSeguridadRepository.save(posicion);
    }

    @Transactional
    public void deleteById(Long id) {
        // Podría lanzar DataIntegrityViolationException si hay FKs
        posicionSeguridadRepository.deleteById(id);
    }

    public long count() {
        return posicionSeguridadRepository.count();
    }

    // Método de búsqueda simple (a implementar en repo si se usa)
    @Transactional(readOnly = true)
    public List<PosicionSeguridad> findByNombre(String nombre) {
         if (nombre == null || nombre.isEmpty()) {
            return posicionSeguridadRepository.findAll();
        } else {
             System.err.println("Advertencia: Búsqueda por nombre de posición no implementada en repositorio, devolviendo todo.");
             // Devolver vacío o implementar findByNombrePosicionContainingIgnoreCase en el repo
             return posicionSeguridadRepository.findAll();
        }
    }
}