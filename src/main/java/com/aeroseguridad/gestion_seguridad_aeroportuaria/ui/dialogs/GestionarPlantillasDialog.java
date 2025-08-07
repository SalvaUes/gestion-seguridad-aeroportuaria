package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui.dialogs;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PlantillaTurno;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.PlantillaTurnoService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class GestionarPlantillasDialog extends Dialog {

    private final PlantillaTurnoService plantillaTurnoService;
    private final Agente agente;

    private Grid<PlantillaTurno> plantillasGrid = new Grid<>(PlantillaTurno.class, false);

    public GestionarPlantillasDialog(Agente agente, PlantillaTurnoService plantillaTurnoService) {
        this.agente = agente;
        this.plantillaTurnoService = plantillaTurnoService;

        setHeaderTitle("Gestionar Plantillas para " + agente.getNombreCompleto());
        setWidth("800px");
        setHeight("70vh");

        configureGrid();
        add(createToolbar(), plantillasGrid);

        getFooter().add(new Button("Cerrar", e -> close()));

        refreshGrid();
    }

    private void configureGrid() {
        plantillasGrid.setSizeFull();
        plantillasGrid.addColumn(PlantillaTurno::getNombrePlantilla).setHeader("Nombre").setSortable(true);
        plantillasGrid.addColumn(PlantillaTurno::getFechaInicioVigencia).setHeader("Válida Desde");
        plantillasGrid.addColumn(PlantillaTurno::getFechaFinVigencia).setHeader("Válida Hasta");
        plantillasGrid.addComponentColumn(this::createActionButtons).setHeader("Acciones").setFlexGrow(0).setWidth("200px");
    }

    private Component createToolbar() {
        Button addPlantillaButton = new Button("Añadir Nueva Plantilla", VaadinIcon.PLUS.create());
        addPlantillaButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addPlantillaButton.addClickListener(e -> openPlantillaForm(null));
        return new HorizontalLayout(addPlantillaButton);
    }
    
    private Component createActionButtons(PlantillaTurno plantilla) {
        Button editButton = new Button("Editar", VaadinIcon.EDIT.create(), e -> openPlantillaForm(plantilla));
        Button deleteButton = new Button(VaadinIcon.TRASH.create(), e -> deletePlantilla(plantilla));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_ICON);
        return new HorizontalLayout(editButton, deleteButton);
    }

    private void openPlantillaForm(PlantillaTurno plantilla) {
        PlantillaTurnoDialog formDialog = new PlantillaTurnoDialog(agente, plantilla, savedPlantilla -> {
            plantillaTurnoService.savePlantilla(savedPlantilla);
            refreshGrid();
            Notification.show("Plantilla guardada.", 2000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        formDialog.open();
    }

    private void deletePlantilla(PlantillaTurno plantilla) {
        // Aquí se podría añadir un diálogo de confirmación si se desea
        plantillaTurnoService.deletePlantilla(plantilla.getId());
        refreshGrid();
        Notification.show("Plantilla eliminada.", 2000, Notification.Position.BOTTOM_CENTER);
    }

    private void refreshGrid() {
        plantillasGrid.setItems(plantillaTurnoService.findAllPlantillasByAgente(agente.getIdAgente()));
    }
}