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
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct; // Importar
import jakarta.annotation.security.PermitAll;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired; // Importar
import org.springframework.dao.DataIntegrityViolationException;

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

    // Servicios
    private final VueloService vueloService;
    private final AerolineaService aerolineaService;
    private final NecesidadVueloService necesidadService;
    private final PosicionSeguridadService posicionService;

    // Componentes UI Vuelos
    private Grid<Vuelo> gridVuelos;
    private TextField filterText;
    private DatePicker fechaInicioFiltro;
    private DatePicker fechaFinFiltro;
    private Button addVueloButton;
    private VueloForm formVuelo; // Usar la versión que no depende del Binder problemático

    // Componentes UI Panel Derecho (Form Vuelo + Necesidades)
    private Div rightPanel;
    private H4 tituloNecesidades;
    private Button addNecesidadButton;
    private Grid<NecesidadVuelo> gridNecesidades;
    private NecesidadVueloForm formNecesidadDialog; // Usar la versión que no depende del Binder problemático

    private Vuelo vueloSeleccionado; // Guarda el vuelo actualmente seleccionado/editado
    private HorizontalLayout toolbar; // Mover declaración aquí para que sea accesible en initLayout
    private SplitLayout splitLayout; // Mover declaración aquí

    private static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Autowired // Inyección por constructor
    public VueloListView(VueloService vueloService, AerolineaService aerolineaService,
                         NecesidadVueloService necesidadService, PosicionSeguridadService posicionService) {
        this.vueloService = vueloService;
        this.aerolineaService = aerolineaService;
        this.necesidadService = necesidadService;
        this.posicionService = posicionService;

        addClassName("vuelo-list-view");
        setSizeFull();
    }

    // Usar @PostConstruct para asegurar que las dependencias estén inyectadas antes de construir la UI
    @PostConstruct
    private void initLayout() {
        try {
            // Inicializar componentes
            createGridVuelos();
            createToolbar(); // Crea y configura la barra de herramientas
            createGridNecesidades();
            createFormVuelo(); // Necesita lista de aerolíneas
            createFormNecesidadDialog(); // Necesita lista de posiciones
            createRightPanel(); // Organiza el panel derecho

            // Verificar que los formularios cruciales se hayan instanciado
            if (formVuelo == null || formNecesidadDialog == null) {
                 throw new IllegalStateException("Error crítico: Uno o más formularios no pudieron ser instanciados.");
            }

            splitLayout = new SplitLayout(gridVuelos, rightPanel);
            splitLayout.setOrientation(SplitLayout.Orientation.HORIZONTAL);
            splitLayout.setSplitterPosition(65); // Ajusta tamaño relativo
            splitLayout.setSizeFull();

            add(toolbar, splitLayout); // Añade toolbar y contenido principal
            setDefaultDateFilters(); // Establece fechas y dispara updateListVuelos inicial
            closeEditorVuelo(); // Asegura que el panel derecho esté oculto al inicio

        } catch (Exception e) {
             System.err.println("!!! FATAL ERROR during VueloListView initLayout: " + e.getMessage());
             e.printStackTrace();
             removeAll(); // Limpia la vista en caso de error grave
             add(new H4("Error crítico al inicializar la vista de Vuelos. Contacte al administrador."));
        }
    }


     private void createRightPanel() {
        if (formVuelo == null) { // Seguridad por si formVuelo no se creó
            rightPanel = new Div(new H4("Error al cargar formulario de vuelo."));
            return;
        }

        VerticalLayout needsLayout = new VerticalLayout(tituloNecesidades, addNecesidadButton, gridNecesidades);
        needsLayout.setPadding(false);
        needsLayout.setSpacing(true);
        needsLayout.addClassName("necesidades-section");
        needsLayout.setWidthFull();

        rightPanel = new Div(); // Crear instancia de rightPanel aquí
        rightPanel.add(formVuelo, needsLayout);
        rightPanel.addClassName("right-panel");
        rightPanel.setWidth("500px"); // Ancho fijo para el panel derecho
        rightPanel.getElement().getStyle().set("flex-shrink", "0"); // Evita que se encoja
        rightPanel.setVisible(false); // Oculto por defecto

        addNecesidadButton.addClickListener(click -> addNecesidad());
    }

    private void createToolbar() {
        filterText = new TextField();
        filterText.setPlaceholder("Buscar por número vuelo...");
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

        tituloNecesidades = new H4("Necesidades de Seguridad"); // Inicializar aquí
        addNecesidadButton = new Button("Añadir Necesidad", VaadinIcon.PLUS.create()); // Inicializar aquí

        toolbar = new HorizontalLayout(filterText, fechaInicioFiltro, fechaFinFiltro, addVueloButton);
        toolbar.addClassName("toolbar");
        toolbar.setAlignItems(Alignment.BASELINE);
        toolbar.setWidthFull();
        toolbar.setFlexGrow(1, filterText);
    }

    private void createGridVuelos() {
        gridVuelos = new Grid<>(Vuelo.class, false);
        gridVuelos.addClassName("vuelo-grid");
        gridVuelos.setSizeFull();
        gridVuelos.addColumn(Vuelo::getNumeroVuelo).setHeader("Nº Vuelo").setSortable(true).setFrozen(true);
        gridVuelos.addColumn(vuelo -> vuelo.getAerolinea() != null ? vuelo.getAerolinea().getNombre() : "-").setHeader("Aerolínea").setSortable(true).setWidth("150px");
        gridVuelos.addColumn(Vuelo::getOrigen).setHeader("Origen").setSortable(true).setWidth("100px");
        gridVuelos.addColumn(Vuelo::getDestino).setHeader("Destino").setSortable(true).setWidth("100px");
        gridVuelos.addColumn(vuelo -> formatDateTime(vuelo.getFechaHoraSalida())).setHeader("Salida Prog.").setSortable(true).setWidth("170px");
        gridVuelos.addColumn(vuelo -> formatDateTime(vuelo.getFechaHoraLlegada())).setHeader("Llegada Prog.").setSortable(true).setWidth("170px");
        gridVuelos.addColumn(Vuelo::getEstado).setHeader("Estado").setSortable(true).setWidth("120px");
        gridVuelos.addColumn(Vuelo::getTipoOperacion).setHeader("Operación").setSortable(true).setWidth("130px");
        gridVuelos.addColumn(vuelo -> formatDateTime(vuelo.getFinOperacionSeguridad())).setHeader("Fin Op. Seg.").setSortable(true).setWidth("170px");

        gridVuelos.getColumns().forEach(col -> col.setResizable(true));
        gridVuelos.asSingleSelect().addValueChangeListener(event -> editVuelo(event.getValue()));
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DT_FORMATTER);
    }

     private void createFormVuelo() {
        try {
             List<Aerolinea> aerolineas = aerolineaService.findAll();
             formVuelo = new VueloForm(aerolineas); // Usa la versión de VueloForm sin Binder problemático
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
         gridNecesidades = new Grid<>(NecesidadVuelo.class, false);
         gridNecesidades.addClassName("necesidad-grid");
         gridNecesidades.setWidthFull();
         gridNecesidades.setHeight("250px");

         gridNecesidades.addColumn(nec -> nec.getPosicion() != null ? nec.getPosicion().getNombrePosicion() : "N/A")
             .setHeader("Posición").setSortable(true).setKey("PosicionKey");
         gridNecesidades.addColumn(NecesidadVuelo::getCantidadAgentes)
             .setHeader("Cant.").setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END);
         gridNecesidades.addColumn(nec -> formatDateTime(nec.getInicioCobertura()))
             .setHeader("Inicio Cob.").setSortable(true).setKey("InicioKey");
         gridNecesidades.addColumn(nec -> formatDateTime(nec.getFinCobertura()))
             .setHeader("Fin Cob.").setSortable(true).setKey("FinKey");

         gridNecesidades.addColumn(new ComponentRenderer<>(necesidad -> {
            Button editBtn = new Button(VaadinIcon.EDIT.create());
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
            editBtn.setTooltipText("Editar Necesidad");
            editBtn.addClickListener(e -> editNecesidad(necesidad));

            Button deleteBtn = new Button(VaadinIcon.TRASH.create());
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            deleteBtn.setTooltipText("Eliminar Necesidad");
            deleteBtn.addClickListener(e -> deleteNecesidad(necesidad));

            HorizontalLayout buttons = new HorizontalLayout(editBtn, deleteBtn);
            buttons.setSpacing(false);
            buttons.setPadding(false);
            buttons.getThemeList().add("spacing-xs");
            return buttons;
        })).setHeader("Acciones").setFlexGrow(0).setWidth("100px").setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.CENTER);

        gridNecesidades.getColumns().forEach(col -> col.setResizable(true));
        // Ajustar anchos específicos usando las claves
        gridNecesidades.getColumnByKey("PosicionKey").setWidth("200px");
        gridNecesidades.getColumnByKey("InicioKey").setWidth("170px");
        gridNecesidades.getColumnByKey("FinKey").setWidth("170px");
    }

    private void createFormNecesidadDialog() {
         try {
            // --- CORRECCIÓN: Llamar a findAllActive() en lugar de findAll() ---
            List<PosicionSeguridad> posicionesActivas = posicionService.findAllActive();
            if (posicionesActivas == null) {
                System.err.println("Error: posicionService.findAllActive() devolvió null en createFormNecesidadDialog.");
                posicionesActivas = Collections.emptyList();
            }
            // Usa la versión de NecesidadVueloForm sin Binder problemático
            formNecesidadDialog = new NecesidadVueloForm(posicionesActivas);
            formNecesidadDialog.addListener(NecesidadVueloForm.SaveEvent.class, this::saveNecesidad);
            formNecesidadDialog.addListener(NecesidadVueloForm.CloseEvent.class, e -> formNecesidadDialog.close());
        } catch (Exception e) {
             formNecesidadDialog = null;
             System.err.println("!!! ERROR creando NecesidadVueloForm: " + e.getMessage());
             e.printStackTrace();
         }
    }

     private void updateListVuelos() {
         if (gridVuelos == null || fechaInicioFiltro == null || fechaFinFiltro == null) return;

         LocalDate fechaInicio = fechaInicioFiltro.getValue();
         LocalDate fechaFin = fechaFinFiltro.getValue();
         List<Vuelo> vuelos;

         if (fechaInicio != null && fechaFin != null) {
             if(fechaFin.isBefore(fechaInicio)) {
                 Notification.show("La 'Fecha Hasta' debe ser posterior o igual a la 'Fecha Desde'.", 3000, Notification.Position.BOTTOM_START)
                             .addThemeVariants(NotificationVariant.LUMO_WARNING);
                 vuelos = Collections.emptyList();
             } else {
                 try {
                     LocalDateTime inicioRango = fechaInicio.atStartOfDay();
                     LocalDateTime finRango = fechaFin.atTime(LocalTime.MAX);
                     String filtroTexto = filterText.getValue();
                     vuelos = vueloService.findVuelosByDateRangeAndNumeroVueloForView(inicioRango, finRango, filtroTexto);
                 } catch (Exception e) {
                    Notification.show("Error al cargar vuelos: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    vuelos = Collections.emptyList();
                    e.printStackTrace();
                 }
             }
         } else { // Si alguna fecha es null, carga todos por defecto o según lógica de negocio
              try {
                 // Si se desea cargar TODOS cuando no hay filtro de fecha:
                 // vuelos = vueloService.findAllForView();
                 // Opcionalmente, podrías dejar el grid vacío si no hay un rango de fechas completo.
                 // Por ahora, dejaremos que no muestre nada si las fechas no están completas,
                 // para forzar al usuario a usar el filtro de fecha.
                 // Si quieres mostrar TODOS por defecto si no hay filtro, descomenta la línea de arriba y comenta la de abajo.
                  vuelos = Collections.emptyList();
                  if (fechaInicio == null && fechaFin == null && (filterText.getValue() == null || filterText.getValue().isEmpty())) {
                      // Cargar todos si NINGÚN filtro está activo
                      vuelos = vueloService.findAllForView();
                  } else if (filterText.getValue() != null && !filterText.getValue().isEmpty()) {
                      // Si solo hay filtro de texto, usa un rango muy amplio (ej. 1 año) o un método de servicio que solo filtre por texto
                      vuelos = vueloService.findVuelosByDateRangeAndNumeroVueloForView(
                              LocalDate.now().minusYears(1).atStartOfDay(), // Ejemplo de rango amplio
                              LocalDate.now().plusYears(1).atTime(LocalTime.MAX),
                              filterText.getValue());
                  } else {
                      // Mostrar notificación si solo una fecha está seleccionada o para claridad
                      if (filterText.getValue() == null || filterText.getValue().isEmpty()) {
                        // Notification.show("Seleccione un rango de fechas para ver los vuelos.", 2000, Notification.Position.BOTTOM_START);
                      }
                  }
              } catch (Exception e) {
                 Notification.show("Error al cargar vuelos: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                         .addThemeVariants(NotificationVariant.LUMO_ERROR);
                 vuelos = Collections.emptyList();
                 e.printStackTrace();
              }
         }
         gridVuelos.setItems(vuelos); // Usa setItems directamente
         if (gridVuelos.getDataProvider() != null) {
             gridVuelos.getDataProvider().refreshAll(); // Forzar refresco
         }
    }

    private void updateNecesidadesGrid(Vuelo vuelo) {
        // ... (sin cambios) ...
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
        if (fechaInicioFiltro == null || fechaFinFiltro == null) return;
        LocalDate hoy = LocalDate.now();
        // Por defecto, los campos de fecha estarán vacíos para mostrar todos los vuelos inicialmente
        // fechaInicioFiltro.setValue(hoy.with(java.time.DayOfWeek.MONDAY));
        // fechaFinFiltro.setValue(hoy.with(java.time.DayOfWeek.SUNDAY));
        // Llamar a updateListVuelos aquí para la carga inicial
        // updateListVuelos(); // Se llama desde initLayout después de esto
    }

    private void addVuelo() {
        // ... (sin cambios) ...
        if (formVuelo == null) return;
        gridVuelos.asSingleSelect().clear();
        editVuelo(new Vuelo());
    }

    private void editVuelo(Vuelo vuelo) {
        // ... (sin cambios) ...
        this.vueloSeleccionado = vuelo;
        if (vuelo == null) {
            closeEditorVuelo();
        } else {
            if (formVuelo == null || rightPanel == null) return;
            formVuelo.setVuelo(vuelo);
            updateNecesidadesGrid(vuelo);
            rightPanel.setVisible(true);
            // addClassName("editing"); // Quitado para simplificar
        }
     }


     private void addNecesidad() {
         // ... (sin cambios) ...
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
        if (formNecesidadDialog == null) return;
        if (necesidad == null) return;
        Vuelo vueloAsociado = necesidad.getVuelo();
        if(vueloAsociado == null && this.vueloSeleccionado != null) {
             vueloAsociado = this.vueloSeleccionado;
             necesidad.setVuelo(vueloAsociado);
        } else if (vueloAsociado == null && this.vueloSeleccionado == null) {
             Notification.show("Error: No se puede determinar el vuelo asociado a esta necesidad.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
             return;
        }
        formNecesidadDialog.setNecesidad(necesidad, vueloAsociado);
        formNecesidadDialog.open();
    }

    private void saveNecesidad(NecesidadVueloForm.SaveEvent event) {
        // ... (sin cambios) ...
        try {
            necesidadService.save(event.getNecesidad());
            if (this.vueloSeleccionado != null) {
                 updateNecesidadesGrid(this.vueloSeleccionado);
            }
            Notification.show("Necesidad guardada.", 1500, Notification.Position.BOTTOM_START).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (ConstraintViolationException e) {
             String violations = e.getConstraintViolations().stream()
                                  .map(cv -> cv.getMessage())
                                  .distinct().collect(Collectors.joining("; "));
             String errorMsg = violations.contains("posterior a la hora de inicio")
                             ? "Error: La hora de fin de cobertura debe ser posterior a la de inicio."
                             : "Error de validación: " + violations;
             Notification.show(errorMsg, 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (DataIntegrityViolationException e) {
             String errorMsg = "Error: Ya existe una necesidad para esta posición en este vuelo.";
             Notification.show(errorMsg, 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
             Notification.show("Error inesperado al guardar necesidad: " + e.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
             e.printStackTrace();
        }
    }

    private void deleteNecesidad(NecesidadVuelo necesidad) {
        // ... (sin cambios) ...
        if (necesidad != null && necesidad.getIdNecesidad() != null) {
            try {
                necesidadService.deleteById(necesidad.getIdNecesidad());
                if (this.vueloSeleccionado != null) {
                     updateNecesidadesGrid(this.vueloSeleccionado);
                }
                Notification.show("Necesidad eliminada.", 1500, Notification.Position.BOTTOM_START).addThemeVariants(NotificationVariant.LUMO_CONTRAST);
            } catch (Exception e) {
                 Notification.show("Error al eliminar necesidad: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
                 e.printStackTrace();
            }
        }
    }

    private void saveVuelo(VueloForm.SaveEvent event) {
        // ... (sin cambios) ...
        try {
            Vuelo vueloGuardado = vueloService.save(event.getVuelo());
            updateListVuelos();
            editVuelo(vueloGuardado);
            gridVuelos.select(vueloGuardado);
            Notification.show("Vuelo guardado.", 2000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (ConstraintViolationException e) {
            String violations = e.getConstraintViolations().stream()
                                 .map(cv -> cv.getMessage())
                                 .distinct().collect(Collectors.joining("; "));
            Notification.show("Error de validación al guardar vuelo: " + violations, 5000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (DataIntegrityViolationException e) {
             String errorMsg = "Error de integridad de datos al guardar vuelo.";
             if (e.getMostSpecificCause().getMessage().toLowerCase().contains("vuelos_numero_vuelo_key")) {
                 errorMsg = "Error: El número de vuelo '" + event.getVuelo().getNumeroVuelo() + "' ya existe.";
                 if (formVuelo != null) formVuelo.numeroVuelo.setInvalid(true);
             }
             Notification.show(errorMsg, 5000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
             Notification.show("Error inesperado al guardar vuelo: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
             e.printStackTrace();
        }
    }

    private void deleteVuelo(VueloForm.DeleteEvent event) {
        // ... (sin cambios) ...
        Vuelo vueloAEliminar = event.getVuelo();
        if (formVuelo == null || vueloAEliminar == null || vueloAEliminar.getIdVuelo() == null) {
             Notification.show("No se puede eliminar un vuelo no guardado.", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_WARNING);
             return;
        }
        try {
            vueloService.deleteById(vueloAEliminar.getIdVuelo());
            updateListVuelos();
            closeEditorVuelo();
            Notification.show("Vuelo eliminado (y sus necesidades asociadas).", 2000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_CONTRAST);
        } catch (DataIntegrityViolationException e) {
            Notification.show("Error: No se pudo eliminar el vuelo completamente. Verifique dependencias.", 5000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
             e.printStackTrace();
        } catch (Exception e) {
             Notification.show("Error al eliminar vuelo: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
             e.printStackTrace();
        }
    }

    private void closeEditorVuelo() {
        // ... (sin cambios) ...
         if (formVuelo != null) {
             formVuelo.setVuelo(null);
         }
         if (rightPanel != null) {
             rightPanel.setVisible(false);
         }
         // removeClassName("editing"); // Quitado para simplificar
         this.vueloSeleccionado = null;
         if (gridVuelos != null) { // Chequeo de nulidad
            gridVuelos.asSingleSelect().clear();
         }
     }
}