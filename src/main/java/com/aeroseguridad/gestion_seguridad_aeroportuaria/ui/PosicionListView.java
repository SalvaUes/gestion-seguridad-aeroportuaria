// RUTA: src/main/java/com/aeroseguridad/gestion_seguridad_aeroportuaria/ui/PosicionListView.java
package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PosicionSeguridad;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.PosicionSeguridadService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import jakarta.persistence.EntityNotFoundException;
 
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Collections;

@Route(value = "posiciones", layout = MainLayout.class)
@PageTitle("Posiciones | Gestión Seguridad")
@PermitAll
public class PosicionListView extends VerticalLayout {

    private final PosicionSeguridadService posicionService;

    private Grid<PosicionSeguridad> grid = new Grid<>(PosicionSeguridad.class, false);
    private PosicionForm form;

    private TextField filterText = new TextField("Buscar por nombre");

    public PosicionListView(PosicionSeguridadService posicionService) {
        this.posicionService = posicionService;
        addClassName("posicion-list-view");
        setSizeFull();
        setPadding(false);
    }

    @PostConstruct
    private void initLayout() {
        createGrid();
        createForm();

        HorizontalLayout headerBar = createHeaderBar();
        Button fab = createFab();

        Div contentWrapper = new Div(grid);
        contentWrapper.setSizeFull();
        contentWrapper.getStyle().set("overflow", "auto");
        contentWrapper.getStyle().set("padding", "0 var(--lumo-space-m)");

        add(headerBar, contentWrapper, fab);
        updateList();
    }

    private HorizontalLayout createHeaderBar() {
        H2 title = new H2("Gestión de Posiciones");
        title.getStyle().set("font-size", "var(--lumo-font-size-xxl)").set("margin", "0");

        Button filterButton = new Button("Filtros", VaadinIcon.FILTER.create());
        filterButton.addClickListener(e -> openFiltersDialog());

        HorizontalLayout headerBar = new HorizontalLayout(title, filterButton);
        headerBar.setAlignItems(FlexComponent.Alignment.CENTER);
        headerBar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        headerBar.setWidthFull();
        headerBar.getStyle().set("padding", "var(--lumo-space-m)");
        headerBar.getStyle().set("border-bottom", "1px solid var(--lumo-contrast-10pct)");

        return headerBar;
    }

    private Button createFab() {
        Button fab = new Button(VaadinIcon.PLUS.create());
        fab.addClassName("fab");
        fab.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        fab.setAriaLabel("Añadir nueva posición");
        fab.addClickListener(e -> openPosicionFormDialog(new PosicionSeguridad()));
        return fab;
    }

    private void openFiltersDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Filtrar Posiciones");

        filterText.setPlaceholder("Buscar...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);

        dialog.add(new VerticalLayout(filterText));

        Button applyButton = new Button("Aplicar", e -> {
            updateList();
            dialog.close();
        });
        applyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        Button clearButton = new Button("Limpiar", e -> {
            filterText.clear();
            updateList();
            dialog.close();
        });
        dialog.getFooter().add(clearButton, applyButton);
        dialog.open();
    }

