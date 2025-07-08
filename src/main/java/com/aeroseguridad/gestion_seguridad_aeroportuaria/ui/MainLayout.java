// RUTA: src/main/java/com/aeroseguridad/gestion_seguridad_aeroportuaria/ui/MainLayout.java
package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
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

    // --- MÉTODO REFACTORIZADO ---
    private void createHeader() {
        // El header ahora solo contiene el botón para el menú. Es limpio y minimalista.
        DrawerToggle toggle = new DrawerToggle();

        HorizontalLayout header = new HorizontalLayout(toggle);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.addClassNames("py-0", "px-m");
        
        // Estilo para una separación visual limpia
        header.getStyle().set("border-bottom", "1px solid var(--lumo-contrast-10pct)");

        addToNavbar(header);
    }
    
    // --- MÉTODO REFACTORIZADO ---
    private void createDrawer() {
        // 1. Título de la App dentro del Drawer
        H2 appTitle = new H2("Seguridad App");
        appTitle.getStyle().set("font-size", "var(--lumo-font-size-l)").set("margin", "var(--lumo-space-s)");
        
        // 2. Contenedor de enlaces de navegación
        VerticalLayout navLinks = new VerticalLayout();
        navLinks.setPadding(false);
        navLinks.setSpacing(false);
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

        // 3. Botón de Cerrar Sesión
        Button logoutButton = new Button("Cerrar Sesión", VaadinIcon.SIGN_OUT.create(), e -> logout());
        logoutButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        logoutButton.setWidthFull();

        // 4. Layout principal del Drawer
        VerticalLayout drawerLayout = new VerticalLayout(appTitle, navLinks, logoutButton);
        drawerLayout.setSizeFull();
        drawerLayout.setPadding(false);
        drawerLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        
        // El truco para anclar el botón de logout abajo:
        drawerLayout.setFlexGrow(1, navLinks);
        
        addToDrawer(drawerLayout);
    }

    private RouterLink createMenuLink(Class<? extends Component> viewClass, String caption, VaadinIcon iconName) {
        RouterLink link = new RouterLink();
        link.getStyle().set("padding", "var(--lumo-space-s)");
        link.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        
        Icon icon = iconName.create();
        Span span = new Span(caption);

        HorizontalLayout itemLayout = new HorizontalLayout(icon, span);
        itemLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        itemLayout.getStyle().set("gap", "var(--lumo-space-m)");

        link.add(itemLayout);
        link.setRoute(viewClass);
        link.setHighlightCondition(HighlightConditions.locationPrefix());
        
        // Efecto hover para mejor UX
        link.addFocusListener(e -> link.getStyle().set("background-color", "var(--lumo-contrast-5pct)"));
        link.addBlurListener(e -> link.getStyle().set("background-color", "transparent"));

        return link;
    }

    private void logout() {
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(VaadinServletRequest.getCurrent().getHttpServletRequest(), null, null);
    }
}