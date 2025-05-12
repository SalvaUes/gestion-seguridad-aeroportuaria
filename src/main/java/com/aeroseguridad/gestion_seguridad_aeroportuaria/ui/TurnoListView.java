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
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ConstraintViolation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    private TurnoForm form;
    private SplitLayout splitLayout;
    private HorizontalLayout toolbar;
    private ListDataProvider<Turno> dataProvider;

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
            dataProvider = new ListDataProvider<>(new ArrayList<>());
            createGrid();
            createToolbar(); // Crea los DatePickers y el botón
            createForm();

            if (this.form == null) {
                 throw new IllegalStateException("Error crítico: TurnoForm no pudo ser instanciado.");
            }

            splitLayout = new SplitLayout(grid, form);
            splitLayout.setOrientation(SplitLayout.Orientation.HORIZONTAL);
            splitLayout.setSplitterPosition(75);
            splitLayout.setSizeFull();

            add(toolbar, splitLayout);

            // YA NO LLAMAMOS A setDefaultDateFilters() para poner valores.
            // Los DatePickers estarán vacíos por defecto.
            // Llamar a updateList() directamente para la carga inicial.
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

        fechaInicioFiltro.setClearButtonVisible(true);
        fechaFinFiltro.setClearButtonVisible(true);

        // Los listeners llamarán a updateList cuando cambien las fechas
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
        grid.setDataProvider(dataProvider);

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

     private void updateList() {
         if (grid == null || dataProvider == null || fechaInicioFiltro == null || fechaFinFiltro == null) {
              return;
         }

         LocalDate fechaInicio = fechaInicioFiltro.getValue();
         LocalDate fechaFin = fechaFinFiltro.getValue();
         List<Turno> turnos;

         // Si AMBAS fechas están presentes y son válidas, filtra por rango
         if (fechaInicio != null && fechaFin != null) {
             if(fechaFin.isBefore(fechaInicio)) {
                 Notification.show("La 'Fecha Hasta' debe ser posterior o igual a la 'Fecha Desde'.", 3000, Notification.Position.BOTTOM_START)
                           .addThemeVariants(NotificationVariant.LUMO_WARNING);
                 turnos = Collections.emptyList();
             } else {
                 try {
                     LocalDateTime inicioRango = fechaInicio.atStartOfDay();
                     LocalDateTime finRango = fechaFin.atTime(LocalTime.MAX);
                     turnos = turnoService.findTurnosByDateRange(inicioRango, finRango);
                 } catch (Exception e) {
                     Notification.show("Error al cargar turnos filtrados: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                             .addThemeVariants(NotificationVariant.LUMO_ERROR);
                     turnos = Collections.emptyList();
                     e.printStackTrace();
                 }
             }
         } else {
             // Si una o ambas fechas están vacías, carga TODOS los turnos
             try {
                 turnos = turnoService.findAllTurnosFetchingAgente();
             } catch (Exception e) {
                 Notification.show("Error al cargar todos los turnos: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                         .addThemeVariants(NotificationVariant.LUMO_ERROR);
                 turnos = Collections.emptyList();
                 e.printStackTrace();
             }
         }

         dataProvider.getItems().clear();
         dataProvider.getItems().addAll(turnos);
         dataProvider.refreshAll();
    }

    // --- MÉTODO setDefaultDateFilters YA NO ESTABLECE VALORES ---
     private void setDefaultDateFilters() {
         // Este método se llamaba en initLayout pero ahora no es necesario
         // que ponga valores por defecto si queremos que inicien vacíos.
         // Los DatePicker inician vacíos por sí mismos.
         // Si en el futuro quieres un rango por defecto, lo pondrías aquí y llamarías updateList.
         // LocalDate hoy = LocalDate.now();
         // fechaInicioFiltro.setValue(hoy.with(java.time.DayOfWeek.MONDAY));
         // fechaFinFiltro.setValue(hoy.with(java.time.DayOfWeek.SUNDAY));
    }
    // --- FIN MÉTODO ---

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
            Turno turnoGuardado = turnoService.save(event.getTurno());
            // Después de guardar, siempre llamamos a updateList.
            // Si los filtros de fecha están vacíos, mostrará todos (incluyendo el nuevo).
            // Si los filtros tienen un rango, el nuevo turno aparecerá si cae en ese rango.
            updateList();
            closeEditor();
            Notification.show("Turno guardado.", 2000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
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
    }

     private void deleteTurno(TurnoForm.DeleteEvent event) {
        if (form == null) return;
        Turno turnoAEliminar = event.getTurno();
        if (turnoAEliminar != null && turnoAEliminar.getIdTurno() != null) {
              try {
                turnoService.deleteById(turnoAEliminar.getIdTurno());
                updateList();
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