    private void openPosicionFormDialog(PosicionSeguridad posicion) {
        if (form == null) return;
        
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        dialog.setDraggable(true);
        dialog.setResizable(true);

        H2 title = new H2(posicion.getIdPosicion() == null ? "Nueva Posición" : "Editar Posición");
        Button closeButton = new Button(VaadinIcon.CLOSE_SMALL.create(), e -> dialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        HorizontalLayout dialogHeader = new HorizontalLayout(title, closeButton);
        dialogHeader.setFlexGrow(1, title);
        dialogHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        dialog.getHeader().add(dialogHeader);

        form.setPosicion(posicion);
        dialog.add(form);

        form.addListener(PosicionForm.SaveEvent.class, event -> {
            boolean success = savePosicion(event);
            if (success) {
                dialog.close();
            }
        });
        form.addListener(PosicionForm.DeleteEvent.class, event -> {
            confirmAndDeactivatePosicion(event);
            dialog.close(); // Se cierra el diálogo de edición, se abre el de confirmación
        });
        form.addListener(PosicionForm.CloseEvent.class, event -> dialog.close());

        dialog.open();
    }

    private void createGrid() {
        grid = new Grid<>(PosicionSeguridad.class, false);
        grid.addClassName("posicion-grid");
        grid.setSizeFull();

        grid.addColumn(PosicionSeguridad::getNombrePosicion).setHeader("Nombre Posición").setSortable(true).setFrozen(true);
        grid.addColumn(PosicionSeguridad::getDescripcion).setHeader("Descripción");
        grid.addColumn(PosicionSeguridad::getGeneroRequerido).setHeader("Género Req.").setSortable(true);
        grid.addColumn(pos -> pos.isRequiereEntrenamientoEspecial() ? "Sí" : "No").setHeader("Entren. Esp.").setSortable(true);

        grid.getColumns().forEach(col -> col.setAutoWidth(true).setResizable(true));
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                openPosicionFormDialog(event.getValue());
            }
        });
    }

    private void createForm() {
        try {
            form = new PosicionForm();
            form.setWidth("100%");
        } catch (Exception e) {
            form = null;
            System.err.println("Error creando PosicionForm: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateList() {
        if (grid != null) {
            try {
                grid.setItems(posicionService.findActiveByNombre(filterText.getValue()));
            } catch (Exception e) {
                Notification.show("Error al cargar posiciones: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                grid.setItems(Collections.emptyList());
            }
        }
    }

    private boolean savePosicion(PosicionForm.SaveEvent event) {
        try {
            posicionService.save(event.getPosicion());
            updateList();
            Notification.show("Posición guardada.", 2000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            return true;
        } catch (DataIntegrityViolationException e) {
            String msg = "Error: No se pudo guardar. ";
            if (e.getMostSpecificCause().getMessage().toLowerCase().contains("posiciones_seguridad_nombre_posicion_key") ||
                e.getMostSpecificCause().getMessage().toLowerCase().contains("uk_nombre_posicion")) {
                msg += "El nombre de la posición ya existe.";
            } else {
                msg += "Violación de integridad de datos.";
            }
            Notification.show(msg, 5000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            Notification.show("Error inesperado al guardar: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            e.printStackTrace();
        }
        return false;
    }
    
    private void confirmAndDeactivatePosicion(PosicionForm.DeleteEvent event) {
        PosicionSeguridad posicion = event.getPosicion();
        if (posicion == null || posicion.getIdPosicion() == null) {
            Notification.show("No hay una posición seleccionada para desactivar.", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_WARNING);
            return;
        }

        Dialog confirmationDialog = new Dialog();
        confirmationDialog.setHeaderTitle("Confirmar Desactivación");
        confirmationDialog.add(new VerticalLayout(
            new Span("¿Estás seguro de que quieres desactivar la posición '" + posicion.getNombrePosicion() + "'?"),
            new Span("La posición no se borrará, solo se ocultará de las listas activas.")
        ));
        
        Button confirmButton = new Button("Desactivar", VaadinIcon.WARNING.create(), e -> {
            deactivatePosicion(posicion);
            confirmationDialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        Button cancelButton = new Button("Cancelar", e -> confirmationDialog.close());
        confirmationDialog.getFooter().add(cancelButton, confirmButton);
        confirmationDialog.open();
    } // --- CORRECCIÓN: Se añade la llave de cierre que faltaba ---

    private void deactivatePosicion(PosicionSeguridad posicion) {
        try {
            posicionService.deactivateById(posicion.getIdPosicion());
            updateList();
            Notification.show("Posición desactivada.", 2000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_CONTRAST);
        } catch (EntityNotFoundException enfe) {
            Notification.show("Error: La posición que intenta desactivar no fue encontrada.", 4000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            Notification.show("Error al desactivar posición: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
            e.printStackTrace();
        }
    }
}