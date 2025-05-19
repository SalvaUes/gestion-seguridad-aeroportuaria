package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.EstadoVuelo;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.NecesidadVuelo;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.TipoOperacionVuelo;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Vuelo;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.NecesidadVueloService; // Para cargar necesidades
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class VueloCard extends VerticalLayout {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public VueloCard(Vuelo vuelo, NecesidadVueloService necesidadVueloService, VueloListView listView) {
        addClassName("vuelo-card");
        getStyle().set("border", "1px solid var(--lumo-contrast-10pct)");
        getStyle().set("border-radius", "var(--lumo-border-radius-l)");
        getStyle().set("padding", "var(--lumo-space-m)");
        getStyle().set("margin", "var(--lumo-space-s)");
        setWidth("350px"); // Ancho de la tarjeta

        // Indicador de Estado (Barra de Color)
        Div estadoIndicator = new Div();
        estadoIndicator.setHeight("100%"); // Ocupa toda la altura de la tarjeta
        estadoIndicator.setWidth("8px");
        estadoIndicator.getStyle().set("border-top-left-radius", "var(--lumo-border-radius-l)");
        estadoIndicator.getStyle().set("border-bottom-left-radius", "var(--lumo-border-radius-l)");
        estadoIndicator.getStyle().set("margin-right", "var(--lumo-space-m)");
        setEstadoIndicatorColor(estadoIndicator, vuelo.getEstado());

        // Contenido Principal de la Tarjeta
        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setPadding(false);
        contentLayout.setSpacing(false);
        contentLayout.getThemeList().add("spacing-s"); // Espacio entre elementos internos

        // Header: Número de Vuelo y Aerolínea
        HorizontalLayout header = new HorizontalLayout();
        header.setAlignItems(FlexComponent.Alignment.BASELINE);
        H4 numeroVueloText = new H4(vuelo.getNumeroVuelo());
        numeroVueloText.addClassNames(LumoUtility.Margin.NONE);
        Span aerolineaText = new Span(vuelo.getAerolinea() != null ? vuelo.getAerolinea().getNombre() : "N/A");
        aerolineaText.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);
        header.add(numeroVueloText, aerolineaText);
        header.setFlexGrow(1, numeroVueloText); // Número de vuelo ocupa espacio

        // Ruta: Origen - Destino
        HorizontalLayout rutaLayout = createInfoLineLayout(
                VaadinIcon.ARROWS_LONG_H,
                (vuelo.getOrigen() != null ? vuelo.getOrigen() : "N/A") + " → " + (vuelo.getDestino() != null ? vuelo.getDestino() : "N/A")
        );

        // Horarios
        HorizontalLayout salidaLayout = createInfoLineLayout(
                VaadinIcon.FLIGHT_TAKEOFF,
                "Salida: " + (vuelo.getFechaHoraSalida() != null ? vuelo.getFechaHoraSalida().format(DATE_FORMATTER) + " " + vuelo.getFechaHoraSalida().format(TIME_FORMATTER) : "N/A")
        );
        HorizontalLayout llegadaLayout = createInfoLineLayout(
                VaadinIcon.FLIGHT_LANDING,
                "Llegada: " + (vuelo.getFechaHoraLlegada() != null ? vuelo.getFechaHoraLlegada().format(DATE_FORMATTER) + " " + vuelo.getFechaHoraLlegada().format(TIME_FORMATTER) : "N/A")
        );

        // Tipo de Operación y Estado
        HorizontalLayout tipoOpEstadoLayout = new HorizontalLayout();
        tipoOpEstadoLayout.add(
                createInfoLineLayout(VaadinIcon.COG_O, "Op: " + (vuelo.getTipoOperacion() != null ? vuelo.getTipoOperacion().toString() : "N/A")),
                createInfoLineLayout(VaadinIcon.INFO_CIRCLE_O, "Est: " + (vuelo.getEstado() != null ? vuelo.getEstado().toString() : "N/A"))
        );
        tipoOpEstadoLayout.getStyle().set("gap", "var(--lumo-space-m)");


        // Resumen de Necesidades
        Div necesidadesTitle = new Div(new Span("Necesidades de Seguridad:"));
        necesidadesTitle.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.TERTIARY, LumoUtility.Margin.Top.SMALL);
        VerticalLayout necesidadesLayout = new VerticalLayout();
        necesidadesLayout.setPadding(false);
        necesidadesLayout.setSpacing(false);
        necesidadesLayout.getThemeList().add("spacing-xs");

        if (vuelo.getIdVuelo() != null) {
            List<NecesidadVuelo> necesidades = necesidadVueloService.findByVueloId(vuelo.getIdVuelo());
            if (necesidades == null || necesidades.isEmpty()) {
                necesidadesLayout.add(new Span("Sin definir"));
            } else {
                necesidades.forEach(nec -> {
                    String detalleNec = (nec.getPosicion() != null ? nec.getPosicion().getNombrePosicion() : "N/P") +
                                        ": " + nec.getCantidadAgentes() + " agente(s)";
                    necesidadesLayout.add(createInfoLineLayout(VaadinIcon.USER_CHECK, detalleNec));
                });
            }
        } else {
            necesidadesLayout.add(new Span("Guardar vuelo para ver necesidades."));
        }


        contentLayout.add(header, rutaLayout, salidaLayout, llegadaLayout, tipoOpEstadoLayout, necesidadesTitle, necesidadesLayout);

        // Layout final de la tarjeta con indicador y contenido
        HorizontalLayout cardInnerLayout = new HorizontalLayout(estadoIndicator, contentLayout);
        cardInnerLayout.setPadding(false); // El padding ya está en la tarjeta principal
        cardInnerLayout.setWidthFull();
        add(cardInnerLayout);

        // Hacer toda la tarjeta clickeable para editar
        addClickListener(event -> {
            if (listView != null) {
                listView.editVuelo(vuelo); // Llama al método en VueloListView
            }
        });
        getStyle().set("cursor", "pointer");
        addClassName("vuelo-card-hoverable"); // Para estilos hover opcionales
    }

    private void setEstadoIndicatorColor(Div indicator, EstadoVuelo estado) {
        String color = "var(--lumo-contrast-20pct)"; // Gris por defecto
        if (estado != null) {
            switch (estado) {
                case PROGRAMADO: color = "var(--lumo-primary-color)"; break; // Azul
                case EN_VUELO: color = "var(--lumo-success-color)"; break; // Verde
                case RETRASADO: color = "var(--lumo-warning-text-color)"; break; // Naranja/Amarillo (usar text color para más visibilidad)
                case CANCELADO: color = "var(--lumo-error-color)"; break; // Rojo
                case COMPLETADO: color = "var(--lumo-success-color-50pct)"; break; // Verde más claro
            }
        }
        indicator.getStyle().set("background-color", color);
    }

    private HorizontalLayout createInfoLineLayout(VaadinIcon iconName, String text) {
        Icon icon = iconName.create();
        icon.setSize("1em");
        icon.getStyle().set("margin-right", "0.5em");
        icon.setColor("var(--lumo-contrast-70pct)");

        Span span = new Span(text);
        span.addClassNames(LumoUtility.FontSize.SMALL);

        HorizontalLayout line = new HorizontalLayout(icon, span);
        line.setAlignItems(FlexComponent.Alignment.CENTER);
        return line;
    }
}