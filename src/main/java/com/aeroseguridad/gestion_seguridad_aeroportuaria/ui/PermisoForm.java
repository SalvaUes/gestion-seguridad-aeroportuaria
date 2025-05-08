package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.*;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue; // Asegúrate que esté importado
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;

import java.time.Duration;
import java.util.List;
import java.util.Optional; // Asegúrate que esté importado

public class PermisoForm extends FormLayout {

    // Campos
    ComboBox<Agente> agente = new ComboBox<>("Agente");
    DateTimePicker fechaInicio = new DateTimePicker("Inicio Permiso");
    DateTimePicker fechaFin = new DateTimePicker("Fin Permiso");
    ComboBox<TipoPermiso> tipoPermiso = new ComboBox<>("Tipo Permiso");
    TextArea motivo = new TextArea("Motivo");
    TextField rutaDocumento = new TextField("Documento Adjunto (Ruta)");
    ComboBox<EstadoSolicitudPermiso> estadoSolicitud = new ComboBox<>("Estado Solicitud");

    // Botones
    Button save = new Button("Guardar/Solicitar");
    Button delete = new Button("Eliminar");
    Button cancel = new Button("Cancelar");

    // Binder
    Binder<Permiso> binder = new BeanValidationBinder<>(Permiso.class);
    private Permiso permisoActual; // Referencia local al bean

    public PermisoForm(List<Agente> agentes) {
        addClassName("permiso-form");

        // Configura ComboBoxes
        agente.setItems(agentes);
        agente.setItemLabelGenerator(ag -> ag != null ? ag.getNombre() + " " + ag.getApellido() : "");
        tipoPermiso.setItems(TipoPermiso.values());
        estadoSolicitud.setItems(EstadoSolicitudPermiso.values());

        // Configura DateTimePickers
        fechaInicio.setStep(Duration.ofMinutes(30));
        fechaFin.setStep(Duration.ofMinutes(30));

        // --- MANUAL BINDING (Más explícito) ---
        binder.forField(agente)
              .asRequired("Debe seleccionar un agente.")
              .bind(Permiso::getAgente, Permiso::setAgente);
        binder.forField(fechaInicio)
              .asRequired("La fecha/hora de inicio es obligatoria.")
              .bind(Permiso::getFechaInicio, Permiso::setFechaInicio);
        binder.forField(fechaFin)
              .asRequired("La fecha/hora de fin es obligatoria.")
              .bind(Permiso::getFechaFin, Permiso::setFechaFin);
        binder.forField(tipoPermiso)
              .asRequired("El tipo de permiso es obligatorio.")
              .bind(Permiso::getTipoPermiso, Permiso::setTipoPermiso);
        binder.forField(motivo)
              .asRequired("El motivo no puede estar vacío.")
              .bind(Permiso::getMotivo, Permiso::setMotivo);
        binder.forField(rutaDocumento)
              .bind(Permiso::getRutaDocumento, Permiso::setRutaDocumento);
        binder.forField(estadoSolicitud)
              .asRequired("El estado es obligatorio.")
              .bind(Permiso::getEstadoSolicitud, Permiso::setEstadoSolicitud);
        // --- FIN MANUAL BINDING ---

        // --- ELIMINADO: ValueChangeListener para botón save ---

        add(agente, fechaInicio, fechaFin, tipoPermiso, motivo, rutaDocumento, estadoSolicitud, createButtonsLayout());
    }

     private Component createButtonsLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        save.addClickShortcut(Key.ENTER);
        cancel.addClickShortcut(Key.ESCAPE);

        save.addClickListener(event -> validateAndSave());
        delete.addClickListener(event -> fireEvent(new DeleteEvent(this, permisoActual)));
        cancel.addClickListener(event -> fireEvent(new CloseEvent(this)));

        // Botones deshabilitados por defecto, se habilitan en setPermiso
        save.setEnabled(false);
        delete.setEnabled(false);

