package com.aeroseguridad.gestion_seguridad_aeroportuaria.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    // Nombre de la carpeta donde se guardarán y desde donde se servirán las fotos
    // Esta carpeta se creará en la raíz del proyecto (donde se ejecuta el JAR/aplicación)
    private final String UPLOAD_DIRECTORY_NAME = "agent-photos";
    private final Path UPLOAD_DIRECTORY_PATH = Paths.get(UPLOAD_DIRECTORY_NAME);

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Crear el directorio si no existe al configurar los handlers
        // Esto es útil para el primer arranque o si la carpeta se borra.
        if (!Files.exists(UPLOAD_DIRECTORY_PATH)) {
            try {
                Files.createDirectories(UPLOAD_DIRECTORY_PATH);
                System.out.println("INFO: Directorio de fotos creado en: " + UPLOAD_DIRECTORY_PATH.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("ERROR: No se pudo crear el directorio de fotos '" + UPLOAD_DIRECTORY_NAME + "': " + e.getMessage());
                // Considerar si la aplicación debe fallar al arrancar si esto no se puede crear
                return; // No registrar el handler si no se puede crear el dir
            }
        }

        // Configurar el resource handler para servir archivos desde esta carpeta
        // Las imágenes serán accesibles en la URL "/agent-photos/nombre_archivo.jpg"
        // "file:./" hace que la ruta sea relativa al directorio de ejecución de la aplicación.
        String resourceLocation = "file:./" + UPLOAD_DIRECTORY_NAME + "/";
        registry.addResourceHandler("/" + UPLOAD_DIRECTORY_NAME + "/**")
                .addResourceLocations(resourceLocation);

        System.out.println("INFO: Configurado Resource Handler para /" + UPLOAD_DIRECTORY_NAME + "/** apuntando a " + resourceLocation + " (Absoluta: " + UPLOAD_DIRECTORY_PATH.toAbsolutePath() + ")");
    }
}