// RUTA: com/aeroseguridad/gestion_seguridad_aeroportuaria/ui/AgenteCard.java
package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Rol;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.shared.Registration;

// CAMBIO: Extender Div en lugar de VerticalLayout para un control más directo
public class AgenteCard extends Div {

    private static final String IMAGE_BASE_URL = "agent-photos/";
    private final Agente agente;

    public AgenteCard(Agente agente) {
        this.agente = agente;
        
        // APLICA LA CLASE CSS PRINCIPAL PARA LA TARJETA
        addClassName("agent-card");

        // Crea y añade los elementos internos usando las nuevas clases CSS
        Image avatar = createAvatar(agente);
        H3 name = new H3(agente.getNombreCompleto());
        name.addClassName("agent-name");

        Span role = new Span(agente.getRol() != null ? agente.getRol().getDescripcion() : "Sin rol");
        role.addClassName("agent-role");

        Div details = new Div(
            new Span("Carnet: " + (agente.getNumeroCarnet() != null ? agente.getNumeroCarnet() : "N/A"))
        );
        details.addClassName("agent-details");

        add(avatar, name, role, details);

        // Agrega el listener para el evento de click
        addClickListener(event -> fireEvent(new CardClickEvent(this, this.agente)));
    }

    private Image createAvatar(Agente agente) {
        String imageUrl = IMAGE_BASE_URL + (agente.getRutaFotografia() != null && !agente.getRutaFotografia().isEmpty()
            ? agente.getRutaFotografia()
            : "default_avatar.png"); // Una imagen por defecto es buena práctica

        Image avatar = new Image(imageUrl, "Avatar de " + agente.getNombreCompleto());
        avatar.addClassName("avatar"); // Usa la clase CSS para el estilo del avatar
        return avatar;
    }
    
    // --- Sistema de Eventos Personalizado (sin cambios) ---
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
        return addListener(CardClickEvent.class, listener);
    }
}