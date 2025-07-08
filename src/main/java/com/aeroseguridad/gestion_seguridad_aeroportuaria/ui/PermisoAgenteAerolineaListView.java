// RUTA: src/main/java/com/aeroseguridad/gestion_seguridad_aeroportuaria/ui/PermisoAgenteAerolineaListView.java
package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Aerolinea;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.EstadoPermiso;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PermisoAgenteAerolinea;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.AerolineaService;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.AgenteService;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.PermisoAgenteAerolineaService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;

@Route(value = "permisos-agente-aerolinea-gestion", layout = MainLayout.class)
@PageTitle("Permisos por Aerolínea | Gestión Seguridad")
@PermitAll
public class PermisoAgenteAerolineaListView extends VerticalLayout {

    private final PermisoAgenteAerolineaService service;
    private final AgenteService agenteService;
    private final AerolineaService aerolineaService;

    private PermisoAgenteAerolineaForm form;
    private TextField carnetAgenteFilter;
    private Grid<PermisoAgenteAerolinea> overviewGrid = new Grid<>(PermisoAgenteAerolinea.class, false);
    private Div contentContainer;

    @Autowired
    public PermisoAgenteAerolineaListView(PermisoAgenteAerolineaService service,
                                          AgenteService agenteService,
                                          AerolineaService aerolineaService) {
        this.service = service;
        this.agenteService = agenteService;
        this.aerolineaService = aerolineaService;
        addClassName("gestion-permisos-view");
        setSizeFull();
        setPadding(false);
    }

    @PostConstruct
    private void initLayout() {
        createForm();
        createOverviewGrid();

        if (form == null) {
            throw new IllegalStateException("El formulario no pudo ser instanciado.");
        }

        HorizontalLayout headerBar = createHeaderBar();
        HorizontalLayout filterLayout = createFilterLayout();
        
        contentContainer = new Div(form, overviewGrid);
        contentContainer.setVisible(false); // Oculto inicialmente
        contentContainer.setWidthFull();

        VerticalLayout mainContent = new VerticalLayout(filterLayout, contentContainer);
        mainContent.setPadding(true);
        mainContent.setSpacing(true);
        mainContent.getStyle().set("padding", "var(--lumo-space-l)");
        
        add(headerBar, mainContent);
    }

    private HorizontalLayout createHeaderBar() {
        H2 title = new H2("Permisos por Aerolínea");
        title.getStyle().set("font-size", "var(--lumo-font-size-xxl)").set("margin", "0");

        HorizontalLayout headerBar = new HorizontalLayout(title);
        headerBar.setAlignItems(FlexComponent.Alignment.CENTER);
        headerBar.setWidthFull();
        headerBar.getStyle().set("padding", "var(--lumo-space-m)");
        headerBar.getStyle().set("border-bottom", "1px solid var(--lumo-contrast-10pct)");

        return headerBar;
    }

    private HorizontalLayout createFilterLayout() {
        carnetAgenteFilter = new TextField("Buscar Agente por Carnet");
        carnetAgenteFilter.setPlaceholder("Ingrese número...");
        carnetAgenteFilter.setClearButtonVisible(true);
        carnetAgenteFilter.setWidthFull();
        carnetAgenteFilter.setMaxWidth("400px");

        Button buscarButton = new Button("Cargar Agente", VaadinIcon.SEARCH.create());
        buscarButton.addClickListener(e -> buscarYcargarAgente());

        HorizontalLayout layout = new HorizontalLayout(carnetAgenteFilter, buscarButton);
        layout.setAlignItems(Alignment.BASELINE);
        layout.getStyle().set("flex-wrap", "wrap");
        return layout;
    }

    private void buscarYcargarAgente() {
        String carnet = carnetAgenteFilter.getValue();
        if (carnet != null && !carnet.trim().isEmpty()) {
            Optional<Agente> agenteOpt = agenteService.findActivoByNumeroCarnet(carnet.trim());
            if (agenteOpt.isPresent()) {
                Agente agente = agenteOpt.get();
                if (form != null) {
                    form.setAgente(agente);
                    contentContainer.setVisible(true);
                }
            } else {
                Notification.show("Agente no encontrado con carnet: " + carnet, 3000, Notification.Position.BOTTOM_START);
                if (form != null) form.setAgente(null);
                contentContainer.setVisible(false);
            }
        } else {
            Notification.show("Ingrese un número de carnet para buscar.", 2000, Notification.Position.BOTTOM_START);
            if (form != null) form.setAgente(null);
            contentContainer.setVisible(false);
        }
    }

    private void createForm() {
        try {
            List<Agente> todosLosAgentes = agenteService.findAllActiveForView("");
            List<Aerolinea> todasLasAerolineas = aerolineaService.findAll();
            form = new PermisoAgenteAerolineaForm(todosLosAgentes, todasLasAerolineas);
            form.setWidth("100%");
            form.addListener(PermisoAgenteAerolineaForm.SaveEvent.class, this::savePermisosDelAgente);
            form.addListener(PermisoAgenteAerolineaForm.AgenteSelectedEvent.class, this::cargarPermisosParaAgenteEnForm);
            form.addListener(PermisoAgenteAerolineaForm.CloseEvent.class, e -> {
                if (form != null) form.setAgente(null);
                carnetAgenteFilter.clear();
                contentContainer.setVisible(false);
            });
        } catch (Exception e) {
            this.form = null;
        }
    }

    private void createOverviewGrid() {
        overviewGrid.setHeight("300px");
        overviewGrid.addColumn(paa -> paa.getAgente().getNombreCompleto()).setHeader("Agente").setSortable(true);
        overviewGrid.addColumn(paa -> paa.getAerolinea().getNombre()).setHeader("Aerolínea").setSortable(true);
        overviewGrid.addColumn(PermisoAgenteAerolinea::getEstadoPermiso).setHeader("Estado").setSortable(true);
        overviewGrid.getColumns().forEach(col -> col.setAutoWidth(true).setResizable(true));
    }

    private void cargarPermisosParaAgenteEnForm(PermisoAgenteAerolineaForm.AgenteSelectedEvent event) {
        Agente agenteSeleccionado = event.getAgente();
        if (agenteSeleccionado != null) {
            List<PermisoAgenteAerolinea> permisosExistentes = service.findByAgenteId(agenteSeleccionado.getIdAgente());
            if (form != null) form.setPermisosExistentes(permisosExistentes);
            updateOverviewGrid(agenteSeleccionado);
        } else {
            if (form != null) form.setPermisosExistentes(Collections.emptyList());
            updateOverviewGrid(null);
        }
    }

    private void savePermisosDelAgente(PermisoAgenteAerolineaForm.SaveEvent event) {
        Agente agente = event.getAgente();
        if (agente == null) {
            Notification.show("No hay agente seleccionado para guardar.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_WARNING);
            return;
        }

        try {
            Map<Long, EstadoPermiso> mapaPermisos = event.getPermisosAActualizar().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v2));
            service.guardarPermisosParaAgente(agente, mapaPermisos);
            Notification.show("Permisos guardados para: " + agente.getNombreCompleto(), 2000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            updateOverviewGrid(agente);
        } catch (Exception e) {
            Notification.show("Error inesperado al guardar permisos: " + e.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            e.printStackTrace();
        }
    }

    private void updateOverviewGrid(Agente agenteFiltrado) {
        if (overviewGrid == null) return;
        if (agenteFiltrado != null) {
            overviewGrid.setItems(service.findByAgenteId(agenteFiltrado.getIdAgente()));
        } else {
            overviewGrid.setItems(Collections.emptyList());
        }
    }
}