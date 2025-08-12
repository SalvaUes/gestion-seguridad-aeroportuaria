package com.aeroseguridad.gestion_seguridad_aeroportuaria.service;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Aerolinea;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PosicionSeguridad;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Rol;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.AgenteRepository;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.AerolineaRepository;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.PosicionSeguridadRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AgenteService {

    private static final Logger log = LoggerFactory.getLogger(AgenteService.class);

    private final AgenteRepository agenteRepository;
    private final PosicionSeguridadRepository posicionSeguridadRepository;
    private final AerolineaRepository aerolineaRepository;
    private final String UPLOAD_DIR_NAME = "agent-photos";
    private Path rootLocation;

    public AgenteService(AgenteRepository agenteRepository, PosicionSeguridadRepository posicionSeguridadRepository, AerolineaRepository aerolineaRepository) {
        this.agenteRepository = agenteRepository;
        this.posicionSeguridadRepository = posicionSeguridadRepository;
        this.aerolineaRepository = aerolineaRepository;
    }

    @PostConstruct
    public void init() {
        try {
            this.rootLocation = Paths.get("src/main/resources/static/" + UPLOAD_DIR_NAME);
            if (Files.notExists(this.rootLocation)) {
                Files.createDirectories(this.rootLocation);
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo inicializar el directorio de subida de archivos.", e);
        }
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
    public Agente saveAgenteData(Agente agenteFromForm, InputStream fotoInputStream, String nombreArchivoOriginal) {
        Agente agenteToSave;
        
        // Si es una actualización, cargamos la entidad existente.
        if (agenteFromForm.getIdAgente() != null) {
            agenteToSave = agenteRepository.findById(agenteFromForm.getIdAgente())
                .orElseThrow(() -> new EntityNotFoundException("Agente no encontrado: " + agenteFromForm.getIdAgente()));
        } else {
            agenteToSave = agenteFromForm;
        }

        // Actualizamos todos los campos simples
        agenteToSave.setNombre(agenteFromForm.getNombre());
        agenteToSave.setApellido(agenteFromForm.getApellido());
        agenteToSave.setNumeroCarnet(agenteFromForm.getNumeroCarnet());
        agenteToSave.setGenero(agenteFromForm.getGenero());
        agenteToSave.setRol(agenteFromForm.getRol());
        agenteToSave.setEmail(agenteFromForm.getEmail());
        agenteToSave.setTelefono(agenteFromForm.getTelefono());
        agenteToSave.setFechaNacimiento(agenteFromForm.getFechaNacimiento());
        agenteToSave.setDireccion(agenteFromForm.getDireccion());
        agenteToSave.setActivo(agenteFromForm.getActivo());

        // Manejamos la foto
        if (fotoInputStream != null) {
            borrarFoto(agenteToSave.getRutaFotografia());
            try {
                String nuevoNombreArchivo = guardarFoto(fotoInputStream, nombreArchivoOriginal);
                agenteToSave.setRutaFotografia(nuevoNombreArchivo);
            } catch (IOException e) {
                throw new RuntimeException("Fallo al guardar la nueva foto.", e);
            }
        }
        
        return agenteRepository.save(agenteToSave);
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
    @Transactional public void deleteById(Long id) { Agente agente = agenteRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("No se encontró el Agente con ID: " + id + " para borrar.")); String photoFileName = agente.getRutaFotografia(); agenteRepository.delete(agente); log.info("Agente con ID {} borrado de la base de datos.", id); borrarFoto(photoFileName); }
    private String guardarFoto(InputStream inputStream, String nombreOriginal) throws IOException { String fileExtension = ""; int i = nombreOriginal.lastIndexOf('.'); if (i > 0) { fileExtension = nombreOriginal.substring(i); } String nuevoNombreArchivo = UUID.randomUUID().toString() + fileExtension; Path destinationFile = this.rootLocation.resolve(Paths.get(nuevoNombreArchivo)).normalize().toAbsolutePath(); if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) { throw new IOException("No se puede guardar el archivo fuera del directorio de carga actual."); } Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING); return nuevoNombreArchivo; }
    private void borrarFoto(String photoFileName) { if (photoFileName != null && !photoFileName.isEmpty()) { try { Path filePath = this.rootLocation.resolve(photoFileName).normalize(); if (Files.exists(filePath)) { Files.delete(filePath); log.info("Archivo de foto '{}' borrado con éxito.", photoFileName); } } catch (IOException e) { log.warn("No se pudo borrar el archivo de foto '{}': {}", photoFileName, e.getMessage()); } } }
    public Optional<Agente> findById(Long id) { return agenteRepository.findById(id); }
    public Optional<Agente> findByIdFetchingPosiciones(Long id) { return agenteRepository.findByIdFetchingPosiciones(id); }
    public Optional<Agente> findActivoByNumeroCarnet(String numeroCarnet) { if (!StringUtils.hasText(numeroCarnet)) { return Optional.empty(); } return agenteRepository.findActivoByNumeroCarnetIgnoreCaseFetchingPosiciones(numeroCarnet.trim()); }
    @Transactional public void deactivateById(Long id) { Agente agente = agenteRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("No se encontrado con ID: " + id)); agente.setActivo(false); agenteRepository.save(agente); }
    public List<PosicionSeguridad> findAllPosiciones() { return posicionSeguridadRepository.findByActivoTrueOrderByNombrePosicionAsc(); }
    public List<Aerolinea> findAllAerolineas() { return aerolineaRepository.findAllByOrderByNombreAsc(); }
    public long countAll() { return agenteRepository.count(); }
}