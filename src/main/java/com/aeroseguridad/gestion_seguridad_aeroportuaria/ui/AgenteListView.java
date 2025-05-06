package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PosicionSeguridad;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.AgenteService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import jakarta.persistence.EntityNotFoundException; // Importar
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Collections; // Para lista vacía
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "agentes", layout = MainLayout.class)
@PageTitle("Agentes | Gestión Seguridad")
@PermitAll
public class AgenteListView extends VerticalLayout {

    private final AgenteService agenteService;

    Grid<Agente> grid = new Grid<>(Agente.class, false);
    TextField filterText = new TextField();
    Button addAgenteButton = new Button("Nuevo Agente", VaadinIcon.PLUS.create());
    AgenteForm form;

    public AgenteListView(AgenteService agenteService) {
        this.agenteService = agenteService;
        addClassName("agente-list-view");
        setSizeFull();

        configureForm(); // Intenta configurar el form primero
        configureGrid();
        configureToolbar();

        if (form != null) {
             SplitLayout content = new SplitLayout(grid, form);
             content.setOrientation(SplitLayout.Orientation.HORIZONTAL);
             content.setSplitterPosition(70); // Ajusta el porcentaje para el grid
             content.setSizeFull();
             add(configureToolbar(), content);
             updateList();
             closeEditor();
        } else {
             // Fallback si el formulario no se pudo crear
             add(configureToolbar()); // Muestra toolbar
             add(grid); // Muestra grid solo
             Notification.show("Error crítico: No se pudo inicializar el formulario de Agente.", 0, Notification.Position.MIDDLE)
                         .addThemeVariants(NotificationVariant.LUMO_ERROR);
             updateList(); // Intenta cargar grid igualmente
        }
    }

    private HorizontalLayout configureToolbar() {
        filterText.setPlaceholder("Buscar por nombre o apellido");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY); // Busca al dejar de teclear
        filterText.addValueChangeListener(e -> updateList());

        addAgenteButton.addClickListener(click -> addAgente());

