// RUTA: src/main/java/com/aeroseguridad/gestion_seguridad_aeroportuaria/ui/PermisoListView.java
package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.EstadoSolicitudPermiso;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Permiso;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.AgenteService;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.PermisoService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.ConstraintViolationException;
 

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Route(value = "permisos", layout = MainLayout.class)
@PageTitle("Permisos | Gestión Seguridad")
@PermitAll
public class PermisoListView extends VerticalLayout {

    private final PermisoService permisoService;
    private final AgenteService agenteService;

    private Grid<Permiso> grid = new Grid<>(Permiso.class, false);
    private PermisoForm form;

    // Componentes de Filtro
    private DatePicker fechaInicioFiltro = new DatePicker("Fecha Desde");
    private DatePicker fechaFinFiltro = new DatePicker("Fecha Hasta");

    private static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public PermisoListView(PermisoService permisoService, AgenteService agenteService) {
        this.permisoService = permisoService;
        this.agenteService = agenteService;
        addClassName("permiso-list-view");
        setSizeFull();
        setPadding(false);
    }

    @PostConstruct
    private void initLayout() {
        configureForm();
        configureGrid();

        HorizontalLayout headerBar = createHeaderBar();
        Button fab = createFab();
        
        Div contentWrapper = new Div(grid);
        contentWrapper.setSizeFull();
        contentWrapper.getStyle().set("overflow", "auto");
        contentWrapper.getStyle().set("padding", "0 var(--lumo-space-m)");

        add(headerBar, contentWrapper, fab);
        setDefaultDateFilters();
        updateList();
    }

    private HorizontalLayout createHeaderBar() {
        H2 title = new H2("Gestión de Permisos");
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
        fab.setAriaLabel("Solicitar nuevo permiso");
        fab.addClickListener(e -> openPermisoFormDialog(new Permiso()));
        return fab;
    }

    private void openFiltersDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Filtrar Permisos por Fecha");

        fechaInicioFiltro.setClearButtonVisible(true);
        fechaFinFiltro.setClearButtonVisible(true);

        dialog.add(new VerticalLayout(fechaInicioFiltro, fechaFinFiltro));

