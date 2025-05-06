package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

// Modificación: value="" y layout = MainLayout.class
@Route(value = "", layout = MainLayout.class)
@PageTitle("Inicio | Gestión Seguridad")
@PermitAll
public class MainView extends VerticalLayout {

    public MainView() {
        add(
            new H1("Bienvenido a la Gestión de Seguridad Aeroportuaria"),
            new Paragraph("Seleccione una opción del menú lateral.")
        );

        // Quité setSizeFull() para que el layout principal lo controle mejor
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        addClassName("main-view");
    }
}