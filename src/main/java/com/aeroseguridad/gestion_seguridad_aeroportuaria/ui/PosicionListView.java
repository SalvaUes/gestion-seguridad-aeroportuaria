package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PosicionSeguridad;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.PosicionSeguridadService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField; // Añadir si usas filtro
import com.vaadin.flow.data.value.ValueChangeMode; // Añadir si usas filtro
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.dao.DataIntegrityViolationException; // Para catch

import java.util.Collections; // Para lista vacía

@Route(value = "posiciones", layout = MainLayout.class)
@PageTitle("Posiciones | Gestión Seguridad")
@PermitAll
public class PosicionListView extends VerticalLayout {

    private final PosicionSeguridadService posicionService;

    Grid<PosicionSeguridad> grid = new Grid<>(PosicionSeguridad.class, false);
    // TextField filterText = new TextField(); // Filtro opcional
    Button addPosicionButton = new Button("Nueva Posición", VaadinIcon.PLUS.create());
    PosicionForm form;

    public PosicionListView(PosicionSeguridadService posicionService) {
        this.posicionService = posicionService;
        addClassName("posicion-list-view");
        setSizeFull();

        configureForm(); // Formulario sin dependencias externas por ahora
        configureGrid();
        configureToolbar();

        SplitLayout content = new SplitLayout(grid, form);
        content.setOrientation(SplitLayout.Orientation.HORIZONTAL);
        content.setSplitterPosition(65); // Ajusta según necesites
        content.setSizeFull();

        add(configureToolbar(), content);
        updateList();
        closeEditor();
    }

    private HorizontalLayout configureToolbar() {
        // filterText.setPlaceholder("Buscar por nombre...");
        // filterText.setClearButtonVisible(true);
        // filterText.setValueChangeMode(ValueChangeMode.LAZY);
        // filterText.addValueChangeListener(e -> updateList());

        addPosicionButton.addClickListener(click -> addPosicion());

        // HorizontalLayout toolbar = new HorizontalLayout(filterText, addPosicionButton);
        HorizontalLayout toolbar = new HorizontalLayout(addPosicionButton); // Toolbar sin filtro por ahora
        toolbar.addClassName("toolbar");
        return toolbar;
    }

     private void configureGrid() {
        grid.addClassName("posicion-grid");
        grid.setSizeFull();

        grid.addColumn(PosicionSeguridad::getNombrePosicion).setHeader("Nombre Posición").setSortable(true);
        grid.addColumn(PosicionSeguridad::getDescripcion).setHeader("Descripción");
        grid.addColumn(PosicionSeguridad::getGeneroRequerido).setHeader("Género Req.").setSortable(true);
        grid.addColumn(pos -> pos.isRequiereEntrenamientoEspecial() ? "Sí" : "No").setHeader("Entren. Esp.").setSortable(true);


        grid.getColumns().forEach(col -> col.setAutoWidth(true));
        grid.asSingleSelect().addValueChangeListener(event -> editPosicion(event.getValue()));
    }

    private void configureForm() {
        try {
             form = new PosicionForm(); // No necesita lista de dependencias
             form.setSizeFull();
             form.addListener(PosicionForm.SaveEvent.class, this::savePosicion);
             form.addListener(PosicionForm.DeleteEvent.class, this::deletePosicion);
             form.addListener(PosicionForm.CloseEvent.class, e -> closeEditor());
        } catch (Exception e) {
             Notification.show("Error crítico al configurar formulario Posición: " + e.getMessage(), 0, Notification.Position.MIDDLE)
                         .addThemeVariants(NotificationVariant.LUMO_ERROR);
             form = null;
         }
    }

    private void updateList() {
         if (grid != null) {
             try {
                // String filter = filterText.getValue(); // Si usaras filtro
                // grid.setItems(posicionService.findByNombre(filter));
                 grid.setItems(posicionService.findAll()); // Muestra todas por ahora
             } catch (Exception e) {
                Notification.show("Error al cargar posiciones: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
         }
    }

     private void addPosicion() {
        if (form == null) return;
        grid.asSingleSelect().clear();
        editPosicion(new PosicionSeguridad());
    }

    private void editPosicion(PosicionSeguridad posicion) {
        if (form == null) return;
        if (posicion == null) {
            closeEditor();
        } else {
            form.setPosicion(posicion);
            form.setVisible(true);
            addClassName("editing");
        }
    }

     private void savePosicion(PosicionForm.SaveEvent event) {
        try {
            posicionService.save(event.getPosicion());
            updateList();
            closeEditor();
            Notification.show("Posición guardada.", 2000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (DataIntegrityViolationException e) { // Error de unicidad (nombre) o FK
             Notification.show("Error: No se pudo guardar. El nombre de la posición ya existe o hay datos relacionados.", 5000, Notification.Position.BOTTOM_CENTER)
                  .addThemeVariants(NotificationVariant.LUMO_ERROR);
             if (form != null && form.nombrePosicion != null) {
                  form.nombrePosicion.setInvalid(true);
                  form.nombrePosicion.setErrorMessage("Este nombre ya existe");
             }
        } catch (Exception e) {
             Notification.show("Error inesperado al guardar posición: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }


    private void deletePosicion(PosicionForm.DeleteEvent event) {
        if (form == null) return;
        if (event.getPosicion() != null && event.getPosicion().getIdPosicion() != null) {
              try {
                // Advertencia: Borrar una posición puede fallar si agentes tienen esa habilidad asignada (FK)
                posicionService.deleteById(event.getPosicion().getIdPosicion());
                updateList();
                closeEditor();
                Notification.show("Posición eliminada.", 2000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
             } catch (DataIntegrityViolationException e) { // Error si está en uso
                 Notification.show("Error: No se puede eliminar la posición porque está asignada a uno o más agentes.", 5000, Notification.Position.BOTTOM_CENTER)
                  .addThemeVariants(NotificationVariant.LUMO_ERROR);
             } catch (Exception e) {
                Notification.show("Error al eliminar posición: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } else {
             Notification.show("No se puede eliminar una posición no guardada.", 3000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
        }
    }

    private void closeEditor() {
         if (form != null) {
            form.setPosicion(null);
            form.setVisible(false);
            removeClassName("editing");
         }
    }
}