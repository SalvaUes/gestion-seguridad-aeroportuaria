package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed; // ¡IMPORTANTE!

@Route("login") // 1. Mapea esta vista a la ruta /login
@PageTitle("Login | Gestión Seguridad")
@AnonymousAllowed // 2. ¡CRÍTICO! Permite el acceso a esta página sin estar logueado
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm login = new LoginForm();

    public LoginView() {
        addClassName("login-view");
        setSizeFull(); // Ocupa toda la pantalla
        // Centra el contenido
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // Acción del formulario apunta a 'login' (manejado por Spring Security)
        login.setAction("login");

        add(new H1("Gestión Seguridad Aeroportuaria"), login);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        // Muestra un mensaje de error si el login falla (parámetro ?error)
        if (beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters().containsKey("error")) {
            login.setError(true);
        }
    }
}