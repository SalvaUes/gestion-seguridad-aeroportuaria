package com.aeroseguridad.gestion_seguridad_aeroportuaria.service;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PosicionSeguridad;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.AgenteRepository;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.PosicionSeguridadRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AgenteService {

    private final AgenteRepository agenteRepository;
    private final PosicionSeguridadRepository posicionSeguridadRepository;

    private final String UPLOAD_DIR_NAME = "agent-photos";
    private Path rootLocation;

    public AgenteService(AgenteRepository agenteRepository, PosicionSeguridadRepository posicionSeguridadRepository) {
        this.agenteRepository = agenteRepository;
        this.posicionSeguridadRepository = posicionSeguridadRepository;
    }

    // --- CORRECCIÓN DEFINITIVA DE LA RUTA ---
    @PostConstruct
    public void init() {
        try {
            // Esta ruta es robusta: apunta a la carpeta de recursos de origen.
            // Es la forma más fiable para un entorno de desarrollo.
            this.rootLocation = Paths.get("src/main/resources/static/" + UPLOAD_DIR_NAME);

            if (Files.notExists(this.rootLocation)) {
                Files.createDirectories(this.rootLocation);
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo inicializar el directorio de subida de archivos.", e);
        }
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
                borrarFotoAntiguaSiExiste(oldPhotoPath);
            } catch (IOException e) {
                System.err.println("ERROR: Fallo al guardar la foto del agente: " + nombreArchivoOriginal);
                e.printStackTrace();
                throw new RuntimeException("Fallo al guardar la foto: " + nombreArchivoOriginal, e);
            }
        }
        return agenteRepository.save(agente);
    }

    private String guardarFoto(InputStream inputStream, String nombreOriginal) throws IOException {
        String fileExtension = "";
        int i = nombreOriginal.lastIndexOf('.');
        if (i > 0) {
            fileExtension = nombreOriginal.substring(i);
        }
        String nuevoNombreArchivo = UUID.randomUUID().toString() + fileExtension;
        
        Path destinationFile = this.rootLocation.resolve(nuevoNombreArchivo);
        
        Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        
        return nuevoNombreArchivo;
    }

    private void borrarFotoAntiguaSiExiste(String oldPhotoFileName) {
        if (oldPhotoFileName != null && !oldPhotoFileName.isEmpty()) {
            try {
                Path oldFilePath = this.rootLocation.resolve(oldPhotoFileName);
                Files.deleteIfExists(oldFilePath);
            } catch (IOException e) {
                System.err.println("WARN: No se pudo borrar la foto antigua " + oldPhotoFileName + ": " + e.getMessage());
            }
        }
    }

    // --- El resto de los métodos permanecen sin cambios ---
    @Transactional(readOnly = true)
    public List<Agente> findAllActiveForView(String searchTerm) {
        if (!StringUtils.hasText(searchTerm)) {
            return agenteRepository.findActivosFetchingPosiciones();
        } else {
            return agenteRepository.searchActivosByNombreOrApellidoFetchingPosiciones(searchTerm.trim());
        }
    }
    
    @Transactional(readOnly = true)
    public Optional<Agente> findById(Long id) { return agenteRepository.findById(id); }
    
    @Transactional(readOnly = true)
    public Optional<Agente> findByIdFetchingPosiciones(Long id) { return agenteRepository.findByIdFetchingPosiciones(id); }
    
    @Transactional(readOnly = true)
    public Optional<Agente> findActivoByNumeroCarnet(String numeroCarnet) {
        if (!StringUtils.hasText(numeroCarnet)) { return Optional.empty(); }
        return agenteRepository.findActivoByNumeroCarnetIgnoreCaseFetchingPosiciones(numeroCarnet.trim());
    }
    
    @Transactional
    public void deactivateById(Long id) {
        Agente agente = agenteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agente no encontrado con ID: " + id));
        agente.setActivo(false);
        agenteRepository.save(agente);
    }

    public long countActive() { return agenteRepository.findActivosFetchingPosiciones().size(); }
    public long countAll() { return agenteRepository.count(); }

    @Transactional(readOnly = true)
    public List<PosicionSeguridad> findAllPosiciones() { return posicionSeguridadRepository.findByActivoTrueOrderByNombrePosicionAsc(); }
}