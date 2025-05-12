package com.aeroseguridad.gestion_seguridad_aeroportuaria.service;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PosicionSeguridad;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.PosicionSeguridadRepository;
import jakarta.persistence.EntityNotFoundException; // Para manejo de errores
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PosicionSeguridadService {

    private final PosicionSeguridadRepository posicionSeguridadRepository;

    // Devuelve todas las posiciones activas (para la UI principal)
    @Transactional(readOnly = true)
    public List<PosicionSeguridad> findAllActive() {
        return posicionSeguridadRepository.findByActivoTrueOrderByNombrePosicionAsc();
    }

    // Devuelve todas las posiciones (activas e inactivas, para algún caso administrativo si se necesita)
    @Transactional(readOnly = true)
    public List<PosicionSeguridad> findAllIncludingInactive() {
        return posicionSeguridadRepository.findAll();
    }


    @Transactional(readOnly = true)
    public Optional<PosicionSeguridad> findById(Long id) {
        // Devuelve la posición independientemente de su estado activo,
        // la lógica de si se puede editar una inactiva va en la UI o al guardar.
        return posicionSeguridadRepository.findById(id);
    }

    @Transactional
    public PosicionSeguridad save(PosicionSeguridad posicion) {
        // Si es una nueva posición, asegurar que 'activo' sea true por defecto
        if (posicion.getIdPosicion() == null && posicion.getActivo() == null) {
            posicion.setActivo(true);
        }
        return posicionSeguridadRepository.save(posicion);
    }

    // --- MÉTODO deleteById CAMBIADO a deactivateById ---
    @Transactional
    public void deactivateById(Long id) {
        PosicionSeguridad posicion = posicionSeguridadRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Posición no encontrada con ID: " + id));
        posicion.setActivo(false); // Cambia el estado a inactivo
        posicionSeguridadRepository.save(posicion); // Guarda el cambio
    }
    // --- FIN MÉTODO CAMBIADO ---

    // Reactivar una posición (opcional, si se necesita)
    @Transactional
    public void reactivateById(Long id) {
        PosicionSeguridad posicion = posicionSeguridadRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Posición no encontrada con ID: " + id));
        posicion.setActivo(true);
        posicionSeguridadRepository.save(posicion);
    }


    public long count() {
        // Considerar si contar solo activas:
        // return posicionSeguridadRepository.countByActivoTrue(); // Necesitaría este método en repo
        return posicionSeguridadRepository.count(); // Cuenta todas por ahora
    }

    // Método de búsqueda que filtra por nombre entre posiciones activas
    @Transactional(readOnly = true)
    public List<PosicionSeguridad> findActiveByNombre(String nombre) {
         if (nombre == null || nombre.trim().isEmpty()) {
            return findAllActive(); // Si no hay filtro, devuelve todas las activas
         } else {
             return posicionSeguridadRepository.findActivasByNombrePosicionContainingIgnoreCase(nombre.trim());
         }
    }
}