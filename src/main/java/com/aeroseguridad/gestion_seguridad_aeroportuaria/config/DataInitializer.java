package com.aeroseguridad.gestion_seguridad_aeroportuaria.config;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Usuario;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Crea el usuario 'salva' si no existe.
        if (usuarioRepository.findByUsername("salva").isEmpty()) {
            System.out.println("--- Creando usuario 'salva' inicial ---");
            Usuario admin = new Usuario();
            admin.setUsername("salva");
            admin.setPassword(passwordEncoder.encode("2020"));
            admin.setRol("ROLE_ADMIN");
            usuarioRepository.save(admin);
            System.out.println("--- Usuario 'salva' / '2020' creado ---");
        }
    }
}