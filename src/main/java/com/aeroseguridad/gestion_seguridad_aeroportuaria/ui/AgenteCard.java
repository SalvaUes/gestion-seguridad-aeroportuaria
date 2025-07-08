// RUTA: src/main/java/com/aeroseguridad/gestion_seguridad_aeroportuaria/ui/AgenteCard.java
package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Rol;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.LumoUtility;


public class AgenteCard extends VerticalLayout {

    private static final String IMAGE_BASE_URL = "agent-photos/";
    private final Agente agente;

    public AgenteCard(Agente agente) {
        this.agente = agente;
        
        addClassName("agente-card"); // Usa la clase CSS para los estilos principales
        setSpacing(false);
        getThemeList().add("spacing-s");
        
        // --- MEJORA: Ancho Responsivo ---
        setWidthFull();
        setMaxWidth("340px");

        getStyle().setPosition(Style.Position.RELATIVE);

        add(createRolEtiqueta(agente));
        add(createHeaderLayout(agente));
        add(createDetailsLayout(agente));
        setAlignItems(FlexComponent.Alignment.START);

        addClickListener(event -> fireEvent(new CardClickEvent(this, this.agente)));
    }

    private Span createRolEtiqueta(Agente agente) {
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
        return rolEtiqueta;
    }

    private HorizontalLayout createHeaderLayout(Agente agente) {
        Div imageContainer = new Div();
        imageContainer.setWidth("70px");
        imageContainer.setHeight("70px");
        imageContainer.getStyle().set("border-radius", "50%").set("margin-right", "var(--lumo-space-m)").set("flex-shrink", "0");
        if (agente.getRutaFotografia() != null && !agente.getRutaFotografia().isEmpty()) {
            String imageUrl = IMAGE_BASE_URL + agente.getRutaFotografia();
            imageContainer.getStyle().set("background-image", "url('" + imageUrl + "')").set("background-size", "cover").set("background-position", "center");
        } else {
            Icon placeholderIcon = VaadinIcon.USER.create();
            placeholderIcon.setSize("32px");
            placeholderIcon.setColor("var(--lumo-contrast-60pct)");
            imageContainer.getStyle().set("display", "flex").set("align-items", "center").set("justify-content", "center").set("background-color", "var(--lumo-contrast-10pct)");
            imageContainer.add(placeholderIcon);
        }

        H4 nombreCompleto = new H4(agente.getNombreCompleto());
        nombreCompleto.addClassNames(LumoUtility.Margin.Top.NONE, LumoUtility.Margin.Bottom.XSMALL);
        Span carnet = new Span("Carnet: " + (agente.getNumeroCarnet() != null ? agente.getNumeroCarnet() : "N/A"));
        carnet.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);
        VerticalLayout infoPrincipal = new VerticalLayout(nombreCompleto, carnet);
        infoPrincipal.setSpacing(false);
        infoPrincipal.setPadding(false);

        HorizontalLayout headerLayout = new HorizontalLayout(imageContainer, infoPrincipal);
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        return headerLayout;
    }

    private VerticalLayout createDetailsLayout(Agente agente) {
        VerticalLayout detailsLayout = new VerticalLayout();
        detailsLayout.setSpacing(false);
        detailsLayout.setPadding(false);
        detailsLayout.getThemeList().add("spacing-xs");
        detailsLayout.addClassNames(LumoUtility.Margin.Top.SMALL);
        if (agente.getTelefono() != null && !agente.getTelefono().isEmpty()) {
            detailsLayout.add(createInfoLine(VaadinIcon.PHONE, agente.getTelefono()));
        }
        return detailsLayout;
    }

    private HorizontalLayout createInfoLine(VaadinIcon icon, String text) {
        Icon i = icon.create();
        i.setSize("1em");
        i.getStyle().set("margin-right", "0.5em").set("color", "var(--lumo-contrast-70pct)");
        Span s = new Span(text);
        s.addClassNames(LumoUtility.FontSize.SMALL);
        return new HorizontalLayout(i, s);
    }
    
    // --- Sistema de Eventos Personalizado ---
    public static class CardClickEvent extends ComponentEvent<AgenteCard> {
        private final Agente agente;
        public CardClickEvent(AgenteCard source, Agente agente) {
            super(source, false);
            this.agente = agente;
        }
        public Agente getAgente() {
            return agente;
        }
    }

    public Registration addCardClickListener(ComponentEventListener<CardClickEvent> listener) {
        getStyle().set("cursor", "pointer");
        addClassName("agente-card-hoverable"); // Añade una clase para estilos hover en CSS si se desea
        return addListener(CardClickEvent.class, listener);
    }
}