package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Aerolinea; // NUEVA IMPORTACIÓN
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Genero;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PosicionSeguridad;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Rol;
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
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.shared.Registration;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;

public class AgenteForm extends FormLayout {

    // --- CAMPOS DEL FORMULARIO ---
    TextField nombre = new TextField("Nombre");
    TextField apellido = new TextField("Apellido");
    TextField numeroCarnet = new TextField("Número Carnet");
    ComboBox<Genero> genero = new ComboBox<>("Género");
    ComboBox<Rol> rol = new ComboBox<>("Rol en la Operación");
    EmailField email = new EmailField("Email");
    TextField telefono = new TextField("Teléfono");
    DatePicker fechaNacimiento = new DatePicker("Fecha Nacimiento");
    TextField direccion = new TextField("Dirección");
    Checkbox activo = new Checkbox("Activo");
    CheckboxGroup<PosicionSeguridad> posicionesHabilitadas = new CheckboxGroup<>("Posiciones Habilitadas");
    // --- NUEVO CAMPO para gestionar permisos de aerolíneas ---
    CheckboxGroup<Aerolinea> aerolineasPermitidas = new CheckboxGroup<>("Aerolíneas Permitidas");

    // --- LÓGICA DE UPLOAD ---
    private MemoryBuffer buffer = new MemoryBuffer();
    private Upload upload = new Upload(buffer);
    private Image previsualizacionFoto = new Image();
    private Span nombreArchivoSubido = new Span();
    private String nombreArchivoOriginalParaGuardar;
    private InputStream inputStreamArchivoParaGuardar;

    // --- BOTONES ---
    Button save = new Button("Guardar");
    Button delete = new Button("Eliminar");
    Button cancel = new Button("Cancelar");

    private Binder<Agente> binder = new BeanValidationBinder<>(Agente.class);
    private Agente agenteActual;

    // --- MODIFICADO: Constructor ahora acepta la lista de aerolíneas ---
    public AgenteForm(List<PosicionSeguridad> listaPosicionesDisponibles, List<Aerolinea> listaAerolineasDisponibles) {
        addClassName("agente-form");
        
        configureBinder();

        // --- Configuración Campos ---
        genero.setItems(Genero.values());
        rol.setItems(Rol.values());
        rol.setItemLabelGenerator(Rol::getDescripcion);
        
        posicionesHabilitadas.setItems(listaPosicionesDisponibles);
        posicionesHabilitadas.setItemLabelGenerator(PosicionSeguridad::getNombrePosicion);
        posicionesHabilitadas.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);

