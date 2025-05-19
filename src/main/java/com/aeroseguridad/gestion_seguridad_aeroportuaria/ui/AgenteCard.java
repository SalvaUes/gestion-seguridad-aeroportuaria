package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PosicionSeguridad;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.stream.Collectors;

public class AgenteCard extends VerticalLayout {

    private static final String IMAGE_BASE_URL = "agent-photos/"; // Constante para la URL base

    public AgenteCard(Agente agente, AgenteListView listView) {
        addClassName("agente-card");
        setSpacing(false); // Quitar espaciado por defecto del VerticalLayout
        getThemeList().add("spacing-s"); // Usar Lumo spacing si se necesita
        getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
        getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        getStyle().set("padding", "var(--lumo-space-m)");
        getStyle().set("margin", "var(--lumo-space-s)"); // Margen entre tarjetas
        setWidth("280px"); // Ancho fijo para la tarjeta, ayuda a la consistencia del layout

        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER); // Alinear verticalmente
        headerLayout.getStyle().set("margin-bottom", "var(--lumo-space-s)"); // Espacio debajo del header

        // Contenedor para la imagen o el placeholder, para asegurar tamaño fijo
        Div imageContainer = new Div();
        imageContainer.setWidth("70px");
        imageContainer.setHeight("70px");
        imageContainer.getStyle().set("border-radius", "50%");
        imageContainer.getStyle().set("margin-right", "var(--lumo-space-m)");
        imageContainer.getStyle().set("overflow", "hidden"); // Importante para que border-radius afecte a la imagen
        imageContainer.getStyle().set("display", "flex"); // Para centrar placeholder si es necesario
        imageContainer.getStyle().set("align-items", "center");
        imageContainer.getStyle().set("justify-content", "center");

        if (agente.getRutaFotografia() != null && !agente.getRutaFotografia().isEmpty()) {
            Image foto = new Image();
            // La URL debe ser relativa al contexto de la aplicación
            String imageUrl = IMAGE_BASE_URL + agente.getRutaFotografia();
            foto.setSrc(imageUrl);
            foto.setAlt(agente.getNombreCompleto());
            // Estilos para que la imagen llene el contenedor Div redondo
            foto.setWidth("100%");
            foto.setHeight("100%");
            foto.getStyle().set("object-fit", "cover"); // Escala y recorta para llenar
            System.out.println("AgenteCard - Agente: " + agente.getNombreCompleto() + ", URL Imagen: " + foto.getSrc());
            imageContainer.add(foto);
        } else {
            // Placeholder visual si no hay foto
            Icon placeholderIcon = VaadinIcon.USER.create();
            placeholderIcon.setSize("32px"); // Tamaño del icono
            placeholderIcon.setColor("var(--lumo-contrast-60pct)");
            imageContainer.getStyle().set("background-color", "var(--lumo-contrast-10pct)");
            imageContainer.add(placeholderIcon);
            System.out.println("AgenteCard - Agente: " + agente.getNombreCompleto() + ", No hay foto, mostrando placeholder.");
        }
        headerLayout.add(imageContainer);


        VerticalLayout infoPrincipal = new VerticalLayout();
        infoPrincipal.setSpacing(false);
        infoPrincipal.setPadding(false);
        H4 nombreCompleto = new H4(agente.getNombreCompleto());
        nombreCompleto.addClassNames(LumoUtility.Margin.Top.NONE, LumoUtility.Margin.Bottom.XSMALL);
        Span carnet = new Span("Carnet: " + (agente.getNumeroCarnet() != null ? agente.getNumeroCarnet() : "N/A"));
        carnet.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);
        infoPrincipal.add(nombreCompleto, carnet);
        headerLayout.add(infoPrincipal);
        headerLayout.expand(infoPrincipal);

        VerticalLayout detailsLayout = new VerticalLayout();
        detailsLayout.setSpacing(false);
        detailsLayout.setPadding(false);
        detailsLayout.getThemeList().add("spacing-xs");
        detailsLayout.addClassNames(LumoUtility.Margin.Top.SMALL);

        if (agente.getEmail() != null && !agente.getEmail().isEmpty()) {
            detailsLayout.add(createInfoLine(VaadinIcon.ENVELOPE_O, agente.getEmail()));
        }
        if (agente.getTelefono() != null && !agente.getTelefono().isEmpty()) {
            detailsLayout.add(createInfoLine(VaadinIcon.PHONE, agente.getTelefono()));
        }

        String habilidadesStr = agente.getPosicionesHabilitadas().stream()
                                .filter(PosicionSeguridad::getActivo)
                                .map(PosicionSeguridad::getNombrePosicion)
                                .sorted()
                                .collect(Collectors.joining(", "));
        if (!habilidadesStr.isEmpty()) {
            detailsLayout.add(createInfoLine(VaadinIcon.TOOLS, "Habilidades: " + habilidadesStr));
        }

        add(headerLayout, detailsLayout);
        setAlignItems(FlexComponent.Alignment.START); // Alinear contenido de la tarjeta al inicio

        addClickListener(event -> {
            if (listView != null) {
                listView.editAgente(agente);
            }
        });
        getStyle().set("cursor", "pointer");
        addClassName("agente-card-hoverable"); // Para CSS opcional
    }

    private HorizontalLayout createInfoLine(VaadinIcon iconName, String text) {
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