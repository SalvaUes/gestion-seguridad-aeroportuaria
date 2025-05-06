package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

// Asegúrate de tener todos los imports necesarios
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Aerolinea;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.AerolineaService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route; // Import correcto
import jakarta.annotation.security.PermitAll; // Import correcto
import org.springframework.beans.factory.annotation.Autowired;


// Modificación: layout = MainLayout.class
@Route(value = "aerolineas", layout = MainLayout.class)
@PageTitle("Aerolíneas | Gestión Seguridad")
@PermitAll
public class AerolineaListView extends VerticalLayout {

    private final AerolineaService aerolineaService;

    private Grid<Aerolinea> grid = new Grid<>(Aerolinea.class, false);
    private FormLayout form = new FormLayout();
    private TextField nombre = new TextField("Nombre");
    private TextField codigoIata = new TextField("Código IATA");
    private Checkbox activo = new Checkbox("Activo");
    private Button saveButton = new Button("Guardar");
    private Button cancelButton = new Button("Cancelar");
    private Button addButton = new Button("Nueva Aerolínea");

    private Binder<Aerolinea> binder = new BeanValidationBinder<>(Aerolinea.class);
    private Aerolinea aerolineaActual;

    @Autowired
    public AerolineaListView(AerolineaService aerolineaService) {
        this.aerolineaService = aerolineaService;
        addClassName("aerolinea-list-view");
        // Quité setSizeFull()

        configureGrid();
        configureForm();
        configureToolbar();

        add(
                configureToolbar(),
                grid,
                form
        );

        updateList();
        closeEditor();
    }

    private HorizontalLayout configureToolbar() {
        addButton.addClickListener(click -> addAerolinea());
        HorizontalLayout toolbar = new HorizontalLayout(addButton);
        toolbar.addClassName("toolbar");
        return toolbar;
    }

    private void configureGrid() {
        grid.addClassName("aerolinea-grid");
        // grid.setSizeFull(); // El layout padre controla tamaño
        grid.addColumn(Aerolinea::getNombre).setHeader("Nombre").setSortable(true);
        grid.addColumn(Aerolinea::getCodigoIata).setHeader("Código IATA").setSortable(true);
        grid.addColumn(aerolinea -> aerolinea.getActivo() ? "Sí" : "No").setHeader("Activo").setSortable(true);
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

    private void configureForm() {
        binder.bindInstanceFields(this);
        saveButton.addClickListener(event -> validateAndSave());
        cancelButton.addClickListener(event -> closeEditor());
        form.add(nombre, codigoIata, activo, saveButton, cancelButton);
    }

    private void updateList() {
        grid.setItems(aerolineaService.findAll());
    }

    private void addAerolinea() {
        grid.asSingleSelect().clear();
        editAerolinea(new Aerolinea());
    }

    private void editAerolinea(Aerolinea aerolinea) {
        if (aerolinea == null) {
            closeEditor();
        } else {
            this.aerolineaActual = aerolinea;
            binder.setBean(aerolineaActual);
            form.setVisible(true);
            addClassName("editing");
            // nombre.focus(); // Comentado para evitar errores previos
        }
    }

    private void validateAndSave() {
        try {
            binder.writeBean(aerolineaActual);
            aerolineaService.save(aerolineaActual);
            Notification.show("Aerolínea guardada.", 3000, Notification.Position.BOTTOM_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            updateList();
            closeEditor();
        } catch (ValidationException e) {
            Notification.show("Error de validación. Revise los campos.", 3000, Notification.Position.BOTTOM_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
             Notification.show("Error al guardar: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void closeEditor() {
        this.aerolineaActual = null;
        binder.setBean(null);
        form.setVisible(false);
        removeClassName("editing");
    }
}