// RUTA: src/main/java/com/aeroseguridad/gestion_seguridad_aeroportuaria/ui/SupervisoresView.java
package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Rol;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.AgenteService;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import jakarta.annotation.security.RolesAllowed;

@Route(value = "organigrama", layout = MainLayout.class)
@PageTitle("Organigrama | Gestión Seguridad")
@RolesAllowed("ROLE_ADMIN")
public class SupervisoresView extends VerticalLayout {

    private final AgenteService agenteService;
    private final Div contentContainer;

    public SupervisoresView(AgenteService agenteService) {
        this.agenteService = agenteService;
        setSizeFull();
        setPadding(false); // Padding controlado por los elementos internos

        // Encabezado estándar
        H2 title = new H2("Organigrama y Equipos");
        title.getStyle().set("font-size", "var(--lumo-font-size-xxl)").set("margin", "0");
        
        HorizontalLayout headerBar = new HorizontalLayout(title);
        headerBar.setWidthFull();
        headerBar.getStyle().set("padding", "var(--lumo-space-m)");
        headerBar.getStyle().set("border-bottom", "1px solid var(--lumo-contrast-10pct)");
        
        // Selector de Coordinador
        ComboBox<Agente> coordinadorSelector = new ComboBox<>("Seleccione un Coordinador");
        coordinadorSelector.setItems(agenteService.findByRol(Rol.COORDINADOR));
        coordinadorSelector.setItemLabelGenerator(Agente::getNombreCompleto);
        coordinadorSelector.setWidthFull();
        coordinadorSelector.setMaxWidth("700px");

        // Contenedor para el resultado
        contentContainer = new Div();
        contentContainer.setWidthFull();

        coordinadorSelector.addValueChangeListener(e -> {
            contentContainer.removeAll();
            if (e.getValue() != null) {
                displayCoordinatorForEditing(e.getValue());
            }
        });
        
        // Layout para centrar el contenido principal
        VerticalLayout mainContent = new VerticalLayout(coordinadorSelector, contentContainer);
        mainContent.setWidthFull();
        mainContent.setAlignItems(FlexComponent.Alignment.CENTER);
        mainContent.getStyle().set("padding", "var(--lumo-space-l)");
        
        add(headerBar, mainContent);
    }

    private void displayCoordinatorForEditing(Agente coordinador) {
        AgenteCard card = new AgenteCard(coordinador);

        card.addCardClickListener(event -> {
            TeamBuilderDialog dialog = new TeamBuilderDialog(event.getAgente(), agenteService);
            dialog.open();
        });

        Span instructionText = new Span("Haga clic en la tarjeta para construir o editar el equipo.");
        instructionText.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.Margin.Bottom.MEDIUM);
        
        VerticalLayout cardLayout = new VerticalLayout(instructionText, card);
        cardLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        cardLayout.setSpacing(true);
        cardLayout.setPadding(false);

        contentContainer.add(cardLayout);
    }
}