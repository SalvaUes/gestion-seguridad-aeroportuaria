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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import jakarta.persistence.EntityNotFoundException;

import java.util.Collections;

@Route(value = "posiciones", layout = MainLayout.class)
@PageTitle("Posiciones | Gestión Seguridad")
@PermitAll
public class PosicionListView extends VerticalLayout {

    private final PosicionSeguridadService posicionService;

    private Grid<PosicionSeguridad> grid;
    private TextField filterText;
    private Button addPosicionButton;
    private PosicionForm form;
    private HorizontalLayout toolbar;
    private SplitLayout splitLayout;


    @Autowired
    public PosicionListView(PosicionSeguridadService posicionService) {
        this.posicionService = posicionService;
        addClassName("posicion-list-view");
        setSizeFull();
    }

    @PostConstruct
    private void initLayout() {
        try {
            createGrid();
            createForm(); // Importante que use el PosicionForm corregido
            createToolbar();

            if (this.form == null) {
                throw new IllegalStateException("PosicionForm no pudo ser instanciado.");
            }

            splitLayout = new SplitLayout(grid, form);
            splitLayout.setOrientation(SplitLayout.Orientation.HORIZONTAL);
            splitLayout.setSplitterPosition(65);
            splitLayout.setSizeFull();

            add(toolbar, splitLayout);
            updateList();
            closeEditor();

        } catch (Exception e) {
            System.err.println("Error inicializando PosicionListView: " + e.getMessage());
            e.printStackTrace();
            Notification.show("Error al cargar la vista de Posiciones.",0 , Notification.Position.MIDDLE);
        }
    }


    private void createToolbar() {
        filterText = new TextField();
        filterText.setPlaceholder("Buscar por nombre...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateList());

        addPosicionButton = new Button("Nueva Posición", VaadinIcon.PLUS.create());
        addPosicionButton.addClickListener(click -> addPosicion());

        toolbar = new HorizontalLayout(filterText, addPosicionButton);
        toolbar.addClassName("toolbar");
        toolbar.setWidthFull();
        toolbar.setFlexGrow(1, filterText);
    }

     private void createGrid() {
        grid = new Grid<>(PosicionSeguridad.class, false);
        grid.addClassName("posicion-grid");
        grid.setSizeFull();

        grid.addColumn(PosicionSeguridad::getNombrePosicion).setHeader("Nombre Posición").setSortable(true).setFrozen(true);
        grid.addColumn(PosicionSeguridad::getDescripcion).setHeader("Descripción");
        grid.addColumn(PosicionSeguridad::getGeneroRequerido).setHeader("Género Req.").setSortable(true);
        grid.addColumn(pos -> pos.isRequiereEntrenamientoEspecial() ? "Sí" : "No").setHeader("Entren. Esp.").setSortable(true);
        // No mostramos 'activo' en el grid, ya que solo mostraremos activas.

        grid.getColumns().forEach(col -> col.setAutoWidth(true).setResizable(true));
        grid.asSingleSelect().addValueChangeListener(event -> editPosicion(event.getValue()));
    }

    private void createForm() {
        try {
             form = new PosicionForm(); // Usa el PosicionForm corregido con el campo 'activo'
             form.setWidth("400px");
             form.addListener(PosicionForm.SaveEvent.class, this::savePosicion);
             form.addListener(PosicionForm.DeleteEvent.class, this::deactivatePosicion); // Llamar a desactivar
             form.addListener(PosicionForm.CloseEvent.class, e -> closeEditor());
        } catch (Exception e) {
             form = null;
             System.err.println("Error creando PosicionForm: " + e.getMessage());
             e.printStackTrace();
         }
    }

    private void updateList() {
         if (grid != null) {
             try {
                // Llama al servicio para buscar posiciones ACTIVAS por el término del filtro
                grid.setItems(posicionService.findActiveByNombre(filterText.getValue()));
             } catch (Exception e) {
                Notification.show("Error al cargar posiciones: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                grid.setItems(Collections.emptyList());
             }
         }
    }

     private void addPosicion() {
        if (form == null) return;
        grid.asSingleSelect().clear();
        editPosicion(new PosicionSeguridad()); // 'activo' será true por defecto en la entidad
    }

    private void editPosicion(PosicionSeguridad posicion) {
        if (form == null) return;
        if (posicion == null) {
            closeEditor();
        } else {
            // Si es una posición inactiva seleccionada (aunque no deberían mostrarse),
            // se cargará con activo=false en el form.
            form.setPosicion(posicion);
            form.setVisible(true);
        }
    }

     private void savePosicion(PosicionForm.SaveEvent event) {
        try {
            posicionService.save(event.getPosicion());
            updateList();
            closeEditor();
            Notification.show("Posición guardada.", 2000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (DataIntegrityViolationException e) {
             String msg = "Error: No se pudo guardar. ";
             if (e.getMostSpecificCause().getMessage().toLowerCase().contains("posiciones_seguridad_nombre_posicion_key") ||
                 e.getMostSpecificCause().getMessage().toLowerCase().contains("uk_nombre_posicion")) {
                 msg += "El nombre de la posición ya existe.";
                 if (form != null && form.nombrePosicion != null) {
                     form.nombrePosicion.setInvalid(true);
                     form.nombrePosicion.setErrorMessage("Este nombre ya existe");
                 }
             } else {
                 msg += "Violación de integridad de datos.";
             }
             Notification.show(msg, 5000, Notification.Position.BOTTOM_CENTER)
                  .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
             Notification.show("Error inesperado al guardar posición: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
             e.printStackTrace();
        }
    }

    // --- MÉTODO para DESACTIVAR Posicion ---
    private void deactivatePosicion(PosicionForm.DeleteEvent event) {
        if (form == null) return;
        PosicionSeguridad posicionADesactivar = event.getPosicion();

        if (posicionADesactivar == null || posicionADesactivar.getIdPosicion() == null) {
             Notification.show("Seleccione una posición guardada para desactivar.", 3000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
             return;
        }
        try {
            posicionService.deactivateById(posicionADesactivar.getIdPosicion());
            updateList();
            closeEditor();
            Notification.show("Posición desactivada.", 2000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
        } catch (EntityNotFoundException enfe) {
               Notification.show("Error: La posición que intenta desactivar no fue encontrada.", 4000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            // DataIntegrityViolationException podría ocurrir aquí si hay FKs que impiden
            // incluso una actualización de 'activo' (aunque es menos común para un soft delete).
            // Es más probable con borrado físico.
            Notification.show("Error al desactivar posición: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
             e.printStackTrace();
        }
    }
    // --- FIN MÉTODO ---

    private void closeEditor() {
         if (form != null) {
            form.setPosicion(null);
            form.setVisible(false);
         }
         if (grid != null) {
            grid.asSingleSelect().clear();
         }
    }
}