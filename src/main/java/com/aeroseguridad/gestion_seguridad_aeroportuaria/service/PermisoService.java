package com.aeroseguridad.gestion_seguridad_aeroportuaria.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.EstadoSolicitudPermiso;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Permiso;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.PermisoRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PermisoService {

    private final PermisoRepository permisoRepository;
    private final PCAService pcaService; // <-- INYECCIÓN DEL PCA

    @Transactional(readOnly = true)
    public List<Permiso> findAll() {
        return permisoRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public List<Permiso> findByDateRange(LocalDateTime rangoInicio, LocalDateTime rangoFin) {
        return permisoRepository.findByFechasSolapadasFetchingAgente(rangoInicio, rangoFin);
    }
    
    @Transactional(readOnly = true)
    public Optional<Permiso> findById(Long id) {
        return permisoRepository.findById(id);
    }

    @Transactional
    public Permiso save(Permiso permiso) {
        if (permiso.getIdPermiso() == null) {
            permiso.setFechaSolicitud(LocalDateTime.now());
            if (permiso.getEstadoSolicitud() == null) {
                permiso.setEstadoSolicitud(EstadoSolicitudPermiso.SOLICITADO);
            }
        }
        return permisoRepository.save(permiso);
    }

    @Transactional
    public Permiso aprobarPermiso(Long id) {
        Permiso permiso = permisoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Permiso no encontrado con ID: " + id));
        
        // --- INICIO DE LA INTEGRACIÓN CON PCA ---
        // Solo verificamos si el permiso aún no está aprobado para evitar re-verificaciones.
        if (permiso.getEstadoSolicitud() != EstadoSolicitudPermiso.APROBADO) {
            pcaService.verificarConflictoPorPermiso(permiso);
        }
        // --- FIN DE LA INTEGRACIÓN CON PCA ---

        permiso.setEstadoSolicitud(EstadoSolicitudPermiso.APROBADO);
        return permisoRepository.save(permiso);
    }

    @Transactional
    public Permiso rechazarPermiso(Long id) {
        Permiso permiso = permisoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Permiso no encontrado con ID: " + id));
        permiso.setEstadoSolicitud(EstadoSolicitudPermiso.RECHAZADO);
        return permisoRepository.save(permiso);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!permisoRepository.existsById(id)) {
            throw new EntityNotFoundException("Permiso no encontrado con ID: " + id);
        }
        permisoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean hasApprovedLeave(Agente agente, LocalDateTime rangoInicio, LocalDateTime rangoFin) {
        List<Permiso> permisosAprobados = permisoRepository.findByAgenteAndEstadoAndFechasSolapadas(
                agente,
                EstadoSolicitudPermiso.APROBADO,
                rangoInicio,
                rangoFin);
        return !permisosAprobados.isEmpty();
    }

    public long count() {
        return permisoRepository.count();
    }
}