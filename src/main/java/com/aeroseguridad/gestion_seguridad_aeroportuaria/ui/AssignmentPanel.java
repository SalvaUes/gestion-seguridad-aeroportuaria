// RUTA: src/main/java/com/aeroseguridad/gestion_seguridad_aeroportuaria/ui/AssignmentPanel.java
package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.selection.SelectionListener;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.shared.Registration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class AssignmentPanel<T> extends HorizontalLayout {

    private final Grid<T> availableGrid = new Grid<>();
    private final Grid<T> assignedGrid = new Grid<>();
    private final Button assignButton = new Button(VaadinIcon.ARROW_RIGHT.create());
    private final Button unassignButton = new Button(VaadinIcon.ARROW_LEFT.create());
    
    private final List<T> availableItems = new ArrayList<>();
    private final List<T> assignedItems = new ArrayList<>();

    public AssignmentPanel(String titleAvailable, String titleAssigned, ValueProvider<T, String> labelProvider) {
        availableGrid.addColumn(labelProvider).setHeader(titleAvailable);
        availableGrid.setSizeFull();
        availableGrid.setSelectionMode(Grid.SelectionMode.SINGLE);

        assignedGrid.addColumn(labelProvider).setHeader(titleAssigned);
        assignedGrid.setSizeFull();
        assignedGrid.setSelectionMode(Grid.SelectionMode.SINGLE);

        assignButton.addClickListener(this::onAssign);
        unassignButton.addClickListener(this::onUnassign);
        assignButton.setEnabled(false);
        unassignButton.setEnabled(false);

        availableGrid.asSingleSelect().addValueChangeListener(e -> assignButton.setEnabled(e.getValue() != null));
        assignedGrid.asSingleSelect().addValueChangeListener(e -> unassignButton.setEnabled(e.getValue() != null));

        VerticalLayout buttonLayout = new VerticalLayout(assignButton, unassignButton);
        buttonLayout.setAlignItems(Alignment.CENTER);
        buttonLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        buttonLayout.setSpacing(true);

        add(availableGrid, buttonLayout, assignedGrid);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);
        setSizeFull();
    }

    public void setItems(Collection<T> available, Collection<T> assigned) {
        this.availableItems.clear();
        this.availableItems.addAll(available);
        this.assignedItems.clear();
        this.assignedItems.addAll(assigned);
        refreshGrids();
    }
    
    private void refreshGrids() {
        availableGrid.setItems(new ArrayList<>(this.availableItems));
        assignedGrid.setItems(new ArrayList<>(this.assignedItems));
        assignButton.setEnabled(false);
        unassignButton.setEnabled(false);
    }

    private void onAssign(ClickEvent<Button> event) {
        Set<T> selected = availableGrid.getSelectedItems();
        if (!selected.isEmpty()) {
            T itemToAssign = selected.iterator().next();
            fireEvent(new AssignmentEvent<>(this, itemToAssign, true));
        }
    }

    private void onUnassign(ClickEvent<Button> event) {
        Set<T> selected = assignedGrid.getSelectedItems();
        if (!selected.isEmpty()) {
            T itemToUnassign = selected.iterator().next();
            fireEvent(new AssignmentEvent<>(this, itemToUnassign, false));
        }
    }
    
    public Registration addAssignedSelectionListener(SelectionListener<Grid<T>, T> listener) {
        return assignedGrid.addSelectionListener(listener);
    }

    // CORRECCIÓN CLAVE: Método específico y de tipo seguro para el evento de asignación.
    // Esto resuelve el error de inferencia de tipos del compilador.
    @SuppressWarnings("unchecked")
    public Registration addAssignmentListener(ComponentEventListener<AssignmentEvent<T>> listener) {
        return getEventBus().addListener((Class<AssignmentEvent<T>>) (Class<?>) AssignmentEvent.class, listener);
    }

    // Definición de Eventos
    public static abstract class AssignmentPanelEvent<T> extends ComponentEvent<AssignmentPanel<T>> {
        private final T item;
        public AssignmentPanelEvent(AssignmentPanel<T> source, T item) {
            super(source, false);
            this.item = item;
        }
        public T getItem() { return item; }
    }

    public static class AssignmentEvent<T> extends AssignmentPanelEvent<T> {
        private final boolean assigned;
        public AssignmentEvent(AssignmentPanel<T> source, T item, boolean assigned) {
            super(source, item);
            this.assigned = assigned;
        }
        public boolean isAssignment() { return assigned; }
    }
}