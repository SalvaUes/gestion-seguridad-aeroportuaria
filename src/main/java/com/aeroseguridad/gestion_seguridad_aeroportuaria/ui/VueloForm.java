package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.*;
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
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class VueloForm extends FormLayout {

    // Campos
    TextField numeroVuelo = new TextField("Número Vuelo");
    ComboBox<Aerolinea> aerolinea = new ComboBox<>("Aerolínea");
    TextField origen = new TextField("Origen");
    TextField destino = new TextField("Destino");
    DateTimePicker fechaHoraSalida = new DateTimePicker("Salida Programada");
    DateTimePicker fechaHoraLlegada = new DateTimePicker("Llegada Programada");
    ComboBox<EstadoVuelo> estado = new ComboBox<>("Estado");
    ComboBox<TipoOperacionVuelo> tipoOperacion = new ComboBox<>("Tipo Operación");
    DateTimePicker finOperacionSeguridad = new DateTimePicker("Fin Op. Seguridad"); // Puede ser null

    // Botones
    Button save = new Button("Guardar");
    Button delete = new Button("Eliminar");
    Button cancel = new Button("Cancelar");

    // Binder
    Binder<Vuelo> binder = new BeanValidationBinder<>(Vuelo.class);
    private Vuelo vueloActual;

    public VueloForm(List<Aerolinea> aerolineas) {
        addClassName("vuelo-form");

        // Configuración Campos
        numeroVuelo.setRequiredIndicatorVisible(true); // Usa @NotBlank
        aerolinea.setRequiredIndicatorVisible(true); // Usa @NotNull
        origen.setRequiredIndicatorVisible(true); // Usa @NotBlank
        destino.setRequiredIndicatorVisible(true); // Usa @NotBlank
        fechaHoraSalida.setRequiredIndicatorVisible(true); // Usa @NotNull
        fechaHoraLlegada.setRequiredIndicatorVisible(true); // Usa @NotNull
        estado.setRequiredIndicatorVisible(true); // Usa @NotNull
        tipoOperacion.setRequiredIndicatorVisible(true); // Usa @NotNull
        // finOperacionSeguridad no es requerido

        aerolinea.setItems(aerolineas);
        aerolinea.setItemLabelGenerator(a -> a != null ? a.getNombre() : "");
        aerolinea.setRequired(true); // Redundante con @NotNull

        estado.setItems(EstadoVuelo.values());
        estado.setRequired(true);

        tipoOperacion.setItems(TipoOperacionVuelo.values());
        tipoOperacion.setRequired(true);

        fechaHoraSalida.setStep(Duration.ofMinutes(5));
        fechaHoraLlegada.setStep(Duration.ofMinutes(5));
        finOperacionSeguridad.setStep(Duration.ofMinutes(5));

        // Enlaza campos (Usará anotaciones @NotNull, @NotBlank de Vuelo)
        binder.bindInstanceFields(this);

        // Listener para actualizar botón Guardar (simplificado)
        binder.addValueChangeListener(event -> updateSaveButtonState());

        add(numeroVuelo, aerolinea, origen, destino, fechaHoraSalida, fechaHoraLlegada,
            estado, tipoOperacion, finOperacionSeguridad, createButtonsLayout());
    }

    private Component createButtonsLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        save.addClickShortcut(Key.ENTER);
        cancel.addClickShortcut(Key.ESCAPE);

        save.addClickListener(event -> validateAndSave());
        delete.addClickListener(event -> fireEvent(new DeleteEvent(this, vueloActual)));
        cancel.addClickListener(event -> fireEvent(new CloseEvent(this)));

        save.setEnabled(false); // Se habilita en listener/setVuelo
        delete.setEnabled(false); // Se habilita en setVuelo

        return new HorizontalLayout(save, delete, cancel);
    }

     private void validateAndSave() {
        try {
            // writeBean validará usando las anotaciones de la entidad Vuelo (@NotNull, etc)
            binder.writeBean(vueloActual);
            // Si llega aquí, los campos individuales son válidos. Dispara evento.
            fireEvent(new SaveEvent(this, vueloActual));
        } catch (ValidationException e) {
             // Intentar mostrar mensaje de bean validation si existe
             String errorMsg = "Formulario inválido. Revise los campos marcados.";
             if (!binder.isValid() && !binder.validate().getBeanValidationErrors().isEmpty()) {
                 errorMsg = binder.validate().getBeanValidationErrors().get(0).getErrorMessage();
             }
             Notification.show(errorMsg, 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

     public void setVuelo(Vuelo vuelo) {
        this.vueloActual = vuelo;
        binder.readBean(vuelo);
        boolean isExisting = vuelo != null && vuelo.getIdVuelo() != null;
        delete.setEnabled(isExisting);

        // Si es nuevo, poner estado PROGRAMADO por defecto?
        if (!isExisting && estado.getValue() == null) {
            estado.setValue(EstadoVuelo.PROGRAMADO);
            // Forzar re-evaluación del botón guardar?
             updateSaveButtonState();
        }

        updateSaveButtonState(); // Llama a la lógica actualizada
    }

    // --- MÉTODO Actualizar estado botón Guardar (simplificado) ---
    private void updateSaveButtonState() {
        // Habilita si hay bean y el binder lo considera válido (según @NotNull, @NotBlank)
        save.setEnabled(binder.getBean() != null && binder.isValid());
    }
    // --- FIN MÉTODO ---


    // --- Eventos Personalizados ---
     public static abstract class VueloFormEvent extends ComponentEvent<VueloForm> {
         private Vuelo vuelo;
         protected VueloFormEvent(VueloForm source, Vuelo vuelo) { super(source, false); this.vuelo = vuelo; }
         public Vuelo getVuelo() { return vuelo; }
     }
     public static class SaveEvent extends VueloFormEvent { SaveEvent(VueloForm source, Vuelo vuelo) { super(source, vuelo); } }
     public static class DeleteEvent extends VueloFormEvent { DeleteEvent(VueloForm source, Vuelo vuelo) { super(source, vuelo); } }
     public static class CloseEvent extends VueloFormEvent { CloseEvent(VueloForm source) { super(source, null); } }
     public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener) { return getEventBus().addListener(eventType, listener); }

}