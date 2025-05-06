package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.*;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public class TurnoForm extends FormLayout {

    // Campos
    ComboBox<Agente> agente = new ComboBox<>("Agente");
    DateTimePicker inicioTurno = new DateTimePicker("Inicio Turno");
    DateTimePicker finTurno = new DateTimePicker("Fin Turno");
    ComboBox<TipoTurno> tipoTurno = new ComboBox<>("Tipo Turno");
    ComboBox<EstadoTurno> estadoTurno = new ComboBox<>("Estado Turno");

    // Botones
    Button save = new Button("Guardar");
    Button delete = new Button("Eliminar");
    Button cancel = new Button("Cancelar");

    // Binder
    Binder<Turno> binder = new BeanValidationBinder<>(Turno.class);
    private Turno turnoActual; // Local reference to the bean

    public TurnoForm(List<Agente> agentes) {
        addClassName("turno-form");

        // Configuración Campos
        agente.setRequiredIndicatorVisible(true);
        inicioTurno.setRequiredIndicatorVisible(true);
        finTurno.setRequiredIndicatorVisible(true);
        tipoTurno.setRequiredIndicatorVisible(true);
        estadoTurno.setRequiredIndicatorVisible(true);

        agente.setItems(agentes);
        agente.setItemLabelGenerator(ag -> ag != null ? ag.getNombre() + " " + ag.getApellido() : "");
        agente.setRequired(true);

        tipoTurno.setItems(TipoTurno.values());
        tipoTurno.setRequired(true);

        estadoTurno.setItems(EstadoTurno.values());
        estadoTurno.setRequired(true);

        inicioTurno.setStep(Duration.ofMinutes(30));
        finTurno.setStep(Duration.ofMinutes(30));

        // Enlace campos
        binder.bindInstanceFields(this);

        // --- ELIMINADO: Listener que actualizaba el botón ---
        // binder.addValueChangeListener(event -> updateSaveButtonState());
        // --- FIN ELIMINADO ---

        add(agente, inicioTurno, finTurno, tipoTurno, estadoTurno, createButtonsLayout());
        // System.out.println("TurnoForm constructor finished."); // Debug opcional
    }

     private Component createButtonsLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        save.addClickShortcut(Key.ENTER);
        cancel.addClickShortcut(Key.ESCAPE);

        save.addClickListener(event -> validateAndSave());
        delete.addClickListener(event -> fireEvent(new DeleteEvent(this, turnoActual)));
        cancel.addClickListener(event -> fireEvent(new CloseEvent(this)));

        // Los botones se habilitan/deshabilitan en setTurno
        save.setEnabled(false);
        delete.setEnabled(false);

        return new HorizontalLayout(save, delete, cancel);
     }

     private void validateAndSave() {
        try {
            if (turnoActual == null) {
                 Notification.show("Error: No hay datos de turno para guardar.", 3000, Notification.Position.MIDDLE)
                           .addThemeVariants(NotificationVariant.LUMO_ERROR);
                 return;
            }
            // Validar explícitamente ANTES de escribir
            BinderValidationStatus<Turno> status = binder.validate();
            if (status.hasErrors()) {
                 // Si hay errores (de campo o de bean como AssertTrue), mostrar notificación
                 String errorMsg = "Formulario inválido. ";
                 Optional<String> beanError = status.getBeanValidationErrors().stream()
                     .map(err -> err.getErrorMessage())
                     .findFirst();
                 Optional<String> fieldError = status.getFieldValidationErrors().stream()
                     .map(err -> err.getMessage().orElse("Revise campos marcados"))
                     .findFirst();

                 if(beanError.isPresent()) {
                     errorMsg = beanError.get(); // Prioriza error de bean (AssertTrue)
                 } else if (fieldError.isPresent()) {
                     errorMsg = fieldError.get(); // Muestra primer error de campo
                 }
                 Notification.show(errorMsg, 3000, Notification.Position.MIDDLE)
                           .addThemeVariants(NotificationVariant.LUMO_ERROR);
                 return; // No continuar si hay errores
            }

            // Si la validación pasa, escribir el bean y disparar evento
            binder.writeBean(turnoActual);
            fireEvent(new SaveEvent(this, turnoActual));

        } catch (ValidationException e) {
            // Esta excepción podría ocurrir si writeBean fallara por otra razón, aunque ya validamos
            Notification.show("Error inesperado al validar/guardar.", 3000, Notification.Position.MIDDLE)
                      .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            Notification.show("Error inesperado: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                      .addThemeVariants(NotificationVariant.LUMO_ERROR);
            e.printStackTrace();
        }
      }

     // --- MÉTODO setTurno MODIFICADO ---
     public void setTurno(Turno turno) {
         this.turnoActual = turno;
         binder.readBean(turno); // Lee datos en los campos

         // Habilitar/Deshabilitar botones basado en si hay un turno cargado
         boolean beanPresent = turno != null;
         save.setEnabled(beanPresent); // Habilita Guardar si hay un bean (nuevo o existente)

         boolean isExisting = beanPresent && turno.getIdTurno() != null;
         delete.setEnabled(isExisting); // Habilita Eliminar solo si es existente

         // Lógica para estado por defecto en nuevos turnos
         if (!isExisting && beanPresent && estadoTurno.getValue() == null) {
             estadoTurno.setValue(EstadoTurno.PROGRAMADO);
         }
         // Puedes añadir más lógica aquí si necesitas deshabilitar campos específicos
         // por ejemplo, si el turno ya está COMPLETADO.
         // agente.setReadOnly(isExisting); // Ejemplo: No permitir cambiar agente al editar
     }
     // --- FIN MÉTODO setTurno ---

    // --- ELIMINADO: Ya no se necesita updateSaveButtonState ---
    // private void updateSaveButtonState() { ... }
    // --- FIN ELIMINADO ---

    // --- Eventos Personalizados (Sin cambios) ---
     public static abstract class TurnoFormEvent extends ComponentEvent<TurnoForm> {
         private Turno turno;
         protected TurnoFormEvent(TurnoForm source, Turno turno) { super(source, false); this.turno = turno; }
         public Turno getTurno() { return turno; }
     }
     public static class SaveEvent extends TurnoFormEvent { SaveEvent(TurnoForm source, Turno turno) { super(source, turno); } }
     public static class DeleteEvent extends TurnoFormEvent { DeleteEvent(TurnoForm source, Turno turno) { super(source, turno); } }
     public static class CloseEvent extends TurnoFormEvent { CloseEvent(TurnoForm source) { super(source, null); } }
     public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener) { return getEventBus().addListener(eventType, listener); }
}