// RUTA: com/aeroseguridad/gestion_seguridad_aeroportuaria/ui/AgenteListView.java
package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PosicionSeguridad;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Rol;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.AgenteService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Route(value = "agentes", layout = MainLayout.class)
@PageTitle("Gestión de Personal")
@PermitAll
public class AgenteListView extends VerticalLayout {

    private final AgenteService agenteService;
    private Div agentContainer; // CAMBIADO a Div para usar Grid CSS
    private AgenteForm form;

    private TextField filterText = new TextField("Buscar por nombre");
    private ComboBox<Rol> rolFilter = new ComboBox<>("Rol");
    private ComboBox<Boolean> estadoFilter = new ComboBox<>("Estado");

    @Autowired
    public AgenteListView(AgenteService agenteService) {
        this.agenteService = agenteService;
        addClassName("agente-list-view");
        setSizeFull();
        setPadding(false);
        setSpacing(false); // Evitar espaciado extra del VerticalLayout
    }

    @PostConstruct
    private void initLayout() {
        createAgentContainer();
        createForm();
        
        HorizontalLayout headerBar = createHeaderBar();
        
        add(headerBar, agentContainer); // Se añade el contenedor directamente
        updateList();
    }

    private HorizontalLayout createHeaderBar() {
        // ESTA BARRA SE PUEDE ELIMINAR SI EL HEADER DE MainLayout YA ES SUFICIENTE
        // O MANTENER PARA TÍTULOS Y FILTROS ESPECÍFICOS DE LA VISTA
        H2 title = new H2("Personal Registrado");
        title.getStyle().set("font-size", "var(--lumo-font-size-xxl)").set("margin", "0");

        Button filterButton = new Button("Filtros", VaadinIcon.FILTER.create());
        filterButton.addClickListener(e -> openFiltersDialog());

        Button addButton = new Button("Nuevo Personal", VaadinIcon.PLUS.create());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> openAgenteFormDialog(new Agente()));

        HorizontalLayout headerBar = new HorizontalLayout(title, filterButton, addButton);
        headerBar.setAlignItems(FlexComponent.Alignment.CENTER);
        headerBar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        headerBar.setWidthFull();
        headerBar.getStyle().set("padding", "0 var(--lumo-space-m) var(--lumo-space-l) var(--lumo-space-m)");
        
