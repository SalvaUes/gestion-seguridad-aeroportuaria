package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

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
import com.vaadin.flow.component.grid.Grid; // Lo mantenemos para Necesidades
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout; // Para las tarjetas
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
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ConstraintViolation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils; // Para StringUtils.hasText

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

    // --- CAMBIO: Ya no hay gridVuelos, ahora es flightCardContainer ---
    // private Grid<Vuelo> gridVuelos;
    private FlexLayout flightCardContainer; // Contenedor para las tarjetas de vuelo
    // --- FIN CAMBIO ---

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
            createFlightCardContainer(); // <--- NUEVO: Crea contenedor de tarjetas
            createToolbar();
            createGridNecesidades(); // Grid para las necesidades del vuelo seleccionado
            createFormVuelo();
            createFormNecesidadDialog();
            createRightPanel(); // Panel que contendrá formVuelo y el grid de necesidades

            if (formVuelo == null || formNecesidadDialog == null) {
                 throw new IllegalStateException("Error crítico: Uno o más formularios no pudieron ser instanciados.");
            }

            // El SplitLayout ahora tiene el contenedor de tarjetas a la izquierda
            splitLayout = new SplitLayout(flightCardContainer, rightPanel);
            splitLayout.setOrientation(SplitLayout.Orientation.HORIZONTAL);
            splitLayout.setSplitterPosition(70); // Ajustar según preferencia (70% para tarjetas)
            splitLayout.setSizeFull();

            add(toolbar, splitLayout);
            setDefaultDateFilters(); // Inicia filtros de fecha vacíos
            updateListVuelos();      // Carga inicial (debería mostrar todos los vuelos)
            closeEditorVuelo();

        } catch (Exception e) {
             System.err.println("!!! FATAL ERROR during VueloListView initLayout: " + e.getMessage());
             e.printStackTrace();
             removeAll();
             add(new H4("Error crítico al inicializar la vista de Vuelos."));
        }
    }

     private void createRightPanel() {
        if (formVuelo == null) {
            rightPanel = new Div(new H4("Error al cargar formulario de vuelo."));
            return;
        }
        if (tituloNecesidades == null) tituloNecesidades = new H4("Necesidades de Seguridad");
        if (addNecesidadButton == null) addNecesidadButton = new Button("Añadir Necesidad", VaadinIcon.PLUS.create());
        addNecesidadButton.addClickListener(click -> addNecesidad());

        VerticalLayout needsLayout = new VerticalLayout(tituloNecesidades, addNecesidadButton, gridNecesidades);
        needsLayout.setPadding(false); needsLayout.setSpacing(true);
        needsLayout.addClassName("necesidades-section"); needsLayout.setWidthFull();

        rightPanel = new Div();
        rightPanel.add(formVuelo, needsLayout);
        rightPanel.addClassName("right-panel");
        rightPanel.setWidth("35%"); // Ancho para el panel de edición/necesidades
        rightPanel.getElement().getStyle().set("flex-shrink", "0");
        rightPanel.setVisible(false);
    }

    private void createToolbar() {
        filterText = new TextField();
        filterText.setPlaceholder("Buscar por nº vuelo...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateListVuelos());

        fechaInicioFiltro = new DatePicker("Fecha Desde");
        fechaFinFiltro = new DatePicker("Fecha Hasta");
        fechaInicioFiltro.setClearButtonVisible(true);
        fechaFinFiltro.setClearButtonVisible(true);
        fechaInicioFiltro.addValueChangeListener(e -> updateListVuelos());
        fechaFinFiltro.addValueChangeListener(e -> updateListVuelos());

        addVueloButton = new Button("Nuevo Vuelo", VaadinIcon.PLUS.create());
        addVueloButton.addClickListener(click -> addVuelo());

        toolbar = new HorizontalLayout(filterText, fechaInicioFiltro, fechaFinFiltro, addVueloButton);
        toolbar.addClassName("toolbar");
        toolbar.setAlignItems(Alignment.BASELINE);
        toolbar.setWidthFull();
        toolbar.setFlexGrow(1, filterText);
    }

    // --- NUEVO MÉTODO para crear el contenedor de tarjetas ---
    private void createFlightCardContainer() {
        flightCardContainer = new FlexLayout();
        flightCardContainer.addClassName("vuelo-card-container"); // Para estilos CSS
        flightCardContainer.setFlexWrap(FlexLayout.FlexWrap.WRAP); // Las tarjetas se ajustan
        flightCardContainer.setAlignItems(FlexComponent.Alignment.START); // Alinea tarjetas arriba
        flightCardContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.START); // Desde la izquierda
        flightCardContainer.getStyle().set("overflow-y", "auto"); // Scroll vertical
        flightCardContainer.getStyle().set("padding", "var(--lumo-space-s)");
        flightCardContainer.setSizeFull(); // Ocupar espacio disponible en el SplitLayout
    }
    // --- FIN NUEVO MÉTODO ---


    // --- MÉTODO createGridVuelos ELIMINADO (ya no se usa para la lista principal) ---
    // private void createGridVuelos() { ... }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DT_FORMATTER);
    }

     private void createFormVuelo() {
        try {
             List<Aerolinea> aerolineas = aerolineaService.findAll();
             formVuelo = new VueloForm(aerolineas); // Usar VueloForm manual
             formVuelo.setWidth("100%");
             formVuelo.addListener(VueloForm.SaveEvent.class, this::saveVuelo);
             formVuelo.addListener(VueloForm.DeleteEvent.class, this::deleteVuelo);
             formVuelo.addListener(VueloForm.CloseEvent.class, e -> closeEditorVuelo());
        } catch (Exception e) {
             formVuelo = null;
             System.err.println("!!! ERROR creando VueloForm: " + e.getMessage());
             e.printStackTrace();
         }
    }

    private void createGridNecesidades() {
        // ... (sin cambios, este grid es para las necesidades del vuelo SELECCIONADO) ...
         gridNecesidades = new Grid<>(NecesidadVuelo.class, false);
         gridNecesidades.addClassName("necesidad-grid");
         gridNecesidades.setWidthFull();
         gridNecesidades.setHeight("200px");
         gridNecesidades.addColumn(nec -> nec.getPosicion() != null ? nec.getPosicion().getNombrePosicion() : "N/A")
             .setHeader("Posición").setSortable(true).setKey("PosicionKeyNec");
         gridNecesidades.addColumn(NecesidadVuelo::getCantidadAgentes)
             .setHeader("Cant.").setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END);
         gridNecesidades.addColumn(nec -> formatDateTime(nec.getInicioCobertura()))
             .setHeader("Inicio Cob.").setSortable(true).setKey("InicioKeyNec");
         gridNecesidades.addColumn(nec -> formatDateTime(nec.getFinCobertura()))
             .setHeader("Fin Cob.").setSortable(true).setKey("FinKeyNec");
         gridNecesidades.addColumn(new ComponentRenderer<>(necesidad -> {
            Button editBtn = new Button(VaadinIcon.EDIT.create());
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
            editBtn.addClickListener(e -> editNecesidad(necesidad));
            Button deleteBtn = new Button(VaadinIcon.TRASH.create());
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            deleteBtn.addClickListener(e -> deleteNecesidad(necesidad));
            HorizontalLayout buttons = new HorizontalLayout(editBtn, deleteBtn);
            buttons.setSpacing(false); buttons.setPadding(false);
            buttons.getThemeList().add("spacing-xs");
            return buttons;
        })).setHeader("Acciones").setFlexGrow(0).setWidth("100px").setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.CENTER);
        gridNecesidades.getColumns().forEach(col -> col.setResizable(true));
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
             formNecesidadDialog = null;
             System.err.println("!!! ERROR creando NecesidadVueloForm: " + e.getMessage());
             e.printStackTrace();
         }
    }

     // --- updateListVuelos MODIFICADO para poblar el flightCardContainer ---
     private void updateListVuelos() {
         if (flightCardContainer == null || fechaInicioFiltro == null || fechaFinFiltro == null || filterText == null) return;

         LocalDate fechaInicio = fechaInicioFiltro.getValue();
         LocalDate fechaFin = fechaFinFiltro.getValue();
         String textoFiltro = filterText.getValue();
         List<Vuelo> vuelos;

         try {
             if (fechaInicio != null && fechaFin != null) { // Filtro por rango de fechas (y texto opcional)
                 if(fechaFin.isBefore(fechaInicio)) {
                     Notification.show("La 'Fecha Hasta' debe ser posterior o igual a la 'Fecha Desde'.", 3000, Notification.Position.BOTTOM_START)
                                 .addThemeVariants(NotificationVariant.LUMO_WARNING);
                     vuelos = Collections.emptyList();
                 } else {
                     LocalDateTime inicioRango = fechaInicio.atStartOfDay();
                     LocalDateTime finRango = fechaFin.atTime(LocalTime.MAX);
                     vuelos = vueloService.findVuelosByDateRangeAndNumeroVueloForView(inicioRango, finRango, textoFiltro);
                 }
             } else if (StringUtils.hasText(textoFiltro)) { // Solo filtro de texto
                  // Usar el método que solo busca por número de vuelo (y trae aerolínea)
                  vuelos = vueloService.findByNumeroVueloForView(textoFiltro);
             } else { // Sin filtros de fecha ni de texto, cargar todos
                  vuelos = vueloService.findAllForView();
             }
         } catch (Exception e) {
            Notification.show("Error al cargar vuelos: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            vuelos = Collections.emptyList();
            e.printStackTrace();
         }

         flightCardContainer.removeAll(); // Limpiar tarjetas anteriores
         if (CollectionUtils.isEmpty(vuelos)) {
             flightCardContainer.add(new Span("No se encontraron vuelos para los criterios seleccionados."));
         } else {
             // Pasar el necesidadService al constructor de VueloCard
             vuelos.forEach(vuelo -> flightCardContainer.add(new VueloCard(vuelo, necesidadService, this)));
         }
    }
    // --- FIN updateListVuelos ---

    private void updateNecesidadesGrid(Vuelo vuelo) {
        // ... (sin cambios) ...
        if (gridNecesidades == null) return;
        if (vuelo != null && vuelo.getIdVuelo() != null) {
             try {
                 List<NecesidadVuelo> necesidades = necesidadService.findByVueloId(vuelo.getIdVuelo());
                 gridNecesidades.setItems(necesidades);
             } catch(EntityNotFoundException enf) {
                  Notification.show("Error: El vuelo seleccionado ya no existe.", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
                  gridNecesidades.setItems(Collections.emptyList());
                  closeEditorVuelo();
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
        // Los campos de fecha inician vacíos para mostrar todos los vuelos por defecto
        // Si fechaInicioFiltro y fechaFinFiltro son null, updateListVuelos cargará todos.
        // No es necesario llamar a updateListVuelos() aquí si los listeners lo hacen,
        // pero la carga inicial en initLayout() ya se encarga.
    }

    private void addVuelo() {
        if (formVuelo == null) return;
        // Ya no hay grid principal de vuelos para deseleccionar
        // gridVuelos.asSingleSelect().clear();
        editVuelo(new Vuelo());
    }

    // editVuelo ahora se llama desde VueloCard o al crear nuevo
    public void editVuelo(Vuelo vuelo) {
        this.vueloSeleccionado = vuelo;
        if (formVuelo == null || rightPanel == null) return;
        if (vuelo == null) {
            closeEditorVuelo();
        } else {
            formVuelo.setVuelo(vuelo); // Usa el form manual
            updateNecesidadesGrid(vuelo); // Carga/limpia las necesidades del vuelo actual
            rightPanel.setVisible(true); // Muestra el panel derecho con el form y las necesidades
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
        // ... (sin cambios) ...
        if (formNecesidadDialog == null || necesidad == null) return;
        Vuelo vueloAsociado = this.vueloSeleccionado;
        if (vueloAsociado == null) {
             Notification.show("Error: No hay un vuelo principal seleccionado para esta necesidad.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
             return;
        }
        formNecesidadDialog.setNecesidad(necesidad, vueloAsociado);
        formNecesidadDialog.open();
    }

    private void saveNecesidad(NecesidadVueloForm.SaveEvent event) {
        try {
            necesidadService.save(event.getNecesidad());
            if (this.vueloSeleccionado != null) {
                 updateNecesidadesGrid(this.vueloSeleccionado);
                 updateListVuelos(); // REFRESCAR EL GRID PRINCIPAL para que se actualice el resumen de la tarjeta
            }
            Notification.show("Necesidad guardada.", 1500, Notification.Position.BOTTOM_START).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
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
                necesidadService.deleteById(necesidad.getIdNecesidad());
                if (this.vueloSeleccionado != null) {
                     updateNecesidadesGrid(this.vueloSeleccionado);
                     updateListVuelos(); // REFRESCAR EL GRID PRINCIPAL
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
            updateListVuelos(); // Refresca la lista de tarjetas
            // Al guardar, queremos que el formulario siga mostrando el vuelo guardado (ahora con ID)
            // y que las necesidades también se actualicen para este vuelo.
            editVuelo(vueloGuardado); // Esto re-seteará el vuelo en el form y actualizará necesidades
            // No necesitamos gridVuelos.select() porque ya no hay grid principal de vuelos
            Notification.show("Vuelo guardado.", 2000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (ConstraintViolationException e) {
            String violations = e.getConstraintViolations().stream().map(ConstraintViolation::getMessage).distinct().collect(Collectors.joining("; "));
            Notification.show("Error de validación al guardar vuelo: " + violations, 5000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (DataIntegrityViolationException e) {
             String errorMsg = "Error de integridad de datos al guardar vuelo.";
             if (e.getMostSpecificCause().getMessage().toLowerCase().contains("vuelos_numero_vuelo_key")) { // Ajustar al nombre real del constraint
                 errorMsg = "Error: El número de vuelo '" + event.getVuelo().getNumeroVuelo() + "' ya existe.";
                 if (formVuelo != null && formVuelo.numeroVuelo != null) formVuelo.numeroVuelo.setInvalid(true); // Marcar campo si es posible
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
             Notification.show("No se puede eliminar un vuelo no guardado.", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_WARNING);
             return;
        }
        try {
            vueloService.deleteById(vueloAEliminar.getIdVuelo()); // El servicio ya borra necesidades
            updateListVuelos(); // Refresca la lista de tarjetas
            closeEditorVuelo(); // Cierra el panel de edición
            Notification.show("Vuelo eliminado.", 2000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_CONTRAST);
        } catch (DataIntegrityViolationException e) {
            Notification.show("Error: No se pudo eliminar el vuelo. Verifique dependencias.", 5000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
             e.printStackTrace();
        } catch (Exception e) {
             Notification.show("Error al eliminar vuelo: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
             e.printStackTrace();
        }
    }

    private void closeEditorVuelo() {
         if (formVuelo != null) {
             formVuelo.setVuelo(null); // Limpia el formulario de vuelo
         }
         if (rightPanel != null) {
             rightPanel.setVisible(false); // Oculta todo el panel derecho
         }
         this.vueloSeleccionado = null; // Limpia la selección interna
         // Ya no hay gridVuelos para deseleccionar
         // if (gridVuelos != null) gridVuelos.asSingleSelect().clear();
     }
}