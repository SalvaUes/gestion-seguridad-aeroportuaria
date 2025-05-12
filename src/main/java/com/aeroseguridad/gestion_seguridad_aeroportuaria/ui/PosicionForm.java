package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Genero;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PosicionSeguridad;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox; // Importar Checkbox
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder; // Aún lo usamos para validación
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;
import java.util.Optional;

public class PosicionForm extends FormLayout {

    TextField nombrePosicion = new TextField("Nombre Posición");
    TextArea descripcion = new TextArea("Descripción");
    ComboBox<Genero> generoRequerido = new ComboBox<>("Género Requerido");
    Checkbox requiereEntrenamientoEspecial = new Checkbox("Requiere Entrenamiento Especial");
    Checkbox activo = new Checkbox("Activo"); // <-- NUEVO CAMPO

    Button save = new Button("Guardar");
    Button delete = new Button("Desactivar"); // <-- Texto cambiado
    Button cancel = new Button("Cancelar");

    Binder<PosicionSeguridad> binder = new BeanValidationBinder<>(PosicionSeguridad.class);
    private PosicionSeguridad posicionActual;

    public PosicionForm() {
        addClassName("posicion-form");

        generoRequerido.setItems(Genero.values());
        nombrePosicion.setRequiredIndicatorVisible(true);
        generoRequerido.setRequiredIndicatorVisible(true);
        activo.setValue(true); // Por defecto activo al crear

        // --- BINDING MANUAL ---
        binder.forField(nombrePosicion)
              .asRequired("El nombre de la posición es obligatorio.")
              .bind(PosicionSeguridad::getNombrePosicion, PosicionSeguridad::setNombrePosicion);
        binder.forField(descripcion)
              .bind(PosicionSeguridad::getDescripcion, PosicionSeguridad::setDescripcion);
        binder.forField(generoRequerido)
              .asRequired("Debe seleccionar un género requerido.")
              .bind(PosicionSeguridad::getGeneroRequerido, PosicionSeguridad::setGeneroRequerido);
        binder.forField(requiereEntrenamientoEspecial)
              .bind(PosicionSeguridad::isRequiereEntrenamientoEspecial, PosicionSeguridad::setRequiereEntrenamientoEspecial);
        binder.forField(activo) // <-- BINDING CAMPO ACTIVO
              .bind(PosicionSeguridad::getActivo, PosicionSeguridad::setActivo);
        // --- FIN MANUAL BINDING ---

        // Añadir 'activo' al layout
        add(nombrePosicion, descripcion, generoRequerido, requiereEntrenamientoEspecial, activo, createButtonsLayout());
    }

    private Component createButtonsLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addClickShortcut(Key.ENTER);
        cancel.addClickShortcut(Key.ESCAPE);

        save.addClickListener(event -> validateAndSave());
        delete.addClickListener(event -> fireEvent(new DeleteEvent(this, posicionActual)));
        cancel.addClickListener(event -> fireEvent(new CloseEvent(this)));

        save.setEnabled(false);
        delete.setEnabled(false);
        return new HorizontalLayout(save, delete, cancel);
    }

     private void validateAndSave() {
         try {
            if (posicionActual == null) {
                 Notification.show("Error: No hay datos de posición para guardar.", 3000, Notification.Position.MIDDLE)
                           .addThemeVariants(NotificationVariant.LUMO_ERROR);
                 return;
            }
            // Forzar la lectura de los campos al bean ANTES de validar con el binder
            // (Esto es necesario porque no usamos writeBean si no usamos el binder para todo)
            posicionActual.setNombrePosicion(nombrePosicion.getValue());
            posicionActual.setDescripcion(descripcion.getValue());
            posicionActual.setGeneroRequerido(generoRequerido.getValue());
            posicionActual.setRequiereEntrenamientoEspecial(requiereEntrenamientoEspecial.getValue());
            posicionActual.setActivo(activo.getValue()); // Actualizar 'activo'

            BinderValidationStatus<PosicionSeguridad> status = binder.validate(); // Validar con las anotaciones
            if (status.hasErrors()) {
                 String errorMsg = status.getValidationErrors().stream()
                     .map(err -> err.getErrorMessage())
                     .findFirst()
                     .orElse("Formulario inválido. Revise los campos marcados.");
                 Notification.show(errorMsg, 3000, Notification.Position.MIDDLE)
                           .addThemeVariants(NotificationVariant.LUMO_ERROR);
                 return;
            }
            // No necesitamos binder.writeBean(posicionActual) si ya actualizamos manualmente
            fireEvent(new SaveEvent(this, posicionActual));
        } catch (Exception e) { // Cambio de ValidationException a Exception para capturar más
             Notification.show("Error inesperado al guardar: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                 .addThemeVariants(NotificationVariant.LUMO_ERROR);
            e.printStackTrace();
        }
    }

    public void setPosicion(PosicionSeguridad posicion) {
        this.posicionActual = posicion;
        binder.setBean(posicion); // Usar setBean en lugar de readBean para asociar al binder y validar

        boolean beanPresent = posicion != null;
        boolean isExisting = beanPresent && posicion.getIdPosicion() != null;

        if (beanPresent) {
            nombrePosicion.setValue(posicion.getNombrePosicion() != null ? posicion.getNombrePosicion() : "");
            descripcion.setValue(posicion.getDescripcion() != null ? posicion.getDescripcion() : "");
            generoRequerido.setValue(posicion.getGeneroRequerido() != null ? posicion.getGeneroRequerido() : Genero.CUALQUIERA);
            requiereEntrenamientoEspecial.setValue(posicion.isRequiereEntrenamientoEspecial());
            activo.setValue(posicion.getActivo()); // Cargar estado 'activo'

            save.setEnabled(true); // Habilitar Guardar si hay bean
            // Habilitar Desactivar solo si es existente y actualmente activo
            delete.setEnabled(isExisting && posicion.getActivo());
            // Permitir editar 'activo' solo para existentes
            activo.setEnabled(isExisting);

        } else { // Limpiar formulario
            nombrePosicion.clear();
            descripcion.clear();
            generoRequerido.clear();
            requiereEntrenamientoEspecial.clear();
            activo.setValue(true); // Valor por defecto para nuevos
            activo.setEnabled(false); // Deshabilitado si no hay bean
            save.setEnabled(false);
            delete.setEnabled(false);
        }
    }

    public static abstract class PosicionFormEvent extends ComponentEvent<PosicionForm> { /* ... sin cambios ... */
        private PosicionSeguridad posicion;
        protected PosicionFormEvent(PosicionForm source, PosicionSeguridad posicion) { super(source, false); this.posicion = posicion; }
        public PosicionSeguridad getPosicion() { return posicion; }
    }
    public static class SaveEvent extends PosicionFormEvent { SaveEvent(PosicionForm source, PosicionSeguridad posicion) { super(source, posicion); } }
    public static class DeleteEvent extends PosicionFormEvent { DeleteEvent(PosicionForm source, PosicionSeguridad posicion) { super(source, posicion); } }
    public static class CloseEvent extends PosicionFormEvent { CloseEvent(PosicionForm source) { super(source, null); } }
    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }
}