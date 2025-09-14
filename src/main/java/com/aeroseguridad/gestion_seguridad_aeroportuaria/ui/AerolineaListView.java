// RUTA: src/main/java/com/aeroseguridad/gestion_seguridad_aeroportuaria/ui/AerolineaListView.java
package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Aerolinea;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.AerolineaService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
 

@Route(value = "aerolineas", layout = MainLayout.class)
@PageTitle("Aerolíneas | Gestión Seguridad")
@PermitAll
public class AerolineaListView extends VerticalLayout {

    private final AerolineaService aerolineaService;
    private Grid<Aerolinea> grid = new Grid<>(Aerolinea.class, false);

    public AerolineaListView(AerolineaService aerolineaService) {
        this.aerolineaService = aerolineaService;
        addClassName("aerolinea-list-view");
        setSizeFull();
        setPadding(false);
    }

    @PostConstruct
    private void initLayout() {
        configureGrid();

        HorizontalLayout headerBar = createHeaderBar();
        Button fab = createFab();

        Div contentWrapper = new Div(grid);
        contentWrapper.setSizeFull();
        contentWrapper.getStyle().set("overflow", "auto");
        contentWrapper.getStyle().set("padding", "0 var(--lumo-space-m)");

        add(headerBar, contentWrapper, fab);
        updateList();
    }

    private HorizontalLayout createHeaderBar() {
        H2 title = new H2("Gestión de Aerolíneas");
        title.getStyle().set("font-size", "var(--lumo-font-size-xxl)").set("margin", "0");

        // Esta vista no tiene filtros, por lo que el header es más simple.
        HorizontalLayout headerBar = new HorizontalLayout(title);
        headerBar.setAlignItems(FlexComponent.Alignment.CENTER);
        headerBar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        headerBar.setWidthFull();
        headerBar.getStyle().set("padding", "var(--lumo-space-m)");
        headerBar.getStyle().set("border-bottom", "1px solid var(--lumo-contrast-10pct)");

        return headerBar;
    }

    private Button createFab() {
        Button fab = new Button(VaadinIcon.PLUS.create());
        fab.addClassName("fab");
        fab.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        fab.setAriaLabel("Añadir nueva aerolínea");
        fab.addClickListener(e -> openAerolineaFormDialog(new Aerolinea()));
        return fab;
    }

    private void openAerolineaFormDialog(Aerolinea aerolinea) {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        dialog.setDraggable(true);
        dialog.setResizable(true);

        // --- Header del diálogo ---
        H2 title = new H2(aerolinea.getIdAerolinea() == null ? "Nueva Aerolínea" : "Editar Aerolínea");
        Button closeButton = new Button(VaadinIcon.CLOSE_SMALL.create(), e -> dialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        HorizontalLayout dialogHeader = new HorizontalLayout(title, closeButton);
        dialogHeader.setFlexGrow(1, title);
        dialogHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        dialog.getHeader().add(dialogHeader);

        // --- Creación del formulario DENTRO del diálogo ---
        FormLayout formLayout = new FormLayout();
        TextField nombre = new TextField("Nombre");
        TextField codigoIata = new TextField("Código IATA");
        Checkbox activo = new Checkbox("Activo", true);

        Binder<Aerolinea> binder = new BeanValidationBinder<>(Aerolinea.class);
        binder.bind(nombre, "nombre");
        binder.bind(codigoIata, "codigoIata");
        binder.bind(activo, "activo");
        binder.setBean(aerolinea);

        formLayout.add(nombre, codigoIata, activo);
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );
        dialog.add(formLayout);

        // --- Botones del Footer ---
        Button saveButton = new Button("Guardar", e -> {
            try {
                binder.writeBean(aerolinea);
                aerolineaService.save(aerolinea);
                Notification.show("Aerolínea guardada.", 2000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateList();
                dialog.close();
            } catch (ValidationException ex) {
                Notification.show("Error de validación. Revise los campos.", 3000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (Exception ex) {
                Notification.show("Error al guardar: " + ex.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("Cancelar", e -> dialog.close());
        dialog.getFooter().add(cancelButton, saveButton);

        dialog.open();
    }

    private void configureGrid() {
        grid.addClassName("aerolinea-grid");
        grid.setSizeFull();
        grid.addColumn(Aerolinea::getNombre).setHeader("Nombre").setSortable(true);
        grid.addColumn(Aerolinea::getCodigoIata).setHeader("Código IATA").setSortable(true);
        grid.addColumn(aerolinea -> aerolinea.getActivo() ? "Sí" : "No").setHeader("Activo").setSortable(true);
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
        grid.asSingleSelect().addValueChangeListener(e -> {
            if (e.getValue() != null) {
                openAerolineaFormDialog(e.getValue());
            }
        });
    }

    private void updateList() {
        grid.setItems(aerolineaService.findAll());
    }
}