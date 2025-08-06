package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import java.time.DayOfWeek;
import java.time.Duration;
import java.util.List;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Aerolinea;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.NecesidadVuelo;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PlantillaVuelo;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PosicionSeguridad;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.TipoOperacionVuelo;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.shared.Registration;

public class PlantillaVueloForm extends FormLayout {

    // --- Campos del Formulario ---
    TextField nombrePlantilla = new TextField("Nombre Descriptivo de la Plantilla");
    TextField numeroVuelo = new TextField("Número Vuelo");
    ComboBox<Aerolinea> aerolinea = new ComboBox<>("Aerolínea");
    TextField origen = new TextField("Origen");
    TextField destino = new TextField("Destino");
    TimePicker horaSalida = new TimePicker("Hora Salida");
    TimePicker horaLlegada = new TimePicker("Hora Llegada");
    ComboBox<TipoOperacionVuelo> tipoOperacion = new ComboBox<>("Tipo Operación");
    TimePicker horaFinOperacionSeguridad = new TimePicker("Fin Op. Seguridad (Hora)");
    CheckboxGroup<DayOfWeek> diasOperacion = new CheckboxGroup<>("Días de Operación");
    DatePicker fechaInicioContrato = new DatePicker("Inicio de Contrato");
    DatePicker fechaFinContrato = new DatePicker("Fin de Contrato");

    Grid<NecesidadVuelo> gridNecesidades = new Grid<>(NecesidadVuelo.class, false);
    private final List<PosicionSeguridad> todasLasPosiciones;

    // --- Botones ---
    Button save = new Button("Guardar Plantilla");
    Button delete = new Button("Eliminar");
    Button cancel = new Button("Cancelar");

    private PlantillaVuelo plantillaActual;

    public PlantillaVueloForm(List<Aerolinea> aerolineas, List<PosicionSeguridad> posiciones) {
        this.todasLasPosiciones = posiciones;
        addClassName("plantilla-vuelo-form");

        configureFields(aerolineas);
        configureGridNecesidades();

        VerticalLayout necesidadesLayout = new VerticalLayout(
            new H4("Necesidades de Seguridad Estándar"),
            gridNecesidades
        );
        necesidadesLayout.setPadding(false);
        necesidadesLayout.setSpacing(false);
        
        // --- CAMBIO: Guardar el layout de botones en una variable ---
        HorizontalLayout buttonsLayout = createButtonsLayout();

        add(
            nombrePlantilla, numeroVuelo, aerolinea, origen, destino,
            horaSalida, horaLlegada, tipoOperacion, horaFinOperacionSeguridad,
            diasOperacion, fechaInicioContrato, fechaFinContrato,
            necesidadesLayout,
            buttonsLayout
        );
        
        setResponsiveSteps(new FormLayout.ResponsiveStep("0", 3));
        setColspan(nombrePlantilla, 3);
        setColspan(aerolinea, 1);
        setColspan(origen, 1);
        setColspan(destino, 1);
        setColspan(diasOperacion, 3);
        setColspan(necesidadesLayout, 3);
        // --- CAMBIO: Usar la variable para referenciar el layout de botones ---
        setColspan(buttonsLayout, 3);
    }

    private void configureFields(List<Aerolinea> aerolineas) {
        nombrePlantilla.setRequired(true);
        numeroVuelo.setRequired(true);
        aerolinea.setRequired(true);
        origen.setRequired(true);
        destino.setRequired(true);
        horaSalida.setRequired(true);
        horaLlegada.setRequired(true);
        tipoOperacion.setRequired(true);
        diasOperacion.setRequired(true);
        fechaInicioContrato.setRequired(true);
        fechaFinContrato.setRequired(true);
        
        aerolinea.setItems(aerolineas);
        aerolinea.setItemLabelGenerator(Aerolinea::getNombre);
        tipoOperacion.setItems(TipoOperacionVuelo.values());
        
        diasOperacion.setItems(DayOfWeek.values());
        diasOperacion.setItemLabelGenerator(this::getDiaSemanaEnEspanol);
        
        horaSalida.setStep(Duration.ofMinutes(5));
        horaLlegada.setStep(Duration.ofMinutes(5));
        horaFinOperacionSeguridad.setStep(Duration.ofMinutes(5));
    }

    private void configureGridNecesidades() {
        gridNecesidades.addClassName("necesidad-grid-plantilla");
        gridNecesidades.addColumn(nec -> nec.getPosicion() != null ? nec.getPosicion().getNombrePosicion() : "N/A").setHeader("Posición");
        gridNecesidades.addColumn(NecesidadVuelo::getCantidadAgentes).setHeader("Cant.");
        gridNecesidades.addColumn(nec -> nec.getInicioCobertura() != null ? nec.getInicioCobertura().toLocalTime().toString() : "").setHeader("Inicio Cob.");
        gridNecesidades.addColumn(nec -> nec.getFinCobertura() != null ? nec.getFinCobertura().toLocalTime().toString() : "").setHeader("Fin Cob.");

        gridNecesidades.addColumn(new ComponentRenderer<>(necesidad -> {
            Button deleteBtn = new Button(VaadinIcon.TRASH.create(), e -> {
                if (plantillaActual != null) {
                    plantillaActual.removeNecesidadEstandar(necesidad);
                    refreshGridNecesidades();
                }
            });
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR);
            return deleteBtn;
        })).setHeader("Acciones");

