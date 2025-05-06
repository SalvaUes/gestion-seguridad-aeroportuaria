package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span; // Asegúrate de importar Span
import com.vaadin.flow.component.icon.Icon; // Asegúrate de importar Icon
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinServletRequest;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

public class MainLayout extends AppLayout {

    public MainLayout() {
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("Gestión Seguridad App");
        logo.addClassNames("text-l", "m-m");

        Button logoutButton = new Button("Cerrar Sesión", VaadinIcon.SIGN_OUT.create(), e -> logout());
        logoutButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), logo, logoutButton);

        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setWidthFull();
        header.addClassNames("py-0", "px-m");

        addToNavbar(header);
    }

    private void createDrawer() {
        // ... (Links Inicio, Aerolíneas, Vuelos, Agentes, Turnos, Permisos como estaban) ...
        RouterLink inicioLink = new RouterLink(); inicioLink.add(VaadinIcon.HOME.create(), new Span(" Inicio")); inicioLink.setRoute(MainView.class); inicioLink.setHighlightCondition(HighlightConditions.locationPrefix(""));
        RouterLink aerolineasLink = new RouterLink(); aerolineasLink.add(VaadinIcon.AIRPLANE.create(), new Span(" Aerolíneas")); aerolineasLink.setRoute(AerolineaListView.class);
        RouterLink vuelosLink = new RouterLink(); vuelosLink.add(VaadinIcon.FLIGHT_TAKEOFF.create(), new Span(" Vuelos")); vuelosLink.setRoute(VueloListView.class);
        RouterLink agentesLink = new RouterLink(); agentesLink.add(VaadinIcon.USERS.create(), new Span(" Agentes")); agentesLink.setRoute(AgenteListView.class);
        RouterLink turnosLink = new RouterLink(); turnosLink.add(VaadinIcon.CLOCK.create(), new Span(" Turnos")); turnosLink.setRoute(TurnoListView.class);
        RouterLink permisosLink = new RouterLink(); permisosLink.add(VaadinIcon.CALENDAR_USER.create(), new Span(" Permisos")); permisosLink.setRoute(PermisoListView.class);

        // --- INICIO: AÑADIR LINK POSICIONES ---
        RouterLink posicionesLink = new RouterLink();
        Icon checkSquareIcon = VaadinIcon.CHECK_SQUARE_O.create(); // Icono para posiciones/tareas
        posicionesLink.add(checkSquareIcon, new Span(" Posiciones"));
        posicionesLink.setRoute(PosicionListView.class); // Apunta a la nueva vista
        // --- FIN: AÑADIR LINK POSICIONES ---


        addToDrawer(new VerticalLayout(
                inicioLink,
                aerolineasLink,
                vuelosLink,
                agentesLink,
                turnosLink,
                permisosLink,
                posicionesLink // Añade el nuevo link
        ));
    }

     private void logout() {
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(VaadinServletRequest.getCurrent().getHttpServletRequest(), null, null);
    }
}