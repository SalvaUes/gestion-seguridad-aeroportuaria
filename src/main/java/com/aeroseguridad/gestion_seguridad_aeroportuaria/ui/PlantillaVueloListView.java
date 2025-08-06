package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Aerolinea;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PlantillaVuelo;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PosicionSeguridad;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.AerolineaService;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.PlantillaVueloService;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.PosicionSeguridadService;
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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "plantillas-vuelo", layout = MainLayout.class)
@PageTitle("Plantillas de Vuelo | Gestión Seguridad")
@PermitAll
public class PlantillaVueloListView extends VerticalLayout {

    private final PlantillaVueloService plantillaService;
    private final AerolineaService aerolineaService;
    private final PosicionSeguridadService posicionService;

    private Grid<PlantillaVuelo> grid = new Grid<>(PlantillaVuelo.class);
    private PlantillaVueloForm form;
    private Dialog editorDialog = new Dialog();

    public PlantillaVueloListView(PlantillaVueloService plantillaService,
                                  AerolineaService aerolineaService,
                                  PosicionSeguridadService posicionService) {
        this.plantillaService = plantillaService;
        this.aerolineaService = aerolineaService;
        this.posicionService = posicionService;

        addClassName("plantilla-vuelo-list-view");
        setSizeFull();
        configureGrid();
        configureFormAndDialog();

        Div content = new Div(grid);
        content.addClassName("content");
        content.setSizeFull();

        add(getToolbar(), content);
        updateList();
    }

    private void configureGrid() {
        grid.addClassName("plantilla-vuelo-grid");
        grid.setSizeFull();
        grid.setColumns("nombrePlantilla", "numeroVuelo", "origen", "destino");
        
        grid.addColumn(plantilla -> plantilla.getAerolinea() != null ? plantilla.getAerolinea().getNombre() : "N/A").setHeader("Aerolínea").setSortable(true);
        grid.addColumn(plantilla -> formatTime(plantilla.getHoraSalida())).setHeader("Hora Salida").setSortable(true);
        grid.addColumn(plantilla -> formatTime(plantilla.getHoraLlegada())).setHeader("Hora Llegada").setSortable(true);

        grid.addColumn(plantilla -> {
            if (plantilla.getDiasOperacion() == null || plantilla.getDiasOperacion().isEmpty()) return "";
            return plantilla.getDiasOperacion().stream()
                .sorted()
                .map(day -> getDiaSemanaEnEspanol(day).substring(0, 3))
                .collect(Collectors.joining(", "));
        }).setHeader("Días Op.").setSortable(true);

        grid.addColumn(new ComponentRenderer<>(this::createActionButtons))
            .setHeader("Acciones").setFlexGrow(0);

        grid.getColumns().forEach(col -> col.setAutoWidth(true).setResizable(true));
        
        grid.asSingleSelect().addValueChangeListener(event -> editPlantilla(event.getValue()));
    }
    
    private HorizontalLayout createActionButtons(PlantillaVuelo plantilla) {
        Button editButton = new Button(VaadinIcon.EDIT.create());
        editButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY_INLINE);
        editButton.addClickListener(e -> editPlantilla(plantilla));

        Button generateButton = new Button(VaadinIcon.AUTOMATION.create());
        generateButton.setTooltipText("Generar vuelos desde esta plantilla");
        generateButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_ICON);
        generateButton.addClickListener(e -> openGenerationDialog(plantilla));

        return new HorizontalLayout(editButton, generateButton);
    }

    private void configureFormAndDialog() {
        List<Aerolinea> aerolineas = aerolineaService.findAll();
        List<PosicionSeguridad> posiciones = posicionService.findAllActive();
        form = new PlantillaVueloForm(aerolineas, posiciones); 
        
        form.addListener(PlantillaVueloForm.SaveEvent.class, this::savePlantilla);
        form.addListener(PlantillaVueloForm.DeleteEvent.class, this::deletePlantilla);
        form.addListener(PlantillaVueloForm.CloseEvent.class, e -> closeEditor());

        editorDialog.add(form);
        editorDialog.setModal(true);
        editorDialog.setWidth("70%");
        editorDialog.setMaxWidth("900px");
        editorDialog.setDraggable(true);
    }

    private HorizontalLayout getToolbar() {
        H2 title = new H2("Gestión de Plantillas de Vuelo");
        Button addPlantillaButton = new Button("Crear Nueva Plantilla");
        addPlantillaButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addPlantillaButton.addClickListener(click -> addPlantilla());

        HorizontalLayout toolbar = new HorizontalLayout(title, addPlantillaButton);
        toolbar.addClassName("toolbar");
        toolbar.setJustifyContentMode(JustifyContentMode.BETWEEN);
        toolbar.setWidthFull();
        return toolbar;
    }
    
    private void openGenerationDialog(PlantillaVuelo plantilla) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Generar Vuelos: " + plantilla.getNombrePlantilla());

        DatePicker fechaInicio = new DatePicker("Generar desde");
        DatePicker fechaFin = new DatePicker("Generar hasta");
        
        fechaInicio.setValue(LocalDate.now());
        fechaFin.setValue(LocalDate.now().plusMonths(1));

        dialog.add(new VerticalLayout(fechaInicio, fechaFin));

        Button generateButton = new Button("Generar", VaadinIcon.CHECK.create(), e -> {
            if (fechaInicio.getValue() != null && fechaFin.getValue() != null && !fechaFin.getValue().isBefore(fechaInicio.getValue())) {
                try {
                    int count = plantillaService.generarVuelosDesdePlantilla(plantilla.getId(), fechaInicio.getValue(), fechaFin.getValue());
                    Notification.show(count + " vuelos nuevos generados.", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    dialog.close();
                } catch (Exception ex) {
                    Notification.show("Error al generar vuelos: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            } else {
                Notification.show("Por favor, seleccione un rango de fechas válido.").addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
        });
        generateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(new Button("Cancelar", e -> dialog.close()), generateButton);
        dialog.open();
    }
    
    private void addPlantilla() {
        grid.asSingleSelect().clear();
        editPlantilla(new PlantillaVuelo());
    }

    private void editPlantilla(PlantillaVuelo plantilla) {
        if (plantilla == null) {
            closeEditor();
        } else {
            form.setPlantilla(plantilla);
            editorDialog.setHeaderTitle(plantilla.getId() == null ? "Nueva Plantilla" : "Editar Plantilla");
            editorDialog.open();
        }
    }
    
    private void savePlantilla(PlantillaVueloForm.SaveEvent event) {
        plantillaService.save(event.getPlantilla());
        updateList();
        closeEditor();
        Notification.show("Plantilla guardada.", 2000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
    
    private void deletePlantilla(PlantillaVueloForm.DeleteEvent event) {
        // Implementar lógica de borrado si se desea
        // plantillaService.deleteById(event.getPlantilla().getId());
        updateList();
        closeEditor();
    }
    
    private void closeEditor() {
        editorDialog.close();
        form.setPlantilla(null);
    }

    private void updateList() {
        grid.setItems(plantillaService.findAll());
    }

    private String formatTime(java.time.LocalTime time) {
        if (time == null) return "";
        return time.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm a"));
    }

    private String getDiaSemanaEnEspanol(DayOfWeek day) {
        switch (day) {
            case MONDAY: return "LUN";
            case TUESDAY: return "MAR";
            case WEDNESDAY: return "MIÉ";
            case THURSDAY: return "JUE";
            case FRIDAY: return "VIE";
            case SATURDAY: return "SÁB";
            case SUNDAY: return "DOM";
            default: return "";
        }
    }
}