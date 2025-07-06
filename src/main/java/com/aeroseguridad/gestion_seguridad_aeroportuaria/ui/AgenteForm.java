// RUTA: src/main/java/com/aeroseguridad/gestion_seguridad_aeroportuaria/ui/AgenteForm.java
package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
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
import com.vaadin.flow.data.binder.BeanValidationBinder; // NUEVO
import com.vaadin.flow.data.binder.Binder; // NUEVO
import com.vaadin.flow.data.binder.ValidationException; // NUEVO
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.shared.Registration;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;

public class AgenteForm extends FormLayout {

    // --- CAMPOS DEL FORMULARIO (Sin cambios) ---
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

    // --- LÓGICA DE UPLOAD ---
    private MemoryBuffer buffer = new MemoryBuffer();
    private Upload upload = new Upload(buffer);
    private Image previsualizacionFoto = new Image();
    private Span nombreArchivoSubido = new Span();
    private String nombreArchivoOriginalParaGuardar;
    private InputStream inputStreamArchivoParaGuardar;

    // --- BOTONES ---
    Button save = new Button("Guardar");
    // CORRECCIÓN: El texto del botón ahora refleja la acción de borrado permanente.
    Button delete = new Button("Eliminar");
    Button cancel = new Button("Cancelar");

    // NUEVO: Se introduce el Binder de Vaadin para gestionar el estado del formulario.
    private Binder<Agente> binder = new BeanValidationBinder<>(Agente.class);
    private Agente agenteActual; // Mantenemos la referencia al bean actual

    public AgenteForm(List<PosicionSeguridad> listaPosicionesDisponibles) {
        addClassName("agente-form");
        
        // MODIFICADO: Se configura el Binder para vincular los campos con el bean.
        configureBinder();

        // --- Configuración Campos ---
        genero.setItems(Genero.values());
        rol.setItems(Rol.values());
        rol.setItemLabelGenerator(Rol::getDescripcion);
        posicionesHabilitadas.setItems(listaPosicionesDisponibles);
        posicionesHabilitadas.setItemLabelGenerator(PosicionSeguridad::getNombrePosicion);
        posicionesHabilitadas.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);

        // --- Configuración Upload (sin cambios) ---
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
        add(rol, nombre, apellido, numeroCarnet, genero, email, telefono, fechaNacimiento, direccion, activo, fotoLayout, posicionesHabilitadas, createButtonsLayout());
        setColspan(rol, 2);
        setColspan(posicionesHabilitadas, 2);
        setColspan(direccion, 2);
        setColspan(fotoLayout, 2);
    }
    
    // NUEVO: Método para encapsular la configuración del Binder.
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

        // Cuando cualquier valor cambia, se habilita el botón de guardar.
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

        // MODIFICADO: El listener de Guardar ahora usa el Binder.
        save.addClickListener(event -> validateAndSave());
        delete.addClickListener(event -> fireEvent(new DeleteEvent(this, agenteActual)));
        cancel.addClickListener(event -> fireEvent(new CloseEvent(this)));
        
        return new HorizontalLayout(save, delete, cancel);
    }

    // MODIFICADO: Lógica de guardado simplificada gracias al Binder.
    private void validateAndSave() {
        try {
            // El binder escribe los valores de la UI al bean 'agenteActual'.
            // Si la validación falla, lanza una excepción y no continúa.
            binder.writeBean(agenteActual);
            
            // Si la escritura y validación son exitosas, disparamos el evento de guardado.
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

    // MODIFICADO: setAgente ahora es mucho más simple.
    public void setAgente(Agente agente) {
        this.agenteActual = agente;
        // El Binder se encarga de poblar todos los campos vinculados.
        binder.setBean(agente);

        // Reseteamos el estado de la subida de archivos
        this.inputStreamArchivoParaGuardar = null;
        this.nombreArchivoOriginalParaGuardar = null;
        nombreArchivoSubido.setText("");
        upload.clearFileList();

        boolean isNew = agente == null || agente.getIdAgente() == null;
        
        if (agente == null) {
            // Si el agente es nulo, el formulario se limpia y los botones se desactivan.
            setVisible(false);
            return;
        }

        setVisible(true);
        nombre.focus();

        if (agente.getPosicionesHabilitadas() == null) {
            agente.setPosicionesHabilitadas(new HashSet<>());
        }
        
        if (!isNew && agente.getRutaFotografia() != null && !agente.getRutaFotografia().isEmpty()) {
            previsualizacionFoto.setSrc("agent-photos/" + agente.getRutaFotografia());
            previsualizacionFoto.setVisible(true);
            nombreArchivoSubido.setText("Foto actual: " + agente.getRutaFotografia());
        } else {
            previsualizacionFoto.getElement().removeAttribute("src");
            previsualizacionFoto.setVisible(false);
        }
        
        // La habilitación del botón de guardar es manejada por el status listener del binder
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