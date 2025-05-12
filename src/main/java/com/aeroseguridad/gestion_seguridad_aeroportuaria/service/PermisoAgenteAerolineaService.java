package com.aeroseguridad.gestion_seguridad_aeroportuaria.service;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PermisoAgenteAerolinea;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Aerolinea;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.PermisoAgenteAerolineaRepository;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.AgenteRepository;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.AerolineaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PermisoAgenteAerolineaService {

    private final PermisoAgenteAerolineaRepository repository;
    private final AgenteRepository agenteRepository; // Para validar existencia
    private final AerolineaRepository aerolineaRepository; // Para validar existencia

    @Transactional(readOnly = true)
    public List<PermisoAgenteAerolinea> findAll() {
        return repository.findAllFetchingAll();
    }

    @Transactional(readOnly = true)
    public Optional<PermisoAgenteAerolinea> findById(Long id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<PermisoAgenteAerolinea> findByAgente(Long agenteId) {
        Agente agente = agenteRepository.findById(agenteId)
                .orElseThrow(() -> new EntityNotFoundException("Agente no encontrado: " + agenteId));
        return repository.findByAgenteFetchingAerolinea(agente);
    }

    @Transactional(readOnly = true)
    public List<PermisoAgenteAerolinea> findByAerolinea(Long aerolineaId) {
        Aerolinea aerolinea = aerolineaRepository.findById(aerolineaId)
                .orElseThrow(() -> new EntityNotFoundException("Aerolínea no encontrada: " + aerolineaId));
        return repository.findByAerolineaFetchingAgente(aerolinea);
    }

    @Transactional(readOnly = true)
    public Optional<PermisoAgenteAerolinea> findByAgenteAndAerolinea(Long agenteId, Long aerolineaId) {
        Agente agente = agenteRepository.findById(agenteId)
                .orElseThrow(() -> new EntityNotFoundException("Agente no encontrado: " + agenteId));
        Aerolinea aerolinea = aerolineaRepository.findById(aerolineaId)
                .orElseThrow(() -> new EntityNotFoundException("Aerolínea no encontrada: " + aerolineaId));
        return repository.findByAgenteAndAerolinea(agente, aerolinea);
    }

    @Transactional
    public PermisoAgenteAerolinea save(PermisoAgenteAerolinea permisoAgenteAerolinea) {
        // Validar que agente y aerolinea no sean null y existan
        if (permisoAgenteAerolinea.getAgente() == null || permisoAgenteAerolinea.getAgente().getIdAgente() == null ||
            !agenteRepository.existsById(permisoAgenteAerolinea.getAgente().getIdAgente())) {
            throw new IllegalArgumentException("Agente inválido o no especificado.");
        }
        if (permisoAgenteAerolinea.getAerolinea() == null || permisoAgenteAerolinea.getAerolinea().getIdAerolinea() == null ||
            !aerolineaRepository.existsById(permisoAgenteAerolinea.getAerolinea().getIdAerolinea())) {
            throw new IllegalArgumentException("Aerolínea inválida o no especificada.");
        }
        // La constraint UNIQUE en la BD (agente, aerolinea) manejará duplicados.
        // Podríamos verificar aquí si ya existe uno y actualizarlo en lugar de crear.
        Optional<PermisoAgenteAerolinea> existing = repository.findByAgenteAndAerolinea(
            permisoAgenteAerolinea.getAgente(), permisoAgenteAerolinea.getAerolinea());

        if(existing.isPresent() && (permisoAgenteAerolinea.getIdPermisoAa() == null ||
           !existing.get().getIdPermisoAa().equals(permisoAgenteAerolinea.getIdPermisoAa())) ) {
            // Si existe uno y no es el mismo que se está intentando guardar (caso de edición de uno nuevo que duplica)
            throw new DataIntegrityViolationException("Ya existe un permiso para este agente y aerolínea.");
        }

        return repository.save(permisoAgenteAerolinea);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Permiso Agente-Aerolínea no encontrado: " + id);
        }
        repository.deleteById(id);
    }
}