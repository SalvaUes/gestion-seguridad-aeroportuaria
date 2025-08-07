package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui.dialogs;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.function.Consumer;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.ReglaDeTurno;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.TipoTurno;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;

public class ReglaTurnoFormDialog extends Dialog {

    private ReglaDeTurno reglaDeTurno;
    private final Consumer<ReglaDeTurno> saveListener;
    private Binder<ReglaDeTurno> binder = new Binder<>(ReglaDeTurno.class);

    // --- CAMBIO CLAVE: De ComboBox a CheckboxGroup para selección múltiple ---
    private CheckboxGroup<DayOfWeek> diasDeLaSemana = new CheckboxGroup<>("Días de la semana");
    private TimePicker horaInicio = new TimePicker("Hora de inicio");
    private TimePicker horaFin = new TimePicker("Hora de fin");
    private ComboBox<TipoTurno> tipoTurno = new ComboBox<>("Tipo de turno");

    public ReglaTurnoFormDialog(ReglaDeTurno regla, Consumer<ReglaDeTurno> saveListener) {
        this.reglaDeTurno = regla;
        this.saveListener = saveListener;

        if (this.reglaDeTurno == null) {
            this.reglaDeTurno = new ReglaDeTurno();
            setHeaderTitle("Nueva Regla");
        } else {
            setHeaderTitle("Editar Regla");
        }
        
        setWidth("450px");
        add(createFormLayout());
        createFooter();
        
        binder.setBean(this.reglaDeTurno);
    }

    private FormLayout createFormLayout() {
        // Configuramos el CheckboxGroup
        diasDeLaSemana.setItems(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
        diasDeLaSemana.setItemLabelGenerator(d -> d.getDisplayName(TextStyle.FULL, new Locale("es", "ES")));
        diasDeLaSemana.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        
        horaInicio.setStep(Duration.ofMinutes(30));
        horaFin.setStep(Duration.ofMinutes(30));
        
        tipoTurno.setItems(TipoTurno.values());

        // Actualizamos el binder
        binder.forField(diasDeLaSemana).asRequired("Debe seleccionar al menos un día.").bind(ReglaDeTurno::getDiasDeLaSemana, ReglaDeTurno::setDiasDeLaSemana);
        binder.forField(horaInicio).asRequired().bind(ReglaDeTurno::getHoraInicio, ReglaDeTurno::setHoraInicio);
        binder.forField(horaFin).asRequired().bind(ReglaDeTurno::getHoraFin, ReglaDeTurno::setHoraFin);
        binder.forField(tipoTurno).asRequired().bind(ReglaDeTurno::getTipoTurno, ReglaDeTurno::setTipoTurno);

        FormLayout layout = new FormLayout(diasDeLaSemana, horaInicio, horaFin, tipoTurno);
        layout.setColspan(diasDeLaSemana, 2); // Hacemos que los checkboxes ocupen todo el ancho
        return layout;
    }

    private void createFooter() {
        Button cancelButton = new Button("Cancelar", e -> close());
        Button saveButton = new Button("Guardar Regla", e -> save());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(cancelButton, saveButton);
    }

    private void save() {
        try {
            binder.writeBean(reglaDeTurno);
            saveListener.accept(reglaDeTurno);
            close();
        } catch (ValidationException e) {
            // El binder mostrará los errores en los campos
        }
    }
}