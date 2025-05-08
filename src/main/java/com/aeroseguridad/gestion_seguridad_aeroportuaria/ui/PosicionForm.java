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
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
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
import java.util.Optional;

public class PosicionForm extends FormLayout {

    // Campos
    TextField nombrePosicion = new TextField("Nombre Posición");
    TextArea descripcion = new TextArea("Descripción");
    ComboBox<Genero> generoRequerido = new ComboBox<>("Género Requerido");
    Checkbox requiereEntrenamientoEspecial = new Checkbox("Requiere Entrenamiento Especial");

    // Botones
    Button save = new Button("Guardar");
    Button delete = new Button("Eliminar");
    Button cancel = new Button("Cancelar");

    // Binder
    Binder<PosicionSeguridad> binder = new BeanValidationBinder<>(PosicionSeguridad.class);
    private PosicionSeguridad posicionActual;

    public PosicionForm() {
        addClassName("posicion-form");

        // Configuración campos
        generoRequerido.setItems(Genero.values()); // Incluye CUALQUIERA
        nombrePosicion.setRequiredIndicatorVisible(true); // Usa @NotBlank
        generoRequerido.setRequiredIndicatorVisible(true); // Usa @NotNull

        // --- MANUAL BINDING (Más robusto si bindInstanceFields da problemas) ---
         binder.forField(nombrePosicion)
               .asRequired("El nombre de la posición es obligatorio.") // Mensaje para UI
               .bind(PosicionSeguridad::getNombrePosicion, PosicionSeguridad::setNombrePosicion);
         binder.forField(descripcion)
               .bind(PosicionSeguridad::getDescripcion, PosicionSeguridad::setDescripcion);
         binder.forField(generoRequerido)
               .asRequired("Debe seleccionar un género requerido.") // Mensaje para UI
               .bind(PosicionSeguridad::getGeneroRequerido, PosicionSeguridad::setGeneroRequerido);
         binder.forField(requiereEntrenamientoEspecial)
               // No necesita asRequired si el default (false) es aceptable
               .bind(PosicionSeguridad::isRequiereEntrenamientoEspecial, PosicionSeguridad::setRequiereEntrenamientoEspecial);
        // --- FIN MANUAL BINDING ---

        add(nombrePosicion, descripcion, generoRequerido, requiereEntrenamientoEspecial, createButtonsLayout());
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

        save.setEnabled(false); // Habilitado en setPosicion
        delete.setEnabled(false); // Habilitado en setPosicion
        return new HorizontalLayout(save, delete, cancel);
    }

     private void validateAndSave() {
         try {
            if (posicionActual == null) {
                 Notification.show("Error: No hay datos de posición para guardar.", 3000, Notification.Position.MIDDLE)
                           .addThemeVariants(NotificationVariant.LUMO_ERROR);
                 return;
            }
            // Validar explícitamente ANTES de escribir
            BinderValidationStatus<PosicionSeguridad> status = binder.validate();
            if (status.hasErrors()) {
                 // Intenta obtener un mensaje de error útil
                 String errorMsg = status.getValidationErrors().stream()
                     .map(err -> err.getErrorMessage())
                     .findFirst()
                     .orElse("Formulario inválido. Revise los campos marcados.");
                 Notification.show(errorMsg, 3000, Notification.Position.MIDDLE)
                           .addThemeVariants(NotificationVariant.LUMO_ERROR);
                 return; // No continuar si hay errores
            }
            // Si la validación pasa, escribir el bean y disparar evento
            binder.writeBean(posicionActual);
            fireEvent(new SaveEvent(this, posicionActual));
        } catch (ValidationException e) {
             Notification.show("Error inesperado al validar/guardar.", 2000, Notification.Position.MIDDLE)
                 .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
             Notification.show("Error inesperado: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                 .addThemeVariants(NotificationVariant.LUMO_ERROR);
            e.printStackTrace();
        }
    }

    public void setPosicion(PosicionSeguridad posicion) {
        this.posicionActual = posicion;
        binder.readBean(posicion); // Carga datos en los campos

        // Habilitar/Deshabilitar botones basado en si hay una Posicion cargada
        boolean beanPresent = posicion != null;
        // Habilita Guardar si hay un bean (nuevo o existente). La validación se hará al hacer clic.
        save.setEnabled(beanPresent);

        boolean isExisting = beanPresent && posicion.getIdPosicion() != null;
        // Habilita Eliminar solo si es un registro existente
        delete.setEnabled(isExisting);

        // Establecer valores por defecto para nuevos registros si es necesario
        if (!isExisting && beanPresent) {
             if(generoRequerido.getValue() == null) {
                 generoRequerido.setValue(Genero.CUALQUIERA); // Asegurar default
             }
             // El checkbox 'requiereEntrenamientoEspecial' usará el default 'false' del bean
        }
    }

    // --- Eventos Personalizados (Sin cambios) ---
    public static abstract class PosicionFormEvent extends ComponentEvent<PosicionForm> {
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