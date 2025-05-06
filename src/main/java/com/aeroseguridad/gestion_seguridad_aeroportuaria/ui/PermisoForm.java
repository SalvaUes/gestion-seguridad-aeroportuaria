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
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;

import java.time.Duration; // Para step
import java.util.List;

public class PermisoForm extends FormLayout {

    // Campos
    ComboBox<Agente> agente = new ComboBox<>("Agente");
    DateTimePicker fechaInicio = new DateTimePicker("Inicio Permiso");
    DateTimePicker fechaFin = new DateTimePicker("Fin Permiso");
    ComboBox<TipoPermiso> tipoPermiso = new ComboBox<>("Tipo Permiso");
    TextArea motivo = new TextArea("Motivo");
    TextField rutaDocumento = new TextField("Documento Adjunto (Ruta)"); // Simplificado
    ComboBox<EstadoSolicitudPermiso> estadoSolicitud = new ComboBox<>("Estado Solicitud");

    // Botones
    Button save = new Button("Guardar/Solicitar");
    Button delete = new Button("Eliminar");
    Button cancel = new Button("Cancelar");

    // Binder
    Binder<Permiso> binder = new BeanValidationBinder<>(Permiso.class);
    private Permiso permisoActual;

    public PermisoForm(List<Agente> agentes) {
        addClassName("permiso-form");

        // Configura ComboBoxes e Indicadores
        agente.setItems(agentes);
        agente.setItemLabelGenerator(ag -> ag != null ? ag.getNombre() + " " + ag.getApellido() : "");
        agente.setRequiredIndicatorVisible(true); // Usa @NotNull de entidad

        tipoPermiso.setItems(TipoPermiso.values());
        tipoPermiso.setRequiredIndicatorVisible(true); // Usa @NotNull de entidad

        estadoSolicitud.setItems(EstadoSolicitudPermiso.values());
        estadoSolicitud.setRequiredIndicatorVisible(true); // Usa @NotNull de entidad

        fechaInicio.setRequiredIndicatorVisible(true); // Usa @NotNull de entidad
        fechaInicio.setStep(Duration.ofMinutes(30));
        fechaFin.setRequiredIndicatorVisible(true); // Usa @NotNull de entidad
        fechaFin.setStep(Duration.ofMinutes(30));
        motivo.setRequiredIndicatorVisible(true); // Usa @NotBlank de entidad

        // Enlaza campos (usará @NotNull, @NotBlank, @AssertTrue de Permiso)
        binder.bindInstanceFields(this);

        // Listener para actualizar botón Guardar (simplificado)
        binder.addValueChangeListener(e -> updateSaveButtonState());

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

        save.setEnabled(false); // Se habilita en setPermiso / listener
        delete.setEnabled(false); // Se habilita en setPermiso si es existente

        return new HorizontalLayout(save, delete, cancel);
     }

     private void validateAndSave() {
        try {
            // writeBean ejecuta las validaciones (@NotNull, @AssertTrue, etc.)
            binder.writeBean(permisoActual);
            // Si llega aquí, es válido. Dispara el evento Save.
            fireEvent(new SaveEvent(this, permisoActual));
        } catch (ValidationException e) {
             // Si writeBean falla, intentar mostrar mensaje de bean validation si existe
             String errorMsg = "Formulario inválido. Revise los campos marcados.";
             if (!binder.isValid() && !binder.validate().getBeanValidationErrors().isEmpty()) {
                 errorMsg = binder.validate().getBeanValidationErrors().get(0).getErrorMessage();
             }
             Notification.show(errorMsg, 3000, Notification.Position.MIDDLE)
                       .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

     public void setPermiso(Permiso permiso) {
        this.permisoActual = permiso;
        binder.readBean(permiso);
        boolean isExisting = permiso != null && permiso.getIdPermiso() != null;
        // Habilita delete solo si existe Y está en estado SOLICITADO? (Regla de negocio opcional)
        delete.setEnabled(isExisting && permiso.getEstadoSolicitud() == EstadoSolicitudPermiso.SOLICITADO);

        // Habilita/Deshabilita edición de estado
        // Permitir cambiar estado solo si es existente?
        estadoSolicitud.setEnabled(isExisting);
        if (!isExisting) {
             // Si es nuevo, forzar estado SOLICITADO y deshabilitar cambio
             estadoSolicitud.setValue(EstadoSolicitudPermiso.SOLICITADO);
             estadoSolicitud.setEnabled(false);
        } else {
            // Si es existente, permitir cambio (para aprobar/rechazar)
             estadoSolicitud.setEnabled(true);
        }


        updateSaveButtonState(); // Actualiza botón guardar
    }

    // --- MÉTODO Actualizar estado botón Guardar (simplificado) ---
    private void updateSaveButtonState() {
        // Habilita si hay bean y el binder lo considera válido (según @NotNull, etc.)
        save.setEnabled(binder.getBean() != null && binder.isValid());
        // La validación @AssertTrue se comprobará al hacer click en Guardar (dentro de writeBean)
    }
    // --- FIN MÉTODO ---


    // --- Eventos Personalizados ---
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