package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PosicionSeguridad;
// Quitar import de Grid si ya no se usa para la lista principal
// import com.vaadin.flow.component.grid.Grid;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.AgenteService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span; // Para mensajes
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent; // Para FlexLayout
import com.vaadin.flow.component.orderedlayout.FlexLayout; // Para FlexLayout
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Collections;
import java.util.List;

@Route(value = "agentes", layout = MainLayout.class)
@PageTitle("Agentes | Gestión Seguridad")
@PermitAll
public class AgenteListView extends VerticalLayout {

    private final AgenteService agenteService;

    // --- CAMBIO: Quitar Grid, Añadir FlexLayout para tarjetas ---
    // private Grid<Agente> grid;
    private FlexLayout agentContainer; // Contenedor para las tarjetas
    // --- FIN CAMBIO ---

    private TextField filterText;
    private Button addAgenteButton;
    private AgenteForm form; // Debe ser la versión con Upload
    private HorizontalLayout toolbar;
    private SplitLayout splitLayout;


    @Autowired
    public AgenteListView(AgenteService agenteService) {
        this.agenteService = agenteService;
        addClassName("agente-list-view");
        setSizeFull();
    }

    @PostConstruct
    private void initLayout() {
        try {
            // --- CAMBIO: Crear contenedor de tarjetas en lugar de grid ---
            createAgentContainer();
            // --- FIN CAMBIO ---
            createForm(); // Asegúrate que AgenteForm es la versión con Upload
            createToolbar();

            if (form == null) {
                throw new IllegalStateException("AgenteForm no pudo ser instanciado.");
            }

            // --- CAMBIO: Usar agentContainer en el SplitLayout ---
            splitLayout = new SplitLayout(agentContainer, form);
            // --- FIN CAMBIO ---
            splitLayout.setOrientation(SplitLayout.Orientation.HORIZONTAL);
            // Ajustar splitter: más espacio para tarjetas, menos para el form
            splitLayout.setSplitterPosition(75); // 75% para tarjetas, 25% para form
            splitLayout.setSizeFull();

            add(toolbar, splitLayout);
            updateList();
            closeEditor();
        } catch (Exception e) {
            System.err.println("Error inicializando AgenteListView: " + e.getMessage());
            e.printStackTrace();
            Notification.show("Error al cargar la vista de Agentes.",0 , Notification.Position.MIDDLE);
        }
    }

    private void createToolbar() {
        filterText = new TextField();
        filterText.setPlaceholder("Buscar por nombre o apellido");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateList());

        addAgenteButton = new Button("Nuevo Agente", VaadinIcon.PLUS.create());
        addAgenteButton.addClickListener(click -> addAgente());

