package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.EstadoVuelo;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.NecesidadVuelo;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.TipoOperacionVuelo;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Vuelo;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.NecesidadVueloService;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class VueloCard extends VerticalLayout {

    private Vuelo vuelo;
    private NecesidadVueloService necesidadService;
    private VueloListView vueloListView;

    private static final DateTimeFormatter CARD_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter CARD_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yy");

    public VueloCard(Vuelo vuelo, NecesidadVueloService necesidadService, VueloListView vueloListView) {
        this.vuelo = vuelo;
        this.necesidadService = necesidadService;
        this.vueloListView = vueloListView;

        addClassName("vuelo-card");
        setSpacing(false);
        setPadding(false); // Controlaremos el padding con CSS

        // Contenedor Principal de la Información
        Div cardContent = new Div();
        cardContent.addClassName("vuelo-card-content");

        // Sección 1: Encabezado (Número de Vuelo y Aerolínea)
        Div headerSection = new Div();
        headerSection.addClassNames("card-section", "header-section");
        Span numeroVuelo = new Span(vuelo.getNumeroVuelo() != null ? vuelo.getNumeroVuelo() : "N/V");
        numeroVuelo.addClassName("numero-vuelo");
        Span aerolinea = new Span(vuelo.getAerolinea() != null && vuelo.getAerolinea().getNombre() != null ? vuelo.getAerolinea().getNombre() : "Aerolínea N/A");
        aerolinea.addClassName("aerolinea");
        headerSection.add(numeroVuelo, aerolinea);

        // Sección 2: Ruta (Origen -> Destino) y Tipo de Operación
        Div rutaSection = new Div();
        rutaSection.addClassNames("card-section", "ruta-section");
        // Se ha corregido la constante, asumiendo que en lugar de LLEGADA se usa ARRIBO
        Icon tipoIcono = vuelo.getTipoOperacion() == TipoOperacionVuelo.LLEGADA_SOLO ? VaadinIcon.FLIGHT_LANDING.create() : VaadinIcon.FLIGHT_TAKEOFF.create();
        tipoIcono.addClassName("tipo-operacion-icono");
        Span origen = new Span(vuelo.getOrigen() != null ? vuelo.getOrigen() : "---");
        origen.addClassName("origen");
        Icon flecha = VaadinIcon.ARROW_RIGHT.create();
        flecha.addClassName("flecha-ruta");
        Span destino = new Span(vuelo.getDestino() != null ? vuelo.getDestino() : "---");
        destino.addClassName("destino");
        rutaSection.add(tipoIcono, origen, flecha, destino);

        // Sección 3: Fechas y Horas
        Div fechasSection = new Div();
        fechasSection.addClassNames("card-section", "fechas-section");
        fechasSection.add(createDateTimeElement("Salida:", vuelo.getFechaHoraSalida()));
        fechasSection.add(createDateTimeElement("Llegada:", vuelo.getFechaHoraLlegada()));

        // Sección 4: Estado y Necesidades
        Div footerSection = new Div();
        footerSection.addClassNames("card-section", "footer-section");
        Span estadoBadge = createEstadoBadge(vuelo.getEstado());

        long countNecesidades = 0;
        if (vuelo.getIdVuelo() != null && necesidadService != null) {
            try {
                List<NecesidadVuelo> necesidades = necesidadService.findByVueloId(vuelo.getIdVuelo());
                countNecesidades = necesidades.size();
            } catch (Exception e) {
                // Se controla el error silenciosamente, ya se loguea en VueloListView
            }
        }
        Span necesidadesInfo = new Span();
        necesidadesInfo.add(VaadinIcon.SHIELD.create(), new Span(String.valueOf(countNecesidades)));
        necesidadesInfo.addClassName("necesidades-info");

        Span tipoBadge = createTipoBadge(vuelo.getTipoOperacion());

        footerSection.add(estadoBadge, tipoBadge, necesidadesInfo);

        cardContent.add(headerSection, rutaSection, fechasSection, footerSection);
        add(cardContent);

        // Hacer la tarjeta clickeable para editar
        addClickListener(event -> {
            if (vueloListView != null) {
                vueloListView.editVuelo(this.vuelo);
            }
        });
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
}