        HorizontalLayout toolbar = new HorizontalLayout(filterText, addAgenteButton);
        toolbar.addClassName("toolbar");
        toolbar.setWidthFull(); // Ocupar ancho
        toolbar.setFlexGrow(1, filterText); // Permite que el filtro crezca
        return toolbar;
    }

    private void configureGrid() {
        grid.addClassName("agente-grid");
        grid.setSizeFull();

        grid.addColumn(Agente::getNombre).setHeader("Nombre").setSortable(true).setFrozen(true); // Congelado
        grid.addColumn(Agente::getApellido).setHeader("Apellido").setSortable(true).setFrozen(true); // Congelado
        grid.addColumn(agente -> {
            LocalDate fechaNac = agente.getFechaNacimiento();
            if (fechaNac != null) {
                // --- CORRECCIÓN: Usar LocalDate.now() ---
                LocalDate hoy = LocalDate.now();
                try {
                    return Period.between(fechaNac, hoy).getYears();
                } catch (Exception e) {
                    return "Error"; // En caso de fecha inválida
                }
            } else {
                return "N/A";
            }
        }).setHeader("Edad").setSortable(true).setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END);
        grid.addColumn(Agente::getNumeroCarnet).setHeader("Nº Carnet").setSortable(true);
        grid.addColumn(agente -> agente.getPosicionesHabilitadas().stream()
                                     .map(PosicionSeguridad::getNombrePosicion)
                                     .sorted()
                                     .collect(Collectors.joining(", ")))
            .setHeader("Habilidades").setSortable(false).setAutoWidth(false).setWidth("300px"); // Ancho fijo para habilidades
        grid.addColumn(Agente::getTelefono).setHeader("Teléfono").setSortable(false);
        grid.addColumn(Agente::getEmail).setHeader("Email").setSortable(true);

        grid.getColumns().forEach(col -> col.setResizable(true)); // Todas redimensionables
        grid.getColumns().stream() // AutoWidth para las demás si no se especifica
            .filter(col -> col.getWidth() == null)
            .forEach(col -> col.setAutoWidth(true));

        grid.asSingleSelect().addValueChangeListener(event -> editAgente(event.getValue()));
    }


    private void configureForm() {
        try {
            List<PosicionSeguridad> allPosiciones = agenteService.findAllPosiciones();
            form = new AgenteForm(allPosiciones);
            // form.setSizeFull(); // El SplitLayout maneja el tamaño
            form.setWidth("400px"); // Ancho fijo para el formulario
            form.addListener(AgenteForm.SaveEvent.class, this::saveAgente);
            form.addListener(AgenteForm.DeleteEvent.class, this::deactivateAgente); // Escucha el evento de desactivación
            form.addListener(AgenteForm.CloseEvent.class, e -> closeEditor());
         } catch (Exception e) {
            form = null; // Marcar como nulo si falla la inicialización
        }
    }

    private void updateList() {
         if (grid != null) {
             try {
                // Llama al servicio para obtener solo agentes ACTIVOS filtrados
                grid.setItems(agenteService.findAllActiveForView(filterText.getValue()));
             } catch (Exception e) {
                Notification.show("Error al cargar agentes activos: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                grid.setItems(Collections.emptyList()); // Limpia el grid en caso de error
             }
         }
    }

    private void addAgente() {
        if (form == null) return;
        grid.asSingleSelect().clear();
        editAgente(new Agente()); // Pasa un nuevo objeto Agente
    }

    private void editAgente(Agente agente) {
        if (form == null) return;
        if (agente == null) {
            closeEditor();
        } else {
            // Asumiendo que findAllActiveForView ya trae las posiciones (JOIN FETCH)
            form.setAgente(agente);
            form.setVisible(true);
            addClassName("editing"); // Clase CSS para indicar modo edición (opcional)
        }
    }

    private void saveAgente(AgenteForm.SaveEvent event) {
        try {
            agenteService.save(event.getAgente());
            updateList(); // Refresca el grid
            closeEditor(); // Cierra el formulario
            Notification.show("Agente guardado.", 2000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (DataIntegrityViolationException e) {
             String message = "Error: No se pudo guardar el agente.";
             String specificCause = e.getMostSpecificCause().getMessage().toLowerCase();
             // Intenta detectar violación de constraint por nombre (puede variar según BD)
             if (specificCause.contains("agentes_numero_carnet_key") || specificCause.contains("uk_") && specificCause.contains("numero_carnet")) {
                 message = "Error: El Número de Carnet '" + event.getAgente().getNumeroCarnet() + "' ya existe.";
                 if (form != null && form.numeroCarnet != null) {
                     form.numeroCarnet.setInvalid(true);
                     form.numeroCarnet.setErrorMessage("Este número de carnet ya existe");
                 }
             } else if (specificCause.contains("agentes_email_key") || specificCause.contains("uk_") && specificCause.contains("email")) { // Ejemplo si email fuera unique
                 message = "Error: El Email '" + event.getAgente().getEmail() + "' ya existe.";
                 if (form != null && form.email != null) {
                     form.email.setInvalid(true);
                     form.email.setErrorMessage("Este email ya existe");
                 }
             }
             Notification.show(message, 5000, Notification.Position.BOTTOM_CENTER)
                  .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) { // Captura otros errores inesperados
             Notification.show("Error inesperado al guardar agente: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
             e.printStackTrace(); // Imprime stack trace para depuración
        }
    }

    // --- MÉTODO para DESACTIVAR Agente ---
    private void deactivateAgente(AgenteForm.DeleteEvent event) {
        if (form == null) return;
        Agente agenteADesactivar = event.getAgente();

        if (agenteADesactivar == null || agenteADesactivar.getIdAgente() == null) {
             // No debería ocurrir si el botón solo se habilita para existentes
             Notification.show("Seleccione un agente guardado para desactivar.", 3000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
             return;
        }

        try {
            agenteService.deactivateById(agenteADesactivar.getIdAgente());
            updateList(); // Refresca el grid (el agente ya no aparecerá)
            closeEditor(); // Cierra el formulario
            Notification.show("Agente desactivado.", 2000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_CONTRAST); // Variante contraste para desactivación
        } catch (EntityNotFoundException enfe) {
               Notification.show("Error: El agente que intenta desactivar no fue encontrado.", 4000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            Notification.show("Error al desactivar agente: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
             e.printStackTrace(); // Imprime stack trace para depuración
        }
    }
    // --- FIN MÉTODO ---

    private void closeEditor() {
         if (form != null) {
            form.setAgente(null); // Limpia el bean del binder
            form.setVisible(false); // Oculta el formulario
            removeClassName("editing"); // Quita clase CSS de edición
         }
         grid.asSingleSelect().clear(); // Deselecciona fila en el grid
    }
}