package com.aeroseguridad.gestion_seguridad_aeroportuaria.service;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PosicionSeguridad;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.AgenteRepository;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.PosicionSeguridadRepository;
import jakarta.persistence.EntityNotFoundException; // Para manejo de errores
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AgenteService {

    private final AgenteRepository agenteRepository;
    private final PosicionSeguridadRepository posicionSeguridadRepository;

    // Modificado para buscar solo activos Y con filtro opcional
    @Transactional(readOnly = true)
    public List<Agente> findAllActiveForView(String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            // Necesitamos un método en el repo que busque solo activos con JOIN FETCH
            return agenteRepository.findActivosFetchingPosiciones(); // <-- NECESITAMOS CREAR ESTE MÉTODO
        } else {
             // Necesitamos un método en el repo que busque por término Y activos con JOIN FETCH
            return agenteRepository.searchActivosByNombreOrApellidoFetchingPosiciones(searchTerm); // <-- NECESITAMOS CREAR ESTE MÉTODO
        }
    }

    @Transactional(readOnly = true)
    public Optional<Agente> findById(Long id) {
        return agenteRepository.findById(id);
    }

     @Transactional(readOnly = true)
    public Optional<Agente> findByIdFetchingPosiciones(Long id) {
         return agenteRepository.findById(id); // Considerar crear query específica con fetch si es necesario aquí
    }


    @Transactional
    public Agente save(Agente agente) {
        // Asegurarse que si es nuevo, esté activo por defecto
        if (agente.getIdAgente() == null) {
            agente.setActivo(true);
        }
        return agenteRepository.save(agente);
    }

    // --- MÉTODO deleteById ELIMINADO ---
    // public void deleteById(Long id) {
    //     agenteRepository.deleteById(id);
    // }

    // --- NUEVO MÉTODO para DESACTIVAR (Soft Delete) ---
    @Transactional
    public void deactivateById(Long id) {
        Agente agente = agenteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agente no encontrado con ID: " + id));
        agente.setActivo(false); // Cambia el estado a inactivo
        agenteRepository.save(agente); // Guarda el cambio
    }
    // --- FIN NUEVO MÉTODO ---

    public long count() {
        // Podríamos querer contar solo activos:
        // return agenteRepository.countByActivoTrue(); // <-- Necesitaríamos este método en el repo
        return agenteRepository.count(); // Por ahora cuenta todos
    }

    @Transactional(readOnly = true)
    public List<PosicionSeguridad> findAllPosiciones() {
        return posicionSeguridadRepository.findAll();
    }
}