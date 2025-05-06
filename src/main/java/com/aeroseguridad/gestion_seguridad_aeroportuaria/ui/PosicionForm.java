package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Genero;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PosicionSeguridad;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
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
        generoRequerido.setRequiredIndicatorVisible(true); // Usa @NotNull
        nombrePosicion.setRequiredIndicatorVisible(true); // Usa @NotBlank

        // Enlace
        binder.bindInstanceFields(this);

        // Listener para actualizar botón Guardar (simplificado)
        binder.addValueChangeListener(e -> updateSaveButtonState());

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

        save.setEnabled(false);
        delete.setEnabled(false);
        return new HorizontalLayout(save, delete, cancel);
    }

     private void validateAndSave() {
        try {
            binder.writeBean(posicionActual);
            fireEvent(new SaveEvent(this, posicionActual));
        } catch (ValidationException e) {
             Notification.show("Formulario inválido. Revise los campos.", 2000, Notification.Position.MIDDLE)
                 .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    public void setPosicion(PosicionSeguridad posicion) {
        this.posicionActual = posicion;
        binder.readBean(posicion);
        boolean isExisting = posicion != null && posicion.getIdPosicion() != null;
        delete.setEnabled(isExisting);
        updateSaveButtonState(); // Llama al método actualizado
    }


    // --- MÉTODO Actualizar estado botón Guardar (simplificado) ---
    private void updateSaveButtonState() {
        // Habilita si hay bean y el binder lo considera válido (según @NotNull, @NotBlank)
        save.setEnabled(binder.getBean() != null && binder.isValid());
    }
    // --- FIN MÉTODO ---


    // --- Eventos Personalizados ---
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