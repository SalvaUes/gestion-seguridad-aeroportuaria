package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.NecesidadVuelo;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Vuelo;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.shared.Registration;

import java.time.format.DateTimeFormatter;

public class VueloCard extends VerticalLayout {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm 'hrs'");

    public VueloCard(Vuelo vuelo, int personalAsignado) {
        addClassName("vuelo-card");
        setSpacing(false);
        setPadding(false);

        // -- Encabezado (Aerolínea, Vuelo) --
        Span nombreAerolinea = new Span(vuelo.getAerolinea().getNombre());
        nombreAerolinea.addClassName("aerolinea-nombre");
        Span numeroVuelo = new Span(vuelo.getNumeroVuelo());
        numeroVuelo.addClassName("numero-vuelo");
        HorizontalLayout headerLayout = new HorizontalLayout(nombreAerolinea, numeroVuelo);
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        // -- Ruta --
        Span rutaVuelo = new Span(vuelo.getOrigen() + " → " + vuelo.getDestino());
        rutaVuelo.addClassName("ruta-vuelo");

        // -- Sección de Estado (Hora y Barra de Progreso) --
        Div statusSection = createStatusSection(vuelo, personalAsignado);
        
        // -- Añadir todo a la tarjeta --
        add(headerLayout, rutaVuelo, statusSection);
        
        // La tarjeta dispara nuestro evento personalizado al hacer clic
        addClickListener(event -> fireEvent(new CardClickEvent(this, vuelo)));
    }

    private Div createStatusSection(Vuelo vuelo, int asignado) {
        Div footer = new Div();
        footer.setWidthFull();

        int necesario = vuelo.getNecesidades().stream()
                .mapToInt(NecesidadVuelo::getCantidadAgentes)
                .sum();

        // Hora de Operación
        String horaOperacion = vuelo.getTipoOperacion().toString().contains("LLEGADA") ?
                vuelo.getFechaHoraLlegada().format(TIME_FORMATTER) :
                vuelo.getFechaHoraSalida().format(TIME_FORMATTER);
        Span hora = new Span(horaOperacion);
        hora.addClassName("hora-operacion");

        // Barra de Progreso
        ProgressBar progressBar = new ProgressBar(0, necesario, asignado);
        Span progressText = new Span("Personal: " + asignado + "/" + necesario);
        progressText.addClassName("progress-text");

        // Icono de Conflicto
        Icon conflictIcon = VaadinIcon.WARNING.create();
        conflictIcon.addClassName("conflict-icon");
        conflictIcon.setVisible(asignado < necesario);

        HorizontalLayout progressLayout = new HorizontalLayout(progressText, conflictIcon);
        progressLayout.setAlignItems(Alignment.CENTER);
        progressLayout.getStyle().set("margin-left", "auto");

        HorizontalLayout statusLayout = new HorizontalLayout(hora, progressLayout);
        statusLayout.setWidthFull();
        statusLayout.setAlignItems(Alignment.CENTER);
        
        footer.add(progressBar, statusLayout);
        updateStatusStyles(asignado, necesario);
        return footer;
    }

    private void updateStatusStyles(int asignado, int necesario) {
        removeClassName("status-ok");
        removeClassName("status-warning");
        removeClassName("status-critical");

        if (necesario == 0 || asignado >= necesario) {
            addClassName("status-ok");
        } else if (asignado > 0) {
            addClassName("status-warning");
        } else {
            addClassName("status-critical");
        }
    }

    // --- INICIO DEL CÓDIGO AÑADIDO ---
    // Clase interna para el evento de clic personalizado
    public static class CardClickEvent extends ComponentEvent<VueloCard> {
        private final Vuelo vuelo;

        public CardClickEvent(VueloCard source, Vuelo vuelo) {
            super(source, false);
            this.vuelo = vuelo;
        }

        public Vuelo getVuelo() {
            return vuelo;
        }
    }

    // Método público para agregar un listener para nuestro evento personalizado
    public Registration addCardClickListener(ComponentEventListener<CardClickEvent> listener) {
        getStyle().set("cursor", "pointer"); // Cambia el cursor para indicar que es clickeable
        return addListener(CardClickEvent.class, listener);
    }
    // --- FIN DEL CÓDIGO AÑADIDO ---
}