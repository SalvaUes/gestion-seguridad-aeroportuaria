package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;
import java.util.function.BiConsumer;

public class SupervisorOrganigramaCard extends VerticalLayout {

    private final Agente supervisor;
    private final VerticalLayout agentesContainer;

    public SupervisorOrganigramaCard(Agente supervisor, List<Agente> agentesSinAsignar, BiConsumer<Agente, Agente> onAgenteAsignado) {
        this.supervisor = supervisor;

        // --- Estilo de la Tarjeta ---
        setSpacing(false);
        setPadding(true);
        getStyle().set("border", "1px solid var(--lumo-contrast-10pct)");
        getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        setWidth("350px");

        // --- Encabezado con foto e info del Supervisor ---
        Image foto = new Image("agent-photos/" + supervisor.getRutaFotografia(), "Foto");
        foto.setWidth("50px");
        foto.setHeight("50px");
        foto.getStyle().set("border-radius", "50%");
        foto.getStyle().set("object-fit", "cover");

        VerticalLayout infoLayout = new VerticalLayout();
        infoLayout.setSpacing(false);
        infoLayout.setPadding(false);
        infoLayout.add(new H4(supervisor.getNombreCompleto()));
        infoLayout.add(new Span(supervisor.getNumeroCarnet()));

        HorizontalLayout header = new HorizontalLayout(foto, infoLayout);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        // --- ComboBox para asignar Agentes ---
        ComboBox<Agente> agenteComboBox = new ComboBox<>("Asignar Agente");
        agenteComboBox.setItems(agentesSinAsignar);
        agenteComboBox.setItemLabelGenerator(Agente::getNombreCompleto);
        agenteComboBox.setWidthFull();
        agenteComboBox.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                // Notificar a la vista principal para que maneje la lógica de guardado
                onAgenteAsignado.accept(this.supervisor, event.getValue());
                agenteComboBox.clear(); // Limpiar para la siguiente selección
            }
        });

        // --- Contenedor para la lista de agentes asignados ---
        this.agentesContainer = new VerticalLayout();
        this.agentesContainer.setSpacing(false);
        this.agentesContainer.setPadding(false);
        this.agentesContainer.getStyle().set("margin-top", "var(--lumo-space-s)");

        add(header, agenteComboBox, agentesContainer);
        actualizarListaAgentes(); // Carga inicial
    }

    // Método para refrescar la lista de agentes mostrada en la tarjeta
    public void actualizarListaAgentes() {
        agentesContainer.removeAll();
        if (supervisor.getSubordinados() != null && !supervisor.getSubordinados().isEmpty()) {
            supervisor.getSubordinados().forEach(agente -> {
                Span agenteSpan = new Span(VaadinIcon.USER.create(), new Span(" " + agente.getNombreCompleto()));
                agentesContainer.add(agenteSpan);
            });
        } else {
            agentesContainer.add(new Span("Sin agentes asignados."));
        }
    }
}