        toolbar = new HorizontalLayout(filterText, addAgenteButton);
        toolbar.addClassName("toolbar");
        toolbar.setWidthFull();
        toolbar.setFlexGrow(1, filterText);
    }

    // --- NUEVO MÉTODO para crear el contenedor de tarjetas ---
    private void createAgentContainer() {
        agentContainer = new FlexLayout();
        agentContainer.addClassName("agente-container"); // Para estilos CSS
        agentContainer.setFlexWrap(FlexLayout.FlexWrap.WRAP); // Tarjetas se ajustan en múltiples líneas
        agentContainer.setAlignItems(FlexComponent.Alignment.START); // Alinea tarjetas arriba
        agentContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.START); // Empieza desde la izquierda
        // Para permitir scroll vertical si hay muchas tarjetas
        agentContainer.getStyle().set("overflow-y", "auto");
        agentContainer.getStyle().set("padding", "var(--lumo-space-s)"); // Pequeño padding alrededor
        agentContainer.setSizeFull();
    }
    // --- FIN NUEVO MÉTODO ---

    // --- MÉTODO configureGrid YA NO ES NECESARIO para la lista principal ---
    // private void configureGrid() { ... }


    private void createForm() {
        // ... (sin cambios, asegúrate que AgenteForm es la versión con Upload) ...
        try {
            //findAllPosiciones ahora devuelve solo activas
            List<PosicionSeguridad> allPosiciones = agenteService.findAllPosiciones();
            form = new AgenteForm(allPosiciones);
            form.setWidth("400px");
            form.addListener(AgenteForm.SaveEvent.class, this::saveAgente);
            form.addListener(AgenteForm.DeleteEvent.class, this::deactivateAgente);
            form.addListener(AgenteForm.CloseEvent.class, e -> closeEditor());
         } catch (Exception e) {
            form = null;
            e.printStackTrace();
        }
    }

    // --- updateList MODIFICADO para poblar el agentContainer con AgenteCard ---
    private void updateList() {
         if (agentContainer != null) { // Verificar que el contenedor exista
             try {
                List<Agente> agentes = agenteService.findAllActiveForView(filterText.getValue());
                agentContainer.removeAll(); // Limpiar tarjetas anteriores
                if (agentes.isEmpty()) {
                    agentContainer.add(new Span("No se encontraron agentes."));
                } else {
                    agentes.forEach(agente -> agentContainer.add(new AgenteCard(agente, this)));
                }
             } catch (Exception e) {
                Notification.show("Error al cargar agentes: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                agentContainer.removeAll();
                agentContainer.add(new Span("Error al cargar la lista de agentes."));
             }
         }
    }
    // --- FIN updateList ---

    private void addAgente() {
        if (form == null) return;
        // grid.asSingleSelect().clear(); // Ya no hay grid principal para deseleccionar
        editAgente(new Agente());
    }

    // editAgente ahora se llama desde AgenteCard o al crear nuevo
    // Asegúrate que el form se muestre correctamente
    public void editAgente(Agente agente) {
        if (form == null) return;
        if (agente == null) {
            closeEditor();
        } else {
            form.setAgente(agente);
            form.setVisible(true); // Asegura que el form sea visible
            // addClassName("editing"); // Opcional si se usa SplitLayout y se quiere estilizar
        }
    }

    private void saveAgente(AgenteForm.SaveEvent event) {
        // ... (La lógica de saveAgente, incluyendo el manejo de la foto, permanece igual) ...
        try {
            agenteService.save(event.getAgente(), event.getFotoStream(), event.getNombreOriginalFoto());
            updateList();
            closeEditor();
            Notification.show("Agente guardado.", 2000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (DataIntegrityViolationException e) {
             handleDataIntegrityViolation(e, event.getAgente());
        } catch (RuntimeException e) {
            Notification.show("Error al guardar la foto: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
        catch (Exception e) {
             Notification.show("Error inesperado al guardar agente: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
             e.printStackTrace();
        }
    }

    private void handleDataIntegrityViolation(DataIntegrityViolationException e, Agente agente) {
        // ... (sin cambios) ...
         String message = "Error: No se pudo guardar el agente.";
         String specificCause = e.getMostSpecificCause().getMessage().toLowerCase();
         if (specificCause.contains("agentes_numero_carnet_key") || specificCause.contains("uk_") && specificCause.contains("numero_carnet")) {
             message = "Error: El Número de Carnet '" + agente.getNumeroCarnet() + "' ya existe.";
             if (form != null && form.numeroCarnet != null) {
                 form.numeroCarnet.setInvalid(true);
                 form.numeroCarnet.setErrorMessage("Este número de carnet ya existe");
             }
         } else if (specificCause.contains("agentes_email_key") || specificCause.contains("uk_") && specificCause.contains("email")) {
             message = "Error: El Email '" + agente.getEmail() + "' ya existe.";
              if (form != null && form.email != null) {
                 form.email.setInvalid(true);
                 form.email.setErrorMessage("Este email ya existe");
             }
         }
         Notification.show(message, 5000, Notification.Position.BOTTOM_CENTER)
              .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private void deactivateAgente(AgenteForm.DeleteEvent event) {
        // ... (sin cambios) ...
        if (form == null) return;
        Agente agenteADesactivar = event.getAgente();

        if (agenteADesactivar == null || agenteADesactivar.getIdAgente() == null) {
             Notification.show("Seleccione un agente guardado para desactivar.", 3000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
             return;
        }
        try {
            agenteService.deactivateById(agenteADesactivar.getIdAgente());
            updateList();
            closeEditor();
            Notification.show("Agente desactivado.", 2000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
        } catch (EntityNotFoundException enfe) {
               Notification.show("Error: El agente que intenta desactivar no fue encontrado.", 4000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            Notification.show("Error al desactivar agente: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
             e.printStackTrace();
        }
    }

    private void closeEditor() {
         if (form != null) {
            form.setAgente(null);
            form.setVisible(false);
            // removeClassName("editing"); // Opcional
         }
         // if (grid != null) { // Ya no hay grid principal para deseleccionar
         //    grid.asSingleSelect().clear();
         // }
    }
}