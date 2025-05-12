package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.NecesidadVuelo;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PosicionSeguridad;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Vuelo;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
// import com.vaadin.flow.component.HasValue; // Ya no necesitamos el listener complejo
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.shared.Registration;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class NecesidadVueloForm extends Dialog {

    ComboBox<PosicionSeguridad> posicion = new ComboBox<>("Posición Requerida");
    IntegerField cantidadAgentes = new IntegerField("Cantidad Agentes");
    DateTimePicker inicioCobertura = new DateTimePicker("Inicio Cobertura");
    DateTimePicker finCobertura = new DateTimePicker("Fin Cobertura");

    Button save = new Button("Guardar Necesidad");
    Button cancel = new Button("Cancelar");

    private NecesidadVuelo necesidadActual;
    private Vuelo vueloPadre;

    private final Validator validator;

    public NecesidadVueloForm(List<PosicionSeguridad> posiciones) {
        setHeaderTitle("Añadir/Editar Necesidad de Seguridad");
        setDraggable(true);
        setResizable(true);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        posicion.setItems(posiciones);
        if (posiciones == null || posiciones.isEmpty()) {
            posicion.setPlaceholder("¡No hay posiciones definidas!");
        }
        posicion.setItemLabelGenerator(p -> p != null ? p.getNombrePosicion() : "");
        posicion.setRequiredIndicatorVisible(true);

        cantidadAgentes.setRequiredIndicatorVisible(true);
        cantidadAgentes.setStepButtonsVisible(true);
        cantidadAgentes.setMin(1);
        cantidadAgentes.setValue(1); // Valor por defecto

        inicioCobertura.setRequiredIndicatorVisible(true);
        finCobertura.setRequiredIndicatorVisible(true);
        inicioCobertura.setStep(Duration.ofMinutes(15));
        finCobertura.setStep(Duration.ofMinutes(15));

        // --- ELIMINADO ValueChangeListener para habilitar save ---
        // HasValue.ValueChangeListener enableSaveListener = e -> { ... };
        // y sus llamadas a addValueChangeListener(...)
        // --- FIN ELIMINADO ---

        FormLayout formLayout = new FormLayout(posicion, cantidadAgentes, inicioCobertura, finCobertura);
        HorizontalLayout buttonLayout = createButtonsLayout();
        add(formLayout, buttonLayout);
    }

     private HorizontalLayout createButtonsLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addClickShortcut(Key.ENTER);
        cancel.addClickShortcut(Key.ESCAPE);

        save.addClickListener(event -> validateAndSaveManually());
        cancel.addClickListener(event -> {
            fireEvent(new CloseEvent(this));
            close();
        });

        save.setEnabled(false); // Se habilita en setNecesidad
        return new HorizontalLayout(save, cancel);
     }

     private void validateAndSaveManually() {
        if (this.necesidadActual == null || this.vueloPadre == null) {
             Notification.show("Error interno: No se ha asociado correctamente la necesidad al vuelo.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
             return;
        }
        try {
            necesidadActual.setVuelo(this.vueloPadre);
            necesidadActual.setPosicion(posicion.getValue());
            Integer cant = cantidadAgentes.getValue();
            necesidadActual.setCantidadAgentes(cant != null ? cant : 0);
            necesidadActual.setInicioCobertura(inicioCobertura.getValue());
            necesidadActual.setFinCobertura(finCobertura.getValue());

            // Validar fechas explícitamente
            if (necesidadActual.getInicioCobertura() != null &&
                necesidadActual.getFinCobertura() != null &&
                !necesidadActual.getFinCobertura().isAfter(necesidadActual.getInicioCobertura())) {
                Notification.show("Error: La hora de fin de cobertura debe ser posterior a la hora de inicio.", 3000, Notification.Position.MIDDLE)
                          .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            Set<ConstraintViolation<NecesidadVuelo>> violations = validator.validate(necesidadActual);
            if (!violations.isEmpty()) {
                String errorMsg = violations.stream()
                                    .map(ConstraintViolation::getMessage)
                                    .collect(Collectors.joining("; "));
                 Notification.show("Error de validación: " + errorMsg, 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                 return;
            }

            fireEvent(new SaveEvent(this, necesidadActual));
            close();

        } catch (Exception ex) {
            Notification.show("Error inesperado al preparar/validar datos: " + ex.getMessage(), 4000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            ex.printStackTrace();
        }
    }

     public void setNecesidad(NecesidadVuelo necesidad, Vuelo vueloPadre) {
        this.necesidadActual = necesidad;
        this.vueloPadre = vueloPadre;

        if (necesidad != null) {
            posicion.setValue(necesidad.getPosicion());
            cantidadAgentes.setValue(necesidad.getCantidadAgentes() > 0 ? necesidad.getCantidadAgentes() : 1 ); // Default 1 si es nueva y 0
            inicioCobertura.setValue(necesidad.getInicioCobertura());
            finCobertura.setValue(necesidad.getFinCobertura());

            // Habilitar botón Guardar SIEMPRE que se cargue una necesidad (nueva o existente)
            // La validación ocurrirá al hacer clic en el botón.
            save.setEnabled(true);
        } else {
            posicion.clear();
            cantidadAgentes.clear();
            cantidadAgentes.setValue(1); // Valor por defecto
            inicioCobertura.clear();
            finCobertura.clear();
            save.setEnabled(false); // Deshabilitar si la necesidad es null (al cerrar)
        }
    }

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