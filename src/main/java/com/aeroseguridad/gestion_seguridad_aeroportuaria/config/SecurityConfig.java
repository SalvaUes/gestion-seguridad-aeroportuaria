package com.aeroseguridad.gestion_seguridad_aeroportuaria.config;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.ui.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends VaadinWebSecurity {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Permite acceso a recursos estáticos como imágenes si los tienes
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(new AntPathRequestMatcher("/images/**")).permitAll()
                // Puedes añadir más rutas públicas aquí si es necesario
        );

        // Llama a la configuración base de VaadinWebSecurity (IMPORTANTE)
        super.configure(http);

        // Establece la vista de Login personalizada
        setLoginView(http, LoginView.class);

        // Configura el Form Login para redirigir siempre a "/" (MainView) tras éxito
        // El 'true' al final fuerza esta URL ignorando la que se intentó acceder antes
        http.formLogin(customizer -> customizer.defaultSuccessUrl("/", true));

        // Configura el logout (opcional, VaadinWebSecurity puede manejarlo)
        // http.logout(customizer -> customizer.logoutSuccessUrl("/login?logout"));
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user = User.builder()
                .username("salva")
                .password(passwordEncoder.encode("2020")) // Contraseña codificada
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(user); // Gestiona el usuario en memoria
    }
}