package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Rol;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.AgenteRepository;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

@Route(value = "organigrama", layout = MainLayout.class) // Renombramos la ruta
@PageTitle("Organigrama Jerárquico")
@RolesAllowed("ROLE_ADMIN")
public class SupervisoresView extends VerticalLayout {

    private final AgenteRepository agenteRepository;

    private ComboBox<Agente> coordinadorSelector;
    private FlexLayout supervisoresContainer;

    public SupervisoresView(AgenteRepository agenteRepository) {
        this.agenteRepository = agenteRepository;
        setSizeFull();
        setSpacing(true);

        add(new H2("Constructor de Organigrama"));

        // 1. Crear el selector de Coordinador principal
        createCoordinadorSelector();
        add(coordinadorSelector);

        // 2. Crear el contenedor para las tarjetas de supervisores
        supervisoresContainer = new FlexLayout();
        supervisoresContainer.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        supervisoresContainer.getStyle().set("gap", "var(--lumo-space-l)");
        add(supervisoresContainer);
    }

    private void createCoordinadorSelector() {
        coordinadorSelector = new ComboBox<>("Seleccionar Coordinador a Cargo");
        // Buscamos solo el personal con el rol de Coordinador
        List<Agente> coordinadores = agenteRepository.findByRol(Rol.COORDINADOR);
        coordinadorSelector.setItems(coordinadores);
        coordinadorSelector.setItemLabelGenerator(Agente::getNombreCompleto);
        coordinadorSelector.setWidth("50%");
        coordinadorSelector.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                // Cuando se selecciona un coordinador, se construye su organigrama
                construirOrganigrama(event.getValue());
            } else {
                supervisoresContainer.removeAll();
            }
        });
    }

    private void construirOrganigrama(Agente coordinador) {
        supervisoresContainer.removeAll();
        // Los supervisores son los subordinados directos del coordinador
        List<Agente> supervisores = List.copyOf(coordinador.getSubordinados());

        // Buscamos todos los agentes que aún no tienen un supervisor
        List<Agente> agentesLibres = agenteRepository.findByRolAndSuperiorIsNull(Rol.AGENTE);

        supervisores.forEach(supervisor -> {
            SupervisorOrganigramaCard card = new SupervisorOrganigramaCard(supervisor, agentesLibres, this::handleAsignacionAgente);
            supervisoresContainer.add(card);
        });
    }

    private void handleAsignacionAgente(Agente supervisor, Agente agente) {
        try {
            // Asignamos el supervisor al agente
            agente.setSuperior(supervisor);
            // Guardamos el agente actualizado en la base de datos
            agenteRepository.save(agente);

            // Refrescamos el organigrama para mostrar el cambio
            construirOrganigrama(coordinadorSelector.getValue());

            Notification.show(agente.getNombre() + " asignado a " + supervisor.getNombre(), 2000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            Notification.show("Error al asignar agente: " + e.getMessage(), 4000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}