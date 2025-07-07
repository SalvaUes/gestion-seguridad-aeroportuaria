// RUTA: src/main/java/com/aeroseguridad/gestion_seguridad_aeroportuaria/ui/SupervisoresView.java
package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Rol;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.AgenteService;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "organigrama", layout = MainLayout.class)
@PageTitle("Supervisores | Gestión Seguridad")
@RolesAllowed("ROLE_ADMIN")
public class SupervisoresView extends VerticalLayout {

    private final AgenteService agenteService;
    private final Div contentContainer;

    public SupervisoresView(AgenteService agenteService) {
        this.agenteService = agenteService;
        setSizeFull();
        setPadding(true);

        // --- MEJORA: Encabezado estándar de la vista ---
        H2 header = new H2("Gestión de Supervisores");
        header.getStyle().set("margin-top", "var(--lumo-space-m)");
        header.getStyle().set("font-size", "var(--lumo-font-size-xxl)");
        add(header);

        ComboBox<Agente> coordinadorSelector = new ComboBox<>("Seleccione un Coordinador para gestionar su equipo");
        coordinadorSelector.setItems(agenteService.findByRol(Rol.COORDINADOR));
        coordinadorSelector.setItemLabelGenerator(Agente::getNombreCompleto);

        // --- MEJORA: Ancho responsivo para el ComboBox ---
        coordinadorSelector.setWidthFull();
        coordinadorSelector.setMaxWidth("700px");

        contentContainer = new Div();
        contentContainer.setWidthFull();

        coordinadorSelector.addValueChangeListener(e -> {
            contentContainer.removeAll();
            if (e.getValue() != null) {
                displayCoordinatorForEditing(e.getValue());
            }
        });

        add(coordinadorSelector, contentContainer);
    }

    private void displayCoordinatorForEditing(Agente coordinador) {
        AgenteCard card = new AgenteCard(coordinador);

        card.addCardClickListener(event -> {
            TeamBuilderDialog dialog = new TeamBuilderDialog(event.getAgente(), agenteService);
            dialog.open();
        });

        Span instructionText = new Span("Haga clic en la tarjeta para construir o editar el equipo de este coordinador.");
        instructionText.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.Margin.Bottom.MEDIUM);

        contentContainer.add(instructionText, card);
        contentContainer.getStyle().set("margin-top", "var(--lumo-space-l)");
    }
}