package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui.dialogs;

import java.time.Duration;
import java.time.LocalDate;
import java.util.function.Consumer;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.TipoTurno;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Turno;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.TurnoService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;

public class TurnoDialog extends Dialog {

    private final TurnoService turnoService;
    private final Agente agente;
    private Turno turnoActual;
    private final Consumer<Void> onSaveCallback;

    private final DateTimePicker inicioTurno = new DateTimePicker("Inicio del Turno");
    private final DateTimePicker finTurno = new DateTimePicker("Fin del Turno");
    private final ComboBox<TipoTurno> tipoTurno = new ComboBox<>("Tipo de Turno");
    private final Binder<Turno> binder = new BeanValidationBinder<>(Turno.class);

    public TurnoDialog(TurnoService turnoService, Agente agente, Turno turno, LocalDate fechaSeleccionada, Consumer<Void> onSaveCallback) {
        this.turnoService = turnoService;
        this.agente = agente;
        this.onSaveCallback = onSaveCallback;

        boolean isNew = turno == null;
        this.turnoActual = isNew ? createNewTurno(fechaSeleccionada) : turno;
        
        setHeaderTitle(isNew ? "Crear Nuevo Turno" : "Editar Turno");

        FormLayout formLayout = createFormLayout();
        binder.bindInstanceFields(this);
        binder.setBean(this.turnoActual);

        add(formLayout);
        getFooter().add(createButtons());
    }

    private Turno createNewTurno(LocalDate fecha) {
        Turno nuevoTurno = new Turno();
        nuevoTurno.setAgente(this.agente);
        nuevoTurno.setInicioTurno(fecha.atTime(7, 0)); // Hora de inicio por defecto
        nuevoTurno.setFinTurno(fecha.atTime(19, 0)); // Hora de fin por defecto
        nuevoTurno.setTipoTurno(TipoTurno.REGULAR);
        return nuevoTurno;
    }

    private FormLayout createFormLayout() {
        tipoTurno.setItems(TipoTurno.values());
        inicioTurno.setStep(Duration.ofMinutes(30));
        finTurno.setStep(Duration.ofMinutes(30));
        return new FormLayout(inicioTurno, finTurno, tipoTurno);
    }

    private HorizontalLayout createButtons() {
        Button saveButton = new Button("Guardar", VaadinIcon.CHECK.create());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> saveTurno());

        Button deleteButton = new Button("Eliminar", VaadinIcon.TRASH.create());
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteButton.setVisible(turnoActual.getIdTurno() != null); // Solo visible si el turno ya existe
        deleteButton.addClickListener(e -> deleteTurno());

        Button cancelButton = new Button("Cancelar", e -> close());
        
        return new HorizontalLayout(saveButton, deleteButton, cancelButton);
    }

    private void saveTurno() {
        try {
            binder.writeBean(turnoActual);
            turnoService.save(turnoActual);
            Notification.show("Turno guardado.", 2000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            onSaveCallback.accept(null); // Llama al callback para refrescar el calendario
            close();
        } catch (Exception e) {
            Notification.show("Error al guardar: " + e.getMessage(), 4000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void deleteTurno() {
        try {
            turnoService.deleteById(turnoActual.getIdTurno());
            Notification.show("Turno eliminado.", 2000, Notification.Position.BOTTOM_CENTER);
            onSaveCallback.accept(null); // Llama al callback para refrescar el calendario
            close();
        } catch (Exception e) {
            Notification.show("Error al eliminar: " + e.getMessage(), 4000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}