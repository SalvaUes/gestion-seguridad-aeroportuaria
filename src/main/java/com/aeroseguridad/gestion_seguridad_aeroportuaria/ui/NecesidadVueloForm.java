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
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.shared.Registration;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class NecesidadVueloForm extends Dialog {

    // --- Componentes ---
    ComboBox<PosicionSeguridad> posicion = new ComboBox<>("Posición Requerida");
    IntegerField cantidadAgentes = new IntegerField("Cantidad Agentes");
    DateTimePicker inicioCoberturaDT = new DateTimePicker("Inicio Cobertura");
    DateTimePicker finCoberturaDT = new DateTimePicker("Fin Cobertura");
    TimePicker inicioCoberturaT = new TimePicker("Hora Inicio Cobertura");
    TimePicker finCoberturaT = new TimePicker("Hora Fin Cobertura");

    Button save = new Button("Guardar Necesidad");
    Button cancel = new Button("Cancelar");

    private NecesidadVuelo necesidadActual;
    private Vuelo vueloPadre;
    private boolean isTemplateMode = false;
    private final Validator validator;

    public NecesidadVueloForm(List<PosicionSeguridad> posiciones) {
        setHeaderTitle("Añadir/Editar Necesidad de Seguridad");
        setDraggable(true);
        setResizable(true);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        configureFields(posiciones);

        FormLayout formLayout = new FormLayout(
            posicion, cantidadAgentes, 
            inicioCoberturaDT, finCoberturaDT, 
            inicioCoberturaT, finCoberturaT
        );
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));
        formLayout.setColspan(posicion, 2);

        HorizontalLayout buttonLayout = createButtonsLayout();
        add(formLayout, buttonLayout);
    }
    
    private void configureFields(List<PosicionSeguridad> posiciones) {
        posicion.setItems(posiciones);
        if (posiciones == null || posiciones.isEmpty()) {
            posicion.setPlaceholder("¡No hay posiciones definidas!");
        }
        posicion.setItemLabelGenerator(p -> p != null ? p.getNombrePosicion() : "");
        posicion.setRequiredIndicatorVisible(true);

        cantidadAgentes.setRequiredIndicatorVisible(true);
        cantidadAgentes.setStepButtonsVisible(true);
        cantidadAgentes.setMin(1);
        cantidadAgentes.setValue(1);

        inicioCoberturaDT.setRequiredIndicatorVisible(true);
        finCoberturaDT.setRequiredIndicatorVisible(true);
        inicioCoberturaDT.setStep(Duration.ofMinutes(15));
        finCoberturaDT.setStep(Duration.ofMinutes(15));
        
        inicioCoberturaT.setRequiredIndicatorVisible(true);
        finCoberturaT.setRequiredIndicatorVisible(true);
        inicioCoberturaT.setStep(Duration.ofMinutes(15));
        finCoberturaT.setStep(Duration.ofMinutes(15));
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

        save.setEnabled(false);
        return new HorizontalLayout(save, cancel);
    }

    private void validateAndSaveManually() {
        if (this.necesidadActual == null) {
            Notification.show("Error interno: No hay una necesidad para guardar.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        
        try {
            necesidadActual.setPosicion(posicion.getValue());
            Integer cant = cantidadAgentes.getValue();
            necesidadActual.setCantidadAgentes(cant != null ? cant : 0);
            
            if (isTemplateMode) {
                LocalTime inicio = inicioCoberturaT.getValue();
                LocalTime fin = finCoberturaT.getValue();
                LocalDate placeholderDate = LocalDate.of(1970, 1, 1);
                necesidadActual.setInicioCobertura(inicio != null ? inicio.atDate(placeholderDate) : null);
                necesidadActual.setFinCobertura(fin != null ? fin.atDate(placeholderDate) : null);
                necesidadActual.setVuelo(null);
            } else {
                necesidadActual.setInicioCobertura(inicioCoberturaDT.getValue());
                necesidadActual.setFinCobertura(finCoberturaDT.getValue());
                necesidadActual.setVuelo(this.vueloPadre);
            }
            
            if (necesidadActual.getInicioCobertura() != null && necesidadActual.getFinCobertura() != null &&
                !necesidadActual.getFinCobertura().isAfter(necesidadActual.getInicioCobertura())) {
                Notification.show("Error: La hora/fecha de fin debe ser posterior a la de inicio.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            // --- CAMBIO: Omitir la validación de la entidad en modo plantilla ---
            if (!isTemplateMode) {
                Set<ConstraintViolation<NecesidadVuelo>> violations = validator.validate(necesidadActual);
                if (!violations.isEmpty()) {
                    String errorMsg = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining("; "));
                    Notification.show("Error de validación: " + errorMsg, 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
            }

            fireEvent(new SaveEvent(this, necesidadActual));
            close();

        } catch (Exception ex) {
            Notification.show("Error inesperado al validar: " + ex.getMessage(), 4000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            ex.printStackTrace();
        }
    }

    public void setNecesidad(NecesidadVuelo necesidad, Vuelo vueloPadre) {
        this.necesidadActual = necesidad;
        this.vueloPadre = vueloPadre;
        this.isTemplateMode = (vueloPadre == null);

        if (necesidad == null) {
            save.setEnabled(false);
            return;
        }
        
        posicion.setValue(necesidad.getPosicion());
        cantidadAgentes.setValue(necesidad.getCantidadAgentes() > 0 ? necesidad.getCantidadAgentes() : 1);

        if (isTemplateMode) {
            inicioCoberturaDT.setVisible(false);
            finCoberturaDT.setVisible(false);
            inicioCoberturaT.setVisible(true);
            finCoberturaT.setVisible(true);

            inicioCoberturaT.setValue(necesidad.getInicioCobertura() != null ? necesidad.getInicioCobertura().toLocalTime() : null);
            finCoberturaT.setValue(necesidad.getFinCobertura() != null ? necesidad.getFinCobertura().toLocalTime() : null);
        } else {
            inicioCoberturaDT.setVisible(true);
            finCoberturaDT.setVisible(true);
            inicioCoberturaT.setVisible(false);
            finCoberturaT.setVisible(false);

            inicioCoberturaDT.setValue(necesidad.getInicioCobertura());
            finCoberturaDT.setValue(necesidad.getFinCobertura());
        }
        
        save.setEnabled(true);
    }

    // --- Eventos Personalizados ---
    public static abstract class NecesidadVueloFormEvent extends ComponentEvent<NecesidadVueloForm> {
        private final NecesidadVuelo necesidad;
        protected NecesidadVueloFormEvent(NecesidadVueloForm source, NecesidadVuelo necesidad) { super(source, false); this.necesidad = necesidad; }
        public NecesidadVuelo getNecesidad() { return necesidad; }
    }
    public static class SaveEvent extends NecesidadVueloFormEvent { SaveEvent(NecesidadVueloForm source, NecesidadVuelo necesidad) { super(source, necesidad); } }
    public static class CloseEvent extends NecesidadVueloFormEvent { CloseEvent(NecesidadVueloForm source) { super(source, null); } }
    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener) { return getEventBus().addListener(eventType, listener); }
}