package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui.components;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.NecesidadVuelo;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Vuelo;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;

import java.time.format.DateTimeFormatter;

public class VueloCard extends VerticalLayout {

    public VueloCard(Vuelo vuelo, int personalAsignado) {
        // --- Cálculo de Necesidades ---
        int personalNecesario = vuelo.getNecesidades().stream()
                .mapToInt(NecesidadVuelo::getCantidadAgentes)
                .sum();

        // --- Estructura Visual ---
        addClassName("vuelo-card");
        setSpacing(false);
        setPadding(false);

        // -- Encabezado (Aerolínea, Vuelo, Ruta) --
        Span nombreAerolinea = new Span(vuelo.getAerolinea().getNombre());
        nombreAerolinea.addClassName("aerolinea-nombre");

        Span numeroVuelo = new Span(vuelo.getNumeroVuelo());
        numeroVuelo.addClassName("numero-vuelo");

        Span rutaVuelo = new Span(vuelo.getOrigen() + " → " + vuelo.getDestino());
        rutaVuelo.addClassName("ruta-vuelo");

        HorizontalLayout headerLayout = new HorizontalLayout(nombreAerolinea, numeroVuelo);
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        // -- Cuerpo (Hora y Barra de Progreso) --
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm 'hrs'");
        String horaOperacion = vuelo.getTipoOperacion().toString().contains("LLEGADA") ?
                vuelo.getFechaHoraLlegada().format(timeFormatter) :
                vuelo.getFechaHoraSalida().format(timeFormatter);

        Span hora = new Span(horaOperacion);
        hora.addClassName("hora-operacion");

        ProgressBar progressBar = new ProgressBar(0, personalNecesario, personalAsignado);
        Span progressText = new Span("Personal: " + personalAsignado + "/" + personalNecesario);
        progressText.addClassName("progress-text");

        // -- Indicador de Conflicto --
        Icon conflictIcon = VaadinIcon.WARNING.create();
        conflictIcon.addClassName("conflict-icon");
        conflictIcon.setVisible(personalAsignado < personalNecesario); // Solo visible si hay conflictos

        HorizontalLayout footerLayout = new HorizontalLayout(hora, progressText, conflictIcon);
        footerLayout.setWidthFull();
        footerLayout.setAlignItems(Alignment.CENTER);
        
        // -- Añadir todo a la tarjeta --
        add(headerLayout, rutaVuelo, progressBar, footerLayout);
        
        // -- Aplicar Estilos de Estado --
        updateStatusStyles(personalAsignado, personalNecesario);
    }

    private void updateStatusStyles(int asignado, int necesario) {
        // Eliminar clases de estado anteriores para evitar conflictos
        removeClassName("status-ok");
        removeClassName("status-warning");
        removeClassName("status-critical");

        if (asignado >= necesario) {
            addClassName("status-ok"); // Verde
        } else if (asignado > 0) {
            addClassName("status-warning"); // Amarillo
        } else {
            addClassName("status-critical"); // Rojo
        }
    }
}