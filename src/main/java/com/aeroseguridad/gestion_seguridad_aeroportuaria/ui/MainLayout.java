// RUTA: com/aeroseguridad/gestion_seguridad_aeroportuaria/ui/MainLayout.java
package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.router.PageTitle; // <-- IMPORTACIÓN NECESARIA
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinServletRequest;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

public class MainLayout extends AppLayout {

    private H1 pageTitle;

    public MainLayout() {
        getElement().getClassList().add("view-container");

        createHeader();
        createDrawer();

        addAttachListener(event -> {
            UI ui = event.getUI();
            ui.addAfterNavigationListener(this::updatePageTitle);
        });
    }
    
    /**
     * MÉTODO PARA ACTUALIZAR EL TÍTULO (CORREGIDO)
     */
    private void updatePageTitle(AfterNavigationEvent event) {
        // Obtenemos la clase de la vista activa. get(0) es la vista principal.
        // Se usa .get(0) porque es la vista principal en la cadena de navegación.
        Class<?> viewClass = event.getActiveChain().get(0).getClass();
        
        // Se busca la anotación @PageTitle en la clase de la vista.
        PageTitle titleAnnotation = viewClass.getAnnotation(PageTitle.class);

        if (titleAnnotation != null && pageTitle != null) {
            // Si la anotación existe, usamos su valor para el título.
            pageTitle.setText(titleAnnotation.value());
        } else if (pageTitle != null) {
            // Si no hay anotación, dejamos el título en blanco o con un valor por defecto.
            pageTitle.setText("AeroPrime");
        }
    }

    private void createHeader() {
        DrawerToggle toggle = new DrawerToggle();

        Div headerContainer = new Div(toggle);
        headerContainer.addClassName("app-header");

        Div headerContent = new Div();
        headerContent.addClassName("app-header-content");

        pageTitle = new H1();
        pageTitle.addClassName("page-title");
        
        Div headerActions = new Div();
        headerActions.addClassName("header-actions");
        
        headerContent.add(pageTitle, headerActions);
        headerContainer.add(headerContent);

        addToNavbar(headerContainer);
    }

    private void createDrawer() {
        H2 appTitle = new H2("Seguridad App");
        appTitle.addClassName("app-title");

        VerticalLayout navLinks = new VerticalLayout();
        navLinks.setPadding(false);
        navLinks.setSpacing(false);
        navLinks.addClassName("nav-links");

        navLinks.add(
            createMenuLink(MainView.class, "Inicio", VaadinIcon.HOME),
            createMenuLink(AgenteListView.class, "Agentes", VaadinIcon.USERS),
            createMenuLink(TurnoListView.class, "Turnos", VaadinIcon.CLOCK),
            createMenuLink(PosicionListView.class, "Posiciones", VaadinIcon.CHECK_SQUARE_O),
            createMenuLink(SupervisoresView.class, "Supervisores", VaadinIcon.USER_CARD),
            createMenuLink(AerolineaListView.class, "Aerolíneas", VaadinIcon.AIRPLANE),
            createMenuLink(VueloListView.class, "Vuelos", VaadinIcon.FLIGHT_TAKEOFF),
            createMenuLink(PermisoListView.class, "Permisos", VaadinIcon.CALENDAR_USER),
            createMenuLink(PermisoAgenteAerolineaListView.class, "Permisos Aerolíneas", VaadinIcon.CONNECT)
        );

        Button logoutButton = new Button("Cerrar Sesión", VaadinIcon.SIGN_OUT.create(), e -> logout());
        logoutButton.addClassName("logout-button");
        logoutButton.setWidthFull();

        VerticalLayout drawerLayout = new VerticalLayout(appTitle, navLinks, logoutButton);
        drawerLayout.setSizeFull();
        drawerLayout.setPadding(false);
        drawerLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        
        addToDrawer(drawerLayout);
    }

    private RouterLink createMenuLink(Class<? extends Component> viewClass, String caption, VaadinIcon iconName) {
        RouterLink link = new RouterLink();
        link.addClassName("vaadin-drawer-item");

        Icon icon = iconName.create();
        Span span = new Span(caption);
        span.addClassName("menu-item-text");

        HorizontalLayout itemLayout = new HorizontalLayout(icon, span);
        itemLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        itemLayout.getStyle().set("gap", "var(--lumo-space-m)");

        link.add(itemLayout);
        link.setRoute(viewClass);
        link.setHighlightCondition(HighlightConditions.locationPrefix());

        return link;
    }

    private void logout() {
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(VaadinServletRequest.getCurrent().getHttpServletRequest(), null, null);
    }
}