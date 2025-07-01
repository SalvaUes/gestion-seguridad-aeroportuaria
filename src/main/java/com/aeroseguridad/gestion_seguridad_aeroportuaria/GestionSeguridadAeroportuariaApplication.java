package com.aeroseguridad.gestion_seguridad_aeroportuaria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * El punto de entrada de la aplicación Spring Boot.
 *
 * Usa la anotación @SpringBootApplication para habilitar la autoconfiguración,
 * el escaneo de componentes y la configuración de beans de Spring.
 * Esta clase NO debe configurar el AppShell, ya que esa responsabilidad
 * recae en la clase AppShell.java.
 *
 */
@SpringBootApplication
public class GestionSeguridadAeroportuariaApplication {

    public static void main(String[] args) {
        SpringApplication.run(GestionSeguridadAeroportuariaApplication.class, args);
    }

}