package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PosicionSeguridad;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.AgenteService;
import com.vaadin.flow.component.button.Button;
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
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

@Route(value = "agentes", layout = MainLayout.class)
@PageTitle("Personal | Gestión Seguridad") // Renombramos el título para reflejar la nueva realidad
@PermitAll
public class AgenteListView extends VerticalLayout {

    private final AgenteService agenteService;

    private FlexLayout agentContainer;
    private TextField filterText;
    private Button addAgenteButton;
    private AgenteForm form;
    private HorizontalLayout toolbar;
    private SplitLayout splitLayout;

    // --- 1. CONSTRUCTOR SIMPLIFICADO ---
    // Se elimina la dependencia de SupervisorRepository
    @Autowired
    public AgenteListView(AgenteService agenteService) {
        this.agenteService = agenteService;
        addClassName("agente-list-view");
        setSizeFull();
    }

    @PostConstruct
    private void initLayout() {
        try {
            createAgentContainer();
            createForm(); // Este método ahora es más simple
            createToolbar();

            if (form == null) {
                throw new IllegalStateException("El formulario de personal no pudo ser instanciado.");
            }
            
            splitLayout = new SplitLayout(agentContainer, form);
            splitLayout.setOrientation(SplitLayout.Orientation.HORIZONTAL);
            splitLayout.setSplitterPosition(75);
            splitLayout.setSizeFull();

            add(toolbar, splitLayout);
            updateList();
            closeEditor();
        } catch (Exception e) {
            System.err.println("Error inicializando AgenteListView: " + e.getMessage());
            e.printStackTrace();
            Notification.show("Error al cargar la vista de Personal.",0 , Notification.Position.MIDDLE);
        }
    }
    
    // --- 2. MÉTODO createForm ACTUALIZADO ---
    private void createForm() {
        try {
            // El formulario ahora solo necesita la lista de posiciones de seguridad.
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
    
    // El resto de la clase no necesita cambios funcionales, ya que la lógica
    // de guardado y edición depende del evento del formulario, que ya hemos refactorizado.

    private void createToolbar() {
        filterText = new TextField();
        filterText.setPlaceholder("Buscar por nombre o apellido");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateList());

        addAgenteButton = new Button("Nuevo Personal", VaadinIcon.PLUS.create());
        addAgenteButton.addClickListener(click -> addAgente());

        toolbar = new HorizontalLayout(filterText, addAgenteButton);
        toolbar.addClassName("toolbar");
        toolbar.setWidthFull();
        toolbar.setFlexGrow(1, filterText);
    }

    private void createAgentContainer() {
        agentContainer = new FlexLayout();
        agentContainer.addClassName("agente-container");
        agentContainer.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        agentContainer.setAlignItems(FlexComponent.Alignment.START);
        agentContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        agentContainer.getStyle().set("overflow-y", "auto");
        agentContainer.getStyle().set("padding", "var(--lumo-space-s)");
        agentContainer.setSizeFull();
    }

    private void updateList() {
         if (agentContainer != null) {
              try {
                  List<Agente> agentes = agenteService.findAllActiveForView(filterText.getValue());
                  agentContainer.removeAll();
                  if (agentes.isEmpty()) {
                      agentContainer.add(new Span("No se encontró personal."));
                  } else {
                      agentes.forEach(agente -> agentContainer.add(new AgenteCard(agente, this)));
                  }
               } catch (Exception e) {
                  Notification.show("Error al cargar personal: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                           .addThemeVariants(NotificationVariant.LUMO_ERROR);
                  agentContainer.removeAll();
                  agentContainer.add(new Span("Error al cargar la lista de personal."));
               }
         }
    }

    private void addAgente() {
        if (form == null) return;
        editAgente(new Agente());
    }

    public void editAgente(Agente agente) {
        if (form == null) return;
        if (agente == null) {
            closeEditor();
        } else {
            form.setAgente(agente);
            form.setVisible(true);
        }
    }

    private void saveAgente(AgenteForm.SaveEvent event) {
        try {
            agenteService.save(event.getAgente(), event.getFotoStream(), event.getNombreOriginalFoto());
            updateList();
            closeEditor();
            Notification.show("Personal guardado.", 2000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (DataIntegrityViolationException e) {
             handleDataIntegrityViolation(e, event.getAgente());
        } catch (RuntimeException e) {
            Notification.show("Error al guardar la foto: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
        catch (Exception e) {
             Notification.show("Error inesperado al guardar: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                     .addThemeVariants(NotificationVariant.LUMO_ERROR);
             e.printStackTrace();
        }
    }

    private void handleDataIntegrityViolation(DataIntegrityViolationException e, Agente agente) {
         String message = "Error: No se pudo guardar el registro.";
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
        if (form == null) return;
        Agente agenteADesactivar = event.getAgente();

        if (agenteADesactivar == null || agenteADesactivar.getIdAgente() == null) {
             Notification.show("Seleccione un registro guardado para desactivar.", 3000, Notification.Position.BOTTOM_CENTER)
                     .addThemeVariants(NotificationVariant.LUMO_WARNING);
             return;
        }
        try {
            agenteService.deactivateById(agenteADesactivar.getIdAgente());
            updateList();
            closeEditor();
            Notification.show("Personal desactivado.", 2000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
        } catch (EntityNotFoundException enfe) {
               Notification.show("Error: El registro que intenta desactivar no fue encontrado.", 4000, Notification.Position.BOTTOM_CENTER)
               .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            Notification.show("Error al desactivar: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
             e.printStackTrace();
        }
    }

    private void closeEditor() {
         if (form != null) {
            form.setAgente(null);
            form.setVisible(false);
         }
    }
}