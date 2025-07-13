// RUTA: src/main/java/com/aeroseguridad/gestion_seguridad_aeroportuaria/AppShell.java
package com.aeroseguridad.gestion_seguridad_aeroportuaria;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;

/**
 * Esta es la configuración central y única para el tema y los estilos globales.
 */
@Theme("gestionseguridadaeroportuaria")
// --- CAMBIO FINAL Y DEFINITIVO EN LA RUTA ---
// Le damos la ruta completa desde la carpeta 'frontend' para eliminar toda ambigüedad.
@CssImport("./themes/gestionseguridadaeroportuaria/styles.css")
public class AppShell implements AppShellConfigurator {
    // La clase puede estar vacía. Su propósito es configurar el tema global.
}