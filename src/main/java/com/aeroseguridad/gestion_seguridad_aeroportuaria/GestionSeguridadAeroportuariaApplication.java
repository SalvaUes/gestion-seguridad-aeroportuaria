package com.aeroseguridad.gestion_seguridad_aeroportuaria;

import com.vaadin.flow.component.page.AppShellConfigurator; // <<<--- AÑADE ESTA IMPORTACIÓN
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.vaadin.flow.theme.Theme;

@SpringBootApplication
@Theme(value = "gestionseguridadaeroportuaria") // Esta anotación ahora está en una clase AppShellConfigurator
public class GestionSeguridadAeroportuariaApplication implements AppShellConfigurator { // <<<--- IMPLEMENTA AppShellConfigurator

    public static void main(String[] args) {
        SpringApplication.run(GestionSeguridadAeroportuariaApplication.class, args);
    }

}