package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PosicionSeguridad;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Rol;
import com.vaadin.flow.component.dependency.CssImport;
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

@CssImport("./themes/gestionseguridadaeroportuaria/styles.css")
public class AgenteCard extends VerticalLayout {

    private static final String IMAGE_BASE_URL = "agent-photos/";

    public AgenteCard(Agente agente, AgenteListView listView) {
        // Estilos de la tarjeta (sin cambios)
        addClassName("agente-card");
        setSpacing(false);
        getThemeList().add("spacing-s");
        getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
        getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        getStyle().set("padding", "var(--lumo-space-m)");
        getStyle().set("margin", "var(--lumo-space-s)");
        setWidth("280px");
        getStyle().set("position", "relative"); 

        // Etiqueta de Rol (sin cambios)
        Span rolEtiqueta = new Span();
        if (agente.getRol() != null) {
            rolEtiqueta.setText(agente.getRol().name());
            rolEtiqueta.getElement().getThemeList().add("badge");
            if (agente.getRol() == Rol.COORDINADOR) {
                rolEtiqueta.getElement().getThemeList().add("success");
            } else if (agente.getRol() == Rol.SUPERVISOR) {
                rolEtiqueta.getElement().getThemeList().add("contrast");
            }
            rolEtiqueta.getStyle().set("position", "absolute").set("top", "8px").set("right", "8px").set("font-size", "var(--lumo-font-size-xs)");
        }
        
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLayout.getStyle().set("margin-bottom", "var(--lumo-space-s)");

        // --- REFACTORIZACIÓN DEL CONTENEDOR DE IMAGEN ---
        Div imageContainer = new Div();
        imageContainer.setWidth("70px");
        imageContainer.setHeight("70px");
        imageContainer.getStyle().set("border-radius", "50%");
        imageContainer.getStyle().set("margin-right", "var(--lumo-space-m)");
        imageContainer.getStyle().set("flex-shrink", "0"); // Evita que el div se encoja

        if (agente.getRutaFotografia() != null && !agente.getRutaFotografia().isEmpty()) {
            String imageUrl = IMAGE_BASE_URL + agente.getRutaFotografia();
            // Aplicamos la imagen como fondo del Div
            imageContainer.getStyle().set("background-image", "url('" + imageUrl + "')");
            imageContainer.getStyle().set("background-size", "cover");      // Equivalente a object-fit: cover
            imageContainer.getStyle().set("background-position", "center"); // Centra la imagen
        } else {
            // Placeholder si no hay foto (con display:flex para centrar el icono)
            imageContainer.getStyle().set("display", "flex");
            imageContainer.getStyle().set("align-items", "center");
            imageContainer.getStyle().set("justify-content", "center");
            Icon placeholderIcon = VaadinIcon.USER.create();
            placeholderIcon.setSize("32px");
            placeholderIcon.setColor("var(--lumo-contrast-60pct)");
            imageContainer.getStyle().set("background-color", "var(--lumo-contrast-10pct)");
            imageContainer.add(placeholderIcon);
        }
        headerLayout.add(imageContainer);

        // Info Principal (sin cambios)
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

        // Detalles (sin cambios)
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

        add(rolEtiqueta, headerLayout, detailsLayout);
        setAlignItems(FlexComponent.Alignment.START);

        // Lógica de Clic (sin cambios)
        addClickListener(event -> {
            if (listView != null) {
                listView.editAgente(agente);
            }
        });
        getStyle().set("cursor", "pointer");
        addClassName("agente-card-hoverable");
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