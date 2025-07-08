// RUTA: src/main/java/com/aeroseguridad/gestion_seguridad_aeroportuaria/ui/VueloListView.java
package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Aerolinea;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.NecesidadVuelo;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PosicionSeguridad;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Vuelo;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.AerolineaService;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.NecesidadVueloService;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.PosicionSeguridadService;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.VueloService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;

@Route(value = "vuelos", layout = MainLayout.class)
@PageTitle("Vuelos | Gestión Seguridad")
@PermitAll
public class VueloListView extends VerticalLayout {

    // --- Services ---
    private final VueloService vueloService;
    private final AerolineaService aerolineaService;
    private final NecesidadVueloService necesidadService;
    private final PosicionSeguridadService posicionService;

    // --- UI Components ---
    private FlexLayout flightCardContainer;
    private VueloForm formVuelo;
    private NecesidadVueloForm formNecesidadDialog;

    // --- Filter Components ---
    private TextField filterText = new TextField("Buscar por Nº Vuelo, Origen, Destino...");
    private DatePicker fechaInicioFiltro = new DatePicker("Desde");
    private DatePicker fechaFinFiltro = new DatePicker("Hasta");

    private static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Autowired
    public VueloListView(VueloService vueloService, AerolineaService aerolineaService,
                         NecesidadVueloService necesidadService, PosicionSeguridadService posicionService) {
        this.vueloService = vueloService;
        this.aerolineaService = aerolineaService;
        this.necesidadService = necesidadService;
        this.posicionService = posicionService;
        addClassName("vuelo-list-view");
        setSizeFull();
        setPadding(false);
    }

    @PostConstruct
    private void initLayout() {
        createFlightCardContainer();
        createFormVuelo();
        createFormNecesidadDialog();

        HorizontalLayout headerBar = createHeaderBar();
        Button fab = createFab();

        Div contentWrapper = new Div(flightCardContainer);
        contentWrapper.setSizeFull();
        contentWrapper.getStyle().set("overflow", "auto");
        
        add(headerBar, contentWrapper, fab);
        setDefaultDateFilters();
        updateListVuelos();
    }
    
    private HorizontalLayout createHeaderBar() {
        H2 title = new H2("Gestión de Vuelos");
        title.getStyle().set("font-size", "var(--lumo-font-size-xxl)").set("margin", "0");

        Button filterButton = new Button("Filtros", VaadinIcon.FILTER.create());
        filterButton.addClickListener(e -> openFiltersDialog());

        HorizontalLayout headerBar = new HorizontalLayout(title, filterButton);
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
        fab.setAriaLabel("Añadir nuevo vuelo");
        fab.addClickListener(e -> openVueloEditorDialog(new Vuelo()));
        return fab;
    }

    private void openFiltersDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Filtrar Vuelos");

        filterText.setPlaceholder("Buscar...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);

        fechaInicioFiltro.setClearButtonVisible(true);
        fechaFinFiltro.setClearButtonVisible(true);

        dialog.add(new VerticalLayout(filterText, fechaInicioFiltro, fechaFinFiltro));

        Button applyButton = new Button("Aplicar", e -> {
            updateListVuelos();
            dialog.close();
        });
        applyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        Button clearButton = new Button("Limpiar", e -> {
            filterText.clear();
            fechaInicioFiltro.clear();
            fechaFinFiltro.clear();
            updateListVuelos();
            dialog.close();
        });
        dialog.getFooter().add(clearButton, applyButton);
        dialog.open();
    }

