package com.aeroseguridad.gestion_seguridad_aeroportuaria.config;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.ui.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends VaadinWebSecurity {

    // Define el encriptador de contraseñas como un Bean para que Spring lo gestione.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Configura la cadena de filtros de seguridad de HTTP.
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // La configuración base de Vaadin protege todas las vistas (`@Route`).
        super.configure(http);

        // Conecta nuestra vista de login personalizada con el motor de Spring Security.
        // Esta es la forma recomendada por Vaadin para asegurar la integración correcta.
        setLoginView(http, LoginView.class);
    }
}