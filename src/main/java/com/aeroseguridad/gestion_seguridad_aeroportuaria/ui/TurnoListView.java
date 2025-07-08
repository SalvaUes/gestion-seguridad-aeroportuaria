// RUTA: src/main/java/com/aeroseguridad/gestion_seguridad_aeroportuaria/ui/TurnoListView.java
package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Turno;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.AgenteService;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.TurnoService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "turnos", layout = MainLayout.class)
@PageTitle("Turnos | Gestión Seguridad")
@PermitAll
public class TurnoListView extends VerticalLayout {

    private final TurnoService turnoService;
    private final AgenteService agenteService;

    private Grid<Turno> grid = new Grid<>(Turno.class, false);
    private TurnoForm form;
    private ListDataProvider<Turno> dataProvider;

    // Componentes de Filtro
    private DatePicker fechaInicioFiltro = new DatePicker("Fecha Desde");
    private DatePicker fechaFinFiltro = new DatePicker("Fecha Hasta");

    private static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Autowired
    public TurnoListView(TurnoService turnoService, AgenteService agenteService) {
        this.agenteService = agenteService;
        this.turnoService = turnoService;
        addClassName("turno-list-view");
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
        updateList(); // Carga inicial sin filtros
    }

    private HorizontalLayout createHeaderBar() {
        H2 title = new H2("Gestión de Turnos");
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
        fab.setAriaLabel("Añadir nuevo turno");
        fab.addClickListener(e -> openTurnoFormDialog(new Turno()));
        return fab;
    }

    private void openFiltersDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Filtrar Turnos por Fecha");
        
        fechaInicioFiltro.setClearButtonVisible(true);
        fechaFinFiltro.setClearButtonVisible(true);

        dialog.add(new VerticalLayout(fechaInicioFiltro, fechaFinFiltro));

        Button applyButton = new Button("Aplicar", e -> {
            updateList();
            dialog.close();
        });
        applyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button clearButton = new Button("Limpiar", e -> {
            fechaInicioFiltro.clear();
            fechaFinFiltro.clear();
            updateList();
            dialog.close();
        });
        dialog.getFooter().add(clearButton, applyButton);
        dialog.open();
    }

    private void openTurnoFormDialog(Turno turno) {
        if (form == null) {
            Notification.show("El formulario no está disponible.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        dialog.setDraggable(true);
        dialog.setResizable(true);

        H2 title = new H2(turno.getIdTurno() == null ? "Nuevo Turno" : "Editar Turno");
        Button closeButton = new Button(VaadinIcon.CLOSE_SMALL.create(), e -> dialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        HorizontalLayout dialogHeader = new HorizontalLayout(title, closeButton);
        dialogHeader.setFlexGrow(1, title);
        dialogHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        dialogHeader.getStyle().set("padding", "var(--lumo-space-m)");
        dialog.getHeader().add(dialogHeader);

        form.setTurno(turno);
        dialog.add(form);

        form.addListener(TurnoForm.SaveEvent.class, event -> {
            boolean success = saveTurno(event);
            if (success) {
                dialog.close();
            }
        });
        form.addListener(TurnoForm.DeleteEvent.class, event -> {
            deleteTurno(event);
            dialog.close();
        });
        form.addListener(TurnoForm.CloseEvent.class, event -> dialog.close());

        dialog.open();
    }

    private void createGrid() {
        grid = new Grid<>(Turno.class, false);
        grid.addClassName("turno-grid");
        grid.setSizeFull();
        
        grid.addColumn(turno -> {
            Agente agente = turno.getAgente();
            return agente != null ? agente.getApellido() + ", " + agente.getNombre() : "N/A";
        }).setHeader("Agente").setSortable(true).setKey("agente").setFrozen(true);
        grid.addColumn(turno -> formatDateTime(turno.getInicioTurno())).setHeader("Inicio Turno").setSortable(true);
        grid.addColumn(turno -> formatDateTime(turno.getFinTurno())).setHeader("Fin Turno").setSortable(true);
        grid.addColumn(Turno::getTipoTurno).setHeader("Tipo").setSortable(true);
        grid.addColumn(Turno::getEstadoTurno).setHeader("Estado").setSortable(true);

        grid.getColumns().forEach(col -> col.setAutoWidth(true).setResizable(true));
        grid.asSingleSelect().addValueChangeListener(event -> openTurnoFormDialog(event.getValue()));
    }

    private void createForm() {
        try {
            List<Agente> agentesActivos = agenteService.findAllActiveForView("");
            this.form = new TurnoForm(agentesActivos);
            this.form.setWidth("100%");
        } catch (Exception e) {
            this.form = null;
            System.err.println("!!! ERROR creating TurnoForm: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateList() {
        LocalDate fechaInicio = fechaInicioFiltro.getValue();
        LocalDate fechaFin = fechaFinFiltro.getValue();
        List<Turno> turnos;

        if (fechaInicio != null && fechaFin != null) {
            if(fechaFin.isBefore(fechaInicio)) {
                Notification.show("La 'Fecha Hasta' debe ser posterior o igual a la 'Fecha Desde'.", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
                turnos = Collections.emptyList();
            } else {
                LocalDateTime inicioRango = fechaInicio.atStartOfDay();
                LocalDateTime finRango = fechaFin.atTime(LocalTime.MAX);
                turnos = turnoService.findTurnosByDateRange(inicioRango, finRango);
            }
        } else {
            // Carga todos si no hay rango de fechas
            turnos = turnoService.findAllTurnosFetchingAgente();
        }
        grid.setItems(turnos);
    }
    
    private boolean saveTurno(TurnoForm.SaveEvent event) {
        try {
            turnoService.save(event.getTurno());
            updateList();
            Notification.show("Turno guardado.", 2000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            return true;
        } catch (IllegalArgumentException e) {
            Notification.show("Error al guardar: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (ConstraintViolationException e) {
            String violations = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .distinct()
                .collect(Collectors.joining("; "));
            String errorMsg = violations.contains("posterior a la fecha/hora de inicio")
                ? "Error: La fecha/hora de fin debe ser posterior a la de inicio."
                : "Error de validación: " + violations;
            Notification.show(errorMsg, 5000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (DataIntegrityViolationException e) {
            Notification.show("Error de integridad de datos.", 5000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            e.printStackTrace();
        } catch (Exception e) {
            Notification.show("Error inesperado al guardar turno: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            e.printStackTrace();
        }
        return false;
    }

    private void deleteTurno(TurnoForm.DeleteEvent event) {
        Turno turnoAEliminar = event.getTurno();
        if (turnoAEliminar != null && turnoAEliminar.getIdTurno() != null) {
            try {
                turnoService.deleteById(turnoAEliminar.getIdTurno());
                updateList();
                Notification.show("Turno eliminado.", 2000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
            } catch (Exception e) {
                Notification.show("Error al eliminar turno: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                e.printStackTrace();
            }
        } else {
            Notification.show("No se puede eliminar un turno no guardado.", 3000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_WARNING);
        }
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DT_FORMATTER);
    }
}