    private void openVueloEditorDialog(Vuelo vuelo) {
        if (formVuelo == null) return;

        Dialog editorDialog = new Dialog();
        editorDialog.setWidth("80vw");
        editorDialog.setMaxWidth("1000px");
        editorDialog.setDraggable(true);

        // Header
        H2 title = new H2(vuelo.getIdVuelo() == null ? "Nuevo Vuelo" : "Editar Vuelo");
        Button closeButton = new Button(VaadinIcon.CLOSE_SMALL.create(), e -> editorDialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        HorizontalLayout dialogHeader = new HorizontalLayout(title, closeButton);
        dialogHeader.setFlexGrow(1, title);
        dialogHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        editorDialog.getHeader().add(dialogHeader);

        // Contenido (Formulario de Vuelo + Grid de Necesidades)
        formVuelo.setVuelo(vuelo);
        VerticalLayout editorContent = createEditorContent(vuelo);
        editorDialog.add(editorContent);

        // Listeners
        formVuelo.addListener(VueloForm.SaveEvent.class, event -> {
            if (saveVuelo(event)) {
                editorDialog.close();
            }
        });
        formVuelo.addListener(VueloForm.DeleteEvent.class, event -> {
            deleteVuelo(event);
            editorDialog.close();
        });
        formVuelo.addListener(VueloForm.CloseEvent.class, e -> editorDialog.close());
        
        editorDialog.open();
    }

    private VerticalLayout createEditorContent(Vuelo vuelo) {
        Grid<NecesidadVuelo> gridNecesidades = createGridNecesidades();
        
        H4 tituloNecesidades = new H4("Necesidades de Seguridad");
        tituloNecesidades.addClassName("necesidades-titulo");

        Button addNecesidadButton = new Button("Añadir Necesidad", VaadinIcon.PLUS_CIRCLE_O.create());
        addNecesidadButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        addNecesidadButton.addClickListener(click -> addNecesidad(vuelo, gridNecesidades));
        addNecesidadButton.setEnabled(vuelo.getIdVuelo() != null); // Solo habilitado para vuelos guardados

        VerticalLayout needsLayout = new VerticalLayout(tituloNecesidades, addNecesidadButton, gridNecesidades);
        needsLayout.setPadding(false);
        needsLayout.setSpacing(true);
        needsLayout.addClassName("necesidades-section");

        if (vuelo.getIdVuelo() != null) {
            gridNecesidades.setItems(necesidadService.findByVueloId(vuelo.getIdVuelo()));
        }

        return new VerticalLayout(formVuelo, needsLayout);
    }
    
    private void createFlightCardContainer() {
        flightCardContainer = new FlexLayout();
        flightCardContainer.addClassName("vuelo-card-container");
    }

    private void createFormVuelo() {
        try {
            List<Aerolinea> aerolineas = aerolineaService.findAll();
            formVuelo = new VueloForm(aerolineas);
            formVuelo.addClassName("vuelo-form-panel");
        } catch (Exception e) {
            formVuelo = null;
        }
    }

    private Grid<NecesidadVuelo> createGridNecesidades() {
        Grid<NecesidadVuelo> grid = new Grid<>(NecesidadVuelo.class, false);
        grid.addClassName("necesidad-grid");
        grid.addColumn(nec -> nec.getPosicion() != null ? nec.getPosicion().getNombrePosicion() : "N/A").setHeader("Posición").setSortable(true);
        grid.addColumn(NecesidadVuelo::getCantidadAgentes).setHeader("Cant.").setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END);
        grid.addColumn(nec -> VueloListView.formatDateTimeStatic(nec.getInicioCobertura())).setHeader("Inicio Cob.");
        grid.addColumn(nec -> VueloListView.formatDateTimeStatic(nec.getFinCobertura())).setHeader("Fin Cob.");
        grid.addColumn(new ComponentRenderer<>(necesidad -> {
            Button editBtn = new Button(VaadinIcon.EDIT.create(), e -> editNecesidad(necesidad, grid));
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY_INLINE);
            Button deleteBtn = new Button(VaadinIcon.TRASH.create(), e -> deleteNecesidad(necesidad, grid));
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
            return new HorizontalLayout(editBtn, deleteBtn);
        })).setHeader("Acciones");
        return grid;
    }

    private void createFormNecesidadDialog() {
        try {
            List<PosicionSeguridad> posicionesActivas = posicionService.findAllActive();
            formNecesidadDialog = new NecesidadVueloForm(posicionesActivas);
        } catch (Exception e) {
            formNecesidadDialog = null;
        }
    }

    private void updateListVuelos() {
        LocalDate fechaInicio = fechaInicioFiltro.getValue();
        LocalDate fechaFin = fechaFinFiltro.getValue();
        List<Vuelo> vuelos;
        try {
            if (fechaInicio != null && fechaFin != null) {
                if (fechaFin.isBefore(fechaInicio)) {
                    vuelos = Collections.emptyList();
                    Notification.show("La 'Fecha Hasta' debe ser posterior o igual a la 'Fecha Desde'.").addThemeVariants(NotificationVariant.LUMO_WARNING);
                } else {
                    vuelos = vueloService.findVuelosByDateRangeAndNumeroVueloForView(fechaInicio.atStartOfDay(), fechaFin.atTime(LocalTime.MAX), filterText.getValue());
                }
            } else {
                 vuelos = vueloService.findVuelosByDateRangeAndNumeroVueloForView(null, null, filterText.getValue());
            }
        } catch (Exception e) {
            vuelos = Collections.emptyList();
        }

        flightCardContainer.removeAll();
        if (CollectionUtils.isEmpty(vuelos)) {
            flightCardContainer.add(new Span("No se encontraron vuelos para los criterios seleccionados."));
        } else {
            vuelos.forEach(vuelo -> {
                VueloCard card = new VueloCard(vuelo, necesidadService);
                card.addCardClickListener(e -> openVueloEditorDialog(e.getVuelo()));
                flightCardContainer.add(card);
            });
        }
    }

    private void addNecesidad(Vuelo vuelo, Grid<NecesidadVuelo> grid) {
        if (formNecesidadDialog == null) return;
        NecesidadVuelo nuevaNecesidad = new NecesidadVuelo();
        formNecesidadDialog.setNecesidad(nuevaNecesidad, vuelo);
        formNecesidadDialog.addListener(NecesidadVueloForm.SaveEvent.class, e -> saveNecesidad(e, grid));
        formNecesidadDialog.open();
    }

    private void editNecesidad(NecesidadVuelo necesidad, Grid<NecesidadVuelo> grid) {
        if (formNecesidadDialog == null || necesidad == null) return;
        formNecesidadDialog.setNecesidad(necesidad, necesidad.getVuelo());
        formNecesidadDialog.addListener(NecesidadVueloForm.SaveEvent.class, e -> saveNecesidad(e, grid));
        formNecesidadDialog.open();
    }

    private void saveNecesidad(NecesidadVueloForm.SaveEvent event, Grid<NecesidadVuelo> grid) {
        try {
            necesidadService.save(event.getNecesidad());
            grid.setItems(necesidadService.findByVueloId(event.getNecesidad().getVuelo().getIdVuelo()));
            updateListVuelos(); // Para refrescar contador de necesidades en la tarjeta
            formNecesidadDialog.close();
        } catch (Exception e) {
            Notification.show("Error al guardar necesidad: " + e.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void deleteNecesidad(NecesidadVuelo necesidad, Grid<NecesidadVuelo> grid) {
        if (necesidad != null && necesidad.getIdNecesidad() != null) {
            Vuelo vueloAfectado = necesidad.getVuelo();
            necesidadService.deleteById(necesidad.getIdNecesidad());
            grid.setItems(necesidadService.findByVueloId(vueloAfectado.getIdVuelo()));
            updateListVuelos(); // Para refrescar contador
        }
    }
    
    private boolean saveVuelo(VueloForm.SaveEvent event) {
        try {
            vueloService.save(event.getVuelo());
            updateListVuelos();
            Notification.show("Vuelo guardado.", 2000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            return true;
        } catch (Exception e) {
            Notification.show("Error inesperado al guardar vuelo: " + e.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }
    }

    private void deleteVuelo(VueloForm.DeleteEvent event) {
        if (event.getVuelo() != null && event.getVuelo().getIdVuelo() != null) {
            vueloService.deleteById(event.getVuelo().getIdVuelo());
            updateListVuelos();
        }
    }

    private void setDefaultDateFilters() {
        fechaInicioFiltro.setValue(LocalDate.now().minusDays(7));
        fechaFinFiltro.setValue(LocalDate.now().plusDays(7));
    }

    public static String formatDateTimeStatic(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DT_FORMATTER);
    }
}