        // --- NUEVA CONFIGURACIÓN para el campo de aerolíneas ---
        aerolineasPermitidas.setItems(listaAerolineasDisponibles);
        aerolineasPermitidas.setItemLabelGenerator(Aerolinea::getNombre);
        aerolineasPermitidas.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);

        // --- Configuración Upload ---
        configureUpload();

        previsualizacionFoto.setWidth("100px");
        previsualizacionFoto.setHeight("100px");
        previsualizacionFoto.getStyle().set("object-fit", "cover");
        previsualizacionFoto.setVisible(false);

        VerticalLayout fotoLayout = new VerticalLayout(new Span("Fotografía"), upload, nombreArchivoSubido, previsualizacionFoto);
        fotoLayout.setSpacing(false);
        fotoLayout.setPadding(false);
        fotoLayout.setAlignItems(Alignment.CENTER);

        // --- Layout del Formulario ---
        add(rol, nombre, apellido, numeroCarnet, genero, email, telefono, fechaNacimiento, direccion, activo, fotoLayout, posicionesHabilitadas, aerolineasPermitidas, createButtonsLayout());
        setColspan(rol, 2);
        setColspan(posicionesHabilitadas, 1);
        setColspan(aerolineasPermitidas, 1); // El nuevo campo ocupa 1 columna
        setColspan(direccion, 2);
        setColspan(fotoLayout, 2);
    }
    
    private void configureBinder() {
        binder.forField(nombre).asRequired("El nombre no puede estar vacío").bind("nombre");
        binder.forField(apellido).asRequired("El apellido no puede estar vacío").bind("apellido");
        binder.forField(numeroCarnet).asRequired("El número de carnet no puede estar vacío").bind("numeroCarnet");
        binder.forField(genero).asRequired("Debe seleccionar un género").bind("genero");
        binder.forField(rol).asRequired("Debe especificar un rol").bind("rol");
        binder.forField(email).asRequired("El email no puede estar vacío").bind("email");
        binder.forField(fechaNacimiento).bind("fechaNacimiento");
        binder.forField(telefono).bind("telefono");
        binder.forField(direccion).bind("direccion");
        binder.forField(activo).bind("activo");
        binder.forField(posicionesHabilitadas).bind("posicionesHabilitadas");
        // --- NUEVO BINDING para el campo de aerolíneas ---
        binder.forField(aerolineasPermitidas).bind("aerolineasPermitidas");

        binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));
    }

    private void configureUpload() {
        upload.setAcceptedFileTypes("image/jpeg", "image/png");
        upload.setMaxFiles(1);
        upload.setDropLabel(new Span("Arrastra la foto aquí"));
        upload.addSucceededListener(event -> {
            this.inputStreamArchivoParaGuardar = buffer.getInputStream();
            this.nombreArchivoOriginalParaGuardar = event.getFileName();
            nombreArchivoSubido.setText("Nuevo archivo: " + nombreArchivoOriginalParaGuardar);
            previsualizacionFoto.setSrc(new StreamResource(event.getFileName(), () -> buffer.getInputStream()));
            previsualizacionFoto.setVisible(true);
        });
        upload.addFileRejectedListener(event -> Notification.show("Archivo rechazado: " + event.getErrorMessage(), 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR));
    }

    private Component createButtonsLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addClickShortcut(Key.ENTER);
        cancel.addClickShortcut(Key.ESCAPE);

        save.addClickListener(event -> validateAndSave());
        delete.addClickListener(event -> fireEvent(new DeleteEvent(this, agenteActual)));
        cancel.addClickListener(event -> fireEvent(new CloseEvent(this)));
        
        return new HorizontalLayout(save, delete, cancel);
    }
    
    private void validateAndSave() {
        try {
            binder.writeBean(agenteActual);
            fireEvent(new SaveEvent(this, agenteActual, inputStreamArchivoParaGuardar, nombreArchivoOriginalParaGuardar));
        } catch (ValidationException e) {
            Notification.show("Hay errores de validación en el formulario.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            Notification.show("Error inesperado al guardar: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            e.printStackTrace();
        }
    }

    public void setAgente(Agente agente) {
        this.agenteActual = agente;
        binder.setBean(agente);

        this.inputStreamArchivoParaGuardar = null;
        this.nombreArchivoOriginalParaGuardar = null;
        nombreArchivoSubido.setText("");
        upload.clearFileList();

        boolean isNew = agente == null || agente.getIdAgente() == null;
        
        if (agente == null) {
            setVisible(false);
            return;
        }

        setVisible(true);
        nombre.focus();

        // Aseguramos que las colecciones no sean nulas para evitar errores en el binding
        if (agente.getPosicionesHabilitadas() == null) {
            agente.setPosicionesHabilitadas(new HashSet<>());
        }
        // --- NUEVA INICIALIZACIÓN para el campo de aerolíneas ---
        if (agente.getAerolineasPermitidas() == null) {
            agente.setAerolineasPermitidas(new HashSet<>());
        }
        
        if (!isNew && agente.getRutaFotografia() != null && !agente.getRutaFotografia().isEmpty()) {
            previsualizacionFoto.setSrc("agent-photos/" + agente.getRutaFotografia());
            previsualizacionFoto.setVisible(true);
            nombreArchivoSubido.setText("Foto actual: " + agente.getRutaFotografia());
        } else {
            previsualizacionFoto.getElement().removeAttribute("src");
            previsualizacionFoto.setVisible(false);
        }
        
        delete.setEnabled(!isNew);
        activo.setEnabled(!isNew);
    }

    // --- Definición de Eventos Personalizados (SIN CAMBIOS) ---
    public static abstract class AgenteFormEvent extends ComponentEvent<AgenteForm> {
        private Agente agente;
        protected AgenteFormEvent(AgenteForm source, Agente agente) {
            super(source, false);
            this.agente = agente;
        }
        public Agente getAgente() { return agente; }
    }

    public static class SaveEvent extends AgenteFormEvent {
        private final InputStream fotoStream;
        private final String nombreOriginalFoto;
        SaveEvent(AgenteForm source, Agente agente, InputStream fotoStream, String nombreOriginalFoto) {
            super(source, agente);
            this.fotoStream = fotoStream;
            this.nombreOriginalFoto = nombreOriginalFoto;
        }
        public InputStream getFotoStream() { return fotoStream; }
        public String getNombreOriginalFoto() { return nombreOriginalFoto; }
    }

    public static class DeleteEvent extends AgenteFormEvent {
        DeleteEvent(AgenteForm source, Agente agente) { super(source, agente); }
    }

    public static class CloseEvent extends AgenteFormEvent {
        CloseEvent(AgenteForm source) { super(source, null); }
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }
}