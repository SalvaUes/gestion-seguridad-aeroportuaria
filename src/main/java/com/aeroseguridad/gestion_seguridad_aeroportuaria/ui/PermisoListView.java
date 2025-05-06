package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente; // Import Agente
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Permiso;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.EstadoSolicitudPermiso; // Import EstadoSolicitudPermiso
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.AgenteService; // Import AgenteService
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.PermisoService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll; // Asumiendo que el usuario logueado puede ver/gestionar todo
import jakarta.validation.ConstraintViolationException; // Para catch
import org.springframework.dao.DataIntegrityViolationException; // Para catch


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.stream.Collectors;

@Route(value = "permisos", layout = MainLayout.class)
@PageTitle("Permisos | Gestión Seguridad")
@PermitAll // Ajustar permisos si es necesario (ej. @RolesAllowed("ADMIN"))
public class PermisoListView extends VerticalLayout {

    private final PermisoService permisoService;
    private final AgenteService agenteService; // Para poblar el ComboBox del form

    Grid<Permiso> grid = new Grid<>(Permiso.class, false);
    // Filtros (pueden añadirse más - agente, estado)
    DatePicker fechaInicioFiltro = new DatePicker("Fecha Desde");
    DatePicker fechaFinFiltro = new DatePicker("Fecha Hasta");
    Button filtrarButton = new Button("Filtrar", VaadinIcon.SEARCH.create());
    Button addPermisoButton = new Button("Solicitar/Nuevo Permiso", VaadinIcon.PLUS.create());
    PermisoForm form;

    private static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public PermisoListView(PermisoService permisoService, AgenteService agenteService) {
        this.permisoService = permisoService;
        this.agenteService = agenteService;
        addClassName("permiso-list-view");
        setSizeFull();

        configureForm();
        configureGrid();
        configureToolbar();

        SplitLayout content = new SplitLayout(grid, form);
        content.setOrientation(SplitLayout.Orientation.HORIZONTAL);
        content.setSplitterPosition(75);
        content.setSizeFull();

        add(configureToolbar(), content);
        setDefaultDateFilters();
        updateList();
        closeEditor();
    }

     private HorizontalLayout configureToolbar() {
        filtrarButton.addClickListener(click -> updateList());
        addPermisoButton.addClickListener(click -> addPermiso());

        HorizontalLayout toolbar = new HorizontalLayout(fechaInicioFiltro, fechaFinFiltro, filtrarButton, addPermisoButton);
        toolbar.addClassName("toolbar");
        toolbar.setAlignItems(Alignment.BASELINE);
        return toolbar;
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
        grid.addColumn(Permiso::getMotivo).setHeader("Motivo"); // Motivo puede ser largo

        grid.getColumns().forEach(col -> col.setAutoWidth(true));
        grid.asSingleSelect().addValueChangeListener(event -> editPermiso(event.getValue()));
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DT_FORMATTER);
    }

    private void configureForm() {
        try {
            // Pasa lista de agentes activos al formulario
            form = new PermisoForm(agenteService.findAllActiveForView(""));
            form.setSizeFull();
            form.addListener(PermisoForm.SaveEvent.class, this::savePermiso);
            form.addListener(PermisoForm.DeleteEvent.class, this::deletePermiso); // Quizás deshabilitar delete
            form.addListener(PermisoForm.CloseEvent.class, e -> closeEditor());
         } catch (Exception e) {
            Notification.show("Error crítico al configurar formulario Permiso: " + e.getMessage(), 0, Notification.Position.MIDDLE)
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
                 Notification.show("Lista actualizada.", 1500, Notification.Position.BOTTOM_START);
             } catch (Exception e) {
                Notification.show("Error al cargar permisos: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
         } else {
              grid.setItems(Collections.emptyList());
              Notification.show("Seleccione fecha de inicio y fin para filtrar.", 2000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
         }
    }

    private void setDefaultDateFilters() {
        LocalDate hoy = LocalDate.now();
        fechaInicioFiltro.setValue(hoy.withDayOfMonth(1)); // Inicio de mes
        fechaFinFiltro.setValue(hoy.withDayOfMonth(hoy.lengthOfMonth())); // Fin de mes
    }

    private void addPermiso() {
        if (form == null) return;
        grid.asSingleSelect().clear();
        editPermiso(new Permiso()); // Crea un permiso nuevo (estado será SOLICITADO por defecto)
    }

     private void editPermiso(Permiso permiso) {
        if (form == null) return;
        if (permiso == null) {
            closeEditor();
        } else {
            // Podríamos necesitar cargar el permiso con su agente si es LAZY
            form.setPermiso(permiso);
            form.setVisible(true);
            addClassName("editing");
            // Quizás deshabilitar campos si el permiso ya está aprobado/rechazado?
            // form.setReadOnly(permiso.getEstadoSolicitud() != EstadoSolicitudPermiso.SOLICITADO);
        }
    }

    private void savePermiso(PermisoForm.SaveEvent event) {
        try {
            permisoService.save(event.getPermiso());
            updateList();
            closeEditor();
            Notification.show("Permiso guardado/solicitado.", 2000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (ConstraintViolationException e) { // Captura errores de validación de entidad
              String violations = e.getConstraintViolations().stream()
                                   .map(cv -> cv.getMessage())
                                   .distinct()
                                   .collect(Collectors.joining("; "));
              Notification.show("Error de validación: " + violations, 5000, Notification.Position.BOTTOM_CENTER)
                     .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) { // Otros errores
             Notification.show("Error inesperado al guardar permiso: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

     private void deletePermiso(PermisoForm.DeleteEvent event) {
        // Considerar si realmente se deben borrar o solo rechazar/cancelar
        if (form == null) return;
        if (event.getPermiso() != null && event.getPermiso().getIdPermiso() != null) {
              try {
                 // Solo permitir borrar si está en estado SOLICITADO?
                 if(event.getPermiso().getEstadoSolicitud() == EstadoSolicitudPermiso.SOLICITADO) {
                    permisoService.deleteById(event.getPermiso().getIdPermiso());
                    updateList();
                    closeEditor();
                    Notification.show("Solicitud de Permiso eliminada.", 2000, Notification.Position.BOTTOM_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
                 } else {
                     Notification.show("No se puede eliminar un permiso Aprobado o Rechazado.", 3000, Notification.Position.BOTTOM_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_WARNING);
                 }
             } catch (Exception e) {
                Notification.show("Error al eliminar permiso: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } else {
             Notification.show("No se puede eliminar un permiso no guardado.", 3000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
        }
    }


    private void closeEditor() {
         if (form != null) {
            form.setPermiso(null);
            form.setVisible(false);
            removeClassName("editing");
         }
    }
}