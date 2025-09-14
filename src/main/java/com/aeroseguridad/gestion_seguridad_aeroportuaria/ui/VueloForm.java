package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Aerolinea;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.EstadoVuelo;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.TipoOperacionVuelo;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Vuelo;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;

import java.time.LocalDateTime;
import java.util.List;

public class VueloForm extends FormLayout {

    // --- CAMPOS DEL FORMULARIO ---
    private TextField numeroVuelo = new TextField("Número de Vuelo");
    private ComboBox<Aerolinea> aerolinea = new ComboBox<>("Aerolínea");
    private ComboBox<TipoOperacionVuelo> tipoOperacion = new ComboBox<>("Tipo de Operación");
    private DateTimePicker fechaHoraSalida = new DateTimePicker("Fecha y Hora de Salida");
    private DateTimePicker fechaHoraLlegada = new DateTimePicker("Fecha y Hora de Llegada");
    private TextField origen = new TextField("Origen");
    private TextField destino = new TextField("Destino");
    private ComboBox<EstadoVuelo> estadoVuelo = new ComboBox<>("Estado del Vuelo");

    // --- BOTONES ---
    private Button save = new Button("Guardar");
    private Button delete = new Button("Eliminar");
    private Button cancel = new Button("Cancelar");
    private Button cancelarEdicion = new Button("Cerrar");

    // --- BINDER ---
    private Binder<Vuelo> binder = new BeanValidationBinder<>(Vuelo.class);
    private Vuelo vueloActual;

    public VueloForm(List<Aerolinea> aerolineas) {
        addClassName("vuelo-form");
        initLayout(aerolineas);
    }

    private void initLayout(List<Aerolinea> aerolineas) {
        configureFields(aerolineas);
        configureBinder();
        configureLayout();
    }

    private void configureFields(List<Aerolinea> aerolineas) {
        aerolinea.setItems(aerolineas);
        aerolinea.setItemLabelGenerator(Aerolinea::getNombre);
        tipoOperacion.setItems(TipoOperacionVuelo.values());
        estadoVuelo.setItems(EstadoVuelo.values());
    }

    private void configureBinder() {
        binder.forField(numeroVuelo).asRequired("El número de vuelo es obligatorio.")
                .bind(Vuelo::getNumeroVuelo, Vuelo::setNumeroVuelo);
        binder.forField(aerolinea).asRequired("Debe seleccionar una aerolínea.")
                .bind(Vuelo::getAerolinea, Vuelo::setAerolinea);
        binder.forField(tipoOperacion).asRequired("El tipo de operación es obligatorio.")
                .bind(Vuelo::getTipoOperacion, Vuelo::setTipoOperacion);
        binder.forField(fechaHoraSalida).asRequired("La fecha de salida es obligatoria.")
                .bind(Vuelo::getFechaHoraSalida, Vuelo::setFechaHoraSalida);
        binder.forField(fechaHoraLlegada).asRequired("La fecha de llegada es obligatoria.")
                .withValidator(llegada -> {
                    LocalDateTime salida = fechaHoraSalida.getValue();
                    return salida != null && llegada != null && llegada.isAfter(salida);
                }, "La llegada debe ser posterior a la salida.")
                .bind(Vuelo::getFechaHoraLlegada, Vuelo::setFechaHoraLlegada);
        binder.forField(origen).asRequired("El origen es obligatorio.")
                .bind(Vuelo::getOrigen, Vuelo::setOrigen);
        binder.forField(destino).asRequired("El destino es obligatorio.")
                .bind(Vuelo::getDestino, Vuelo::setDestino);
        binder.forField(estadoVuelo).asRequired("El estado del vuelo es obligatorio.")
                .bind(Vuelo::getEstado, Vuelo::setEstado);

        binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));
    }

    private void configureLayout() {
        add(
                numeroVuelo,
                aerolinea,
                tipoOperacion,
                fechaHoraSalida,
                fechaHoraLlegada,
                origen,
                destino,
                estadoVuelo,
                createButtonsLayout()
        );
    }

    private Component createButtonsLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelarEdicion.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        save.addClickShortcut(Key.ENTER);
        cancel.addClickShortcut(Key.ESCAPE);

        save.addClickListener(event -> validateAndSave());
        delete.addClickListener(event -> fireEvent(new DeleteEvent(this, vueloActual)));
        cancel.addClickListener(event -> fireEvent(new CloseEvent(this)));
        cancelarEdicion.addClickListener(event -> fireEvent(new CloseEvent(this)));

        return new HorizontalLayout(save, delete, cancel, cancelarEdicion);
    }

    private void validateAndSave() {
        try {
            binder.writeBean(vueloActual);
            fireEvent(new SaveEvent(this, vueloActual));
        } catch (ValidationException e) {
            Notification.show("Por favor, corrija los errores en el formulario.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    public void setVuelo(Vuelo vuelo) {
        this.vueloActual = vuelo;
        binder.readBean(vuelo);

        boolean isNew = vuelo == null || vuelo.getIdVuelo() == null;
        delete.setVisible(!isNew);
        cancelarEdicion.setVisible(!isNew);
        cancel.setVisible(isNew);

        if (vuelo != null) {
            numeroVuelo.focus();
        }
    }

    // --- Eventos ---
    public static abstract class VueloFormEvent extends ComponentEvent<VueloForm> {
        private Vuelo vuelo;

        protected VueloFormEvent(VueloForm source, Vuelo vuelo) {
            super(source, false);
            this.vuelo = vuelo;
        }

        public Vuelo getVuelo() {
            return vuelo;
        }
    }

    public static class SaveEvent extends VueloFormEvent {
        SaveEvent(VueloForm source, Vuelo vuelo) {
            super(source, vuelo);
        }
    }

    public static class DeleteEvent extends VueloFormEvent {
        DeleteEvent(VueloForm source, Vuelo vuelo) {
            super(source, vuelo);
        }
    }

    public static class CloseEvent extends VueloFormEvent {
        CloseEvent(VueloForm source) {
            super(source, null);
        }
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }
}