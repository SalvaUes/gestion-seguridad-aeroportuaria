package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.ui.PermisoAgenteAerolineaListView;
// --- 1. IMPORTACIÓN DE LA NUEVA VISTA ---
import com.aeroseguridad.gestion_seguridad_aeroportuaria.ui.SupervisoresView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
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

    private RouterLink createMenuLink(Class<? extends Component> viewClass, String caption, VaadinIcon iconName) {
        RouterLink link = new RouterLink();
        Icon icon = iconName.create();
        Span span = new Span(caption);

        HorizontalLayout itemLayout = new HorizontalLayout(icon, span);
        itemLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        link.add(itemLayout);
        link.setRoute(viewClass);
        link.setHighlightCondition(HighlightConditions.locationPrefix());
        return link;
    }

    private void createDrawer() {
        RouterLink inicioLink = createMenuLink(MainView.class, "Inicio", VaadinIcon.HOME);
        RouterLink aerolineasLink = createMenuLink(AerolineaListView.class, "Aerolíneas", VaadinIcon.AIRPLANE);
        RouterLink vuelosLink = createMenuLink(VueloListView.class, "Vuelos", VaadinIcon.FLIGHT_TAKEOFF);
        RouterLink agentesLink = createMenuLink(AgenteListView.class, "Agentes", VaadinIcon.USERS);
        
        // --- 2. CREACIÓN DEL NUEVO ENLACE A SUPERVISORES ---
        RouterLink supervisoresLink = createMenuLink(SupervisoresView.class, "Supervisores", VaadinIcon.USER_CARD);

        RouterLink turnosLink = createMenuLink(TurnoListView.class, "Turnos", VaadinIcon.CLOCK);
        RouterLink permisosLink = createMenuLink(PermisoListView.class, "Permisos", VaadinIcon.CALENDAR_USER);
        RouterLink posicionesLink = createMenuLink(PosicionListView.class, "Posiciones", VaadinIcon.CHECK_SQUARE_O);
        RouterLink permisoAgenteAerolineaLink = createMenuLink(
                PermisoAgenteAerolineaListView.class,
                "Permisos Aerolíneas",
                VaadinIcon.CONNECT
        );

        // --- 3. ADICIÓN DEL ENLACE AL MENÚ ---
        addToDrawer(new VerticalLayout(
                inicioLink,
                aerolineasLink,
                vuelosLink,
                agentesLink,
                supervisoresLink, // <-- Ubicado lógicamente después de Agentes
                turnosLink,
                permisosLink,
                posicionesLink,
                permisoAgenteAerolineaLink
        ));
    }

     private void logout() {
         SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
         logoutHandler.logout(VaadinServletRequest.getCurrent().getHttpServletRequest(), null, null);
     }
}