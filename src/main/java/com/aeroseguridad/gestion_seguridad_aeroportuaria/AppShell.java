package com.aeroseguridad.gestion_seguridad_aeroportuaria;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

// --- CORRECCIÓN IMPORTANTE: Se usa el nombre de tema que existe en tu carpeta 'themes' ---
@Theme(value = "gestionseguridadaeroportuaria", variant = Lumo.LIGHT) 
@CssImport("./themes/gestionseguridadaeroportuaria/styles.css")
public class AppShell implements AppShellConfigurator {
    // La clase puede estar vacía. Su único propósito es contener las anotaciones.
}
