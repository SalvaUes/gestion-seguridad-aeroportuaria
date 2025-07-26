package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.dto.ScheduleRequest;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.dto.ScheduleResult;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Assignment;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Vuelo;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.DashboardStateService;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.SchedulerService;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.ui.dialogs.AssignmentDialog;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard | Gestión Seguridad")
@PermitAll
public class MainView extends VerticalLayout {

    private final SchedulerService schedulerService;
    private final DashboardStateService dashboardStateService;

    private final DatePicker startDatePicker;
    private final DatePicker endDatePicker;
    private final Button generateScheduleButton;
    private final ProgressBar progressBar;
    private final Div vueloCardContainer;

    @Autowired
    public MainView(SchedulerService schedulerService, DashboardStateService dashboardStateService) {
        this.schedulerService = schedulerService;
        this.dashboardStateService = dashboardStateService;

        H2 title = new H2("Dashboard de Operaciones y Agendamiento");
        
        startDatePicker = new DatePicker("Fecha de Inicio");
        endDatePicker = new DatePicker("Fecha de Fin");
        startDatePicker.setValue(LocalDate.now().plusDays(1));
        endDatePicker.setValue(LocalDate.now().plusDays(1));

        generateScheduleButton = new Button("Generar Horario", VaadinIcon.CALENDAR_CLOCK.create());
        generateScheduleButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);

        vueloCardContainer = new Div();
        vueloCardContainer.addClassName("vuelo-card-container");
        
        HorizontalLayout controlsLayout = new HorizontalLayout(startDatePicker, endDatePicker, generateScheduleButton);
        controlsLayout.setAlignItems(Alignment.BASELINE);
        
        add(title, controlsLayout, progressBar, vueloCardContainer);
        setSizeFull();

        generateScheduleButton.addClickListener(event -> generateSchedule());
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (dashboardStateService.getLastResult() != null) {
            updateUiWithResults(dashboardStateService.getLastResult());
        }
    }

    private void generateSchedule() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            Notification.show("Por favor, seleccione un rango de fechas válido.", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        setUiStateToProcessing(true);
        ScheduleRequest request = new ScheduleRequest(startDate, endDate);
        Future<ScheduleResult> futureResult = schedulerService.generateSchedule(request);
        
        final UI ui = UI.getCurrent();
        CompletableFuture.runAsync(() -> {
            try {
                ScheduleResult result = futureResult.get();
                dashboardStateService.setLastResult(result);
                ui.access(() -> updateUiWithResults(result));
            } catch (Exception e) {
                ui.access(() -> Notification.show("Error inesperado: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR));
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
            vueloCardContainer.removeAll();
        }
    }

    private void updateUiWithResults(ScheduleResult result) {
        vueloCardContainer.removeAll();
        
        Map<Vuelo, List<Assignment>> assignmentsByVuelo = result.getAssignments().stream()
                .collect(Collectors.groupingBy(Assignment::getVuelo));

        Set<Vuelo> allVuelos = Stream.concat(
                result.getAssignments().stream().map(Assignment::getVuelo),
                result.getConflicts().stream().map(Assignment::getVuelo)
        ).collect(Collectors.toSet());

        if (allVuelos.isEmpty()) {
            vueloCardContainer.add(new Span("No hay vuelos programados en el rango de fechas seleccionado."));
            return;
        }

        allVuelos.stream()
            .sorted((v1, v2) -> v1.getFechaHoraLlegada().compareTo(v2.getFechaHoraLlegada()))
            .forEach(vuelo -> {
                List<Assignment> assignmentsForThisVuelo = assignmentsByVuelo.getOrDefault(vuelo, List.of());
                int personalAsignado = assignmentsForThisVuelo.size();
                
                VueloCard card = new VueloCard(vuelo, personalAsignado);
                
                card.addCardClickListener(event -> {
                    AssignmentDialog dialog = new AssignmentDialog(event.getVuelo(), assignmentsForThisVuelo, schedulerService);
                    
                    // --- CORRECCIÓN AÑADIDA: Callback para refrescar el dashboard al guardar ---
                    dialog.addSaveListener(this::generateSchedule);
                    
                    dialog.open();
                });
                
                vueloCardContainer.add(card);
            });
    }
}