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
import com.vaadin.flow.component.html.Div; // Usar Div para contenedor derecho
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
import jakarta.annotation.security.PermitAll;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
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
    Grid<Vuelo> gridVuelos = new Grid<>(Vuelo.class, false);
    TextField filterText = new TextField();
    DatePicker fechaInicioFiltro = new DatePicker("Fecha Desde");
    DatePicker fechaFinFiltro = new DatePicker("Fecha Hasta");
    Button addVueloButton = new Button("Nuevo Vuelo", VaadinIcon.PLUS.create());
    VueloForm formVuelo;

    // Componentes UI Panel Derecho (Form Vuelo + Necesidades)
    Div rightPanel = new Div(); // Contenedor general para el panel derecho
    H4 tituloNecesidades = new H4("Necesidades de Seguridad");
    Button addNecesidadButton = new Button("Añadir Necesidad", VaadinIcon.PLUS.create());
    Grid<NecesidadVuelo> gridNecesidades = new Grid<>(NecesidadVuelo.class, false);
    NecesidadVueloForm formNecesidadDialog;

    private Vuelo vueloSeleccionado; // Guarda el vuelo actualmente seleccionado/editado

    private static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public VueloListView(VueloService vueloService, AerolineaService aerolineaService,
                         NecesidadVueloService necesidadService, PosicionSeguridadService posicionService) {
        this.vueloService = vueloService;
        this.aerolineaService = aerolineaService;
        this.necesidadService = necesidadService;
        this.posicionService = posicionService;

        addClassName("vuelo-list-view");
        setSizeFull();

        // Inicializar componentes primero
        configureGridVuelos();
        // configureToolbar(); // Se llama después para añadir a layout
        configureGridNecesidades();
        configureFormVuelo(); // Necesita lista de aerolíneas
        configureFormNecesidadDialog(); // Necesita lista de posiciones
        configureRightPanel(); // Organiza el panel derecho

        if (formVuelo != null && formNecesidadDialog != null) {
            SplitLayout content = new SplitLayout(gridVuelos, rightPanel);
            content.setOrientation(SplitLayout.Orientation.HORIZONTAL);
            content.setSplitterPosition(65); // Ajusta tamaño relativo
            content.setSizeFull();

            add(configureToolbar(), content); // Añade toolbar y contenido principal
            setDefaultDateFilters(); // Establece fechas y dispara updateListVuelos inicial
            closeEditorVuelo(); // Asegura que el panel derecho esté oculto al inicio
        } else {
             // Mensaje de error si falla la inicialización de formularios
             add(new H4("Error crítico: No se pudieron inicializar los formularios necesarios. Revise logs."));
             if (formVuelo == null) System.err.println("Error inicializando VueloForm");
             if (formNecesidadDialog == null) System.err.println("Error inicializando NecesidadVueloForm");
        }
    }

     private void configureRightPanel() {
        if (formVuelo == null) return; // No configurar si falló

        VerticalLayout needsLayout = new VerticalLayout(tituloNecesidades, addNecesidadButton, gridNecesidades);
        needsLayout.setPadding(false);
        needsLayout.setSpacing(true);
        needsLayout.addClassName("necesidades-section");
        needsLayout.setWidthFull(); // Ocupar ancho

        rightPanel.add(formVuelo, needsLayout);
        rightPanel.addClassName("right-panel");
        rightPanel.setWidth("500px"); // Ancho fijo para el panel derecho
        rightPanel.getElement().getStyle().set("flex-shrink", "0"); // Evita que se encoja
        rightPanel.setVisible(false); // Oculto por defecto

        // Listener para añadir necesidad (asegura que esté configurado)
        addNecesidadButton.addClickListener(click -> addNecesidad());
    }

    private HorizontalLayout configureToolbar() {
        filterText.setPlaceholder("Buscar por número vuelo...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY); // Busca al dejar de escribir
        filterText.addValueChangeListener(e -> updateListVuelos());

        // Listeners para filtros de fecha
        fechaInicioFiltro.addValueChangeListener(e -> updateListVuelos());
        fechaFinFiltro.addValueChangeListener(e -> updateListVuelos());

        addVueloButton.addClickListener(click -> addVuelo());

        HorizontalLayout toolbar = new HorizontalLayout(filterText, fechaInicioFiltro, fechaFinFiltro, addVueloButton);
        toolbar.addClassName("toolbar");
        toolbar.setAlignItems(Alignment.BASELINE); // Alinea elementos verticalmente
        toolbar.setWidthFull(); // Ocupar ancho
        toolbar.setFlexGrow(1, filterText); // El filtro de texto puede crecer
        return toolbar;
    }

    private void configureGridVuelos() {
        gridVuelos.addClassName("vuelo-grid");
        gridVuelos.setSizeFull();
        gridVuelos.addColumn(Vuelo::getNumeroVuelo).setHeader("Nº Vuelo").setSortable(true).setFrozen(true); // Congelado
        gridVuelos.addColumn(vuelo -> vuelo.getAerolinea() != null ? vuelo.getAerolinea().getNombre() : "-").setHeader("Aerolínea").setSortable(true).setWidth("150px");
        gridVuelos.addColumn(Vuelo::getOrigen).setHeader("Origen").setSortable(true).setWidth("100px");
        gridVuelos.addColumn(Vuelo::getDestino).setHeader("Destino").setSortable(true).setWidth("100px");
        gridVuelos.addColumn(vuelo -> formatDateTime(vuelo.getFechaHoraSalida())).setHeader("Salida Prog.").setSortable(true).setWidth("170px");
        gridVuelos.addColumn(vuelo -> formatDateTime(vuelo.getFechaHoraLlegada())).setHeader("Llegada Prog.").setSortable(true).setWidth("170px");
        gridVuelos.addColumn(Vuelo::getEstado).setHeader("Estado").setSortable(true).setWidth("120px");
        gridVuelos.addColumn(Vuelo::getTipoOperacion).setHeader("Operación").setSortable(true).setWidth("130px");
        gridVuelos.addColumn(vuelo -> formatDateTime(vuelo.getFinOperacionSeguridad())).setHeader("Fin Op. Seg.").setSortable(true).setWidth("170px");

        gridVuelos.getColumns().forEach(col -> col.setResizable(true)); // Todas redimensionables

        gridVuelos.asSingleSelect().addValueChangeListener(event -> editVuelo(event.getValue()));
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DT_FORMATTER);
    }

     private void configureFormVuelo() {
        try {
             List<Aerolinea> aerolineas = aerolineaService.findAll();
             formVuelo = new VueloForm(aerolineas);
             formVuelo.setWidth("100%"); // Ocupar ancho del panel derecho
             formVuelo.addListener(VueloForm.SaveEvent.class, this::saveVuelo);
             formVuelo.addListener(VueloForm.DeleteEvent.class, this::deleteVuelo);
             formVuelo.addListener(VueloForm.CloseEvent.class, e -> closeEditorVuelo());
        } catch (Exception e) {
             formVuelo = null; // Marcar como nulo si falla
             e.printStackTrace();
         }
    }

    private void configureGridNecesidades() {
         gridNecesidades.addClassName("necesidad-grid");
         gridNecesidades.setWidthFull();
         gridNecesidades.setHeight("250px"); // Altura fija para el grid de necesidades

         gridNecesidades.addColumn(nec -> nec.getPosicion() != null ? nec.getPosicion().getNombrePosicion() : "N/A")
             .setHeader("Posición").setSortable(true).setKey("Posición"); // Añadido Key
         gridNecesidades.addColumn(NecesidadVuelo::getCantidadAgentes)
             .setHeader("Cant.").setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END);
         gridNecesidades.addColumn(nec -> formatDateTime(nec.getInicioCobertura()))
             .setHeader("Inicio Cob.").setSortable(true).setKey("Inicio"); // Añadido Key
         gridNecesidades.addColumn(nec -> formatDateTime(nec.getFinCobertura()))
             .setHeader("Fin Cob.").setSortable(true).setKey("Fin"); // Añadido Key

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
        // Ajustar anchos específicos
        gridNecesidades.getColumnByKey("Posición").setWidth("200px");
        gridNecesidades.getColumnByKey("Inicio").setWidth("170px");
        gridNecesidades.getColumnByKey("Fin").setWidth("170px");
    }

    private void configureFormNecesidadDialog() {
         try {
            List<PosicionSeguridad> posiciones = posicionService.findAll();
            formNecesidadDialog = new NecesidadVueloForm(posiciones);
            formNecesidadDialog.addListener(NecesidadVueloForm.SaveEvent.class, this::saveNecesidad);
            formNecesidadDialog.addListener(NecesidadVueloForm.CloseEvent.class, e -> formNecesidadDialog.close());
        } catch (Exception e) {
             formNecesidadDialog = null; // Marcar como nulo si falla
             e.printStackTrace();
         }
    }

     private void updateListVuelos() {
         if (gridVuelos == null) return;

         LocalDate fechaInicio = fechaInicioFiltro.getValue();
         LocalDate fechaFin = fechaFinFiltro.getValue();

         // Si alguna fecha no está seleccionada, no filtrar (o mostrar mensaje)
         if (fechaInicio == null || fechaFin == null) {
             // Podríamos limpiar el grid o mostrar todos los vuelos sin filtrar por fecha
             // Opción: Limpiar grid y mostrar mensaje
              Notification.show("Seleccione fecha de inicio y fin para buscar vuelos.", 2000, Notification.Position.BOTTOM_START)
                         .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
              gridVuelos.setItems(Collections.emptyList());
             return; // Salir si no hay rango de fechas completo
         }

         // Validar que fecha fin no sea anterior a fecha inicio
         if(fechaFin.isBefore(fechaInicio)) {
             Notification.show("La 'Fecha Hasta' debe ser posterior o igual a la 'Fecha Desde'.", 3000, Notification.Position.BOTTOM_START)
                         .addThemeVariants(NotificationVariant.LUMO_WARNING);
             gridVuelos.setItems(Collections.emptyList());
             return;
         }

         // Proceder a la búsqueda si las fechas son válidas
         try {
             LocalDateTime inicioRango = fechaInicio.atStartOfDay();
             LocalDateTime finRango = fechaFin.atTime(LocalTime.MAX);
             String filtroTexto = filterText.getValue();

             // Usar el método de servicio que filtra por ambas cosas
             List<Vuelo> vuelos = vueloService.findVuelosByDateRangeAndNumeroVueloForView(inicioRango, finRango, filtroTexto);
             gridVuelos.setItems(vuelos);

         } catch (Exception e) {
            Notification.show("Error al cargar vuelos: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            gridVuelos.setItems(Collections.emptyList()); // Limpiar en caso de error
            e.printStackTrace(); // Log para debug
         }
    }

    // Actualiza el grid de necesidades basado en el vuelo seleccionado
    private void updateNecesidadesGrid(Vuelo vuelo) {
        if (vuelo != null && vuelo.getIdVuelo() != null) {
             try {
                 // Asegura usar el servicio para obtener las necesidades con posiciones (fetch)
                 List<NecesidadVuelo> necesidades = necesidadService.findByVueloId(vuelo.getIdVuelo());
                 gridNecesidades.setItems(necesidades);
             } catch(EntityNotFoundException enf) {
                  // Esto podría pasar si el vuelo se borra mientras se edita
                  Notification.show("Error: El vuelo seleccionado ya no existe.", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
                  gridNecesidades.setItems(Collections.emptyList());
                  closeEditorVuelo(); // Cerrar editor si el vuelo desaparece
             } catch (Exception e) {
                 Notification.show("Error al cargar necesidades del vuelo: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
                 gridNecesidades.setItems(Collections.emptyList());
                 e.printStackTrace();
             }
        } else {
            // Si no hay vuelo seleccionado (o es nuevo sin ID), limpia el grid
            gridNecesidades.setItems(Collections.emptyList());
        }
    }

    private void setDefaultDateFilters() {
        LocalDate hoy = LocalDate.now();
        // Mostrar semana actual por defecto (Lunes a Domingo)
        fechaInicioFiltro.setValue(hoy.with(java.time.DayOfWeek.MONDAY));
        fechaFinFiltro.setValue(hoy.with(java.time.DayOfWeek.SUNDAY));
        // updateListVuelos() se llamará automáticamente por los listeners al setear valor
    }

    private void addVuelo() {
        if (formVuelo == null) return;
        gridVuelos.asSingleSelect().clear(); // Deselecciona grid
        editVuelo(new Vuelo()); // Llama a edit con un vuelo nuevo
    }

    // Muestra/Oculta el panel derecho y carga los datos del vuelo
    private void editVuelo(Vuelo vuelo) {
        this.vueloSeleccionado = vuelo; // Guarda la selección actual

        if (vuelo == null) {
            closeEditorVuelo(); // Oculta panel si no hay selección
        } else {
            if (formVuelo == null || rightPanel == null) return; // Seguridad extra

            // Cargar datos en el formulario de vuelo
            // Asegurarse que la aerolínea (u otras relaciones LAZY) esté cargada si es necesario
            // Podría requerir un findById específico con fetch si el grid no lo trae todo
             formVuelo.setVuelo(vuelo);

            // Actualizar el grid de necesidades para este vuelo
            updateNecesidadesGrid(vuelo);

            // Mostrar el panel derecho y marcar modo edición
            rightPanel.setVisible(true);
            addClassName("editing");
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
         // Opcional: Pre-rellenar fechas basado en el vuelo seleccionado
         // try {
         //     nuevaNecesidad.setInicioCobertura(vueloSeleccionado.getFechaHoraLlegada().minusHours(1));
         //     nuevaNecesidad.setFinCobertura(vueloSeleccionado.getFechaHoraSalida().plusHours(1));
         // } catch (NullPointerException npe) { /* Ignorar si las fechas del vuelo son nulas */ }

         formNecesidadDialog.setNecesidad(nuevaNecesidad, this.vueloSeleccionado); // Pasa el vuelo padre
         formNecesidadDialog.open(); // Abre el diálogo
     }

    private void editNecesidad(NecesidadVuelo necesidad) {
        if (formNecesidadDialog == null) return;
        if (necesidad == null) return;

        // Asegurarse que la necesidad tiene la referencia al vuelo padre correcta
        Vuelo vueloAsociado = necesidad.getVuelo();
        if(vueloAsociado == null && this.vueloSeleccionado != null) {
            // Si la necesidad no tiene vuelo (quizás por error previo), intenta usar el seleccionado
             vueloAsociado = this.vueloSeleccionado;
             necesidad.setVuelo(vueloAsociado); // Asignar al objeto necesidad
        } else if (vueloAsociado == null && this.vueloSeleccionado == null) {
             // Caso raro: ni la necesidad ni la selección tienen vuelo
             Notification.show("Error: No se puede determinar el vuelo asociado a esta necesidad.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
             return;
        }

        // Pasa la necesidad y su vuelo asociado (asegurado que no sea nulo)
        formNecesidadDialog.setNecesidad(necesidad, vueloAsociado);
        formNecesidadDialog.open();
    }

    // Guarda la necesidad (viene del diálogo NecesidadVueloForm)
    private void saveNecesidad(NecesidadVueloForm.SaveEvent event) {
        try {
            necesidadService.save(event.getNecesidad());
            // Refresca el grid de necesidades del vuelo actualmente seleccionado
            if (this.vueloSeleccionado != null) {
                 updateNecesidadesGrid(this.vueloSeleccionado);
            }
            Notification.show("Necesidad guardada.", 1500, Notification.Position.BOTTOM_START).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (ConstraintViolationException e) {
             String violations = e.getConstraintViolations().stream()
                                  .map(cv -> cv.getMessage()) // Mostrar solo mensaje de validación
                                  .distinct().collect(Collectors.joining("; "));
             // Intenta dar mensaje específico para fechas
             String errorMsg = violations.contains("posterior a la hora de inicio")
                             ? "Error: La hora de fin de cobertura debe ser posterior a la de inicio."
                             : "Error de validación: " + violations;
             Notification.show(errorMsg, 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (DataIntegrityViolationException e) {
             // Error probable por unique constraint (vuelo, posicion)
             String errorMsg = "Error: Ya existe una necesidad para esta posición en este vuelo.";
             // Podríamos intentar obtener más detalles si la BD lo permite
             // e.g., if (e.getMostSpecificCause().getMessage().contains(...))
             Notification.show(errorMsg, 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
             Notification.show("Error inesperado al guardar necesidad: " + e.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
             e.printStackTrace(); // Log para debug
        }
    }

    private void deleteNecesidad(NecesidadVuelo necesidad) {
        if (necesidad != null && necesidad.getIdNecesidad() != null) {
            try {
                necesidadService.deleteById(necesidad.getIdNecesidad());
                // Refrescar grid
                if (this.vueloSeleccionado != null) {
                     updateNecesidadesGrid(this.vueloSeleccionado);
                }
                Notification.show("Necesidad eliminada.", 1500, Notification.Position.BOTTOM_START).addThemeVariants(NotificationVariant.LUMO_CONTRAST);
            } catch (Exception e) {
                 Notification.show("Error al eliminar necesidad: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
                 e.printStackTrace(); // Log para debug
            }
        }
    }


    // Guarda el vuelo (viene del VueloForm)
    private void saveVuelo(VueloForm.SaveEvent event) {
        try {
            Vuelo vueloGuardado = vueloService.save(event.getVuelo());
            updateListVuelos(); // Actualiza el grid principal de vuelos

            // Recarga el vuelo guardado en el editor para reflejar cualquier cambio (ej. ID generado)
            // y selecciona la fila en el grid
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
             if (e.getMostSpecificCause().getMessage().toLowerCase().contains("vuelos_numero_vuelo_key")) { // Adaptar a tu constraint
                 errorMsg = "Error: El número de vuelo '" + event.getVuelo().getNumeroVuelo() + "' ya existe.";
                 if (formVuelo != null) formVuelo.numeroVuelo.setInvalid(true);
             }
             Notification.show(errorMsg, 5000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
             Notification.show("Error inesperado al guardar vuelo: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
             e.printStackTrace(); // Log para debug
        }
    }

    // Elimina el vuelo (viene del VueloForm)
    private void deleteVuelo(VueloForm.DeleteEvent event) {
        Vuelo vueloAEliminar = event.getVuelo();
        if (formVuelo == null || vueloAEliminar == null || vueloAEliminar.getIdVuelo() == null) {
             Notification.show("No se puede eliminar un vuelo no guardado.", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_WARNING);
             return;
        }
        try {
            // El servicio ahora borra las necesidades primero
            vueloService.deleteById(vueloAEliminar.getIdVuelo());
            updateListVuelos(); // Actualiza grid principal
            closeEditorVuelo(); // Cierra el panel derecho
            Notification.show("Vuelo eliminado (y sus necesidades asociadas).", 2000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_CONTRAST);
        } catch (DataIntegrityViolationException e) {
            // Catch residual por si acaso hay otras dependencias no contempladas
            Notification.show("Error: No se pudo eliminar el vuelo completamente. Verifique dependencias.", 5000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
             e.printStackTrace(); // Log para debug
        } catch (Exception e) {
             Notification.show("Error al eliminar vuelo: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
             e.printStackTrace(); // Log para debug
        }
    }

    // Oculta el panel derecho y limpia la selección
    private void closeEditorVuelo() {
         if (formVuelo != null) {
             formVuelo.setVuelo(null); // Limpia el form
         }
         if (rightPanel != null) {
             rightPanel.setVisible(false); // Oculta todo el panel
         }
         removeClassName("editing"); // Quita estilo de edición
         this.vueloSeleccionado = null; // Limpia la selección interna
         gridVuelos.asSingleSelect().clear(); // Deselecciona el grid
     }
}