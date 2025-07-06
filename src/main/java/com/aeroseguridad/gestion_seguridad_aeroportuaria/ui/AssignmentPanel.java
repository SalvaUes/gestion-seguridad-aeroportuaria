// RUTA: src/main/java/com/aeroseguridad/gestion_seguridad_aeroportuaria/ui/AssignmentPanel.java
package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.selection.SelectionListener;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.shared.Registration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Versión con corrección de layout.
 */
public class AssignmentPanel<T> extends HorizontalLayout {

    private final Grid<T> availableGrid = new Grid<>();
    private final Grid<T> assignedGrid = new Grid<>();
    private final Button assignButton = new Button("Asignar", VaadinIcon.ARROW_RIGHT.create());
    private final Button unassignButton = new Button("Quitar", VaadinIcon.ARROW_LEFT.create());
    
    private final List<T> availableItems = new ArrayList<>();
    private final List<T> assignedItems = new ArrayList<>();

    public AssignmentPanel(String titleAvailable, String titleAssigned, 
                           ValueProvider<T, ? extends Component> availableRenderer, 
                           ValueProvider<T, ? extends Component> assignedRenderer) {
        
        availableGrid.addComponentColumn(availableRenderer).setHeader(titleAvailable);
        assignedGrid.addComponentColumn(assignedRenderer).setHeader(titleAssigned);
        
        // CORRECCIÓN CLAVE: Se ajustan los grids para que no tengan padding interno y se vean mejor.
        availableGrid.getStyle().set("--_vaadin-grid-cell-content-padding", "0");
        assignedGrid.getStyle().set("--_vaadin-grid-cell-content-padding", "0");

        configureGridsAndButtons();
    }
    
    public AssignmentPanel(String titleAvailable, String titleAssigned, ValueProvider<T, String> textProvider) {
        this(titleAvailable, titleAssigned, item -> new Span(textProvider.apply(item)), item -> new Span(textProvider.apply(item)));
    }

    private void configureGridsAndButtons() {
        availableGrid.setSizeFull();
        assignedGrid.setSizeFull();
        availableGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        assignedGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        
        assignButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        assignButton.addClickListener(e -> onAssign());
        unassignButton.addClickListener(e -> onUnassign());
        
        availableGrid.asSingleSelect().addValueChangeListener(e -> assignButton.setEnabled(e.getValue() != null));
        assignedGrid.asSingleSelect().addValueChangeListener(e -> unassignButton.setEnabled(e.getValue() != null));
        
        VerticalLayout buttonLayout = new VerticalLayout(assignButton, unassignButton);
        buttonLayout.setAlignItems(Alignment.CENTER);
        buttonLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        buttonLayout.setSpacing(true);
        
        add(availableGrid, buttonLayout, assignedGrid);
        
        // CORRECCIÓN CLAVE: Esta línea le dice a los grids que se expandan para llenar el espacio.
        setFlexGrow(1, availableGrid, assignedGrid);
        
        setSizeFull();
        setAlignItems(Alignment.STRETCH);
        assignButton.setEnabled(false);
        unassignButton.setEnabled(false);
    }
    
    public void setItems(Collection<T> available, Collection<T> assigned) {
        availableItems.clear();
        availableItems.addAll(available);
        assignedItems.clear();
        assignedItems.addAll(assigned);
        refreshGrids();
    }
    
    private void refreshGrids() {
        availableGrid.setItems(new ArrayList<>(availableItems));
        assignedGrid.setItems(new ArrayList<>(assignedItems));
        assignButton.setEnabled(false);
        unassignButton.setEnabled(false);
    }

    private void onAssign() {
        availableGrid.getSelectionModel().getFirstSelectedItem().ifPresent(item -> {
            fireEvent(new AssignmentEvent<>(this, item, true));
        });
    }

    private void onUnassign() {
        assignedGrid.getSelectionModel().getFirstSelectedItem().ifPresent(item -> {
            fireEvent(new AssignmentEvent<>(this, item, false));
        });
    }
    
    public Registration addAssignedSelectionListener(SelectionListener<Grid<T>, T> listener) {
        return assignedGrid.addSelectionListener(listener);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Registration addAssignmentListener(ComponentEventListener<AssignmentEvent<T>> listener) {
        return addListener(AssignmentEvent.class, (ComponentEventListener) listener);
    }

    // --- Clases de Eventos ---
    public static class AssignmentPanelEvent<T> extends ComponentEvent<AssignmentPanel<T>> {
        private final T item;
        public AssignmentPanelEvent(AssignmentPanel<T> source, T item) {
            super(source, false);
            this.item = item;
        }
        public T getItem() { return item; }
    }

    public static class AssignmentEvent<T> extends AssignmentPanelEvent<T> {
        private final boolean isAssignment;
        public AssignmentEvent(AssignmentPanel<T> source, T item, boolean isAssignment) {
            super(source, item);
            this.isAssignment = isAssignment;
        }
        public boolean isAssignment() { return isAssignment; }
    }
}