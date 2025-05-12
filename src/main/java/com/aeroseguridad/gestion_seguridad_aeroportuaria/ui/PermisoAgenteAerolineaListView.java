// src/main/java/com/aeroseguridad/gestion_seguridad_aeroportuaria/ui/PermisoAgenteAerolineaListView.java
package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Aerolinea;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PermisoAgenteAerolinea;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.EstadoPermiso;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.AgenteService;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.AerolineaService;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.PermisoAgenteAerolineaService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Route(value = "permisos-agente-aerolinea-gestion", layout = MainLayout.class)
@PageTitle("Gestión de Permisos por Agente | Gestión Seguridad")
@PermitAll
public class PermisoAgenteAerolineaListView extends VerticalLayout {

    private final PermisoAgenteAerolineaService service;
    private final AgenteService agenteService;
    private final AerolineaService aerolineaService;

    private PermisoAgenteAerolineaForm form;
    private TextField carnetAgenteFilter;
    private Grid<PermisoAgenteAerolinea> overviewGrid = new Grid<>(PermisoAgenteAerolinea.class, false);

    @Autowired
    public PermisoAgenteAerolineaListView(PermisoAgenteAerolineaService service,
                                          AgenteService agenteService,
                                          AerolineaService aerolineaService) {
        this.service = service;
        this.agenteService = agenteService;
        this.aerolineaService = aerolineaService;
        addClassName("gestion-permisos-por-agente-view");
        setSizeFull();
    }

    @PostConstruct
    private void initLayout() {
        try {
            createForm();
            createOverviewGrid();

            if (form == null) {
                throw new IllegalStateException("El formulario PermisoAgenteAerolineaForm no pudo ser instanciado.");
            }

            HorizontalLayout filterLayout = createFilterLayout();
            add(filterLayout, form, overviewGrid);

            form.setVisible(true);
            overviewGrid.setVisible(true);
            updateOverviewGrid(null);

        } catch (Exception e) {
            System.err.println("Error inicializando GestionPermisosPorAgenteView: " + e.getMessage());
            e.printStackTrace();
            Notification.show("Error al cargar la vista de gestión de permisos.", 0, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private HorizontalLayout createFilterLayout() {
        carnetAgenteFilter = new TextField("Buscar Agente por Carnet");
        carnetAgenteFilter.setPlaceholder("Ingrese número...");
        carnetAgenteFilter.setClearButtonVisible(true);
        carnetAgenteFilter.setWidth("300px");

        Button buscarButton = new Button("Cargar Agente", VaadinIcon.SEARCH.create());
        buscarButton.addClickListener(e -> buscarYcargarAgente());

        HorizontalLayout layout = new HorizontalLayout(carnetAgenteFilter, buscarButton);
        layout.setAlignItems(Alignment.BASELINE);
        return layout;
    }

    private void buscarYcargarAgente() {
        String carnet = carnetAgenteFilter.getValue();
        if (carnet != null && !carnet.trim().isEmpty()) {
            Optional<Agente> agenteOpt = agenteService.findActivoByNumeroCarnet(carnet.trim());
            if (agenteOpt.isPresent()) {
                // --- CORRECCIÓN: Usar el getter ---
                if (form != null && form.getAgenteComboBox() != null) {
                    form.getAgenteComboBox().setValue(agenteOpt.get());
                }
                // --- FIN CORRECCIÓN ---
            } else {
                Notification.show("Agente no encontrado con carnet: " + carnet, 3000, Notification.Position.BOTTOM_START);
                if (form != null) form.setAgente(null);
            }
        } else {
            Notification.show("Ingrese un número de carnet para buscar.", 2000, Notification.Position.BOTTOM_START);
            if (form != null) form.setAgente(null);
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
                updateOverviewGrid(null);
            });
        } catch (Exception e) {
            this.form = null;
            System.err.println("Error creando PermisoAgenteAerolineaForm: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createOverviewGrid() {
        overviewGrid.setHeight("300px");
        overviewGrid.addColumn(paa -> paa.getAgente().getNombreCompleto())
            .setHeader("Agente").setSortable(true);
        overviewGrid.addColumn(paa -> paa.getAerolinea().getNombre())
            .setHeader("Aerolínea").setSortable(true);
        overviewGrid.addColumn(PermisoAgenteAerolinea::getEstadoPermiso)
            .setHeader("Estado").setSortable(true);
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
        List<Map.Entry<Long, EstadoPermiso>> permisosAActualizar = event.getPermisosAActualizar();

        if (agente == null || permisosAActualizar == null) {
            Notification.show("No hay agente o permisos para guardar.", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_WARNING);
            return;
        }

        try {
            Map<Long, EstadoPermiso> mapaPermisos = permisosAActualizar.stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v2));

            service.guardarPermisosParaAgente(agente, mapaPermisos);
            Notification.show("Permisos guardados para el agente: " + agente.getNombreCompleto(), 2000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            
            // Vuelve a cargar los permisos para el agente actual en el formulario y actualiza el grid de resumen
            // --- CORRECCIÓN: Usar el getter ---
            if (form != null && form.getAgenteComboBox() != null && form.getAgenteComboBox().getValue() != null && form.getAgenteComboBox().getValue().equals(agente)){
            // --- FIN CORRECCIÓN ---
                 cargarPermisosParaAgenteEnForm(new PermisoAgenteAerolineaForm.AgenteSelectedEvent(form, agente));
            } else if (form != null && form.getAgenteComboBox() != null) { // Asegurar que no sea null
                 form.getAgenteComboBox().setValue(agente); // Esto debería disparar la carga
            }
            updateOverviewGrid(agente);


        } catch (DataIntegrityViolationException e) {
            Notification.show("Error de integridad: " + e.getMostSpecificCause().getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            Notification.show("Error inesperado al guardar permisos: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            e.printStackTrace();
        }
    }

    private void updateOverviewGrid(Agente agenteFiltrado) {
        if (overviewGrid == null) return;
        if (agenteFiltrado != null) {
            overviewGrid.setItems(service.findByAgenteId(agenteFiltrado.getIdAgente()));
        } else {
            overviewGrid.setItems(service.findAll());
        }
        if(overviewGrid.getDataProvider() != null) overviewGrid.getDataProvider().refreshAll();
    }
}