        return new HorizontalLayout(save, delete, cancel);
     }

     private void validateAndSave() {
         try {
            // Asegurarse que hay un bean asociado
            if (permisoActual == null) {
                 Notification.show("Error: No hay datos de permiso para guardar.", 3000, Notification.Position.MIDDLE)
                           .addThemeVariants(NotificationVariant.LUMO_ERROR);
                 return;
            }

            // Validar explícitamente ANTES de escribir
            BinderValidationStatus<Permiso> status = binder.validate();
            if (status.hasErrors()) {
                 String errorMsg = "Formulario inválido. ";
                 // Intenta obtener el mensaje de error más específico
                 Optional<String> beanError = status.getBeanValidationErrors().stream()
                     .map(err -> err.getErrorMessage())
                     .findFirst();
                 Optional<String> fieldError = status.getFieldValidationErrors().stream()
                     .map(err -> err.getMessage().orElse("Revise campos marcados"))
                     .findFirst();

                 if(beanError.isPresent()) errorMsg = beanError.get(); // Prioriza error de bean (@AssertTrue)
                 else if (fieldError.isPresent()) errorMsg = fieldError.get(); // Muestra primer error de campo

                 Notification.show(errorMsg, 3000, Notification.Position.MIDDLE)
                           .addThemeVariants(NotificationVariant.LUMO_ERROR);
                 return; // No continuar si hay errores
            }

            // Si la validación pasa, escribir el bean y disparar evento
            binder.writeBean(permisoActual);
            fireEvent(new SaveEvent(this, permisoActual));

        } catch (ValidationException e) {
            // Esta excepción podría ocurrir si writeBean fallara por otra razón
            Notification.show("Error inesperado al validar/guardar.", 3000, Notification.Position.MIDDLE)
                      .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            Notification.show("Error inesperado: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                      .addThemeVariants(NotificationVariant.LUMO_ERROR);
            e.printStackTrace(); // Loguear error inesperado
        }
    }

     // --- MÉTODO setPermiso REVISADO Y SIMPLIFICADO ---
     public void setPermiso(Permiso permiso) {
        this.permisoActual = permiso; // Guarda la referencia local
        binder.readBean(permiso); // Carga los datos en los campos del formulario

        // Determina el estado basado en el objeto permiso recibido
        boolean isExisting = permiso != null && permiso.getIdPermiso() != null;
        boolean isFinalState = false; // Asume que no es final por defecto

        if (isExisting) {
            // Verifica si el estado es final (Aprobado o Rechazado)
            isFinalState = (permiso.getEstadoSolicitud() == EstadoSolicitudPermiso.APROBADO ||
                            permiso.getEstadoSolicitud() == EstadoSolicitudPermiso.RECHAZADO);

            // Habilita el botón Eliminar SOLO si es existente Y está en estado SOLICITADO
            delete.setEnabled(permiso.getEstadoSolicitud() == EstadoSolicitudPermiso.SOLICITADO);

            // Configura campos como solo lectura si el estado es final
            agente.setReadOnly(isFinalState);
            fechaInicio.setReadOnly(isFinalState);
            fechaFin.setReadOnly(isFinalState);
            tipoPermiso.setReadOnly(isFinalState);
            motivo.setReadOnly(isFinalState);
            rutaDocumento.setReadOnly(isFinalState);
            estadoSolicitud.setReadOnly(isFinalState); // El estado también es solo lectura si es final
            estadoSolicitud.setEnabled(!isFinalState); // Habilita cambio de estado solo si NO es final

        } else if (permiso != null) { // Es un permiso nuevo (no nulo, pero sin ID)
            delete.setEnabled(false); // No se puede eliminar uno nuevo
            // Asegura que todos los campos sean editables
            agente.setReadOnly(false);
            fechaInicio.setReadOnly(false);
            fechaFin.setReadOnly(false);
            tipoPermiso.setReadOnly(false);
            motivo.setReadOnly(false);
            rutaDocumento.setReadOnly(false);
            estadoSolicitud.setReadOnly(false); // Asegura que sea editable antes de setear valor
            // Establece el estado por defecto a SOLICITADO y deshabilita su edición
            estadoSolicitud.setValue(EstadoSolicitudPermiso.SOLICITADO);
            estadoSolicitud.setEnabled(false);

        } else { // Se está cerrando el editor (permiso es null)
            delete.setEnabled(false);
            // Asegura que todos los campos vuelvan a ser editables
            agente.setReadOnly(false);
            fechaInicio.setReadOnly(false);
            fechaFin.setReadOnly(false);
            tipoPermiso.setReadOnly(false);
            motivo.setReadOnly(false);
            rutaDocumento.setReadOnly(false);
            estadoSolicitud.setEnabled(true); // Habilita el campo de estado
            estadoSolicitud.setReadOnly(false); // Quita solo lectura
        }

        // Habilita el botón Guardar SOLAMENTE si hay un permiso cargado (nuevo o existente)
        // Y si dicho permiso NO está en un estado final (Aprobado/Rechazado)
        save.setEnabled(permiso != null && !isFinalState);
    }
    // --- FIN MÉTODO setPermiso ---

    // --- Eventos Personalizados (Sin cambios) ---
     public static abstract class PermisoFormEvent extends ComponentEvent<PermisoForm> {
         private Permiso permiso;
         protected PermisoFormEvent(PermisoForm source, Permiso permiso) { super(source, false); this.permiso = permiso; }
         public Permiso getPermiso() { return permiso; }
     }
     public static class SaveEvent extends PermisoFormEvent { SaveEvent(PermisoForm source, Permiso permiso) { super(source, permiso); } }
     public static class DeleteEvent extends PermisoFormEvent { DeleteEvent(PermisoForm source, Permiso permiso) { super(source, permiso); } }
     public static class CloseEvent extends PermisoFormEvent { CloseEvent(PermisoForm source) { super(source, null); } }
     public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener) {
         return getEventBus().addListener(eventType, listener);
     }
}