        Button applyButton = new Button("Aplicar", e -> {
            updateList();
            dialog.close();
        });
        applyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        Button clearButton = new Button("Limpiar", e -> {
            setDefaultDateFilters();
            updateList();
            dialog.close();
        });
        dialog.getFooter().add(clearButton, applyButton);
        dialog.open();
    }

    private void openPermisoFormDialog(Permiso permiso) {
        if (form == null) return;

        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        dialog.setDraggable(true);
        dialog.setResizable(true);

        H2 title = new H2(permiso.getIdPermiso() == null ? "Solicitar Permiso" : "Editar Permiso");
        Button closeButton = new Button(VaadinIcon.CLOSE_SMALL.create(), e -> dialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        HorizontalLayout dialogHeader = new HorizontalLayout(title, closeButton);
        dialogHeader.setFlexGrow(1, title);
        dialogHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        dialog.getHeader().add(dialogHeader);

        form.setPermiso(permiso);
        dialog.add(form);

        form.addListener(PermisoForm.SaveEvent.class, event -> {
            if (savePermiso(event)) {
                dialog.close();
            }
        });
        form.addListener(PermisoForm.DeleteEvent.class, event -> {
            confirmAndDeletePermiso(event);
            dialog.close();
        });
        form.addListener(PermisoForm.CloseEvent.class, event -> dialog.close());

        dialog.open();
    }

    private void configureGrid() {
        grid.addClassName("permiso-grid");
        grid.setSizeFull();

        grid.addColumn(permiso -> {
            Agente agente = permiso.getAgente();
            return agente != null ? agente.getApellido() + ", " + agente.getNombre() : "N/A";
        }).setHeader("Agente").setSortable(true).setKey("agente");

        grid.addColumn(Permiso::getTipoPermiso).setHeader("Tipo").setSortable(true);
        grid.addColumn(permiso -> formatDateTime(permiso.getFechaInicio())).setHeader("Inicio").setSortable(true);
        grid.addColumn(permiso -> formatDateTime(permiso.getFechaFin())).setHeader("Fin").setSortable(true);
        grid.addColumn(Permiso::getEstadoSolicitud).setHeader("Estado").setSortable(true);
        grid.addColumn(Permiso::getMotivo).setHeader("Motivo");

        grid.getColumns().forEach(col -> col.setAutoWidth(true).setResizable(true));
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                openPermisoFormDialog(event.getValue());
            }
        });
    }

    private void configureForm() {
        try {
            form = new PermisoForm(agenteService.findAllActiveForView(""));
            form.setWidth("100%");
        } catch (Exception e) {
            Notification.show("Error crítico al configurar formulario: " + e.getMessage(), 0, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            form = null;
        }
    }

    private void updateList() {
        if (grid != null && fechaInicioFiltro.getValue() != null && fechaFinFiltro.getValue() != null) {
            try {
                LocalDateTime inicioRango = fechaInicioFiltro.getValue().atStartOfDay();
                LocalDateTime finRango = fechaFinFiltro.getValue().atTime(LocalTime.MAX);
                grid.setItems(permisoService.findByDateRange(inicioRango, finRango));
            } catch (Exception e) {
                Notification.show("Error al cargar permisos: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } else {
            // Si no hay fechas, podríamos cargar los permisos del último mes por defecto o todos.
            // Por ahora, lo dejamos como estaba para mantener la lógica original.
            grid.setItems(permisoService.findAll());
        }
    }

    private void setDefaultDateFilters() {
        LocalDate hoy = LocalDate.now();
        fechaInicioFiltro.setValue(hoy.withDayOfMonth(1));
        fechaFinFiltro.setValue(hoy.withDayOfMonth(hoy.lengthOfMonth()));
    }

    private boolean savePermiso(PermisoForm.SaveEvent event) {
        try {
            permisoService.save(event.getPermiso());
            updateList();
            Notification.show("Permiso guardado/solicitado.", 2000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            return true;
        } catch (ConstraintViolationException e) {
            String violations = e.getConstraintViolations().stream()
                .map(cv -> cv.getMessage())
                .distinct()
                .collect(Collectors.joining("; "));
            Notification.show("Error de validación: " + violations, 5000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            Notification.show("Error inesperado al guardar permiso: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
        return false;
    }
    
    private void confirmAndDeletePermiso(PermisoForm.DeleteEvent event) {
        Permiso permiso = event.getPermiso();
        if (permiso == null || permiso.getIdPermiso() == null) {
            Notification.show("No hay un permiso seleccionado para eliminar.", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_WARNING);
            return;
        }
        
        if (permiso.getEstadoSolicitud() != EstadoSolicitudPermiso.SOLICITADO) {
            Notification.show("No se puede eliminar un permiso Aprobado o Rechazado.", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_WARNING);
            return;
        }
        
        Dialog confirmationDialog = new Dialog();
        confirmationDialog.setHeaderTitle("Confirmar Eliminación");
        confirmationDialog.add(new VerticalLayout(new Span("¿Estás seguro de que quieres eliminar esta solicitud de permiso?")));
        
        Button confirmButton = new Button("Eliminar", VaadinIcon.TRASH.create(), e -> {
            deletePermiso(permiso);
            confirmationDialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        Button cancelButton = new Button("Cancelar", e -> confirmationDialog.close());
        confirmationDialog.getFooter().add(cancelButton, confirmButton);
        confirmationDialog.open();
    }

    private void deletePermiso(Permiso permiso) {
        try {
            permisoService.deleteById(permiso.getIdPermiso());
            updateList();
            Notification.show("Solicitud de Permiso eliminada.", 2000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_CONTRAST);
        } catch (Exception e) {
            Notification.show("Error al eliminar permiso: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DT_FORMATTER);
    }
}