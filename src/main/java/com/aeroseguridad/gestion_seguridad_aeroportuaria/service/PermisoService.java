package com.aeroseguridad.gestion_seguridad_aeroportuaria.service;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.EstadoSolicitudPermiso;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Permiso;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.AgenteRepository;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.PermisoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PermisoService {

    private final PermisoRepository permisoRepository;
    private final AgenteRepository agenteRepository; // Para buscar Agente si es necesario

    @Transactional(readOnly = true)
    public List<Permiso> findAll() {
        // Podríamos necesitar JOIN FETCH aquí si la vista muestra agente
        return permisoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Permiso> findByDateRange(LocalDateTime rangoInicio, LocalDateTime rangoFin) {
        // Usa la consulta que trae el agente para la vista
        return permisoRepository.findByFechasSolapadasFetchingAgente(rangoInicio, rangoFin);
    }

    @Transactional(readOnly = true)
    public Optional<Permiso> findById(Long id) {
        return permisoRepository.findById(id);
    }

    @Transactional
    public Permiso save(Permiso permiso) {
        // Si es un permiso nuevo, establece la fecha de solicitud y el estado inicial
        if (permiso.getIdPermiso() == null) {
            permiso.setFechaSolicitud(LocalDateTime.now());
            // Asegura que el estado inicial sea SOLICITADO, a menos que se cambie explícitamente
            if (permiso.getEstadoSolicitud() == null) {
                 permiso.setEstadoSolicitud(EstadoSolicitudPermiso.SOLICITADO);
            }
        }
        // Aquí podríamos añadir validaciones de negocio (ej. solapamiento con otros permisos APROBADOS)
        return permisoRepository.save(permiso);
    }

    @Transactional
    public Permiso aprobarPermiso(Long id) {
        Permiso permiso = permisoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Permiso no encontrado con ID: " + id));
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
         // Considerar si se pueden borrar permisos o solo cancelar/rechazar
        if (!permisoRepository.existsById(id)) {
             throw new EntityNotFoundException("Permiso no encontrado con ID: " + id);
        }
        permisoRepository.deleteById(id);
    }

    // Método para verificar disponibilidad (a usar por la lógica de asignación)
    @Transactional(readOnly = true)
    public boolean hasApprovedLeave(Agente agente, LocalDateTime rangoInicio, LocalDateTime rangoFin) {
         List<Permiso> permisosAprobados = permisoRepository.findByAgenteAndEstadoAndFechasSolapadas(
                 agente,
                 EstadoSolicitudPermiso.APROBADO,
                 rangoInicio,
                 rangoFin);
         return !permisosAprobados.isEmpty(); // Devuelve true si ENCUENTRA algún permiso aprobado solapado
    }

    public long count() {
        return permisoRepository.count();
    }
}