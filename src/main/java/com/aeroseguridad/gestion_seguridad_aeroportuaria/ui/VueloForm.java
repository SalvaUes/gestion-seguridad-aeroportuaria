package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.*;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue; // Importar
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
import com.vaadin.flow.shared.Registration;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;


import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    DateTimePicker finOperacionSeguridad = new DateTimePicker("Fin Op. Seguridad");

    // Botones
    Button save = new Button("Guardar");
    Button delete = new Button("Eliminar");
    Button cancel = new Button("Cancelar");

    // Sin Binder para set/get
    private Vuelo vueloActual;

    // Validador Manual
    private final Validator validator;

    public VueloForm(List<Aerolinea> aerolineas) {
        addClassName("vuelo-form");

        // Inicializar validador
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        // Configuración Campos
        numeroVuelo.setRequiredIndicatorVisible(true);
        aerolinea.setRequiredIndicatorVisible(true);
        origen.setRequiredIndicatorVisible(true);
        destino.setRequiredIndicatorVisible(true);
        fechaHoraSalida.setRequiredIndicatorVisible(true);
        fechaHoraLlegada.setRequiredIndicatorVisible(true);
        estado.setRequiredIndicatorVisible(true);
        tipoOperacion.setRequiredIndicatorVisible(true);

        aerolinea.setItems(aerolineas);
        aerolinea.setItemLabelGenerator(a -> a != null ? a.getNombre() : "");
        estado.setItems(EstadoVuelo.values());
        tipoOperacion.setItems(TipoOperacionVuelo.values());
        fechaHoraSalida.setStep(Duration.ofMinutes(5));
        fechaHoraLlegada.setStep(Duration.ofMinutes(5));
        finOperacionSeguridad.setStep(Duration.ofMinutes(5));

        // --- Listener para habilitar botón save ---
        // Añadir listener a cada campo que, al cambiar, habilite el botón save
        // si hay un objeto vueloActual presente.
        HasValue.ValueChangeListener enableSaveListener = e -> {
            if (save != null) { // Chequeo de seguridad
                save.setEnabled(vueloActual != null);
            }
        };

        numeroVuelo.addValueChangeListener(enableSaveListener);
        aerolinea.addValueChangeListener(enableSaveListener);
        origen.addValueChangeListener(enableSaveListener);
        destino.addValueChangeListener(enableSaveListener);
        fechaHoraSalida.addValueChangeListener(enableSaveListener);
        fechaHoraLlegada.addValueChangeListener(enableSaveListener);
        estado.addValueChangeListener(enableSaveListener);
        tipoOperacion.addValueChangeListener(enableSaveListener);
        finOperacionSeguridad.addValueChangeListener(enableSaveListener); // También en campos opcionales
        // --- FIN Listener ---


        add(numeroVuelo, aerolinea, origen, destino, fechaHoraSalida, fechaHoraLlegada,
            estado, tipoOperacion, finOperacionSeguridad, createButtonsLayout());
    }

    private Component createButtonsLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        save.addClickShortcut(Key.ENTER);
        cancel.addClickShortcut(Key.ESCAPE);

        save.addClickListener(event -> validateAndSaveManually()); // Llama validación manual
        delete.addClickListener(event -> fireEvent(new DeleteEvent(this, vueloActual)));
        cancel.addClickListener(event -> fireEvent(new CloseEvent(this)));

        save.setEnabled(false); // Deshabilitado inicialmente
        delete.setEnabled(false);

        return new HorizontalLayout(save, delete, cancel);
    }

     // --- validateAndSave MANUALMENTE ---
     private void validateAndSaveManually() {
         if (vueloActual == null) {
             Notification.show("Error: No hay datos de vuelo para guardar.", 3000, Notification.Position.MIDDLE)
                       .addThemeVariants(NotificationVariant.LUMO_ERROR);
             return;
         }
         try {
             // 1. Actualizar el bean manualmente desde los campos
             vueloActual.setNumeroVuelo(numeroVuelo.getValue());
             vueloActual.setAerolinea(aerolinea.getValue());
             vueloActual.setOrigen(origen.getValue());
             vueloActual.setDestino(destino.getValue());
             vueloActual.setFechaHoraSalida(fechaHoraSalida.getValue());
             vueloActual.setFechaHoraLlegada(fechaHoraLlegada.getValue());
             vueloActual.setEstado(estado.getValue());
             vueloActual.setTipoOperacion(tipoOperacion.getValue());
             vueloActual.setFinOperacionSeguridad(finOperacionSeguridad.getValue());

             // 2. Validar el bean manualmente usando Bean Validation API
             Set<ConstraintViolation<Vuelo>> violations = validator.validate(vueloActual);

             if (!violations.isEmpty()) {
                 // Mostrar el primer mensaje de error encontrado
                 String errorMsg = violations.stream()
                                     .map(ConstraintViolation::getMessage)
                                     .collect(Collectors.joining("; "));
                 Notification.show("Error de validación: " + errorMsg, 3000, Notification.Position.MIDDLE)
                           .addThemeVariants(NotificationVariant.LUMO_ERROR);
                 return; // No continuar si hay errores
             }

             // 3. Si es válido, disparar el evento Save
             fireEvent(new SaveEvent(this, vueloActual));

        } catch (Exception e) {
            Notification.show("Error inesperado al preparar/validar datos: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                      .addThemeVariants(NotificationVariant.LUMO_ERROR);
            e.printStackTrace();
        }
    }
    // --- FIN validateAndSave ---

     // --- setVuelo MODIFICADO para poblar/limpiar campos manualmente ---
     public void setVuelo(Vuelo vuelo) {
        this.vueloActual = vuelo; // Guarda la referencia

        // Poblar o limpiar campos manualmente
        if (vuelo != null) {
             numeroVuelo.setValue(vuelo.getNumeroVuelo() != null ? vuelo.getNumeroVuelo() : "");
             aerolinea.setValue(vuelo.getAerolinea());
             origen.setValue(vuelo.getOrigen() != null ? vuelo.getOrigen() : "");
             destino.setValue(vuelo.getDestino() != null ? vuelo.getDestino() : "");
             fechaHoraSalida.setValue(vuelo.getFechaHoraSalida());
             fechaHoraLlegada.setValue(vuelo.getFechaHoraLlegada());
             estado.setValue(vuelo.getEstado() != null ? vuelo.getEstado() : (vuelo.getIdVuelo() == null ? EstadoVuelo.PROGRAMADO : null));
             tipoOperacion.setValue(vuelo.getTipoOperacion());
             finOperacionSeguridad.setValue(vuelo.getFinOperacionSeguridad());

             // Habilitar Guardar porque hay un bean (la validación será al hacer clic)
             save.setEnabled(true);
             // Habilitar Eliminar solo si es existente
             delete.setEnabled(vuelo.getIdVuelo() != null);

        } else { // Limpiar campos si vuelo es null (al cerrar editor)
             numeroVuelo.clear();
             aerolinea.clear();
             origen.clear();
             destino.clear();
             fechaHoraSalida.clear();
             fechaHoraLlegada.clear();
             estado.clear();
             tipoOperacion.clear();
             finOperacionSeguridad.clear();
             // Deshabilitar botones
             save.setEnabled(false);
             delete.setEnabled(false);
        }
    }
    // --- FIN setVuelo ---


    // --- Eventos Personalizados (Sin cambios) ---
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