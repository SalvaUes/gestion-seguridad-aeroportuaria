package com.aeroseguridad.gestion_seguridad_aeroportuaria;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;

/**
 * Configuración central para el tema y los estilos globales.
 */
@Theme("gestionseguridadaeroportuaria")
// --- CORRECCIÓN FINAL Y DEFINITIVA EN LA RUTA ---
// Le damos la ruta completa desde la carpeta 'frontend' para eliminar toda ambigüedad.
@CssImport(value = "./themes/gestionseguridadaeroportuaria/styles.css", themeFor = "vaadin-app-layout")
public class AppShell implements AppShellConfigurator {
    // La clase puede estar vacía. Su propósito es configurar el tema global.
}