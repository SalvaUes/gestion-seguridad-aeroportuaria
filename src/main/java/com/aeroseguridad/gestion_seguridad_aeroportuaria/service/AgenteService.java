// RUTA: src/main/java/com/aeroseguridad/gestion_seguridad_aeroportuaria/service/AgenteService.java
package com.aeroseguridad.gestion_seguridad_aeroportuaria.service;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PosicionSeguridad;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Rol;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.AgenteRepository;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.PosicionSeguridadRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger; // NUEVO
import org.slf4j.LoggerFactory; // NUEVO
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
import java.util.UUID;

@Service
public class AgenteService {

    // NUEVO: Añadido Logger para trazabilidad
    private static final Logger log = LoggerFactory.getLogger(AgenteService.class);

    private final AgenteRepository agenteRepository;
    private final PosicionSeguridadRepository posicionSeguridadRepository;
    private final String UPLOAD_DIR_NAME = "agent-photos";
    private Path rootLocation;

    public AgenteService(AgenteRepository agenteRepository, PosicionSeguridadRepository posicionSeguridadRepository) {
        this.agenteRepository = agenteRepository;
        this.posicionSeguridadRepository = posicionSeguridadRepository;
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

    @Transactional(readOnly = true)
    public List<Agente> list(String nombre, Rol rol, Boolean activo) {
        Specification<Agente> spec = (root, query, cb) -> {
            root.fetch("posicionesHabilitadas", JoinType.LEFT);

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

    @Transactional(readOnly = true)
    public List<Agente> findByRol(Rol rol) {
        return agenteRepository.findByRol(rol);
    }

    @Transactional(readOnly = true)
    public List<Agente> findAgentesDisponiblesPorRol(Rol rol) {
        return agenteRepository.findByRolAndSuperiorIsNull(rol);
    }

    @Transactional(readOnly = true)
    public List<Agente> findSubordinados(Agente superior) {
        return agenteRepository.findBySuperior(superior);
    }

    @Transactional
    public Agente asignarSuperior(Agente subordinado, Agente nuevoSuperior) {
        subordinado.setSuperior(nuevoSuperior);
        return agenteRepository.save(subordinado);
    }

    @Transactional
    public Agente save(Agente agente, InputStream fotoInputStream, String nombreArchivoOriginal) {
        if (agente.getIdAgente() == null) {
            agente.setActivo(true);
        }
        String oldPhotoPath = agente.getRutaFotografia();
        if (fotoInputStream != null && nombreArchivoOriginal != null && !nombreArchivoOriginal.isEmpty()) {
            try {
                String nuevoNombreArchivo = guardarFoto(fotoInputStream, nombreArchivoOriginal);
                agente.setRutaFotografia(nuevoNombreArchivo);
                // Borrar la foto antigua solo después de que la nueva se haya guardado y el nombre esté asignado
                borrarFoto(oldPhotoPath);
            } catch (IOException e) {
                throw new RuntimeException("Fallo al guardar la foto: " + nombreArchivoOriginal, e);
            }
        }
        return agenteRepository.save(agente);
    }
    
    // NUEVO: Método para borrado permanente
    @Transactional
    public void deleteById(Long id) {
        // Primero, busca el agente para obtener la ruta del archivo de la foto
        Agente agente = agenteRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("No se encontró el Agente con ID: " + id + " para borrar."));
        
        String photoFileName = agente.getRutaFotografia();

        // Borra el registro de la base de datos
        agenteRepository.delete(agente);
        log.info("Agente con ID {} borrado de la base de datos.", id);

        // Después de borrar de la BD con éxito, borra el archivo de la foto
        borrarFoto(photoFileName);
    }

    private String guardarFoto(InputStream inputStream, String nombreOriginal) throws IOException {
        String fileExtension = "";
        int i = nombreOriginal.lastIndexOf('.');
        if (i > 0) {
            fileExtension = nombreOriginal.substring(i);
        }
        String nuevoNombreArchivo = UUID.randomUUID().toString() + fileExtension;
        Path destinationFile = this.rootLocation.resolve(Paths.get(nuevoNombreArchivo)).normalize().toAbsolutePath();

        if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
            throw new IOException("No se puede guardar el archivo fuera del directorio de carga actual.");
        }

        Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        return nuevoNombreArchivo;
    }

    // MODIFICADO: Refactorizado a un método de borrado genérico con logging
    private void borrarFoto(String photoFileName) {
        if (photoFileName != null && !photoFileName.isEmpty()) {
            try {
                Path filePath = this.rootLocation.resolve(photoFileName).normalize();
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                    log.info("Archivo de foto '{}' borrado con éxito.", photoFileName);
                }
            } catch (IOException e) {
                log.warn("No se pudo borrar el archivo de foto '{}': {}", photoFileName, e.getMessage());
            }
        }
    }

    @Transactional(readOnly = true)
    public Optional<Agente> findById(Long id) {
        return agenteRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Agente> findByIdFetchingPosiciones(Long id) {
        return agenteRepository.findByIdFetchingPosiciones(id);
    }

    @Transactional(readOnly = true)
    public Optional<Agente> findActivoByNumeroCarnet(String numeroCarnet) {
        if (!StringUtils.hasText(numeroCarnet)) {
            return Optional.empty();
        }
        return agenteRepository.findActivoByNumeroCarnetIgnoreCaseFetchingPosiciones(numeroCarnet.trim());
    }

    @Transactional
    public void deactivateById(Long id) {
        Agente agente = agenteRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Agente no encontrado con ID: " + id));
        agente.setActivo(false);
        agenteRepository.save(agente);
    }

    @Transactional(readOnly = true)
    public List<PosicionSeguridad> findAllPosiciones() {
        return posicionSeguridadRepository.findByActivoTrueOrderByNombrePosicionAsc();
    }

    @Transactional(readOnly = true)
    public List<Agente> findAllActiveForView(String filter) {
        if (filter == null || filter.trim().isEmpty()) {
            return list(null, null, true);
        } else {
            return list(filter.trim(), null, true);
        }
    }

    public long countAll() {
        return agenteRepository.count();
    }
}