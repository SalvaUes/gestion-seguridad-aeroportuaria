package com.aeroseguridad.gestion_seguridad_aeroportuaria.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Aerolinea;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PosicionSeguridad;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Rol;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.AerolineaRepository;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.AgenteRepository;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.PosicionSeguridadRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

@Service
@Transactional(readOnly = true)
public class AgenteService {

    private static final Logger log = LoggerFactory.getLogger(AgenteService.class);

    private final AgenteRepository agenteRepository;
    private final PosicionSeguridadRepository posicionSeguridadRepository;
    private final AerolineaRepository aerolineaRepository;
    private final FileStorageService fileStorageService;

    public AgenteService(AgenteRepository agenteRepository, PosicionSeguridadRepository posicionSeguridadRepository, AerolineaRepository aerolineaRepository, FileStorageService fileStorageService) {
        this.agenteRepository = agenteRepository;
        this.posicionSeguridadRepository = posicionSeguridadRepository;
        this.aerolineaRepository = aerolineaRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<Agente> list(String nombre, Rol rol, Boolean activo) {
        Specification<Agente> spec = (root, query, cb) -> {
            root.fetch("posicionesHabilitadas", JoinType.LEFT);
            root.fetch("aerolineasPermitidas", JoinType.LEFT);
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(nombre)) {
                Predicate nombrePredicate = cb.like(cb.lower(root.get("nombre")), "%" + nombre.toLowerCase() + "%");
                Predicate apellidoPredicate = cb.like(cb.lower(root.get("apellido")), "%" + nombre.toLowerCase() + "%");
                predicates.add(cb.or(nombrePredicate, apellidoPredicate));
            }
            if (rol != null) {
                predicates.add(cb.equal(root.get("rol"), rol));
            }
            if (activo != null) {
                predicates.add(cb.equal(root.get("activo"), activo));
            }
            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return agenteRepository.findAll(spec);
    }
    
    @Transactional
    public Agente createAgente(Agente agente, InputStream fotoInputStream, String nombreArchivoOriginal) {
        if (fotoInputStream != null) {
            try {
                String nuevoNombreArchivo = fileStorageService.store(fotoInputStream, nombreArchivoOriginal);
                agente.setRutaFotografia(nuevoNombreArchivo);
            } catch (IOException e) {
                throw new RuntimeException("Fallo al guardar la foto.", e);
            }
        }
        return agenteRepository.save(agente);
    }

    @Transactional
    public Agente updateAgente(Agente agenteFromForm, InputStream fotoInputStream, String nombreArchivoOriginal) {
        Agente agenteToUpdate = agenteRepository.findById(agenteFromForm.getIdAgente())
            .orElseThrow(() -> new EntityNotFoundException("Agente no encontrado: " + agenteFromForm.getIdAgente()));

        updateAgenteFields(agenteToUpdate, agenteFromForm);

        if (fotoInputStream != null) {
            fileStorageService.delete(agenteToUpdate.getRutaFotografia());
            try {
                String nuevoNombreArchivo = fileStorageService.store(fotoInputStream, nombreArchivoOriginal);
                agenteToUpdate.setRutaFotografia(nuevoNombreArchivo);
            } catch (IOException e) {
                throw new RuntimeException("Fallo al guardar la nueva foto.", e);
            }
        }
        
        return agenteRepository.save(agenteToUpdate);
    }

    private void updateAgenteFields(Agente agenteToUpdate, Agente agenteFromForm) {
        agenteToUpdate.setNombre(agenteFromForm.getNombre());
        agenteToUpdate.setApellido(agenteFromForm.getApellido());
        agenteToUpdate.setNumeroCarnet(agenteFromForm.getNumeroCarnet());
        agenteToUpdate.setGenero(agenteFromForm.getGenero());
        agenteToUpdate.setRol(agenteFromForm.getRol());
        agenteToUpdate.setEmail(agenteFromForm.getEmail());
        agenteToUpdate.setTelefono(agenteFromForm.getTelefono());
        agenteToUpdate.setFechaNacimiento(agenteFromForm.getFechaNacimiento());
        agenteToUpdate.setDireccion(agenteFromForm.getDireccion());
        agenteToUpdate.setActivo(agenteFromForm.getActivo());
    }
    
    @Transactional
    public void sincronizarRelaciones(Long agenteId, Set<PosicionSeguridad> posiciones, Set<Aerolinea> aerolineas) {
        Agente agente = agenteRepository.findById(agenteId)
            .orElseThrow(() -> new EntityNotFoundException("Agente no encontrado para sincronizar relaciones: " + agenteId));
        
        // Sincroniza las posiciones
        agente.getPosicionesHabilitadas().clear();
        if (posiciones != null && !posiciones.isEmpty()) {
            agente.getPosicionesHabilitadas().addAll(posiciones);
        }

        // Sincroniza las aerolíneas
        agente.getAerolineasPermitidas().clear();
        if (aerolineas != null && !aerolineas.isEmpty()) {
            agente.getAerolineasPermitidas().addAll(aerolineas);
        }
        
        // Guardamos para persistir los cambios en las tablas de unión
        agenteRepository.save(agente);
    }

    // ... (El resto de los métodos se mantienen igual)
    public List<Agente> findAllActiveForView(String filter) { if (filter == null || filter.trim().isEmpty()) { return agenteRepository.findAllActivosWithPlantillas(); } else { return agenteRepository.findActivosByFiltroTexto(filter.trim()); } }
    public List<Agente> findByRol(Rol rol) { return agenteRepository.findByRol(rol); }
    public List<Agente> findAgentesDisponiblesPorRol(Rol rol) { return agenteRepository.findByRolAndSuperiorIsNull(rol); }
    public List<Agente> findSubordinados(Agente superior) { return agenteRepository.findBySuperior(superior); }
    @Transactional public Agente asignarSuperior(Agente subordinado, Agente nuevoSuperior) { subordinado.setSuperior(nuevoSuperior); return agenteRepository.save(subordinado); }
    @Transactional public void deleteById(Long id) { Agente agente = agenteRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("No se encontró el Agente con ID: " + id + " para borrar.")); String photoFileName = agente.getRutaFotografia(); agenteRepository.delete(agente); log.info("Agente con ID {} borrado de la base de datos.", id); fileStorageService.delete(photoFileName); }
    public Optional<Agente> findById(Long id) { return agenteRepository.findById(id); }
    public Optional<Agente> findByIdFetchingPosiciones(Long id) { return agenteRepository.findByIdFetchingPosiciones(id); }
    public Optional<Agente> findActivoByNumeroCarnet(String numeroCarnet) { if (!StringUtils.hasText(numeroCarnet)) { return Optional.empty(); } return agenteRepository.findActivoByNumeroCarnetIgnoreCaseFetchingPosiciones(numeroCarnet.trim()); }
    @Transactional public void deactivateById(Long id) { Agente agente = agenteRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("No se encontrado con ID: " + id)); agente.setActivo(false); agenteRepository.save(agente); }
    public List<PosicionSeguridad> findAllPosiciones() { return posicionSeguridadRepository.findByActivoTrueOrderByNombrePosicionAsc(); }
    public List<Aerolinea> findAllAerolineas() { return aerolineaRepository.findAllByOrderByNombreAsc(); }
    public long countAll() { return agenteRepository.count(); }
}