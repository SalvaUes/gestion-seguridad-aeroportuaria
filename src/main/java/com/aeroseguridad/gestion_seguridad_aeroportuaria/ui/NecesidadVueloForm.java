package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.NecesidadVuelo;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PosicionSeguridad;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Vuelo;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;

import java.time.Duration;
import java.util.List;


public class NecesidadVueloForm extends Dialog {

    ComboBox<PosicionSeguridad> posicion = new ComboBox<>("Posición Requerida");
    IntegerField cantidadAgentes = new IntegerField("Cantidad Agentes");
    DateTimePicker inicioCobertura = new DateTimePicker("Inicio Cobertura");
    DateTimePicker finCobertura = new DateTimePicker("Fin Cobertura");

    Button save = new Button("Guardar Necesidad");
    Button cancel = new Button("Cancelar");

    Binder<NecesidadVuelo> binder = new BeanValidationBinder<>(NecesidadVuelo.class);
    private NecesidadVuelo necesidadActual;
    private Vuelo vueloPadre; // Guardamos referencia al vuelo asociado

    public NecesidadVueloForm(List<PosicionSeguridad> posiciones) {
        setHeaderTitle("Añadir/Editar Necesidad de Seguridad");
        setDraggable(true); // Hacer diálogo movible
        setResizable(true); // Hacer diálogo redimensionable

        // Configuración campos
        posicion.setItems(posiciones);
        if (posiciones == null || posiciones.isEmpty()) {
            posicion.setPlaceholder("¡No hay posiciones definidas!");
        }
        posicion.setItemLabelGenerator(p -> p != null ? p.getNombrePosicion() : "");
        posicion.setRequiredIndicatorVisible(true); // Usa validación @NotNull

        cantidadAgentes.setRequiredIndicatorVisible(true); // Usa validación @Min
        cantidadAgentes.setStepButtonsVisible(true);
        cantidadAgentes.setMin(1);

        inicioCobertura.setRequiredIndicatorVisible(true); // Usa validación @NotNull
        finCobertura.setRequiredIndicatorVisible(true); // Usa validación @NotNull
        inicioCobertura.setStep(Duration.ofMinutes(15)); // Ajustar step si se desea
        finCobertura.setStep(Duration.ofMinutes(15));

        // Enlazar Binder (usará @NotNull, @Min, @AssertTrue de la entidad)
        binder.bindInstanceFields(this);

        // Listener para actualizar botón Guardar (simplificado)
        binder.addValueChangeListener(e -> updateSaveButtonState());

        FormLayout formLayout = new FormLayout(posicion, cantidadAgentes, inicioCobertura, finCobertura);
        HorizontalLayout buttonLayout = createButtonsLayout();
        add(formLayout, buttonLayout);
    }

     private HorizontalLayout createButtonsLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addClickShortcut(Key.ENTER);
        cancel.addClickShortcut(Key.ESCAPE);

        save.addClickListener(event -> validateAndSave());
        cancel.addClickListener(event -> {
            fireEvent(new CloseEvent(this));
            close(); // Cierra el diálogo al cancelar
        });

        save.setEnabled(false); // Empieza deshabilitado, se habilita en setNecesidad/listener

        HorizontalLayout buttons = new HorizontalLayout(save, cancel);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttons.setWidthFull();
        return buttons;
     }

     private void validateAndSave() {
        try {
            if (this.necesidadActual == null || this.vueloPadre == null) {
                 Notification.show("Error interno: No se ha asociado correctamente la necesidad al vuelo.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                 return;
            }
             // Asigna el vuelo padre ANTES de escribir/validar el bean
             this.necesidadActual.setVuelo(this.vueloPadre);

            // writeBean ejecuta las validaciones de la entidad (@NotNull, @Min, @AssertTrue)
            binder.writeBean(necesidadActual);

            // Si llega aquí, es válido. Dispara el evento Save.
            fireEvent(new SaveEvent(this, necesidadActual));
            close(); // Cierra el diálogo después de guardar

        } catch (ValidationException e) {
             // El Binder ya debería haber mostrado errores en los campos inválidos
             // Intentar obtener mensaje específico de bean validation si es posible
             String errorMsg = "Formulario inválido. Revise los campos marcados.";
             if (!binder.isValid() && !binder.validate().getBeanValidationErrors().isEmpty()) {
                 errorMsg = binder.validate().getBeanValidationErrors().get(0).getErrorMessage();
             }
             Notification.show(errorMsg, 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);

        } catch (Exception ex) {
            // Captura otros posibles errores (ej. problemas al asignar vuelo padre)
            Notification.show("Error inesperado al intentar guardar: " + ex.getMessage(), 4000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            ex.printStackTrace(); // Log para debug
        }
    }

     public void setNecesidad(NecesidadVuelo necesidad, Vuelo vueloPadre) {
        this.necesidadActual = necesidad;
        this.vueloPadre = vueloPadre; // Guarda la referencia al vuelo
        binder.readBean(necesidad);
        updateSaveButtonState(); // Actualiza estado del botón
    }

    // --- MÉTODO Actualizar estado botón Guardar (simplificado) ---
    private void updateSaveButtonState() {
        // Habilita si hay bean y el binder lo considera válido (según @NotNull, @Min, etc.)
        save.setEnabled(binder.getBean() != null && binder.isValid());
        // La validación @AssertTrue se comprobará al hacer click en Guardar (dentro de writeBean)
    }
    // --- FIN MÉTODO ---


    // --- Eventos Personalizados ---
     public static abstract class NecesidadVueloFormEvent extends ComponentEvent<NecesidadVueloForm> {
         private NecesidadVuelo necesidad;
         protected NecesidadVueloFormEvent(NecesidadVueloForm source, NecesidadVuelo necesidad) { super(source, false); this.necesidad = necesidad; }
         public NecesidadVuelo getNecesidad() { return necesidad; }
     }
     public static class SaveEvent extends NecesidadVueloFormEvent { SaveEvent(NecesidadVueloForm source, NecesidadVuelo necesidad) { super(source, necesidad); } }
     public static class CloseEvent extends NecesidadVueloFormEvent { CloseEvent(NecesidadVueloForm source) { super(source, null); } }
     public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener) { return getEventBus().addListener(eventType, listener); }
}