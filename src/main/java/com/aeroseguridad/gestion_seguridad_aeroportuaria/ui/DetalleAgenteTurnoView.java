package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.TipoTurno;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Turno;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.AgenteService;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.PlantillaTurnoService;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.TurnoService;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.ui.dialogs.GestionarPlantillasDialog; // <-- Importamos el nuevo diálogo
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.RolesAllowed;

@Route(value = "planificador-turnos/:agenteId", layout = MainLayout.class)
@PageTitle("Detalle de Planificación")
@RolesAllowed("ADMIN")
public class DetalleAgenteTurnoView extends VerticalLayout implements BeforeEnterObserver {

    // ... (Clase interna DiaDelMes se mantiene igual)
    public static class DiaDelMes {
        private final LocalDate fecha;
        private final List<Turno> turnos = new ArrayList<>();
        public DiaDelMes(LocalDate fecha) { this.fecha = fecha; }
        public LocalDate getFecha() { return fecha; }
        public List<Turno> getTurnos() { return turnos; }
        public int getNumeroDia() { return fecha.getDayOfMonth(); }
    }

    private final AgenteService agenteService;
    private final TurnoService turnoService;
    private final PlantillaTurnoService plantillaTurnoService;

    private Agente agenteActual;
    private YearMonth mesActual;

    private H2 titulo = new H2();
    private Grid<List<DiaDelMes>> calendarGrid;

    public DetalleAgenteTurnoView(AgenteService agenteService, TurnoService turnoService, PlantillaTurnoService plantillaTurnoService) {
        this.agenteService = agenteService;
        this.turnoService = turnoService;
        this.plantillaTurnoService = plantillaTurnoService;
        this.mesActual = YearMonth.now();
        setSizeFull();
        addClassName("detalle-agente-view");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Long agenteId = event.getRouteParameters().getLong("agenteId").orElseThrow();
        this.agenteActual = agenteService.findById(agenteId).orElseThrow();
        buildUI();
    }

    private void buildUI() {
        removeAll();
        // La sección de plantillas se elimina de aquí para darle espacio al calendario
        add(buildHeader(), buildToolbar(), buildCalendarGrid());
        refreshCalendarGrid();
    }

    private HorizontalLayout buildHeader() {
        Image img = new Image(agenteActual.getRutaFotografia() != null ? "/agent-photos/" + agenteActual.getRutaFotografia() : "images/user.png", "Foto");
        img.addClassName("agente-image-detail");
        H2 nombreAgente = new H2("Planificación de: " + agenteActual.getNombreCompleto());
        HorizontalLayout header = new HorizontalLayout(img, nombreAgente);
        header.setAlignItems(Alignment.CENTER);
        return header;
    }
    
    private HorizontalLayout buildToolbar() {
        Button prevButton = new Button("Mes Anterior", VaadinIcon.ARROW_LEFT.create(), e -> changeMonth(-1));
        Button nextButton = new Button("Mes Siguiente", VaadinIcon.ARROW_RIGHT.create(), e -> changeMonth(1));
        Button todayButton = new Button("Hoy", e -> {
            mesActual = YearMonth.now();
            refreshCalendarGrid();
        });
        
        titulo.setText(formatMonthTitle());
        
        // --- NUEVO BOTÓN PARA GESTIONAR PLANTILLAS ---
        Button gestionarPlantillasButton = new Button("Gestionar Plantillas", VaadinIcon.EDIT.create());
        gestionarPlantillasButton.addClickListener(e -> new GestionarPlantillasDialog(agenteActual, plantillaTurnoService).open());

        Button generarButton = new Button("Generar Turnos", VaadinIcon.COG.create(), e -> generarTurnos());
        generarButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        // Creamos un layout para los botones de la derecha
        HorizontalLayout actionsLayout = new HorizontalLayout(gestionarPlantillasButton, generarButton);
        actionsLayout.getStyle().set("margin-left", "auto");

        HorizontalLayout toolbar = new HorizontalLayout(prevButton, todayButton, nextButton, titulo, actionsLayout);
        toolbar.setWidthFull();
        toolbar.setAlignItems(Alignment.CENTER);
        return toolbar;
    }

