package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Genero;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PosicionSeguridad;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.shared.Registration;

import java.util.List;
import java.util.Objects;

public class AgenteForm extends FormLayout {

    // Campos
    TextField nombre = new TextField("Nombre");
    TextField apellido = new TextField("Apellido");
    TextField numeroCarnet = new TextField("Número Carnet");
    ComboBox<Genero> genero = new ComboBox<>("Género");
    TextField direccion = new TextField("Dirección");
    DatePicker fechaNacimiento = new DatePicker("Fecha Nacimiento");
    TextField telefono = new TextField("Teléfono");
    EmailField email = new EmailField("Email");
    TextField rutaFotografia = new TextField("Ruta Foto"); // Placeholder
    Checkbox activo = new Checkbox("Activo");
    CheckboxGroup<PosicionSeguridad> posicionesHabilitadas = new CheckboxGroup<>("Posiciones Habilitadas");

    // Botones
    Button save = new Button("Guardar");
    Button delete = new Button("Desactivar"); // Cambiado texto para reflejar Soft Delete
    Button cancel = new Button("Cancelar");

    Binder<Agente> binder = new BeanValidationBinder<>(Agente.class);
    private Agente agenteActual;

    public AgenteForm(List<PosicionSeguridad> listaPosiciones) {
        addClassName("agente-form");

        // Enlace explícito y validación para campos requeridos
        binder.forField(nombre)
              .asRequired("El nombre es obligatorio")
              .bind(Agente::getNombre, Agente::setNombre);
        binder.forField(apellido)
              .asRequired("El apellido es obligatorio")
              .bind(Agente::getApellido, Agente::setApellido);
        binder.forField(numeroCarnet)
              .asRequired("El número de carnet es obligatorio")
              .bind(Agente::getNumeroCarnet, Agente::setNumeroCarnet);
        binder.forField(genero)
              .asRequired("Debe seleccionar un género")
              .bind(Agente::getGenero, Agente::setGenero);

        // Enlaza el resto de campos
        binder.bind(direccion, Agente::getDireccion, Agente::setDireccion);
        binder.bind(fechaNacimiento, Agente::getFechaNacimiento, Agente::setFechaNacimiento);
        binder.bind(telefono, Agente::getTelefono, Agente::setTelefono);
        binder.bind(email, Agente::getEmail, Agente::setEmail);
        binder.bind(rutaFotografia, Agente::getRutaFotografia, Agente::setRutaFotografia);
        binder.bind(activo, Agente::getActivo, Agente::setActivo);
        binder.bind(posicionesHabilitadas, Agente::getPosicionesHabilitadas, Agente::setPosicionesHabilitadas);

        // Configura ComboBox de Género
        genero.setItems(Genero.MASCULINO, Genero.FEMENINO);
        genero.setRequiredIndicatorVisible(true);

        // Configura CheckboxGroup de Posiciones
        posicionesHabilitadas.setItems(listaPosiciones);
        posicionesHabilitadas.setItemLabelGenerator(PosicionSeguridad::getNombrePosicion);
        posicionesHabilitadas.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);

        // Listener para actualizar botón Guardar
        binder.addValueChangeListener(e -> updateSaveButtonState());

        // Añade todos los componentes al layout
        add(nombre, apellido, numeroCarnet, genero, direccion, fechaNacimiento,
            telefono, email, rutaFotografia, activo,
            posicionesHabilitadas,
            createButtonsLayout());
    }

    private Component createButtonsLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        save.addClickShortcut(Key.ENTER);
        cancel.addClickShortcut(Key.ESCAPE);

        save.addClickListener(event -> validateAndSave());
        // Evento Delete ahora corresponde a desactivar
        delete.addClickListener(event -> fireEvent(new DeleteEvent(this, agenteActual)));
        cancel.addClickListener(event -> fireEvent(new CloseEvent(this)));

        // Se habilitan/deshabilitan en setAgente y updateSaveButtonState
        save.setEnabled(false);
        delete.setEnabled(false);

        return new HorizontalLayout(save, delete, cancel);
    }

    private void validateAndSave() {
        try {
            binder.writeBean(agenteActual);
            fireEvent(new SaveEvent(this, agenteActual));
        } catch (ValidationException e) {
             Notification.show("Formulario inválido. Revise los campos marcados.", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    public void setAgente(Agente agente) {
        this.agenteActual = agente;
        binder.readBean(agente);
        boolean isExisting = agente != null && agente.getIdAgente() != null;
        // Habilita botón desactivar solo si el agente existe y está activo
        delete.setEnabled(isExisting && agente != null && agente.getActivo());
        // Habilita/deshabilita campo 'activo' solo si es existente
        activo.setEnabled(isExisting);
        if (!isExisting && activo.isEnabled()) {
             activo.setValue(true); // Nuevos agentes siempre activos inicialmente en el form
        }
        updateSaveButtonState();
    }

    // --- MÉTODO Actualizar estado botón Guardar ---
    private void updateSaveButtonState() {
        // Habilita si hay bean y es válido según las validaciones de campo/bean
        save.setEnabled(binder.getBean() != null && binder.isValid());
    }
    // --- FIN MÉTODO ---


    // --- Eventos Personalizados ---
    public static abstract class AgenteFormEvent extends ComponentEvent<AgenteForm> {
        private Agente agente;
        protected AgenteFormEvent(AgenteForm source, Agente agente) { super(source, false); this.agente = agente; }
        public Agente getAgente() { return agente; }
    }
    public static class SaveEvent extends AgenteFormEvent { SaveEvent(AgenteForm source, Agente agente) { super(source, agente); } }
    // DeleteEvent ahora significa "Desactivar"
    public static class DeleteEvent extends AgenteFormEvent { DeleteEvent(AgenteForm source, Agente agente) { super(source, agente); } }
    public static class CloseEvent extends AgenteFormEvent { CloseEvent(AgenteForm source) { super(source, null); } }
    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }
}