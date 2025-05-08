package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.NecesidadVuelo;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PosicionSeguridad;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Vuelo;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue; // Importar
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
// import com.vaadin.flow.component.html.H3; // No usado
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
// Quitar imports de Binder
// import com.vaadin.flow.data.binder.BeanValidationBinder;
// import com.vaadin.flow.data.binder.Binder;
// import com.vaadin.flow.data.binder.BinderValidationStatus;
// import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;
import jakarta.validation.ConstraintViolation; // Importar
import jakarta.validation.Validation;         // Importar
import jakarta.validation.Validator;           // Importar
import jakarta.validation.ValidatorFactory;    // Importar

import java.time.Duration;
import java.util.List;
import java.util.Set; // Importar
import java.util.stream.Collectors; // Importar

public class NecesidadVueloForm extends Dialog {

    ComboBox<PosicionSeguridad> posicion = new ComboBox<>("Posición Requerida");
    IntegerField cantidadAgentes = new IntegerField("Cantidad Agentes");
    DateTimePicker inicioCobertura = new DateTimePicker("Inicio Cobertura");
    DateTimePicker finCobertura = new DateTimePicker("Fin Cobertura");

    Button save = new Button("Guardar Necesidad");
    Button cancel = new Button("Cancelar");

    // --- SIN Binder ---
    // Binder<NecesidadVuelo> binder = new BeanValidationBinder<>(NecesidadVuelo.class);
    private NecesidadVuelo necesidadActual;
    private Vuelo vueloPadre; // Guardamos referencia al vuelo asociado

    // Validador manual
    private final Validator validator;

    public NecesidadVueloForm(List<PosicionSeguridad> posiciones) {
        setHeaderTitle("Añadir/Editar Necesidad de Seguridad");
        setDraggable(true);
        setResizable(true);

        // Inicializar validador
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        // Configuración campos
        posicion.setItems(posiciones);
        if (posiciones == null || posiciones.isEmpty()) {
            posicion.setPlaceholder("¡No hay posiciones definidas!");
        }
        posicion.setItemLabelGenerator(p -> p != null ? p.getNombrePosicion() : "");
        posicion.setRequiredIndicatorVisible(true); // Indicador visual

        cantidadAgentes.setRequiredIndicatorVisible(true); // Indicador visual
        cantidadAgentes.setStepButtonsVisible(true);
        cantidadAgentes.setMin(1);

        inicioCobertura.setRequiredIndicatorVisible(true); // Indicador visual
        finCobertura.setRequiredIndicatorVisible(true); // Indicador visual
        inicioCobertura.setStep(Duration.ofMinutes(15));
        finCobertura.setStep(Duration.ofMinutes(15));

        // --- SIN Binding automático ---

        // Listener simple para habilitar Guardar si hay necesidadActual
        HasValue.ValueChangeListener enableSaveListener = e -> {
             if (save != null) save.setEnabled(necesidadActual != null);
        };
        posicion.addValueChangeListener(enableSaveListener);
        cantidadAgentes.addValueChangeListener(enableSaveListener);
        inicioCobertura.addValueChangeListener(enableSaveListener);
        finCobertura.addValueChangeListener(enableSaveListener);

        FormLayout formLayout = new FormLayout(posicion, cantidadAgentes, inicioCobertura, finCobertura);
        HorizontalLayout buttonLayout = createButtonsLayout();
        add(formLayout, buttonLayout);
    }

     private HorizontalLayout createButtonsLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addClickShortcut(Key.ENTER);
        cancel.addClickShortcut(Key.ESCAPE);

        save.addClickListener(event -> validateAndSaveManually()); // Llamar método manual
        cancel.addClickListener(event -> {
            fireEvent(new CloseEvent(this));
            close(); // Cierra el diálogo
        });

        save.setEnabled(false); // Se habilita en setNecesidad
        // No hay botón delete en este form

        HorizontalLayout buttons = new HorizontalLayout(save, cancel);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttons.setWidthFull();
        return buttons;
     }

     // --- MÉTODO validateAndSave MANUAL ---
     private void validateAndSaveManually() {
        if (this.necesidadActual == null || this.vueloPadre == null) {
             Notification.show("Error interno: No se ha asociado correctamente la necesidad al vuelo.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
             return;
        }
        try {
            // 1. Actualizar bean manualmente
            necesidadActual.setVuelo(this.vueloPadre); // Asegurar vuelo padre
            necesidadActual.setPosicion(posicion.getValue());
            // Obtener valor de IntegerField, manejar posible null si no es requerido por UI
            Integer cant = cantidadAgentes.getValue();
            necesidadActual.setCantidadAgentes(cant != null ? cant : 0); // O lanzar error si es null y requerido
            necesidadActual.setInicioCobertura(inicioCobertura.getValue());
            necesidadActual.setFinCobertura(finCobertura.getValue());

            // 2. Validar bean manualmente
            Set<ConstraintViolation<NecesidadVuelo>> violations = validator.validate(necesidadActual);
            if (!violations.isEmpty()) {
                String errorMsg = violations.stream()
                                    .map(ConstraintViolation::getMessage)
                                    .collect(Collectors.joining("; "));
                 Notification.show("Error de validación: " + errorMsg, 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                 return;
            }

            // 3. Disparar evento Save si es válido
            fireEvent(new SaveEvent(this, necesidadActual));
            close(); // Cierra el diálogo después de disparar evento

        } catch (Exception ex) {
            Notification.show("Error inesperado al preparar/validar datos: " + ex.getMessage(), 4000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            ex.printStackTrace();
        }
    }
    // --- FIN MÉTODO ---

    // --- MÉTODO setNecesidad MODIFICADO (manual) ---
     public void setNecesidad(NecesidadVuelo necesidad, Vuelo vueloPadre) {
        this.necesidadActual = necesidad;
        this.vueloPadre = vueloPadre; // Guarda la referencia al vuelo

        // Poblar/limpiar campos manualmente
        if (necesidad != null) {
            posicion.setValue(necesidad.getPosicion());
            // IntegerField maneja int, asegurarse que no sea null si se setea
            cantidadAgentes.setValue(necesidad.getCantidadAgentes() > 0 ? necesidad.getCantidadAgentes() : null ); // Poner null si es 0? o 1?
            inicioCobertura.setValue(necesidad.getInicioCobertura());
            finCobertura.setValue(necesidad.getFinCobertura());

            // Habilitar botón Guardar porque hay bean
            save.setEnabled(true);
        } else {
            // Limpiar campos
            posicion.clear();
            cantidadAgentes.clear();
            inicioCobertura.clear();
            finCobertura.clear();
            // Deshabilitar botón Guardar
            save.setEnabled(false);
        }
    }
    // --- FIN MÉTODO ---


    // --- Eventos Personalizados (Sin cambios) ---
     public static abstract class NecesidadVueloFormEvent extends ComponentEvent<NecesidadVueloForm> {
         private NecesidadVuelo necesidad;
         protected NecesidadVueloFormEvent(NecesidadVueloForm source, NecesidadVuelo necesidad) { super(source, false); this.necesidad = necesidad; }
         public NecesidadVuelo getNecesidad() { return necesidad; }
     }
     public static class SaveEvent extends NecesidadVueloFormEvent { SaveEvent(NecesidadVueloForm source, NecesidadVuelo necesidad) { super(source, necesidad); } }
     public static class CloseEvent extends NecesidadVueloFormEvent { CloseEvent(NecesidadVueloForm source) { super(source, null); } }
     public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener) { return getEventBus().addListener(eventType, listener); }
}