    private Grid<List<DiaDelMes>> buildCalendarGrid() {
        calendarGrid = new Grid<>();
        calendarGrid.setSizeFull(); // Hacemos que ocupe todo el espacio vertical disponible
        calendarGrid.addClassName("calendar-grid");
        for (DayOfWeek day : DayOfWeek.values()) {
            calendarGrid.addColumn(new ComponentRenderer<>(semana -> createDayCell(semana, day)))
                .setHeader(day.getDisplayName(TextStyle.FULL, new Locale("es", "ES")));
        }
        return calendarGrid;
    }
    
    // ... (El resto de los métodos se mantienen exactamente igual)
    private void refreshCalendarGrid() {
        titulo.setText(formatMonthTitle());
        LocalDateTime inicioMes = mesActual.atDay(1).atStartOfDay();
        LocalDateTime finMes = mesActual.atEndOfMonth().atTime(23, 59, 59);
        Map<LocalDate, List<Turno>> turnosPorDia = turnoService.findTurnosByAgenteAndDateRange(agenteActual.getIdAgente(), inicioMes, finMes)
            .stream().collect(Collectors.groupingBy(turno -> turno.getInicioTurno().toLocalDate()));
        List<List<DiaDelMes>> semanas = new ArrayList<>();
        LocalDate primerDiaDelMes = mesActual.atDay(1);
        int primerDiaOffset = primerDiaDelMes.getDayOfWeek().getValue() - 1; // Lunes=0, Domingo=6

        List<DiaDelMes> semanaActual = new ArrayList<>();
        for (int i = 0; i < primerDiaOffset; i++) {
            semanaActual.add(null);
        }
        for (int i = 1; i <= mesActual.lengthOfMonth(); i++) {
            LocalDate fecha = mesActual.atDay(i);
            DiaDelMes dia = new DiaDelMes(fecha);
            if (turnosPorDia.containsKey(fecha)) {
                dia.getTurnos().addAll(turnosPorDia.get(fecha));
            }
            semanaActual.add(dia);
            if (semanaActual.size() == 7) {
                semanas.add(semanaActual);
                semanaActual = new ArrayList<>();
            }
        }
        if (!semanaActual.isEmpty()) {
            while (semanaActual.size() < 7) {
                semanaActual.add(null);
            }
            semanas.add(semanaActual);
        }
        calendarGrid.setItems(semanas);
    }
    private Component createDayCell(List<DiaDelMes> semana, DayOfWeek dayOfWeek) {
        int dayIndex = dayOfWeek.getValue() - 1;
        if (dayIndex >= semana.size() || semana.get(dayIndex) == null) {
            Div emptyCell = new Div();
            emptyCell.addClassNames("day-cell", "not-in-month");
            return emptyCell;
        }

        DiaDelMes dia = semana.get(dayIndex);
        VerticalLayout cell = new VerticalLayout();
        cell.addClassName("day-cell");
        if(dia.getFecha().equals(LocalDate.now())){
            cell.addClassName("today");
        }
        cell.setSpacing(false);
        cell.setPadding(false);

        Span numeroDia = new Span(String.valueOf(dia.getNumeroDia()));
        numeroDia.addClassName("day-number");
        cell.add(numeroDia);

        for (Turno turno : dia.getTurnos()) {
            Div turnoDiv = new Div(new Span(turno.getTipoTurno().toString()));
            turnoDiv.addClassName("turno-entry");
            if (turno.getTipoTurno() == TipoTurno.NOCTURNO) {
                turnoDiv.addClassName("turno-nocturno");
            } else {
                turnoDiv.addClassName("turno-regular");
            }
            cell.add(turnoDiv);
        }
        return cell;
    }
    private void changeMonth(int amount) {
        mesActual = mesActual.plusMonths(amount);
        refreshCalendarGrid();
    }
    private String formatMonthTitle() {
        String month = mesActual.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
        month = month.substring(0, 1).toUpperCase() + month.substring(1);
        return month + " " + mesActual.getYear();
    }
    private void generarTurnos() {
        try {
            int turnosGenerados = plantillaTurnoService.generarTurnosDesdePlantilla(agenteActual.getIdAgente(), mesActual.getYear(), mesActual.getMonthValue());
            Notification.show(turnosGenerados + " turnos generados.", 3000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            refreshCalendarGrid();
        } catch (Exception e) {
            Notification.show("Error al generar turnos: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}