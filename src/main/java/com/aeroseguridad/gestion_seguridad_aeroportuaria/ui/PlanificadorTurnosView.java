package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.ReglaDeTurno;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.TipoTurno;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.AgenteService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.Set;
import java.util.stream.Collectors;

@Route(value = "planificador-turnos", layout = MainLayout.class)
@PageTitle("Planificador de Turnos")
@RolesAllowed("ADMIN")
public class PlanificadorTurnosView extends VerticalLayout {

    private final AgenteService agenteService;
    
    // --- REEMPLAZO DEL GRID POR UN DIV ---
    private Div agentCardContainer = new Div();
    private TextField filtroTexto = new TextField();

    public PlanificadorTurnosView(AgenteService agenteService) {
        this.agenteService = agenteService;
        setSizeFull();
        addClassName("planificador-turnos-view");
        
        configureFiltro();
        configureContainer();

        add(getToolbar(), agentCardContainer);
        updateList();
    }

    private void configureFiltro() {
        filtroTexto.setPlaceholder("Buscar por nombre, apellido o carnet...");
        filtroTexto.setClearButtonVisible(true);
        filtroTexto.setValueChangeMode(ValueChangeMode.LAZY);
        filtroTexto.addValueChangeListener(e -> updateList());
    }

    private Component getToolbar() {
        HorizontalLayout toolbar = new HorizontalLayout(filtroTexto);
        toolbar.setWidthFull();
        toolbar.setPadding(true);
        toolbar.setBoxSizing(getBoxSizing());
        return toolbar;
    }

    private void configureContainer() {
        // Le aplicamos la clase CSS que lo convierte en una cuadrícula
        agentCardContainer.addClassName("agent-card-container");
        agentCardContainer.setSizeFull();
    }

    private void updateList() {
        // Limpiamos el contenedor
        agentCardContainer.removeAll();
        
        // Obtenemos los agentes
        var agentes = agenteService.findAllActiveForView(filtroTexto.getValue());
        
        // Creamos y añadimos una tarjeta por cada agente
        for (Agente agente : agentes) {
            agentCardContainer.add(createAgentCard(agente));
        }
    }

    private Component createAgentCard(Agente agente) {
        // La tarjeta es un VerticalLayout para organizar el contenido
        VerticalLayout card = new VerticalLayout();
        card.addClassName("agent-card-item");
        card.setSpacing(false);
        card.setPadding(true);
        card.setAlignItems(Alignment.CENTER);

        Image img = new Image(
            agente.getRutaFotografia() != null ? "/agent-photos/" + agente.getRutaFotografia() : "images/user.png",
            "Foto"
        );
        img.addClassName("agent-image");

        Span nombreSpan = new Span(agente.getNombreCompleto());
        nombreSpan.addClassName("agent-name");

        Span carnetSpan = new Span("Carnet: " + agente.getNumeroCarnet());
        carnetSpan.addClassName("agent-carnet");

        HorizontalLayout badgesLayout = createBadges(agente);
        badgesLayout.addClassName("badges-layout");

        card.add(img, nombreSpan, carnetSpan, badgesLayout);

        // Añadimos el evento de clic a toda la tarjeta
        card.addClickListener(e -> navigateToAgenteDetail(agente));
        
        return card;
    }

    private HorizontalLayout createBadges(Agente agente) {
        HorizontalLayout badgesLayout = new HorizontalLayout();
        badgesLayout.setSpacing(true);

        if (agente.getPlantillas() == null || agente.getPlantillas().isEmpty()) {
            badgesLayout.add(createBadge("Sin plantilla", "contrast"));
        } else {
            Set<TipoTurno> tiposDeTurno = agente.getPlantillas().stream()
                .flatMap(p -> p.getReglas().stream())
                .map(ReglaDeTurno::getTipoTurno)
                .collect(Collectors.toSet());
            if (tiposDeTurno.contains(TipoTurno.REGULAR)) {
                badgesLayout.add(createBadge("Regular", ""));
            }
            if (tiposDeTurno.contains(TipoTurno.NOCTURNO)) {
                badgesLayout.add(createBadge("Nocturno", "success"));
            }
        }
        return badgesLayout;
    }

    private Span createBadge(String text, String theme) {
        Span badge = new Span(text);
        badge.getElement().getThemeList().add("badge small " + theme);
        return badge;
    }

    private void navigateToAgenteDetail(Agente agente) {
        UI.getCurrent().navigate("planificador-turnos/" + agente.getIdAgente());
    }
}