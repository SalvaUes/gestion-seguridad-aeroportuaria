package com.aeroseguridad.gestion_seguridad_aeroportuaria.service;

import java.io.InputStream;
import java.io.IOException;

public interface FileStorageService {
    void init();
    String store(InputStream inputStream, String originalFilename) throws IOException;
    void delete(String filename);
}
