package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui.dialogs;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PlantillaTurno;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.ReglaDeTurno;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;

public class PlantillaTurnoDialog extends Dialog {

    private final Agente agente;
    private PlantillaTurno plantillaTurno;
    private final Consumer<PlantillaTurno> saveListener;

    private Binder<PlantillaTurno> binder = new Binder<>(PlantillaTurno.class);
    private List<ReglaDeTurno> reglasBuffer = new ArrayList<>();

    private TextField nombrePlantilla = new TextField("Nombre de la Plantilla");
    private DatePicker fechaInicioVigencia = new DatePicker("Inicio de Vigencia");
    private DatePicker fechaFinVigencia = new DatePicker("Fin de Vigencia");
    private Grid<ReglaDeTurno> reglasGrid = new Grid<>(ReglaDeTurno.class, false);

    public PlantillaTurnoDialog(Agente agente, PlantillaTurno plantilla, Consumer<PlantillaTurno> saveListener) {
        this.agente = agente;
        this.plantillaTurno = plantilla;
        this.saveListener = saveListener;
        
        if (this.plantillaTurno == null) {
            this.plantillaTurno = new PlantillaTurno();
            this.plantillaTurno.setAgente(agente);
            setHeaderTitle("Nueva Plantilla de Turno");
        } else {
            setHeaderTitle("Editar Plantilla de Turno");
            this.plantillaTurno.getReglas().forEach(r -> this.reglasBuffer.add(new ReglaDeTurno(r)));
        }

        setWidth("700px");
        setResizable(true);
        add(createFormLayout());
        createFooter();
        
        binder.setBean(this.plantillaTurno);
    }

    private Component createFormLayout() {
        binder.forField(nombrePlantilla).asRequired("El nombre es obligatorio.").bind("nombrePlantilla");
        binder.forField(fechaInicioVigencia).asRequired("La fecha de inicio es obligatoria.").bind("fechaInicioVigencia");
        binder.bind(fechaFinVigencia, "fechaFinVigencia");
        
        configureReglasGrid();

        VerticalLayout formLayout = new VerticalLayout(
                nombrePlantilla,
                new HorizontalLayout(fechaInicioVigencia, fechaFinVigencia),
                new H3("Reglas Semanales"),
                createReglasToolbar(),
                reglasGrid
        );
        formLayout.setPadding(false);
        formLayout.setSpacing(true);
        return formLayout;
    }
    
    private void configureReglasGrid() {
        // --- CORRECCIÓN CLAVE: Usar el método getDiasDeLaSemana (plural) y formatearlo ---
        reglasGrid.addColumn(r -> formatDias(r.getDiasDeLaSemana()))
                  .setHeader("Días").setSortable(true).setFlexGrow(2);
        reglasGrid.addColumn(ReglaDeTurno::getHoraInicio).setHeader("Hora Inicio");
        reglasGrid.addColumn(ReglaDeTurno::getHoraFin).setHeader("Hora Fin");
        reglasGrid.addColumn(ReglaDeTurno::getTipoTurno).setHeader("Tipo");
        
        reglasGrid.addComponentColumn(this::createRuleActionButtons).setHeader("Acciones").setFlexGrow(0);
        
        reglasGrid.setItems(reglasBuffer);
    }

    /**
     * Helper para mostrar el conjunto de días de forma legible y ordenada.
     */
    private String formatDias(Set<DayOfWeek> dias) {
        if (dias == null || dias.isEmpty()) {
            return "";
        }
        return dias.stream()
                   .sorted() // Ordena los días (Lunes, Martes, etc.)
                   .map(d -> d.getDisplayName(TextStyle.SHORT, new Locale("es", "ES")))
                   .collect(Collectors.joining(", "));
    }
    
    private HorizontalLayout createReglasToolbar() {
        Button addRuleButton = new Button("Añadir Regla", VaadinIcon.PLUS.create(), e -> {
            ReglaTurnoFormDialog dialog = new ReglaTurnoFormDialog(null, nuevaRegla -> {
                reglasBuffer.add(nuevaRegla);
                reglasGrid.getDataProvider().refreshAll();
            });
            dialog.open();
        });
        return new HorizontalLayout(addRuleButton);
    }

    private HorizontalLayout createRuleActionButtons(ReglaDeTurno regla) {
        Button editButton = new Button(VaadinIcon.EDIT.create(), e -> {
            ReglaDeTurno reglaClonada = new ReglaDeTurno(regla);
            ReglaTurnoFormDialog dialog = new ReglaTurnoFormDialog(reglaClonada, reglaEditada -> {
                int index = reglasBuffer.indexOf(regla);
                if (index != -1) {
                    reglasBuffer.set(index, reglaEditada);
                    reglasGrid.getDataProvider().refreshAll();
                }
            });
            dialog.open();
        });
        editButton.addThemeVariants(ButtonVariant.LUMO_ICON);

        Button deleteButton = new Button(VaadinIcon.TRASH.create(), e -> {
            reglasBuffer.remove(regla);
            reglasGrid.getDataProvider().refreshAll();
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_ICON);
        
        return new HorizontalLayout(editButton, deleteButton);
    }
    
    private void createFooter() {
        Button cancelButton = new Button("Cancelar", e -> close());
        Button saveButton = new Button("Guardar Plantilla", e -> save());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(cancelButton, saveButton);
    }

    private void save() {
        try {
            binder.writeBean(plantillaTurno);
            plantillaTurno.getReglas().clear();
            for (ReglaDeTurno reglaBuffer : reglasBuffer) {
                plantillaTurno.addRegla(new ReglaDeTurno(reglaBuffer));
            }
            saveListener.accept(plantillaTurno);
            close();
        } catch (ValidationException e) {
            Notification.show("Por favor, corrige los errores en el formulario.");
        }
    }
}