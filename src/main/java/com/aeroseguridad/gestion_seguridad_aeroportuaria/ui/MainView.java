package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.dto.ScheduleRequest;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.dto.ScheduleResult;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Assignment;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.EstadoAsignacion;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Vuelo;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.DashboardStateService;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.EmailService;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.ReportService;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.SchedulerService;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.ui.dialogs.AssignmentDialog;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.ui.dialogs.ShareReportDialog;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    private final ReportService reportService;
    private final EmailService emailService;

    private final DatePicker startDatePicker;
    private final DatePicker endDatePicker;
    private final Button generateScheduleButton;
    private final ProgressBar progressBar;
    private final Div vueloCardContainer;
    private final MenuBar downloadMenu;
    private final Anchor downloadAnchor;

    // --- NUEVOS COMPONENTES PARA EL PANEL DE CONFLICTOS ---
    private final VerticalLayout conflictPanel;
    private final Grid<Assignment> conflictGrid;

    @Autowired
    public MainView(SchedulerService schedulerService, DashboardStateService dashboardStateService, ReportService reportService, EmailService emailService) {
        this.schedulerService = schedulerService;
        this.dashboardStateService = dashboardStateService;
        this.reportService = reportService;
        this.emailService = emailService;

        H2 title = new H2("Dashboard de Operaciones y Agendamiento");
        
        startDatePicker = new DatePicker("Fecha de Inicio");
        endDatePicker = new DatePicker("Fecha de Fin");
        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now());

        generateScheduleButton = new Button("Generar Horario", VaadinIcon.CALENDAR_CLOCK.create());
        generateScheduleButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        downloadMenu = new MenuBar();
        downloadMenu.setEnabled(false);
        MenuItem downloadItem = downloadMenu.addItem("Opciones de Reporte");
        downloadItem.getSubMenu().addItem("Descargar PDF", e -> downloadReport("pdf"));
        downloadItem.getSubMenu().addItem("Descargar Excel", e -> downloadReport("xlsx"));
        downloadItem.getSubMenu().addSeparator();
        downloadItem.getSubMenu().addItem("Compartir por Correo...", e -> openShareDialog());
        
        downloadAnchor = new Anchor();
        downloadAnchor.getStyle().set("display", "none");
        
        progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);

        vueloCardContainer = new Div();
        vueloCardContainer.addClassName("vuelo-card-container");
        
        HorizontalLayout controlsLayout = new HorizontalLayout(startDatePicker, endDatePicker, generateScheduleButton, downloadMenu, downloadAnchor);
        controlsLayout.setAlignItems(Alignment.BASELINE);
        
        // --- INICIALIZACIÓN DEL PANEL DE CONFLICTOS ---
        conflictPanel = new VerticalLayout();
        conflictGrid = new Grid<>(Assignment.class, false);
        configureConflictPanel();

        add(title, controlsLayout, progressBar, conflictPanel, vueloCardContainer);
        setSizeFull();

        generateScheduleButton.addClickListener(event -> generateSchedule());
    }
    
    private void configureConflictPanel() {
        conflictPanel.setVisible(false); // Oculto por defecto
        conflictPanel.setWidthFull();
        conflictPanel.setPadding(true);
        conflictPanel.getStyle()
            .set("background-color", "var(--lumo-error-color-10pct)")
            .set("border", "1px solid var(--lumo-error-color-50pct)")
            .set("border-radius", "var(--lumo-border-radius-l)");

        H3 conflictTitle = new H3("⚠️ Conflictos de Asignación Detectados");
        conflictTitle.getStyle().set("color", "var(--lumo-error-text-color)");

        configureConflictGrid();
        
        conflictPanel.add(conflictTitle, conflictGrid);
    }
    
    private void configureConflictGrid() {
        conflictGrid.addColumn(a -> a.getVuelo().getNumeroVuelo()).setHeader("Vuelo");
        conflictGrid.addColumn(a -> a.getPosicionSeguridad().getNombrePosicion()).setHeader("Posición");
        conflictGrid.addColumn(a -> formatConflictReason(a.getEstado())).setHeader("Causa del Conflicto");
        
        conflictGrid.addComponentColumn(assignment -> {
            Button resolveButton = new Button("Resolver", e -> {
                // Reutilizamos la lógica de las VueloCards para abrir el diálogo de asignación
                List<Assignment> allAssignmentsForVuelo = dashboardStateService.getLastResult().getAssignments().stream()
                    .filter(a -> a.getVuelo().equals(assignment.getVuelo()))
                    .collect(Collectors.toList());
                
                AssignmentDialog dialog = new AssignmentDialog(assignment.getVuelo(), allAssignmentsForVuelo, schedulerService);
                dialog.addSaveListener(this::generateSchedule); // Recalcula el horario al guardar
                dialog.open();
            });
            return resolveButton;
        }).setHeader("Acción");
    }

    private String formatConflictReason(EstadoAsignacion estado) {
        switch (estado) {
            case CONFLICTO_AGENTE_NO_DISPONIBLE: return "Agente no disponible (turno modificado/eliminado)";
            case CONFLICTO_PERMISO_APROBADO: return "Agente con permiso aprobado";
            case CONFLICTO_SIN_CUALIFICACION: return "Agente sin cualificación requerida";
            case CONFLICTO_AGENTE_INACTIVO_O_SIN_PERMISO: return "Agente inactivo o sin permiso para la aerolínea";
            case CONFLICTO_NO_CUBIERTO: return "No se encontró personal disponible";
            default: return "Conflicto desconocido";
        }
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
            conflictPanel.setVisible(false); // Ocultamos el panel de conflictos mientras se procesa
        }
    }

    private void updateUiWithResults(ScheduleResult result) {
        vueloCardContainer.removeAll();
        downloadMenu.setEnabled(result != null && !result.getAssignments().isEmpty());
        
        // --- LÓGICA PARA MOSTRAR/OCULTAR Y RELLENAR EL PANEL DE CONFLICTOS ---
        if (result.getConflicts() != null && !result.getConflicts().isEmpty()) {
            conflictGrid.setItems(result.getConflicts());
            conflictPanel.setVisible(true);
        } else {
            conflictPanel.setVisible(false);
        }

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
                    dialog.addSaveListener(this::generateSchedule);
                    dialog.open();
                });
                
                vueloCardContainer.add(card);
            });
    }

    // ... (downloadReport y openShareDialog se mantienen igual)
    private void downloadReport(String format) {
        ScheduleResult data = dashboardStateService.getLastResult();
        if (data == null || data.getAssignments().isEmpty()) {
            Notification.show("Primero debe generar un horario con asignaciones.", 3000, Notification.Position.BOTTOM_CENTER);
            return;
        }

        String fileName = "Horario_Seguridad_" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + "." + format;
        
        StreamResource streamResource = new StreamResource(fileName, () -> {
            byte[] reportBytes = "pdf".equals(format) ?
                reportService.generatePdfReport(data) :
                reportService.generateExcelReport(data);
            return new ByteArrayInputStream(reportBytes);
        });

        downloadAnchor.setHref(streamResource);
        downloadAnchor.getElement().callJsFunction("click");
    }
    
    private void openShareDialog() {
        ScheduleResult data = dashboardStateService.getLastResult();
        if (data == null || data.getAssignments().isEmpty()) {
            Notification.show("Primero debe generar un horario con asignaciones.", 3000, Notification.Position.BOTTOM_CENTER);
            return;
        }
        ShareReportDialog dialog = new ShareReportDialog(data, reportService, emailService);
        dialog.open();
    }
}