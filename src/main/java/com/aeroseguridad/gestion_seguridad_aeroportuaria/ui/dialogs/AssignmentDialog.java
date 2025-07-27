package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui.dialogs;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Assignment;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.NecesidadVuelo;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Vuelo;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.SchedulerService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AssignmentDialog extends Dialog {

    private final Vuelo vuelo;
    private final SchedulerService schedulerService;
    private final List<Assignment> workingCopyAssignments;
    private final VerticalLayout assignmentsLayout;
    private Runnable onSave;

    public AssignmentDialog(Vuelo vuelo, List<Assignment> initialAssignments, SchedulerService schedulerService) {
        this.vuelo = vuelo;
        this.schedulerService = schedulerService;
        this.workingCopyAssignments = new ArrayList<>(initialAssignments);

        setHeaderTitle("Gestionar Personal para Vuelo: " + vuelo.getNumeroVuelo());
        setResizable(true);
        setDraggable(true);
        setWidth("80vw");
        setMaxWidth("900px");
        setHeight("90vh");

        assignmentsLayout = new VerticalLayout();
        assignmentsLayout.setPadding(false);
        assignmentsLayout.setSpacing(true);

        Button closeButton = new Button("Cancelar", e -> close());
        Button saveButton = new Button("Guardar Cambios", e -> save());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(closeButton, saveButton);
        
        add(assignmentsLayout);
        refreshAssignmentsView();
    }

    private void refreshAssignmentsView() {
        assignmentsLayout.removeAll();
        for (NecesidadVuelo necesidad : vuelo.getNecesidades()) {
            assignmentsLayout.add(createPositionRow(necesidad));
        }
    }

    /**
    * CÓDIGO RESTAURADO: El cuerpo de este método y el siguiente
    * ahora está completo y es funcional para construir la UI.
    */
    private VerticalLayout createPositionRow(NecesidadVuelo necesidad) {
        VerticalLayout positionLayout = new VerticalLayout();
        positionLayout.setSpacing(false);
        positionLayout.setPadding(false);
        H3 positionTitle = new H3(necesidad.getPosicion().getNombrePosicion() + " (" + necesidad.getCantidadAgentes() + " requeridos)");
        positionTitle.getStyle().set("font-size", "var(--lumo-font-size-l)");
        HorizontalLayout agentChipsLayout = new HorizontalLayout();
        agentChipsLayout.setSpacing(true);

        List<Assignment> assignedToPosition = workingCopyAssignments.stream()
                .filter(a -> a.getAgente() != null && a.getPosicionSeguridad().equals(necesidad.getPosicion()))
                .collect(Collectors.toList());

        for (Assignment assignment : assignedToPosition) {
            agentChipsLayout.add(createAgentChip(assignment));
        }
        
        long assignedCount = assignedToPosition.size();
        long neededCount = necesidad.getCantidadAgentes();
        
        if (assignedCount < neededCount) {
            for (int i = 0; i < (neededCount - assignedCount); i++) {
                agentChipsLayout.add(createCandidateSelector(necesidad));
            }
        }
        
        positionLayout.add(positionTitle, agentChipsLayout);
        return positionLayout;
    }
    
    private HorizontalLayout createAgentChip(Assignment assignment) {
        Span agentName = new Span(assignment.getAgente().getNombreCompleto());
        Button removeButton = new Button(new Icon(VaadinIcon.CLOSE_SMALL), e -> {
            workingCopyAssignments.remove(assignment);
            refreshAssignmentsView();
        });
        removeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        HorizontalLayout chip = new HorizontalLayout(agentName, removeButton);
        chip.setAlignItems(FlexComponent.Alignment.CENTER);
        chip.getStyle().set("background-color", "var(--lumo-contrast-10pct)").set("border-radius", "var(--lumo-border-radius-m)").set("padding", "var(--lumo-space-xs)");
        return chip;
    }

    private ComboBox<Agente> createCandidateSelector(NecesidadVuelo necesidad) {
        ComboBox<Agente> comboBox = new ComboBox<>();
        comboBox.setPlaceholder("Asignar agente...");
        
        try {
            List<Agente> candidates = schedulerService.findCandidates(this.vuelo.getIdVuelo(), necesidad, workingCopyAssignments);
            comboBox.setItems(candidates);
            comboBox.setItemLabelGenerator(Agente::getNombreCompleto);
        } catch (Exception e) {
            Notification.show("Error al cargar candidatos: " + e.getMessage(), 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            comboBox.setEnabled(false);
        }

        comboBox.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                Assignment newAssignment = new Assignment();
                newAssignment.setVuelo(this.vuelo);
                newAssignment.setPosicionSeguridad(necesidad.getPosicion());
                newAssignment.setAgente(event.getValue());
                newAssignment.setEstado("ASIGNADO");
                newAssignment.setFechaAsignacion(vuelo.getFechaHoraLlegada().toLocalDate());
                workingCopyAssignments.add(newAssignment);
                refreshAssignmentsView();
            }
        });
        return comboBox;
    }

    public void addSaveListener(Runnable onSave) {
        this.onSave = onSave;
    }

    private void save() {
        try {
            schedulerService.updateAssignmentsForVuelo(vuelo, workingCopyAssignments);
            if (onSave != null) {
                onSave.run();
            }
            close();
        } catch (Exception e) {
             Notification.show("Error al guardar: " + e.getMessage(), 5000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}