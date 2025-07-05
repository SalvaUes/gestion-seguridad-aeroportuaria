// RUTA: src/main/java/com/aeroseguridad/gestion_seguridad_aeroportuaria/ui/SupervisoresView.java
package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Rol;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.AgenteService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.Collections;

@Route(value = "organigrama", layout = MainLayout.class)
@PageTitle("Constructor de Organigrama")
@RolesAllowed("ROLE_ADMIN")
public class SupervisoresView extends VerticalLayout {

    private final AgenteService agenteService;

    private ComboBox<Agente> coordinadorSelector;
    private AssignmentPanel<Agente> supervisorPanel;
    private AssignmentPanel<Agente> agentePanel;

    private Agente coordinadorSeleccionado;
    private Agente supervisorSeleccionado;

    public SupervisoresView(AgenteService agenteService) {
        this.agenteService = agenteService;
        setSizeFull();
        setSpacing(true);

        add(new H2("Constructor de Organigrama"));

        coordinadorSelector = new ComboBox<>("Seleccione un Coordinador");
        coordinadorSelector.setItems(agenteService.findByRol(Rol.COORDINADOR));
        coordinadorSelector.setItemLabelGenerator(Agente::getNombreCompleto);
        coordinadorSelector.setWidth("50%");
        coordinadorSelector.addValueChangeListener(e -> {
            this.coordinadorSeleccionado = e.getValue();
            updateSupervisorPanel();
            clearAgentePanel();
        });

        supervisorPanel = new AssignmentPanel<>("Supervisores Disponibles", "Supervisores Asignados", Agente::getNombreCompleto);
        agentePanel = new AssignmentPanel<>("Agentes Disponibles", "Agentes Asignados", Agente::getNombreCompleto);

        // CORREGIDO: Se utiliza el nuevo método de tipo seguro.
        supervisorPanel.addAssignmentListener(this::handleSupervisorAssignment);
        agentePanel.addAssignmentListener(this::handleAgenteAssignment);
        
        supervisorPanel.addAssignedSelectionListener(e -> {
            this.supervisorSeleccionado = e.getFirstSelectedItem().orElse(null);
            updateAgentePanel();
        });

        HorizontalLayout panelsLayout = new HorizontalLayout(
                createTitledPanel("Gestión de Supervisores", supervisorPanel),
                createTitledPanel("Gestión de Agentes", agentePanel)
        );
        panelsLayout.setSizeFull();

        add(coordinadorSelector, panelsLayout);

        clearAgentePanel();
    }

    private VerticalLayout createTitledPanel(String title, Component content) {
        H4 header = new H4(title);
        VerticalLayout panel = new VerticalLayout(header, content);
        panel.setPadding(false);
        panel.setSpacing(false);
        panel.setSizeFull();
        return panel;
    }

    private void updateSupervisorPanel() {
        if (coordinadorSeleccionado != null) {
            supervisorPanel.setEnabled(true);
            supervisorPanel.setItems(
                agenteService.findAgentesDisponiblesPorRol(Rol.SUPERVISOR),
                agenteService.findSubordinados(coordinadorSeleccionado)
            );
        } else {
            supervisorPanel.setEnabled(false);
            supervisorPanel.setItems(Collections.emptyList(), Collections.emptyList());
        }
    }

    private void updateAgentePanel() {
        if (supervisorSeleccionado != null) {
            agentePanel.setEnabled(true);
            agentePanel.setItems(
                agenteService.findAgentesDisponiblesPorRol(Rol.AGENTE),
                agenteService.findSubordinados(supervisorSeleccionado)
            );
        } else {
            clearAgentePanel();
        }
    }
    
    private void clearAgentePanel() {
        agentePanel.setEnabled(false);
        agentePanel.setItems(Collections.emptyList(), Collections.emptyList());
    }

    private void handleSupervisorAssignment(AssignmentPanel.AssignmentEvent<Agente> event) {
        try {
            Agente supervisor = event.getItem();
            Agente nuevoSuperior = event.isAssignment() ? coordinadorSeleccionado : null;
            agenteService.asignarSuperior(supervisor, nuevoSuperior);
            updateSupervisorPanel();
            
            if (supervisor.equals(this.supervisorSeleccionado)) {
                clearAgentePanel();
            }
            Notification.show("Supervisor " + (event.isAssignment() ? "asignado." : "desasignado."), 2000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            Notification.show("Error al procesar la asignación del supervisor: " + e.getMessage(), 4000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
    
    private void handleAgenteAssignment(AssignmentPanel.AssignmentEvent<Agente> event) {
        try {
            Agente agente = event.getItem();
            Agente nuevoSuperior = event.isAssignment() ? supervisorSeleccionado : null;
            agenteService.asignarSuperior(agente, nuevoSuperior);
            updateAgentePanel();
            Notification.show("Agente " + (event.isAssignment() ? "asignado." : "desasignado."), 2000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            Notification.show("Error al procesar la asignación del agente: " + e.getMessage(), 4000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}