        Button addNecesidadButton = new Button("Añadir Posición", VaadinIcon.PLUS.create());
        addNecesidadButton.addClickListener(e -> {
            if (plantillaActual != null) {
                NecesidadVueloForm dialogForm = new NecesidadVueloForm(this.todasLasPosiciones);
                dialogForm.setNecesidad(new NecesidadVuelo(), null);
                dialogForm.addListener(NecesidadVueloForm.SaveEvent.class, saveEvent -> {
                    plantillaActual.addNecesidadEstandar(saveEvent.getNecesidad());
                    refreshGridNecesidades();
                    dialogForm.close();
                });
                dialogForm.open();
            }
        });
        
        if (gridNecesidades.getFooterRows().isEmpty()) {
             gridNecesidades.appendFooterRow();
        }
        gridNecesidades.getFooterRows().get(0).getCells().get(0).setComponent(addNecesidadButton);
    }
    
    private void refreshGridNecesidades() {
        if (plantillaActual != null && plantillaActual.getNecesidadesEstandar() != null) {
            gridNecesidades.setItems(plantillaActual.getNecesidadesEstandar());
        } else {
            gridNecesidades.setItems(java.util.Collections.emptyList());
        }
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

    private HorizontalLayout createButtonsLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        save.addClickShortcut(Key.ENTER);
        cancel.addClickShortcut(Key.ESCAPE);

        save.addClickListener(event -> validateAndSave());
        delete.addClickListener(event -> fireEvent(new DeleteEvent(this, plantillaActual)));
        cancel.addClickListener(event -> fireEvent(new CloseEvent(this)));

        delete.setEnabled(false);

        return new HorizontalLayout(save, delete, cancel);
    }

    private void validateAndSave() {
        if (nombrePlantilla.isEmpty() || numeroVuelo.isEmpty() || aerolinea.isEmpty() ||
            origen.isEmpty() || destino.isEmpty() || horaSalida.isEmpty() || horaLlegada.isEmpty() ||
            tipoOperacion.isEmpty() || diasOperacion.isEmpty() || fechaInicioContrato.isEmpty() ||
            fechaFinContrato.isEmpty()) {
            
            Notification.show("Por favor, complete todos los campos requeridos.", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (fechaFinContrato.getValue().isBefore(fechaInicioContrato.getValue())) {
            Notification.show("La 'Fecha Fin de Contrato' no puede ser anterior a la fecha de inicio.", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (plantillaActual != null) {
            plantillaActual.setNombrePlantilla(nombrePlantilla.getValue());
            plantillaActual.setNumeroVuelo(numeroVuelo.getValue());
            plantillaActual.setAerolinea(aerolinea.getValue());
            plantillaActual.setOrigen(origen.getValue());
            plantillaActual.setDestino(destino.getValue());
            plantillaActual.setHoraSalida(horaSalida.getValue());
            plantillaActual.setHoraLlegada(horaLlegada.getValue());
            plantillaActual.setTipoOperacion(tipoOperacion.getValue());
            plantillaActual.setHoraFinOperacionSeguridad(horaFinOperacionSeguridad.getValue());
            plantillaActual.setDiasOperacion(diasOperacion.getValue());
            plantillaActual.setFechaInicioContrato(fechaInicioContrato.getValue());
            plantillaActual.setFechaFinContrato(fechaFinContrato.getValue());
            
            fireEvent(new SaveEvent(this, plantillaActual));
        }
    }

    public void setPlantilla(PlantillaVuelo plantilla) {
        this.plantillaActual = plantilla;
        if (plantilla != null) {
            nombrePlantilla.setValue(plantilla.getNombrePlantilla() != null ? plantilla.getNombrePlantilla() : "");
            numeroVuelo.setValue(plantilla.getNumeroVuelo() != null ? plantilla.getNumeroVuelo() : "");
            aerolinea.setValue(plantilla.getAerolinea());
            origen.setValue(plantilla.getOrigen() != null ? plantilla.getOrigen() : "");
            destino.setValue(plantilla.getDestino() != null ? plantilla.getDestino() : "");
            horaSalida.setValue(plantilla.getHoraSalida());
            horaLlegada.setValue(plantilla.getHoraLlegada());
            tipoOperacion.setValue(plantilla.getTipoOperacion());
            horaFinOperacionSeguridad.setValue(plantilla.getHoraFinOperacionSeguridad());
            diasOperacion.setValue(plantilla.getDiasOperacion() != null ? plantilla.getDiasOperacion() : java.util.Collections.emptySet());
            fechaInicioContrato.setValue(plantilla.getFechaInicioContrato());
            fechaFinContrato.setValue(plantilla.getFechaFinContrato());
            
            refreshGridNecesidades();
            delete.setEnabled(plantilla.getId() != null);
        } else {
            getChildren().forEach(c -> {
                if (c instanceof com.vaadin.flow.component.HasValue) {
                    ((com.vaadin.flow.component.HasValue<?, ?>) c).clear();
                }
            });
            refreshGridNecesidades();
        }
    }

    // --- Clases de Eventos ---
    public static abstract class PlantillaVueloFormEvent extends ComponentEvent<PlantillaVueloForm> {
        private final PlantillaVuelo plantilla;
        protected PlantillaVueloFormEvent(PlantillaVueloForm source, PlantillaVuelo plantilla) {
            super(source, false);
            this.plantilla = plantilla;
        }
        public PlantillaVuelo getPlantilla() {
            return plantilla;
        }
    }

    public static class SaveEvent extends PlantillaVueloFormEvent {
        SaveEvent(PlantillaVueloForm source, PlantillaVuelo plantilla) {
            super(source, plantilla);
        }
    }

    public static class DeleteEvent extends PlantillaVueloFormEvent {
        DeleteEvent(PlantillaVueloForm source, PlantillaVuelo plantilla) {
            super(source, plantilla);
        }
    }

    public static class CloseEvent extends PlantillaVueloFormEvent {
        CloseEvent(PlantillaVueloForm source) {
            super(source, null);
        }
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }
}