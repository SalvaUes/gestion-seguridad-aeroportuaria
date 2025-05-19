package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.vaadin.flow.component.dependency.CssImport;

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
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Route(value = "vuelos", layout = MainLayout.class)
@PageTitle("Vuelos | Gestión Seguridad")
@PermitAll
public class VueloListView extends VerticalLayout {

    private final VueloService vueloService;
    private final AerolineaService aerolineaService;
    private final NecesidadVueloService necesidadService;
    private final PosicionSeguridadService posicionService;

    private FlexLayout flightCardContainer;
    private TextField filterText;
    private DatePicker fechaInicioFiltro;
    private DatePicker fechaFinFiltro;
    private Button addVueloButton;
    private VueloForm formVuelo;

    private Div rightPanel;
    private H4 tituloNecesidades;
    private Button addNecesidadButton;
    private Grid<NecesidadVuelo> gridNecesidades;
    private NecesidadVueloForm formNecesidadDialog;

    private Vuelo vueloSeleccionado;
    private HorizontalLayout toolbar;
    private SplitLayout splitLayout;

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
    }

    @PostConstruct
    private void initLayout() {
        try {
            createFlightCardContainer();
            createToolbar();
            createGridNecesidades();
            createFormVuelo();
            createFormNecesidadDialog();
            createRightPanel(); // Importante: rightPanel se crea aquí

            if (formVuelo == null || formNecesidadDialog == null) {
                throw new IllegalStateException("Error crítico: Uno o más formularios no pudieron ser instanciados.");
            }

            // flightCardContainer y rightPanel deben estar instanciados antes de esta línea
            splitLayout = new SplitLayout(flightCardContainer, rightPanel);
            splitLayout.setOrientation(SplitLayout.Orientation.HORIZONTAL);
            splitLayout.setSplitterPosition(65);
            splitLayout.setSizeFull();
            splitLayout.addClassName("vuelo-split-layout");

            add(toolbar, splitLayout);
            setDefaultDateFilters();
            updateListVuelos();
            closeEditorVuelo();

        } catch (Exception e) {
            System.err.println("!!! FATAL ERROR during VueloListView initLayout: " + e.getMessage());
            e.printStackTrace();
            removeAll();
            add(new H4("Error crítico al inicializar la vista de Vuelos."));
        }
    }

    private void createRightPanel() {
        // formVuelo debe estar inicializado antes que createRightPanel o manejar su nulidad aquí
        // Lo cual se hace en initLayout, createFormVuelo() es llamado antes.
        if (formVuelo == null) {
             // Esto no debería ocurrir si el orden en initLayout es correcto.
            formVuelo = new VueloForm(Collections.emptyList()); // Fallback muy básico
            Notification.show("Error crítico: Formulario de Vuelo no disponible al crear panel derecho.", 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
        
        tituloNecesidades = new H4("Necesidades de Seguridad");
        tituloNecesidades.addClassName("necesidades-titulo");

        addNecesidadButton = new Button("Añadir Necesidad", VaadinIcon.PLUS_CIRCLE_O.create());
        addNecesidadButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        addNecesidadButton.addClickListener(click -> addNecesidad());

        // gridNecesidades debe estar inicializado. createGridNecesidades() es llamado antes en initLayout.
        if (gridNecesidades == null) {
            gridNecesidades = new Grid<>(NecesidadVuelo.class, false); // Fallback muy básico
             Notification.show("Error crítico: Grid de Necesidades no disponible.", 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }

        VerticalLayout needsLayout = new VerticalLayout(tituloNecesidades, addNecesidadButton, gridNecesidades);
        needsLayout.setPadding(false);
        needsLayout.setSpacing(true);
        needsLayout.addClassName("necesidades-section");
        needsLayout.setWidthFull();
        needsLayout.setAlignItems(Alignment.STRETCH);

        rightPanel = new Div(); // Aseguramos que rightPanel es un Div nuevo
        rightPanel.add(formVuelo, needsLayout);
        rightPanel.addClassName("right-panel");
        rightPanel.getElement().getStyle().set("display", "flex");
        rightPanel.getElement().getStyle().set("flex-direction", "column");
        rightPanel.setVisible(false); // Inicialmente oculto
    }

    private void createToolbar() {
        filterText = new TextField();
        filterText.setPlaceholder("Buscar por nº vuelo, origen, destino...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateListVuelos());
        filterText.setPrefixComponent(VaadinIcon.SEARCH.create());

        fechaInicioFiltro = new DatePicker("Desde");
        fechaFinFiltro = new DatePicker("Hasta");
        fechaInicioFiltro.setClearButtonVisible(true);
        fechaFinFiltro.setClearButtonVisible(true);
        fechaInicioFiltro.addValueChangeListener(e -> updateListVuelos());
        fechaFinFiltro.addValueChangeListener(e -> updateListVuelos());

        addVueloButton = new Button("Nuevo Vuelo", VaadinIcon.PLUS_CIRCLE.create());
        addVueloButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addVueloButton.addClickListener(click -> addVuelo());

        toolbar = new HorizontalLayout(filterText, fechaInicioFiltro, fechaFinFiltro, addVueloButton);
        toolbar.addClassName("vuelo-toolbar");
        toolbar.setAlignItems(Alignment.BASELINE);
        toolbar.setWidthFull();
        toolbar.setFlexGrow(1, filterText);
        toolbar.setSpacing(true);
    }

    private void createFlightCardContainer() {
        flightCardContainer = new FlexLayout();
        flightCardContainer.addClassName("vuelo-card-container");
        flightCardContainer.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        flightCardContainer.setAlignItems(FlexComponent.Alignment.STRETCH);
        flightCardContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        flightCardContainer.getStyle().set("overflow-y", "auto");
        flightCardContainer.getStyle().set("padding", "var(--lumo-space-m)");
        flightCardContainer.setSizeFull();
    }
    
    public static String formatDateTimeStatic(LocalDateTime dateTime) {
        return dateTime == null ? "N/A" : dateTime.format(DT_FORMATTER);
    }

    private void createFormVuelo() {
        try {
            List<Aerolinea> aerolineas = aerolineaService.findAll();
            formVuelo = new VueloForm(aerolineas);
            formVuelo.setWidth("100%");
            formVuelo.addListener(VueloForm.SaveEvent.class, this::saveVuelo);
            formVuelo.addListener(VueloForm.DeleteEvent.class, this::deleteVuelo);
            formVuelo.addListener(VueloForm.CloseEvent.class, e -> closeEditorVuelo());
            formVuelo.addClassName("vuelo-form-panel");
        } catch (Exception e) {
            formVuelo = null; // Marcar como nulo si falla
            System.err.println("!!! ERROR creando VueloForm: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createGridNecesidades() {
        gridNecesidades = new Grid<>(NecesidadVuelo.class, false);
        gridNecesidades.addClassName("necesidad-grid");
        gridNecesidades.setWidthFull();
        gridNecesidades.setMinHeight("150px");

        gridNecesidades.addColumn(nec -> nec.getPosicion() != null ? nec.getPosicion().getNombrePosicion() : "N/A")
                .setHeader("Posición").setSortable(true).setAutoWidth(true).setResizable(true);
        gridNecesidades.addColumn(NecesidadVuelo::getCantidadAgentes)
                .setHeader("Cant.").setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END)
                .setAutoWidth(true).setResizable(true).setFlexGrow(0);
        gridNecesidades.addColumn(nec -> formatDateTimeStatic(nec.getInicioCobertura()))
                .setHeader("Inicio Cob.").setSortable(true).setAutoWidth(true).setResizable(true);
        gridNecesidades.addColumn(nec -> formatDateTimeStatic(nec.getFinCobertura()))
                .setHeader("Fin Cob.").setSortable(true).setAutoWidth(true).setResizable(true);

        gridNecesidades.addColumn(new ComponentRenderer<>(necesidad -> {
            Button editBtn = new Button(VaadinIcon.EDIT.create());
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY_INLINE);
            editBtn.addClickListener(e -> editNecesidad(necesidad));
            editBtn.setTooltipText("Editar Necesidad");

            Button deleteBtn = new Button(VaadinIcon.TRASH.create());
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
            deleteBtn.addClickListener(e -> deleteNecesidad(necesidad));
            deleteBtn.setTooltipText("Eliminar Necesidad");

            HorizontalLayout buttons = new HorizontalLayout(editBtn, deleteBtn);
            buttons.setSpacing(false);
            buttons.setPadding(false);
            buttons.getThemeList().add("spacing-xs");
            return buttons;
        })).setHeader("Acciones").setFlexGrow(0).setWidth("100px").setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.CENTER);
    }

    private void createFormNecesidadDialog() {
        try {
            List<PosicionSeguridad> posicionesActivas = posicionService.findAllActive();
            if (CollectionUtils.isEmpty(posicionesActivas)) {
                System.err.println("WARN: No hay posiciones de seguridad ACTIVAS para el formulario de Necesidad.");
            }
            formNecesidadDialog = new NecesidadVueloForm(posicionesActivas);
            formNecesidadDialog.addListener(NecesidadVueloForm.SaveEvent.class, this::saveNecesidad);
            formNecesidadDialog.addListener(NecesidadVueloForm.CloseEvent.class, e -> formNecesidadDialog.close());
        } catch (Exception e) {
            formNecesidadDialog = null; // Marcar como nulo si falla
            System.err.println("!!! ERROR creando NecesidadVueloForm: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateListVuelos() {
        if (flightCardContainer == null || fechaInicioFiltro == null || fechaFinFiltro == null || filterText == null) {
            // Componentes de UI no listos, no hacer nada.
            if(flightCardContainer != null) flightCardContainer.removeAll(); // Limpiar por si acaso
            return;
        }

        LocalDate fechaInicio = fechaInicioFiltro.getValue();
        LocalDate fechaFin = fechaFinFiltro.getValue();
        String textoFiltro = filterText.getValue();
        List<Vuelo> vuelos;

        try {
            // Lógica de carga de vuelos revertida a la original del usuario
            if (fechaInicio != null && fechaFin != null) {
                if (fechaFin.isBefore(fechaInicio)) {
                    Notification.show("La 'Fecha Hasta' debe ser posterior o igual a la 'Fecha Desde'.", 3000, Notification.Position.BOTTOM_START)
                            .addThemeVariants(NotificationVariant.LUMO_WARNING);
                    vuelos = Collections.emptyList();
                } else {
                    LocalDateTime inicioRango = fechaInicio.atStartOfDay();
                    LocalDateTime finRango = fechaFin.atTime(LocalTime.MAX);
                    // Asumiendo que tu servicio VueloService tiene este método exacto o uno similar
                    // que puede manejar un textoFiltro posiblemente vacío o nulo.
                    // Si el método espera que textoFiltro no sea nulo, envuelve con StringUtils.hasText
                    vuelos = vueloService.findVuelosByDateRangeAndNumeroVueloForView(inicioRango, finRango, textoFiltro);
                }
            } else if (StringUtils.hasText(textoFiltro)) {
                vuelos = vueloService.findByNumeroVueloForView(textoFiltro);
            } else {
                vuelos = vueloService.findAllForView();
            }
        } catch (Exception e) {
            Notification.show("Error al cargar vuelos: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            vuelos = Collections.emptyList();
            e.printStackTrace();
        }

        flightCardContainer.removeAll();
        if (CollectionUtils.isEmpty(vuelos)) {
            Span mensajeVacio = new Span("No se encontraron vuelos para los criterios seleccionados.");
            mensajeVacio.addClassName("empty-flight-list-message");
            flightCardContainer.add(mensajeVacio);
            flightCardContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
            flightCardContainer.setAlignItems(FlexComponent.Alignment.CENTER);
        } else {
            flightCardContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
            flightCardContainer.setAlignItems(FlexComponent.Alignment.STRETCH);
            // Necesidad de pasar 'this' (VueloListView) a VueloCard si VueloCard lo requiere.
            vuelos.forEach(vuelo -> flightCardContainer.add(new VueloCard(vuelo, necesidadService, this)));
        }
    }

    private void updateNecesidadesGrid(Vuelo vuelo) {
        if (gridNecesidades == null) return; // Grid no inicializado
        if (vuelo != null && vuelo.getIdVuelo() != null) {
            try {
                List<NecesidadVuelo> necesidades = necesidadService.findByVueloId(vuelo.getIdVuelo());
                gridNecesidades.setItems(necesidades);
            } catch (EntityNotFoundException enf) {
                Notification.show("Error: El vuelo seleccionado ya no existe.", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
                gridNecesidades.setItems(Collections.emptyList());
                closeEditorVuelo(); // Cierra el editor si el vuelo ya no existe
            } catch (Exception e) {
                Notification.show("Error al cargar necesidades del vuelo: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
                gridNecesidades.setItems(Collections.emptyList());
                e.printStackTrace();
            }
        } else {
            gridNecesidades.setItems(Collections.emptyList());
        }
    }

    private void setDefaultDateFilters() {
        // No se establecen valores por defecto, así se cargan todos los vuelos inicialmente
    }

    private void addVuelo() {
        if (formVuelo == null) { // Chequeo de seguridad
            Notification.show("Error: Formulario de Vuelo no está disponible.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        this.vueloSeleccionado = null;
        editVuelo(new Vuelo()); // Pasa un nuevo objeto Vuelo
    }

    public void editVuelo(Vuelo vuelo) {
        if (formVuelo == null || rightPanel == null) { // Chequeos de seguridad
             Notification.show("Error: Componentes de edición no están disponibles.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        this.vueloSeleccionado = vuelo; // Actualiza el vuelo seleccionado

        if (vuelo == null) { // Si el vuelo es nulo (ej. al deseleccionar)
            closeEditorVuelo();
        } else {
            formVuelo.setVuelo(vuelo); // Establece el vuelo en el formulario
            updateNecesidadesGrid(vuelo); // Actualiza el grid de necesidades para este vuelo
            rightPanel.setVisible(true); // Hace visible el panel derecho
            // La línea eliminada sobre splitLayout.setSecondaryComponent no es necesaria aquí.
        }
    }
    
    public void refreshSelectedVueloCard() {
        if (vueloSeleccionado != null) {
            updateListVuelos(); 
            // Considera una actualización más fina en el futuro si el rendimiento se ve afectado.
        }
    }

    private void addNecesidad() {
        if (formNecesidadDialog == null) {
            Notification.show("Error: Formulario de Necesidad no inicializado.").addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        if (this.vueloSeleccionado == null || this.vueloSeleccionado.getIdVuelo() == null) {
            Notification.show("Seleccione y guarde un vuelo primero antes de añadir necesidades.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_WARNING);
            return;
        }
        NecesidadVuelo nuevaNecesidad = new NecesidadVuelo();
        formNecesidadDialog.setNecesidad(nuevaNecesidad, this.vueloSeleccionado);
        formNecesidadDialog.open();
    }

    private void editNecesidad(NecesidadVuelo necesidad) {
        if (formNecesidadDialog == null || necesidad == null) return;
        
        // Asegurarse que vueloSeleccionado es el padre correcto de la necesidad
        // Esto es importante si el contexto de vueloSeleccionado pudiera cambiar.
        // Aquí asumimos que vueloSeleccionado es el correcto.
        Vuelo vueloAsociado = this.vueloSeleccionado; 
        if (vueloAsociado == null || necesidad.getVuelo() == null || !necesidad.getVuelo().getIdVuelo().equals(vueloAsociado.getIdVuelo())) {
             // Intenta obtener el vuelo de la necesidad si es diferente o si vueloSeleccionado es nulo
            if (necesidad.getVuelo() != null) {
                vueloAsociado = necesidad.getVuelo();
            } else {
                Notification.show("Error: No se pudo determinar el vuelo asociado a esta necesidad.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
        }
        formNecesidadDialog.setNecesidad(necesidad, vueloAsociado);
        formNecesidadDialog.open();
    }

    private void saveNecesidad(NecesidadVueloForm.SaveEvent event) {
        try {
            necesidadService.save(event.getNecesidad());
            if (this.vueloSeleccionado != null && event.getNecesidad().getVuelo().getIdVuelo().equals(this.vueloSeleccionado.getIdVuelo())) {
                updateNecesidadesGrid(this.vueloSeleccionado);
                refreshSelectedVueloCard();
            } else { // Si la necesidad guardada no es del vuelo actualmente seleccionado, igual recarga todo
                updateListVuelos();
            }
            Notification.show("Necesidad guardada.", 1500, Notification.Position.BOTTOM_START).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            formNecesidadDialog.close();
        } catch (ConstraintViolationException e) {
            String violations = e.getConstraintViolations().stream().map(ConstraintViolation::getMessage).distinct().collect(Collectors.joining("; "));
            String errorMsg = violations.contains("posterior a la hora de inicio") ? "Error: La hora de fin de cobertura debe ser posterior a la de inicio." : "Error de validación: " + violations;
            Notification.show(errorMsg, 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (DataIntegrityViolationException e) {
            Notification.show("Error: Ya existe una necesidad para esta posición en este vuelo.", 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            Notification.show("Error inesperado al guardar necesidad: " + e.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            e.printStackTrace();
        }
    }

    private void deleteNecesidad(NecesidadVuelo necesidad) {
        if (necesidad != null && necesidad.getIdNecesidad() != null) {
            try {
                Long idVueloAfectado = necesidad.getVuelo().getIdVuelo();
                necesidadService.deleteById(necesidad.getIdNecesidad());
                
                if (this.vueloSeleccionado != null && this.vueloSeleccionado.getIdVuelo().equals(idVueloAfectado)) {
                    updateNecesidadesGrid(this.vueloSeleccionado);
                    refreshSelectedVueloCard();
                } else { // Si la necesidad eliminada no es del vuelo actualmente seleccionado
                     updateListVuelos(); // Recarga todo para asegurar consistencia de contadores en tarjetas
                }
                Notification.show("Necesidad eliminada.", 1500, Notification.Position.BOTTOM_START).addThemeVariants(NotificationVariant.LUMO_CONTRAST);
            } catch (Exception e) {
                Notification.show("Error al eliminar necesidad: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
                e.printStackTrace();
            }
        }
    }

    private void saveVuelo(VueloForm.SaveEvent event) {
        try {
            Vuelo vueloGuardado = vueloService.save(event.getVuelo());
            updateListVuelos(); 
            editVuelo(vueloGuardado); // Mantiene el panel derecho abierto con el vuelo guardado/actualizado
            Notification.show("Vuelo guardado.", 2000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (ConstraintViolationException e) {
            String violations = e.getConstraintViolations().stream().map(ConstraintViolation::getMessage).distinct().collect(Collectors.joining("; "));
            Notification.show("Error de validación al guardar vuelo: " + violations, 5000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (DataIntegrityViolationException e) {
            String errorMsg = "Error de integridad de datos al guardar vuelo.";
            if (e.getMostSpecificCause().getMessage().toLowerCase().contains("vuelos_numero_vuelo_key")) { // Ajusta 'vuelos_numero_vuelo_key' al nombre real de tu constraint unique
                errorMsg = "Error: El número de vuelo '" + event.getVuelo().getNumeroVuelo() + "' ya existe.";
                if (formVuelo != null && formVuelo.numeroVuelo != null) formVuelo.numeroVuelo.setInvalid(true);
            }
            Notification.show(errorMsg, 5000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            Notification.show("Error inesperado al guardar vuelo: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
            e.printStackTrace();
        }
    }

    private void deleteVuelo(VueloForm.DeleteEvent event) {
        Vuelo vueloAEliminar = event.getVuelo();
        if (formVuelo == null || vueloAEliminar == null || vueloAEliminar.getIdVuelo() == null) {
            Notification.show("No se puede eliminar un vuelo no guardado o no seleccionado.", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_WARNING);
            return;
        }
        try {
            // Considerar si hay que eliminar explícitamente las necesidades asociadas ANTES de eliminar el vuelo,
            // dependiendo de la configuración de CascadeType en la entidad Vuelo.
            // Si Vuelo tiene CascadeType.REMOVE o ALL para necesidades, se borrarán automáticamente.
            // Si no, podrían causar un DataIntegrityViolationException si hay un FK constraint.
            // Tu código original indica que el servicio ya borra necesidades, lo cual es bueno.
            vueloService.deleteById(vueloAEliminar.getIdVuelo());
            updateListVuelos();
            closeEditorVuelo();
            Notification.show("Vuelo eliminado.", 2000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_CONTRAST);
        } catch (DataIntegrityViolationException e) {
            // Mensaje más específico si es posible, ej. si hay turnos asociados
            Notification.show("Error: No se pudo eliminar el vuelo. Verifique si tiene turnos u otras dependencias.", 5000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
            e.printStackTrace();
        } catch (Exception e) {
            Notification.show("Error al eliminar vuelo: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
            e.printStackTrace();
        }
    }

    private void closeEditorVuelo() {
        if (formVuelo != null) {
            formVuelo.setVuelo(null);
        }
        if (rightPanel != null) {
            rightPanel.setVisible(false);
        }
        this.vueloSeleccionado = null;
        // Opcional: colapsar el SplitLayout si se desea quitar el panel derecho completamente
        // if (splitLayout != null && rightPanel != null && rightPanel.getParent().isPresent()) {
        //     splitLayout.setSecondaryComponent(null); // Esto quita el componente del slot
        // }
    }
}