        return headerBar;
    }
    
    private void openAgenteFormDialog(Agente agente) {
        if (form == null) {
            Notification.show("El formulario no está disponible.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        Dialog dialog = new Dialog();
        // APLICA EL ANCHO RECOMENDADO PARA EL MODAL
        dialog.setWidth("650px");

        // Crea el header del modal con su clase CSS
        H2 title = new H2(agente.getIdAgente() == null ? "Nuevo Personal" : "Editar Personal");
        Div modalHeader = new Div(title);
        modalHeader.addClassName("form-modal-header");

        form.setAgente(agente);
        
        // El footer se maneja dentro del AgenteForm para mayor encapsulación
        // O se puede crear aquí si los botones son genéricos.
        dialog.add(modalHeader, form);
        
        form.addListener(AgenteForm.SaveEvent.class, event -> {
            boolean success = saveAgente(event);
            if (success) {
                dialog.close();
            }
        });
        form.addListener(AgenteForm.DeleteEvent.class, event -> {
            confirmAndDeleteAgente(event);
        });
        form.addListener(AgenteForm.CloseEvent.class, event -> dialog.close());

        dialog.open();
    }

    private void createAgentContainer() {
        agentContainer = new Div();
        // APLICA LA CLASE PARA LA CUADRÍCULA DE TARJETAS
        agentContainer.addClassName("agent-card-grid");
        agentContainer.setSizeFull();
    }

    private void updateList() {
        try {
            List<Agente> agentes = agenteService.list(
                filterText.getValue(),
                rolFilter.getValue(),
                estadoFilter.getValue()
            );
            agentContainer.removeAll();
            if (agentes.isEmpty()) {
                agentContainer.add(new Span("No se encontró personal con los filtros aplicados."));
            } else {
                agentes.forEach(agente -> {
                    AgenteCard card = new AgenteCard(agente);
                    card.addCardClickListener(e -> openAgenteFormDialog(e.getAgente()));
                    agentContainer.add(card);
                });
            }
        } catch (Exception e) {
            Notification.show("Error al cargar personal: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
            agentContainer.removeAll();
            agentContainer.add(new Span("Error al cargar la lista de personal."));
        }
    }

    // ... RESTO DE MÉTODOS (createForm, saveAgente, deleteAgente, etc.) SIN CAMBIOS ...
    // ... (El código de los otros métodos se mantiene igual que el que proporcionaste)
    private void createForm() {
        try {
            List<PosicionSeguridad> allPosiciones = agenteService.findAllPosiciones();
            form = new AgenteForm(allPosiciones);
            form.setWidth("100%");
        } catch (Exception e) {
            form = null;
            Notification.show("Error crítico al inicializar el formulario: " + e.getMessage(), 0, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            e.printStackTrace();
        }
    }

    private void addAgente() {
        openAgenteFormDialog(new Agente());
    }

    private boolean saveAgente(AgenteForm.SaveEvent event) {
        try {
            agenteService.save(event.getAgente(), event.getFotoStream(), event.getNombreOriginalFoto());
            updateList();
            Notification.show("Personal guardado.", 2000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            return true;
        } catch (DataIntegrityViolationException e) {
            handleDataIntegrityViolation(e, event.getAgente());
            return false;
        } catch (Exception e) {
            Notification.show("Error inesperado al guardar: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
            e.printStackTrace();
            return false;
        }
    }

    private void handleDataIntegrityViolation(DataIntegrityViolationException e, Agente agente) {
        String message = "Error: No se pudo guardar el registro.";
        String specificCause = e.getMostSpecificCause().getMessage().toLowerCase();
        if (specificCause.contains("agentes_numero_carnet_key") || (specificCause.contains("uk_") && specificCause.contains("numero_carnet"))) {
            message = "Error: El Número de Carnet '" + agente.getNumeroCarnet() + "' ya existe.";
        } else if (specificCause.contains("agentes_email_key") || (specificCause.contains("uk_") && specificCause.contains("email"))) {
            message = "Error: El Email '" + agente.getEmail() + "' ya existe.";
        }
        Notification.show(message, 5000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private void confirmAndDeleteAgente(AgenteForm.DeleteEvent event) {
        Agente agenteABorrar = event.getAgente();
        if (agenteABorrar == null || agenteABorrar.getIdAgente() == null) {
            Notification.show("No hay un registro seleccionado para borrar.", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_WARNING);
            return;
        }

        Dialog confirmationDialog = new Dialog();
        confirmationDialog.setHeaderTitle("Confirmar Borrado");
        confirmationDialog.add(new VerticalLayout(
            new Span("¿Estás seguro de que quieres eliminar permanentemente a " + agenteABorrar.getNombreCompleto() + "?"),
            new Span("Esta acción no se puede deshacer.")
        ));
        
        Button confirmButton = new Button("Borrar", VaadinIcon.TRASH.create(), e -> {
            deleteAgente(agenteABorrar);
            confirmationDialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        Button cancelButton = new Button("Cancelar", e -> confirmationDialog.close());
        confirmationDialog.getFooter().add(cancelButton, confirmButton);
        confirmationDialog.open();
    }

    private void deleteAgente(Agente agente) {
        try {
            agenteService.deleteById(agente.getIdAgente());
            updateList();
            Notification.show("Personal eliminado permanentemente.", 2000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_CONTRAST);
        } catch (EntityNotFoundException enfe) {
            Notification.show("Error: El registro que intenta borrar no fue encontrado.", 4000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            Notification.show("Error al eliminar: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
            e.printStackTrace();
        }
    }
    
    // El método openFiltersDialog se mantiene igual que el que proporcionaste
    private void openFiltersDialog() {
        filterText.setPlaceholder("Buscar...");
        rolFilter.setItems(Rol.values());
        rolFilter.setItemLabelGenerator(Rol::getDescripcion);
        rolFilter.setClearButtonVisible(true);
        Map<Boolean, String> estadoItems = new LinkedHashMap<>();
        estadoItems.put(true, "Activo");
        estadoItems.put(false, "Inactivo");
        estadoFilter.setItems(estadoItems.keySet());
        estadoFilter.setItemLabelGenerator(estadoItems::get);
        estadoFilter.setClearButtonVisible(true);

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Filtros de Búsqueda");
        dialog.add(new VerticalLayout(filterText, rolFilter, estadoFilter));

        Button applyButton = new Button("Aplicar", e -> {
            updateList();
            dialog.close();
        });
        applyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        Button clearButton = new Button("Limpiar", e -> {
            filterText.clear();
            rolFilter.clear();
            estadoFilter.clear();
            updateList();
            dialog.close();
        });
        dialog.getFooter().add(clearButton, applyButton);
        dialog.open();
    }
}