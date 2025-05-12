package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Aerolinea;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.EstadoPermiso;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PermisoAgenteAerolinea;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Collections;
import java.util.stream.Collectors;

public class PermisoAgenteAerolineaForm extends FormLayout {

    // --- CAMBIO: Modificador de acceso package-private (default) ---
    ComboBox<Agente> agenteComboBox = new ComboBox<>("Agente");
    // --- FIN CAMBIO ---
    private Grid<AerolineaPermisoWrapper> aerolineasGrid = new Grid<>();
    private List<Aerolinea> todasLasAerolineas;

    private Button save = new Button("Guardar Permisos");
    private Button cancel = new Button("Cancelar");

    private Agente agenteActual;
    private Map<Long, EstadoPermiso> permisosSeleccionadosUI = new HashMap<>();
    private List<PermisoAgenteAerolinea> permisosOriginalesDelAgente;


    public PermisoAgenteAerolineaForm(List<Agente> agentes, List<Aerolinea> aerolineas) {
        this.todasLasAerolineas = aerolineas;
        addClassName("permiso-agente-multifacility-form");

        agenteComboBox.setItems(agentes);
        agenteComboBox.setItemLabelGenerator(a -> a != null ? a.getNombre() + " " + a.getApellido() : "");
        agenteComboBox.setRequired(true);
        agenteComboBox.setPlaceholder("Seleccione un agente");
        agenteComboBox.addValueChangeListener(event -> {
            setAgente(event.getValue());
            save.setEnabled(event.getValue() != null);
        });

        configureAerolineasGrid();

        add(agenteComboBox, aerolineasGrid, createButtonsLayout());
        aerolineasGrid.setVisible(false);
    }

    private void configureAerolineasGrid() {
        aerolineasGrid.setHeight("300px");
        aerolineasGrid.addColumn(AerolineaPermisoWrapper::getNombreAerolinea).setHeader("Aerolínea");
        aerolineasGrid.addColumn(new ComponentRenderer<>(wrapper -> {
            ComboBox<EstadoPermiso> estadoPermisoComboBox = new ComboBox<>();
            estadoPermisoComboBox.setItems(EstadoPermiso.values());
            estadoPermisoComboBox.setPlaceholder("Seleccionar estado");
            estadoPermisoComboBox.setValue(wrapper.getEstadoPermiso());
            estadoPermisoComboBox.addValueChangeListener(event -> {
                if (event.getValue() != null) {
                    permisosSeleccionadosUI.put(wrapper.getAerolineaId(), event.getValue());
                } else {
                    permisosSeleccionadosUI.remove(wrapper.getAerolineaId());
                }
            });
            return estadoPermisoComboBox;
        })).setHeader("Estado del Permiso");
    }


    private Component createButtonsLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        save.addClickListener(event -> fireEvent(new SaveEvent(this, agenteActual, new ArrayList<>(permisosSeleccionadosUI.entrySet()))));
        cancel.addClickListener(event -> fireEvent(new CloseEvent(this)));

        save.setEnabled(false);

        return new HorizontalLayout(save, cancel);
    }

    public void setAgente(Agente agente) {
        this.agenteActual = agente;
        this.permisosSeleccionadosUI.clear();
        this.permisosOriginalesDelAgente = null;

        if (agente != null) {
            fireEvent(new AgenteSelectedEvent(this, agente));
            aerolineasGrid.setVisible(true);
        } else {
            aerolineasGrid.setItems(Collections.emptyList());
            aerolineasGrid.setVisible(false);
            save.setEnabled(false);
        }
    }

    public void setPermisosExistentes(List<PermisoAgenteAerolinea> permisosExistentes) {
        this.permisosOriginalesDelAgente = permisosExistentes;
        permisosSeleccionadosUI.clear();

        Map<Long, EstadoPermiso> permisosMap = new HashMap<>();
        if (permisosExistentes != null) {
            for (PermisoAgenteAerolinea paa : permisosExistentes) {
                if (paa.getAerolinea() != null) {
                    permisosMap.put(paa.getAerolinea().getIdAerolinea(), paa.getEstadoPermiso());
                    permisosSeleccionadosUI.put(paa.getAerolinea().getIdAerolinea(), paa.getEstadoPermiso());
                }
            }
        }

        List<AerolineaPermisoWrapper> wrappers = todasLasAerolineas.stream()
                .map(aerolinea -> new AerolineaPermisoWrapper(
                        aerolinea.getIdAerolinea(),
                        aerolinea.getNombre(),
                        permisosMap.get(aerolinea.getIdAerolinea())
                ))
                .collect(Collectors.toList());
        aerolineasGrid.setItems(wrappers);
        save.setEnabled(agenteActual != null);
    }

    // --- GETTER PÚBLICO AÑADIDO ---
    public ComboBox<Agente> getAgenteComboBox() {
        return agenteComboBox;
    }
    // --- FIN GETTER ---


    public static class AerolineaPermisoWrapper {
        private Long aerolineaId;
        private String nombreAerolinea;
        private EstadoPermiso estadoPermiso;

        public AerolineaPermisoWrapper(Long aerolineaId, String nombreAerolinea, EstadoPermiso estadoPermiso) {
            this.aerolineaId = aerolineaId;
            this.nombreAerolinea = nombreAerolinea;
            this.estadoPermiso = estadoPermiso;
        }
        public Long getAerolineaId() { return aerolineaId; }
        public String getNombreAerolinea() { return nombreAerolinea; }
        public EstadoPermiso getEstadoPermiso() { return estadoPermiso; }
        public void setEstadoPermiso(EstadoPermiso estadoPermiso) { this.estadoPermiso = estadoPermiso; }
    }

    public static abstract class PermisoAgenteAerolineaFormEvent extends ComponentEvent<PermisoAgenteAerolineaForm> {
        protected PermisoAgenteAerolineaFormEvent(PermisoAgenteAerolineaForm source) { super(source, false); }
    }

    public static class AgenteSelectedEvent extends PermisoAgenteAerolineaFormEvent {
        private final Agente agente;
        AgenteSelectedEvent(PermisoAgenteAerolineaForm source, Agente agente) {
            super(source);
            this.agente = agente;
        }
        public Agente getAgente() { return agente; }
    }

    public static class SaveEvent extends PermisoAgenteAerolineaFormEvent {
        private final Agente agente;
        private final List<Map.Entry<Long, EstadoPermiso>> permisosAActualizar;

        SaveEvent(PermisoAgenteAerolineaForm source, Agente agente, List<Map.Entry<Long, EstadoPermiso>> permisosAActualizar) {
            super(source);
            this.agente = agente;
            this.permisosAActualizar = permisosAActualizar;
        }
        public Agente getAgente() { return agente; }
        public List<Map.Entry<Long, EstadoPermiso>> getPermisosAActualizar() { return permisosAActualizar; }
    }

    public static class CloseEvent extends PermisoAgenteAerolineaFormEvent {
        CloseEvent(PermisoAgenteAerolineaForm source) { super(source); }
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }
}