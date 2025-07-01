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
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.shared.Registration;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AgenteForm extends FormLayout {

    TextField nombre = new TextField("Nombre");
    TextField apellido = new TextField("Apellido");
    TextField numeroCarnet = new TextField("Número Carnet");
    ComboBox<Genero> genero = new ComboBox<>("Género");
    TextField direccion = new TextField("Dirección");
    DatePicker fechaNacimiento = new DatePicker("Fecha Nacimiento");
    TextField telefono = new TextField("Teléfono");
    EmailField email = new EmailField("Email");
    Checkbox activo = new Checkbox("Activo");
    CheckboxGroup<PosicionSeguridad> posicionesHabilitadas = new CheckboxGroup<>("Posiciones Habilitadas");

    private MemoryBuffer buffer = new MemoryBuffer();
    private Upload upload = new Upload(buffer);
    private Image previsualizacionFoto = new Image();
    private Span nombreArchivoSubido = new Span();
    private String nombreArchivoOriginalParaGuardar;
    private InputStream inputStreamArchivoParaGuardar;

    Button save = new Button("Guardar");
    Button delete = new Button("Desactivar");
    Button cancel = new Button("Cancelar");

    private Agente agenteActual;
    private final Validator beanValidator;

    public AgenteForm(List<PosicionSeguridad> listaPosicionesDisponibles) {
        addClassName("agente-form");
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        beanValidator = factory.getValidator();

        // --- Configuración Campos ---
        nombre.setRequiredIndicatorVisible(true);
        apellido.setRequiredIndicatorVisible(true);
        numeroCarnet.setRequiredIndicatorVisible(true);
        genero.setRequiredIndicatorVisible(true);
        genero.setItems(Genero.values());

        posicionesHabilitadas.setItems(listaPosicionesDisponibles);
        posicionesHabilitadas.setItemLabelGenerator(PosicionSeguridad::getNombrePosicion);
        posicionesHabilitadas.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);

        // --- Configuración Upload ---
        upload.setAcceptedFileTypes("image/jpeg", "image/png");
        upload.setMaxFiles(1);
        upload.setDropLabel(new Span("Arrastra la foto aquí"));
        upload.addSucceededListener(event -> {
            this.inputStreamArchivoParaGuardar = buffer.getInputStream();
            this.nombreArchivoOriginalParaGuardar = event.getFileName();
            nombreArchivoSubido.setText("Nuevo archivo: " + nombreArchivoOriginalParaGuardar);
            previsualizacionFoto.setSrc(new StreamResource(event.getFileName(), () -> buffer.getInputStream()));
            previsualizacionFoto.setVisible(true);
            if (agenteActual != null) save.setEnabled(true);
        });
        upload.addFileRejectedListener(event -> Notification.show("Archivo rechazado: " + event.getErrorMessage(), 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR));

        previsualizacionFoto.setWidth("100px");
        previsualizacionFoto.setHeight("100px");
        previsualizacionFoto.getStyle().set("object-fit", "cover");
        previsualizacionFoto.setVisible(false);

        VerticalLayout fotoLayout = new VerticalLayout(new Span("Fotografía"), upload, nombreArchivoSubido, previsualizacionFoto);
        fotoLayout.setSpacing(false);
        fotoLayout.setPadding(false);
        fotoLayout.setAlignItems(Alignment.CENTER);

        // --- LAYOUT DEL FORMULARIO ---
        add(
                nombre,
                apellido,
                numeroCarnet,
                genero,
                email,
                telefono,
                fechaNacimiento,
                direccion,
                activo,
                fotoLayout,
                posicionesHabilitadas,
                createButtonsLayout()
        );
        setColspan(posicionesHabilitadas, 2);
        setColspan(direccion, 2);
    }

    private Component createButtonsLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addClickShortcut(Key.ENTER);
        cancel.addClickShortcut(Key.ESCAPE);

        save.addClickListener(event -> validateAndSaveManually());
        delete.addClickListener(event -> fireEvent(new DeleteEvent(this, agenteActual)));
        cancel.addClickListener(event -> fireEvent(new CloseEvent(this)));

        save.setEnabled(false);
        delete.setEnabled(false);
        return new HorizontalLayout(save, delete, cancel);
    }

    private void validateAndSaveManually() {
        if (agenteActual == null) {
            Notification.show("No hay datos de agente para guardar.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        try {
            // 1. Actualizar el bean 'agenteActual'
            agenteActual.setNombre(nombre.getValue());
            agenteActual.setApellido(apellido.getValue());
            agenteActual.setNumeroCarnet(numeroCarnet.getValue());
            agenteActual.setGenero(genero.getValue());
            agenteActual.setDireccion(direccion.getValue());
            agenteActual.setFechaNacimiento(fechaNacimiento.getValue());
            agenteActual.setTelefono(telefono.getValue());
            agenteActual.setEmail(email.getValue());
            agenteActual.setActivo(activo.getValue());
            agenteActual.setPosicionesHabilitadas(posicionesHabilitadas.getValue() != null ? posicionesHabilitadas.getValue() : new HashSet<>());

            // 2. Validar el bean
            Set<ConstraintViolation<Agente>> violations = beanValidator.validate(agenteActual);
            if (!violations.isEmpty()) {
                String errorMsg = violations.stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.joining("; "));
                Notification.show("Error de validación: " + errorMsg, 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            // 3. Disparar evento Save
            fireEvent(new SaveEvent(this, agenteActual, inputStreamArchivoParaGuardar, nombreArchivoOriginalParaGuardar));

        } catch (Exception e) {
            Notification.show("Error inesperado: " + e.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            e.printStackTrace();
        }
    }

    public void setAgente(Agente agente) {
        this.agenteActual = agente;

        this.inputStreamArchivoParaGuardar = null;
        this.nombreArchivoOriginalParaGuardar = null;
        nombreArchivoSubido.setText("");
        upload.clearFileList();

        boolean isExisting = agente != null && agente.getIdAgente() != null;

        if (agente != null) {
            nombre.setValue(agente.getNombre() != null ? agente.getNombre() : "");
            apellido.setValue(agente.getApellido() != null ? agente.getApellido() : "");
            numeroCarnet.setValue(agente.getNumeroCarnet() != null ? agente.getNumeroCarnet() : "");
            genero.setValue(agente.getGenero());
            direccion.setValue(agente.getDireccion() != null ? agente.getDireccion() : "");
            fechaNacimiento.setValue(agente.getFechaNacimiento());
            telefono.setValue(agente.getTelefono() != null ? agente.getTelefono() : "");
            email.setValue(agente.getEmail() != null ? agente.getEmail() : "");
            activo.setValue(agente.getActivo() != null ? agente.getActivo() : true);
            posicionesHabilitadas.setValue(agente.getPosicionesHabilitadas() != null ? agente.getPosicionesHabilitadas() : new HashSet<>());

            if (isExisting && agente.getRutaFotografia() != null && !agente.getRutaFotografia().isEmpty()) {
                previsualizacionFoto.setSrc("agent-photos/" + agente.getRutaFotografia());
                previsualizacionFoto.setVisible(true);
                nombreArchivoSubido.setText("Foto actual: " + agente.getRutaFotografia());
            } else {
                previsualizacionFoto.getElement().removeAttribute("src");
                previsualizacionFoto.setVisible(false);
            }

            save.setEnabled(true);
            delete.setEnabled(isExisting && agente.getActivo());
            activo.setEnabled(isExisting);

        } else {
            // Limpia el formulario cuando agente es null
            nombre.clear();
            apellido.clear();
            numeroCarnet.clear();
            genero.clear();
            direccion.clear();
            fechaNacimiento.clear();
            telefono.clear();
            email.clear();
            activo.setValue(false);

            posicionesHabilitadas.clear();
            previsualizacionFoto.getElement().removeAttribute("src");
            previsualizacionFoto.setVisible(false);
            nombreArchivoSubido.setText("");

            save.setEnabled(false);
            delete.setEnabled(false);
            activo.setEnabled(false);
        }
    }

    // --- Definición de Eventos Personalizados ---
    public static abstract class AgenteFormEvent extends ComponentEvent<AgenteForm> {
        private Agente agente;

        protected AgenteFormEvent(AgenteForm source, Agente agente) {
            super(source, false);
            this.agente = agente;
        }

        public Agente getAgente() {
            return agente;
        }
    }

    public static class SaveEvent extends AgenteFormEvent {
        private final InputStream fotoStream;
        private final String nombreOriginalFoto;

        SaveEvent(AgenteForm source, Agente agente, InputStream fotoStream, String nombreOriginalFoto) {
            super(source, agente);
            this.fotoStream = fotoStream;
            this.nombreOriginalFoto = nombreOriginalFoto;
        }

        public InputStream getFotoStream() {
            return fotoStream;
        }

        public String getNombreOriginalFoto() {
            return nombreOriginalFoto;
        }
    }

    public static class DeleteEvent extends AgenteFormEvent {
        DeleteEvent(AgenteForm source, Agente agente) {
            super(source, agente);
        }
    }

    public static class CloseEvent extends AgenteFormEvent {
        CloseEvent(AgenteForm source) {
            super(source, null);
        }
    }

    // Método genérico para añadir listeners de eventos
    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }
}