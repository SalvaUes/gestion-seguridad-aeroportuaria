// src/main/java/com/aeroseguridad/gestion_seguridad_aeroportuaria/service/PermisoAgenteAerolineaService.java
package com.aeroseguridad.gestion_seguridad_aeroportuaria.service;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PermisoAgenteAerolinea;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Aerolinea;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.EstadoPermiso; // Importar
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.PermisoAgenteAerolineaRepository;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.AgenteRepository;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.AerolineaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList; // Importar
import java.util.List;
import java.util.Map; // Importar
import java.util.Optional;
import java.util.stream.Collectors; // Importar

@Service
@RequiredArgsConstructor
public class PermisoAgenteAerolineaService {

    private final PermisoAgenteAerolineaRepository repository;
    private final AgenteRepository agenteRepository;
    private final AerolineaRepository aerolineaRepository;

    // ... (métodos findAll, findById, findByAgente, findByAerolinea como estaban) ...
    @Transactional(readOnly = true)
    public List<PermisoAgenteAerolinea> findAll() {
        return repository.findAllFetchingAll();
    }

    @Transactional(readOnly = true)
    public Optional<PermisoAgenteAerolinea> findById(Long id) {
        return repository.findByIdFetchingAll(id);
    }

    @Transactional(readOnly = true)
    public List<PermisoAgenteAerolinea> findByAgenteId(Long agenteId) { // Cambiado nombre para claridad
        Agente agente = agenteRepository.findById(agenteId)
                .orElseThrow(() -> new EntityNotFoundException("Agente no encontrado con ID: " + agenteId));
        return repository.findByAgenteFetchingAll(agente);
    }

    @Transactional(readOnly = true)
    public List<PermisoAgenteAerolinea> findByAerolineaId(Long aerolineaId) { // Cambiado nombre
        Aerolinea aerolinea = aerolineaRepository.findById(aerolineaId)
                .orElseThrow(() -> new EntityNotFoundException("Aerolínea no encontrada con ID: " + aerolineaId));
        return repository.findByAerolineaFetchingAll(aerolinea);
    }


    @Transactional // Eliminado save individual, se maneja con el método de abajo
    public PermisoAgenteAerolinea save(PermisoAgenteAerolinea permisoAgenteAerolinea) {
        // ... (lógica del save individual como estaba, para referencia o si se usa en otro lado) ...
        if (permisoAgenteAerolinea.getAgente() == null || permisoAgenteAerolinea.getAgente().getIdAgente() == null) {
            throw new IllegalArgumentException("Agente inválido o no especificado.");
        }
        if (permisoAgenteAerolinea.getAerolinea() == null || permisoAgenteAerolinea.getAerolinea().getIdAerolinea() == null) {
            throw new IllegalArgumentException("Aerolínea inválida o no especificada.");
        }
         Agente agente = agenteRepository.findById(permisoAgenteAerolinea.getAgente().getIdAgente())
                .orElseThrow(() -> new EntityNotFoundException("Agente no encontrado con ID: " + permisoAgenteAerolinea.getAgente().getIdAgente()));
        Aerolinea aerolinea = aerolineaRepository.findById(permisoAgenteAerolinea.getAerolinea().getIdAerolinea())
                .orElseThrow(() -> new EntityNotFoundException("Aerolínea no encontrada con ID: " + permisoAgenteAerolinea.getAerolinea().getIdAerolinea()));
        permisoAgenteAerolinea.setAgente(agente);
        permisoAgenteAerolinea.setAerolinea(aerolinea);

        if (permisoAgenteAerolinea.getIdPermisoAa() == null) {
            Optional<PermisoAgenteAerolinea> existing = repository.findByAgenteAndAerolinea(agente, aerolinea);
            if (existing.isPresent()) {
                // Si ya existe, actualiza el existente en lugar de crear uno nuevo
                PermisoAgenteAerolinea toUpdate = existing.get();
                toUpdate.setEstadoPermiso(permisoAgenteAerolinea.getEstadoPermiso());
                return repository.save(toUpdate);
            }
        }
        return repository.save(permisoAgenteAerolinea);
    }


