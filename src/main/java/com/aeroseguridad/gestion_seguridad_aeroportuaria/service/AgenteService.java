package com.aeroseguridad.gestion_seguridad_aeroportuaria.service;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PosicionSeguridad;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.AgenteRepository;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.PosicionSeguridadRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class AgenteService {

    private final AgenteRepository agenteRepository;
    private final PosicionSeguridadRepository posicionSeguridadRepository;

    private final String UPLOAD_DIRECTORY_NAME = "agent-photos"; // Debe coincidir con MvcConfig
    private final Path rootLocation = Paths.get(UPLOAD_DIRECTORY_NAME);

    // ... (otros métodos como findAllActiveForView, findById, etc. sin cambios) ...
    @Transactional(readOnly = true)
    public List<Agente> findAllActiveForView(String searchTerm) {
        if (!StringUtils.hasText(searchTerm)) {
            return agenteRepository.findActivosFetchingPosiciones();
        } else {
            return agenteRepository.searchActivosByNombreOrApellidoFetchingPosiciones(searchTerm.trim());
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
    public Agente save(Agente agente, InputStream fotoInputStream, String nombreArchivoOriginal) {
        if (agente.getIdAgente() == null) {
            agente.setActivo(true);
        }

        String oldPhotoPath = agente.getRutaFotografia();

        if (fotoInputStream != null && nombreArchivoOriginal != null && !nombreArchivoOriginal.isEmpty()) {
            try {
                String fileExtension = "";
                int i = nombreArchivoOriginal.lastIndexOf('.');
                if (i > 0) {
                    fileExtension = nombreArchivoOriginal.substring(i);
                }
                String nuevoNombreArchivo = UUID.randomUUID().toString() + fileExtension;

                if (!Files.exists(rootLocation)) {
                    Files.createDirectories(rootLocation);
                     System.out.println("INFO: Directorio de fotos (re)creado por AgenteService en: " + rootLocation.toAbsolutePath().toString());
                }

                Path destinationFile = rootLocation.resolve(nuevoNombreArchivo)
                                     .normalize().toAbsolutePath();

                Files.copy(fotoInputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
                // --- LOG CRUCIAL ---
                System.out.println("SUCCESS: Foto guardada en RUTA ABSOLUTA: " + destinationFile.toString());
                // --- FIN LOG CRUCIAL ---

                agente.setRutaFotografia(nuevoNombreArchivo); // Guardar SOLO el nombre del archivo

                if (oldPhotoPath != null && !oldPhotoPath.isEmpty() && !oldPhotoPath.equals(nuevoNombreArchivo)) {
                    try {
                        Path oldFilePathObject = rootLocation.resolve(oldPhotoPath).normalize().toAbsolutePath();
                        Files.deleteIfExists(oldFilePathObject);
                        System.out.println("INFO: Foto antigua borrada: " + oldPhotoPath);
                    } catch (IOException e) {
                        System.err.println("WARN: No se pudo borrar foto antigua " + oldPhotoPath + ": " + e.getMessage());
                    }
                }

            } catch (IOException e) {
                System.err.println("ERROR: Fallo al guardar la foto del agente: " + nombreArchivoOriginal);
                e.printStackTrace();
                // Considerar no cambiar agente.rutaFotografia si falla el guardado del archivo
                // o lanzar una excepción para que la transacción haga rollback.
                // throw new RuntimeException("Fallo al guardar la foto: " + nombreArchivoOriginal, e);
            } finally {
                 try {
                    if(fotoInputStream != null) fotoInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return agenteRepository.save(agente);
    }

    @Transactional
    public Agente save(Agente agente) {
        if (agente.getIdAgente() == null && agente.getActivo() == null) {
            agente.setActivo(true);
        }
        return agenteRepository.save(agente);
    }

    @Transactional
    public void deactivateById(Long id) {
        Agente agente = agenteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agente no encontrado con ID: " + id));
        agente.setActivo(false);
        agenteRepository.save(agente);
    }

    public long countActive() {
        return agenteRepository.findActivosFetchingPosiciones().size();
    }

    public long countAll() {
        return agenteRepository.count();
    }

    @Transactional(readOnly = true)
    public List<PosicionSeguridad> findAllPosiciones() {
        return posicionSeguridadRepository.findByActivoTrueOrderByNombrePosicionAsc();
    }
}