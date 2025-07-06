// RUTA: src/main/java/com/aeroseguridad/gestion_seguridad_aeroportuaria/ui/TeamBuilderDialog.java
package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Rol;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.AgenteService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;

public class TeamBuilderDialog extends Dialog {

    private final AgenteService agenteService;
    private final Agente coordinador;

    private final AssignmentPanel<Agente> supervisorAssignmentPanel;
    private final AssignmentPanel<Agente> agentAssignmentPanel;
    private final VerticalLayout agentDetailContainer;
    private final H4 agentAssignmentHeader;

    private Agente supervisorSeleccionado;

    public TeamBuilderDialog(Agente coordinador, AgenteService agenteService) {
        this.coordinador = coordinador;
        this.agenteService = agenteService;

        setHeaderTitle("Gestionar Equipo de: " + coordinador.getNombreCompleto());
        setWidth("80vw");
        setHeight("90vh");

        // Panel Maestro para Supervisores
        supervisorAssignmentPanel = new AssignmentPanel<>(
            "Supervisores Disponibles",
            "Equipo del Coordinador",
            agente -> new Span(agente.getNombreCompleto()), // Renderer simple para disponibles
            agente -> new AgenteCard(agente)               // Renderer de Tarjeta para asignados
        );

        // Panel Detalle para Agentes
        agentAssignmentHeader = new H4("Seleccione un supervisor para gestionar sus agentes");
        agentAssignmentPanel = new AssignmentPanel<>("Agentes Disponibles", "Agentes Asignados", Agente::getNombreCompleto);
        
        agentDetailContainer = new VerticalLayout(agentAssignmentHeader, agentAssignmentPanel);
        agentDetailContainer.setPadding(false);
        agentDetailContainer.setSpacing(false);
        agentDetailContainer.setVisible(false);

        // Layout principal
        SplitLayout splitLayout = new SplitLayout(supervisorAssignmentPanel, agentDetailContainer);
        splitLayout.setSizeFull();
        splitLayout.setSplitterPosition(50);

        add(splitLayout);
        getFooter().add(new Button("Cerrar", e -> this.close()));
        
        setupEventListeners();
        updateSupervisorAssignmentPanel();
    }
    
    private void setupEventListeners() {
        supervisorAssignmentPanel.addAssignmentListener(event -> {
            agenteService.asignarSuperior(event.getItem(), event.isAssignment() ? coordinador : null);
            updateSupervisorAssignmentPanel();
            if (event.getItem().equals(supervisorSeleccionado) && !event.isAssignment()) {
                supervisorSeleccionado = null;
                updateAgentAssignmentPanel();
            }
        });

        supervisorAssignmentPanel.addAssignedSelectionListener(event -> {
            supervisorSeleccionado = event.getFirstSelectedItem().orElse(null);
            updateAgentAssignmentPanel();
        });

        agentAssignmentPanel.addAssignmentListener(event -> {
            agenteService.asignarSuperior(event.getItem(), event.isAssignment() ? supervisorSeleccionado : null);
            updateAgentAssignmentPanel();
        });
    }

    private void updateSupervisorAssignmentPanel() {
        supervisorAssignmentPanel.setItems(
            agenteService.findAgentesDisponiblesPorRol(Rol.SUPERVISOR),
            agenteService.findSubordinados(coordinador)
        );
    }
    
    private void updateAgentAssignmentPanel() {
        if (supervisorSeleccionado != null) {
            agentDetailContainer.setVisible(true);
            agentAssignmentHeader.setText("Agentes de: " + supervisorSeleccionado.getNombreCompleto());
            agentAssignmentPanel.setItems(
                agenteService.findAgentesDisponiblesPorRol(Rol.AGENTE),
                agenteService.findSubordinados(supervisorSeleccionado)
            );
        } else {
            agentDetailContainer.setVisible(false);
        }
    }
}