    // --- NUEVO MÉTODO PARA GUARDAR MÚLTIPLES PERMISOS ---
    @Transactional
    public List<PermisoAgenteAerolinea> guardarPermisosParaAgente(Agente agente, Map<Long, EstadoPermiso> permisosPorAerolineaId) {
        if (agente == null || agente.getIdAgente() == null) {
            throw new IllegalArgumentException("Agente no puede ser nulo.");
        }
        Agente agenteValidado = agenteRepository.findById(agente.getIdAgente())
                .orElseThrow(() -> new EntityNotFoundException("Agente no encontrado con ID: " + agente.getIdAgente()));

        List<PermisoAgenteAerolinea> resultados = new ArrayList<>();

        // Obtener todos los permisos existentes para este agente
        Map<Long, PermisoAgenteAerolinea> permisosExistentesMap = repository.findByAgente(agenteValidado).stream()
                .collect(Collectors.toMap(paa -> paa.getAerolinea().getIdAerolinea(), paa -> paa));

        for (Map.Entry<Long, EstadoPermiso> entry : permisosPorAerolineaId.entrySet()) {
            Long aerolineaId = entry.getKey();
            EstadoPermiso nuevoEstado = entry.getValue();

            Aerolinea aerolinea = aerolineaRepository.findById(aerolineaId)
                    .orElseThrow(() -> new EntityNotFoundException("Aerolínea no encontrada con ID: " + aerolineaId));

            PermisoAgenteAerolinea permiso = permisosExistentesMap.get(aerolineaId);

            if (permiso != null) { // Si ya existe un permiso para esta aerolínea
                if (nuevoEstado == null) { // Si el nuevo estado es null (ej. se desmarcó), se elimina
                    repository.delete(permiso);
                } else if (permiso.getEstadoPermiso() != nuevoEstado) { // Si el estado cambió
                    permiso.setEstadoPermiso(nuevoEstado);
                    resultados.add(repository.save(permiso));
                } else {
                    resultados.add(permiso); // No hubo cambios, solo añadir a la lista de resultados
                }
            } else if (nuevoEstado != null) { // Si no existía y se especificó un nuevo estado (no null)
                PermisoAgenteAerolinea nuevoPermiso = new PermisoAgenteAerolinea();
                nuevoPermiso.setAgente(agenteValidado);
                nuevoPermiso.setAerolinea(aerolinea);
                nuevoPermiso.setEstadoPermiso(nuevoEstado);
                resultados.add(repository.save(nuevoPermiso));
            }
            permisosExistentesMap.remove(aerolineaId); // Remover de los existentes para ver cuáles quedaron sin modificar
        }

        // Opcional: Eliminar permisos que existían pero no estaban en la nueva lista de permisos (si se desmarcaron y nuevoEstado fue null)
        // Esto ya se maneja arriba si nuevoEstado es null. Si un permiso existente no está en permisosPorAerolineaId
        // pero la UI implica que debe eliminarse, esa lógica debe ser más explícita.
        // Por ahora, solo actualizamos/creamos los que vienen en el mapa.
        // Para una limpieza completa, podrías hacer:
        // for(PermisoAgenteAerolinea paaNoModificado : permisosExistentesMap.values()){
        //     // Si una aerolínea no vino en el mapa de la UI, significa que el usuario la deseleccionó
        //     // o no se le asignó estado, lo que podría implicar eliminar el permiso existente.
        //     if (!permisosPorAerolineaId.containsKey(paaNoModificado.getAerolinea().getIdAerolinea())) {
        //         repository.delete(paaNoModificado);
        //     }
        // }


        return resultados;
    }
    // --- FIN NUEVO MÉTODO ---

    @Transactional
    public void deleteById(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Permiso Agente-Aerolínea no encontrado con ID: " + id);
        }
        repository.deleteById(id);
    }
}