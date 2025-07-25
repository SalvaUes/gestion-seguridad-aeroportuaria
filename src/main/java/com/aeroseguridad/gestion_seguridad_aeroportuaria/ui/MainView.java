package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.dto.ScheduleRequest;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.dto.ScheduleResult;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Assignment;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.SchedulerService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard | Gestión Seguridad")
@PermitAll
public class MainView extends VerticalLayout {

    private final SchedulerService schedulerService;

    // --- Componentes de la UI ---
    private final DatePicker startDatePicker;
    private final DatePicker endDatePicker;
    private final Button generateScheduleButton;
    private final ProgressBar progressBar;
    private final Grid<Assignment> scheduleGrid;
    private final Grid<Assignment> conflictGrid;
    private final H3 scheduleHeader;
    private final H3 conflictHeader;

    @Autowired
    public MainView(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;

        // --- Configuración de Componentes ---
        H2 title = new H2("Dashboard de Operaciones y Agendamiento");
        
        startDatePicker = new DatePicker("Fecha de Inicio");
        endDatePicker = new DatePicker("Fecha de Fin");
        startDatePicker.setValue(LocalDate.now().withDayOfMonth(1));
        endDatePicker.setValue(LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()));

        generateScheduleButton = new Button("Generar Horario", VaadinIcon.CALENDAR_CLOCK.create());
        generateScheduleButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);

        scheduleHeader = new H3("Horario Generado");
        scheduleGrid = new Grid<>(Assignment.class, false);
        configureScheduleGrid();

        conflictHeader = new H3("Conflictos y Posiciones No Cubiertas");
        conflictGrid = new Grid<>(Assignment.class, false);
        configureConflictGrid();
        
        // --- Layout ---
        HorizontalLayout controlsLayout = new HorizontalLayout(startDatePicker, endDatePicker, generateScheduleButton);
        controlsLayout.setAlignItems(Alignment.BASELINE);
        
        scheduleHeader.setVisible(false);
        conflictHeader.setVisible(false);

        add(title, controlsLayout, progressBar, conflictHeader, conflictGrid, scheduleHeader, scheduleGrid);
        setSizeFull();
        setAlignItems(Alignment.STRETCH);

        // --- Lógica de Eventos ---
        generateScheduleButton.addClickListener(event -> generateSchedule());
    }

    private void configureScheduleGrid() {
        scheduleGrid.addColumn(assignment -> assignment.getVuelo().getNumeroVuelo()).setHeader("Vuelo");
        scheduleGrid.addColumn(assignment -> assignment.getVuelo().getAerolinea().getNombre()).setHeader("Aerolínea");
        scheduleGrid.addColumn(assignment -> assignment.getPosicionSeguridad().getNombrePosicion()).setHeader("Posición");
        scheduleGrid.addColumn(assignment -> assignment.getAgente().getNombreCompleto()).setHeader("Agente Asignado");
        scheduleGrid.setVisible(false);
    }
    
    private void configureConflictGrid() {
        conflictGrid.addColumn(assignment -> assignment.getVuelo().getNumeroVuelo()).setHeader("Vuelo");
        conflictGrid.addColumn(assignment -> assignment.getVuelo().getAerolinea().getNombre()).setHeader("Aerolínea");
        conflictGrid.addColumn(assignment -> assignment.getPosicionSeguridad().getNombrePosicion()).setHeader("Posición No Cubierta");
        conflictGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        conflictGrid.setVisible(false);
    }

    private void generateSchedule() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            Notification.show("Por favor, seleccione un rango de fechas válido.", 3000, Notification.Position.BOTTOM_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        setUiStateToProcessing(true);
        ScheduleRequest request = new ScheduleRequest(startDate, endDate);
        Future<ScheduleResult> futureResult = schedulerService.generateSchedule(request);
        
        final UI ui = UI.getCurrent();
        CompletableFuture.runAsync(() -> {
            try {
                ScheduleResult result = futureResult.get();
                ui.access(() -> updateUiWithResults(result));
            } catch (Exception e) {
                ui.access(() -> {
                    Notification.show("Error inesperado durante la generación: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                                .addThemeVariants(NotificationVariant.LUMO_ERROR);
                });
            } finally {
                ui.access(() -> setUiStateToProcessing(false));
            }
        });
    }

    private void setUiStateToProcessing(boolean isProcessing) {
        progressBar.setVisible(isProcessing);
        generateScheduleButton.setEnabled(!isProcessing);
        startDatePicker.setEnabled(!isProcessing);
        endDatePicker.setEnabled(!isProcessing);

        if (isProcessing) {
            Notification.show("Iniciando la generación del horario...", 2000, Notification.Position.BOTTOM_CENTER);
            scheduleGrid.setVisible(false);
            conflictGrid.setVisible(false);
            scheduleHeader.setVisible(false);
            conflictHeader.setVisible(false);
        }
    }

    private void updateUiWithResults(ScheduleResult result) {
        Notification.show("¡Horario generado con éxito!", 3000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        
        if (result.getConflicts() != null && !result.getConflicts().isEmpty()) {
            conflictHeader.setVisible(true);
            conflictGrid.setItems(result.getConflicts());
            conflictGrid.setVisible(true);
            conflictHeader.setText("Conflictos y Posiciones No Cubiertas (" + result.getConflicts().size() + ")");
        }

        if (result.getAssignments() != null && !result.getAssignments().isEmpty()) {
            scheduleHeader.setVisible(true);
            scheduleGrid.setItems(result.getAssignments());
            scheduleGrid.setVisible(true);
            scheduleHeader.setText("Horario Generado (" + result.getAssignments().size() + " asignaciones)");
        }
    }
}