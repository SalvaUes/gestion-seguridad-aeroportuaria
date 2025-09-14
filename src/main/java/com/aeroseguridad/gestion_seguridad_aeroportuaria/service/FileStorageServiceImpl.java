package com.aeroseguridad.gestion_seguridad_aeroportuaria.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageServiceImpl.class);
    private final String UPLOAD_DIR_NAME = "agent-photos";
    private Path rootLocation;

    @Override
    @PostConstruct
    public void init() {
        try {
            this.rootLocation = Paths.get("src/main/resources/static/" + UPLOAD_DIR_NAME);
            if (Files.notExists(this.rootLocation)) {
                Files.createDirectories(this.rootLocation);
                log.info("Created directory for agent photos: {}", rootLocation.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("Could not initialize storage location", e);
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    @Override
    public String store(InputStream inputStream, String originalFilename) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("InputStream cannot be null");
        }
        String fileExtension = "";
        if (originalFilename != null && originalFilename.lastIndexOf('.') > 0) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }
        String newFilename = UUID.randomUUID().toString() + fileExtension;
        Path destinationFile = this.rootLocation.resolve(Paths.get(newFilename)).normalize().toAbsolutePath();

        if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
            throw new IOException("Cannot store file outside current directory.");
        }

        Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        return newFilename;
    }

    @Override
    public void delete(String filename) {
        if (filename == null || filename.isEmpty()) {
            return;
        }
        try {
            Path filePath = this.rootLocation.resolve(filename).normalize();
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Photo file '{}' deleted successfully.", filename);
            }
        } catch (IOException e) {
            log.warn("Could not delete photo file '{}': {}", filename, e.getMessage());
        }
    }
}
