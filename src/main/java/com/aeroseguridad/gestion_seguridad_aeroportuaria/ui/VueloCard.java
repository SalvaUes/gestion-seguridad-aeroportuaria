// RUTA: src/main/java/com/aeroseguridad/gestion_seguridad_aeroportuaria/ui/VueloCard.java
package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.EstadoVuelo;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.NecesidadVuelo;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.TipoOperacionVuelo;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Vuelo;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.NecesidadVueloService;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class VueloCard extends VerticalLayout {

    private final Vuelo vuelo;

    private static final DateTimeFormatter CARD_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter CARD_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yy");

    // --- MEJORA: El constructor ya no depende de VueloListView ---
    public VueloCard(Vuelo vuelo, NecesidadVueloService necesidadService) {
        this.vuelo = vuelo;

        addClassName("vuelo-card");
        setSpacing(false);
        setPadding(false);

        Div cardContent = new Div();
        cardContent.addClassName("vuelo-card-content");

        Div headerSection = createHeaderSection(vuelo);
        Div rutaSection = createRutaSection(vuelo);
        Div fechasSection = createFechasSection(vuelo);
        Div footerSection = createFooterSection(vuelo, necesidadService);

        cardContent.add(headerSection, rutaSection, fechasSection, footerSection);
        add(cardContent);

        // --- MEJORA: Se dispara un evento personalizado al hacer clic ---
        addClickListener(event -> fireEvent(new CardClickEvent(this, this.vuelo)));
    }

    private Div createHeaderSection(Vuelo vuelo) {
        Div headerSection = new Div();
        headerSection.addClassNames("card-section", "header-section");
        Span numeroVuelo = new Span(vuelo.getNumeroVuelo() != null ? vuelo.getNumeroVuelo() : "N/V");
        numeroVuelo.addClassName("numero-vuelo");
        Span aerolinea = new Span(vuelo.getAerolinea() != null && vuelo.getAerolinea().getNombre() != null ? vuelo.getAerolinea().getNombre() : "Aerolínea N/A");
        aerolinea.addClassName("aerolinea");
        headerSection.add(numeroVuelo, aerolinea);
        return headerSection;
    }

    private Div createRutaSection(Vuelo vuelo) {
        Div rutaSection = new Div();
        rutaSection.addClassNames("card-section", "ruta-section");
        Icon tipoIcono = vuelo.getTipoOperacion() == TipoOperacionVuelo.LLEGADA_SOLO ? VaadinIcon.FLIGHT_LANDING.create() : VaadinIcon.FLIGHT_TAKEOFF.create();
        tipoIcono.addClassName("tipo-operacion-icono");
        Span origen = new Span(vuelo.getOrigen() != null ? vuelo.getOrigen() : "---");
        origen.addClassName("origen");
        Icon flecha = VaadinIcon.ARROW_RIGHT.create();
        flecha.addClassName("flecha-ruta");
        Span destino = new Span(vuelo.getDestino() != null ? vuelo.getDestino() : "---");
        destino.addClassName("destino");
        rutaSection.add(tipoIcono, origen, flecha, destino);
        return rutaSection;
    }

    private Div createFechasSection(Vuelo vuelo) {
        Div fechasSection = new Div();
        fechasSection.addClassNames("card-section", "fechas-section");
        fechasSection.add(createDateTimeElement("Salida:", vuelo.getFechaHoraSalida()));
        fechasSection.add(createDateTimeElement("Llegada:", vuelo.getFechaHoraLlegada()));
        return fechasSection;
    }

    private Div createFooterSection(Vuelo vuelo, NecesidadVueloService necesidadService) {
        Div footerSection = new Div();
        footerSection.addClassNames("card-section", "footer-section");
        Span estadoBadge = createEstadoBadge(vuelo.getEstado());
        Span tipoBadge = createTipoBadge(vuelo.getTipoOperacion());

        long countNecesidades = 0;
        if (vuelo.getIdVuelo() != null && necesidadService != null) {
            try {
                List<NecesidadVuelo> necesidades = necesidadService.findByVueloId(vuelo.getIdVuelo());
                countNecesidades = necesidades.size();
            } catch (Exception e) {
                // Error silencioso
            }
        }
        Span necesidadesInfo = new Span();
        necesidadesInfo.add(VaadinIcon.SHIELD.create(), new Span(String.valueOf(countNecesidades)));
        necesidadesInfo.addClassName("necesidades-info");

        footerSection.add(estadoBadge, tipoBadge, necesidadesInfo);
        return footerSection;
    }

    private Div createDateTimeElement(String labelText, LocalDateTime dateTime) {
        Div dateTimeElement = new Div();
        dateTimeElement.addClassName("datetime-element");

        Span label = new Span(labelText);
        label.addClassName("datetime-label");

        Span value = new Span();
        if (dateTime != null) {
            Span timeSpan = new Span(dateTime.format(CARD_TIME_FORMATTER));
            timeSpan.addClassName("datetime-time");
            Span dateSpan = new Span(dateTime.format(CARD_DATE_FORMATTER));
            dateSpan.addClassName("datetime-date");
            value.add(timeSpan, dateSpan);
        } else {
            value.setText("N/A");
            value.addClassName("datetime-na");
        }
        value.addClassName("datetime-value");
        dateTimeElement.add(label, value);
        return dateTimeElement;
    }

    private Span createEstadoBadge(EstadoVuelo estado) {
        String textoEstado = "Desconocido";
        String claseCssEstado = "default";
        if (estado != null) {
            textoEstado = estado.toString().replace("_", " ");
            claseCssEstado = estado.name().toLowerCase();
        }
        Span badge = new Span(textoEstado);
        badge.addClassName("status-badge");
        badge.addClassName("status-badge-" + claseCssEstado);
        return badge;
    }

    private Span createTipoBadge(TipoOperacionVuelo tipo) {
        String textoTipo = "N/A";
        String claseCssTipo = "default";
        if (tipo != null) {
            textoTipo = tipo.toString();
            claseCssTipo = tipo.name().toLowerCase();
        }
        Span badge = new Span(textoTipo);
        badge.addClassName("type-badge");
        badge.addClassName("type-badge-" + claseCssTipo);
        return badge;
    }

    // --- MEJORA: Sistema de Eventos Personalizado ---
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

    public Registration addCardClickListener(ComponentEventListener<CardClickEvent> listener) {
        getStyle().set("cursor", "pointer");
        return addListener(CardClickEvent.class, listener);
    }
}