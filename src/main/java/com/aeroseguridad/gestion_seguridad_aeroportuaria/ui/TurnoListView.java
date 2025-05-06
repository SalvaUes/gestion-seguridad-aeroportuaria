package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Turno;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.AgenteService;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.TurnoService;
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
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
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

    private Grid<Turno> grid;
    private DatePicker fechaInicioFiltro;
    private DatePicker fechaFinFiltro;
    private Button addTurnoButton;
    private TurnoForm form; // Usar la versión final SIN debug detallado
    private SplitLayout splitLayout;
    private HorizontalLayout toolbar;

    private static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Autowired
    public TurnoListView(TurnoService turnoService, AgenteService agenteService) {
        this.agenteService = agenteService;
        this.turnoService = turnoService;
        addClassName("turno-list-view");
        setSizeFull();
    }

    @PostConstruct
    private void initLayout() {
        try {
            createGrid();
            createToolbar();
            createForm(); // Usa la versión final de TurnoForm

            if (this.form == null) {
                 throw new IllegalStateException("Error crítico: TurnoForm no pudo ser instanciado.");
            }

            splitLayout = new SplitLayout(grid, form);
            splitLayout.setOrientation(SplitLayout.Orientation.HORIZONTAL);
            splitLayout.setSplitterPosition(75);
            splitLayout.setSizeFull();

            add(toolbar, splitLayout);

            setDefaultDateFilters();
            updateList();
            closeEditor();

        } catch (Exception e) {
            System.err.println("!!! FATAL ERROR during TurnoListView initLayout: " + e.getMessage());
            e.printStackTrace();
            removeAll();
            add(new HorizontalLayout(new Notification(
                "Error al inicializar la vista de Turnos.", 0, Notification.Position.MIDDLE)
            ));
        }
    }

    private void createToolbar() {
        fechaInicioFiltro = new DatePicker("Fecha Desde");
        fechaFinFiltro = new DatePicker("Fecha Hasta");
        addTurnoButton = new Button("Nuevo Turno", VaadinIcon.PLUS.create());

        fechaInicioFiltro.addValueChangeListener(e -> updateList());
        fechaFinFiltro.addValueChangeListener(e -> updateList());
        addTurnoButton.addClickListener(click -> addTurno());

        toolbar = new HorizontalLayout(fechaInicioFiltro, fechaFinFiltro, addTurnoButton);
        toolbar.addClassName("toolbar");
        toolbar.setAlignItems(Alignment.BASELINE);
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

        grid.asSingleSelect().addValueChangeListener(event -> editTurno(event.getValue()));
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DT_FORMATTER);
    }

     private void createForm() {
        try {
            if (agenteService == null) {
                 throw new IllegalStateException("AgenteService no inyectado");
            }
            List<Agente> agentesActivos = agenteService.findAllActiveForView("");
            // Asegúrate que TurnoForm sea la versión final (SIN debug detallado)
            this.form = new TurnoForm(agentesActivos);
            this.form.setWidth("400px");
            this.form.addListener(TurnoForm.SaveEvent.class, this::saveTurno);
            this.form.addListener(TurnoForm.DeleteEvent.class, this::deleteTurno);
            this.form.addListener(TurnoForm.CloseEvent.class, e -> closeEditor());
         } catch (Exception e) {
             this.form = null;
             System.err.println("!!! ERROR during TurnoListView createForm: " + e.getMessage());
             e.printStackTrace();
             Notification.show("Error al crear el formulario de turnos.", 0, Notification.Position.MIDDLE)
                         .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

     // --- MÉTODO updateList CON grid.getDataProvider().refreshAll() ---
     private void updateList() {
         if (grid == null || fechaInicioFiltro == null || fechaFinFiltro == null) {
              return; // No hacer nada si los componentes no están listos
         }

         LocalDate fechaInicio = fechaInicioFiltro.getValue();
         LocalDate fechaFin = fechaFinFiltro.getValue();

         if (fechaInicio == null || fechaFin == null) {
             grid.setItems(Collections.emptyList());
             return;
         }
         if(fechaFin.isBefore(fechaInicio)) {
             grid.setItems(Collections.emptyList());
             return;
         }

         try {
             LocalDateTime inicioRango = fechaInicio.atStartOfDay();
             LocalDateTime finRango = fechaFin.atTime(LocalTime.MAX);
             List<Turno> turnos = turnoService.findTurnosByDateRange(inicioRango, finRango);
             grid.setItems(turnos); // Establece los nuevos items

             // --- AÑADIDO: Forzar refresco del DataProvider ---
             if (grid.getDataProvider() != null) {
                 grid.getDataProvider().refreshAll();
             }
             // --- FIN AÑADIDO ---

         } catch (Exception e) {
            Notification.show("Error al cargar turnos: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            grid.setItems(Collections.emptyList());
            e.printStackTrace();
         }
    }
    // --- FIN MÉTODO updateList ---

     private void setDefaultDateFilters() {
         if (fechaInicioFiltro == null || fechaFinFiltro == null) return;
        LocalDate hoy = LocalDate.now();
        fechaInicioFiltro.setValue(hoy.with(java.time.DayOfWeek.MONDAY));
        fechaFinFiltro.setValue(hoy.with(java.time.DayOfWeek.SUNDAY));
    }

    private void addTurno() {
        if (form == null) return;
        grid.asSingleSelect().clear();
        editTurno(new Turno());
    }

    private void editTurno(Turno turno) {
        if (form == null) return;
        if (turno == null) {
            closeEditor();
        } else {
            form.setTurno(turno);
            form.setVisible(true);
        }
    }

     private void saveTurno(TurnoForm.SaveEvent event) {
        try {
            turnoService.save(event.getTurno());
            updateList(); // Llama a refrescar la lista (que ahora incluye refreshAll)
            closeEditor();
            Notification.show("Turno guardado.", 2000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (IllegalArgumentException e) {
             Notification.show("Error al guardar: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (ConstraintViolationException e) {
              String violations = e.getConstraintViolations().stream()
                                   .map(cv -> cv.getMessage())
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
    }

     private void deleteTurno(TurnoForm.DeleteEvent event) {
        if (form == null) return;
        Turno turnoAEliminar = event.getTurno();
        if (turnoAEliminar != null && turnoAEliminar.getIdTurno() != null) {
              try {
                turnoService.deleteById(turnoAEliminar.getIdTurno());
                updateList(); // Refresca la lista
                closeEditor();
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

    private void closeEditor() {
         if (form != null) {
            form.setTurno(null);
            form.setVisible(false);
         }
         if (grid != null) {
            grid.asSingleSelect().clear();
         }
    }
}