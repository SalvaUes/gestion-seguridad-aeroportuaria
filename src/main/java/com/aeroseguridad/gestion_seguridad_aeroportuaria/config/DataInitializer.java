package com.aeroseguridad.gestion_seguridad_aeroportuaria.config;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Genero;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Rol;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Usuario;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.AgenteRepository;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AgenteRepository agenteRepository;
    
    // --- 1. CONSTRUCTOR SIMPLIFICADO ---
    // Se elimina SupervisorRepository
    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, AgenteRepository agenteRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.agenteRepository = agenteRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // --- Lógica de creación de usuarios (INTACTA) ---
        if (usuarioRepository.findByUsername("salva").isEmpty()) {
            System.out.println("--- Creando usuario 'salva' inicial ---");
            Usuario admin = new Usuario();
            admin.setUsername("salva");
            admin.setPassword(passwordEncoder.encode("2020"));
            admin.setRol("ROLE_ADMIN");
            usuarioRepository.save(admin);
            System.out.println("--- Usuario 'salva' / '2020' creado ---");
        }
        
        // --- 2. NUEVA LÓGICA DE CREACIÓN DE PERSONAL UNIFICADO ---
        if (agenteRepository.count() == 0) {
            System.out.println("--- Creando Personal de prueba (Coordinador, Supervisores, Agentes) ---");

            // 1. Crear Coordinador
            Agente coordinador = new Agente();
            coordinador.setNombre("Phillip");
            coordinador.setApellido("Broyles");
            coordinador.setNumeroCarnet("COORD-001");
            coordinador.setRol(Rol.COORDINADOR);
            coordinador.setGenero(Genero.MASCULINO);
            coordinador.setActivo(true);
            // El coordinador no tiene superior
            coordinador.setSuperior(null); 

            // 2. Crear Supervisores
            Agente supervisor1 = new Agente();
            supervisor1.setNombre("Olivia");
            supervisor1.setApellido("Dunham");
            supervisor1.setNumeroCarnet("SUP-001");
            supervisor1.setRol(Rol.SUPERVISOR);
            supervisor1.setGenero(Genero.FEMENINO);
            supervisor1.setActivo(true);
            supervisor1.setSuperior(coordinador); // Su superior es el coordinador

            Agente supervisor2 = new Agente();
            supervisor2.setNombre("Peter");
            supervisor2.setApellido("Bishop");
            supervisor2.setNumeroCarnet("SUP-002");
            supervisor2.setRol(Rol.SUPERVISOR);
            supervisor2.setGenero(Genero.MASCULINO);
            supervisor2.setActivo(true);
            supervisor2.setSuperior(coordinador); // Su superior es el coordinador

            // 3. Crear Agentes
            Agente agente1 = new Agente();
            agente1.setNombre("Walter");
            agente1.setApellido("Bishop");
            agente1.setNumeroCarnet("AGT-001");
            agente1.setRol(Rol.AGENTE);
            agente1.setGenero(Genero.MASCULINO);
            agente1.setActivo(true);
            agente1.setSuperior(supervisor1); // Su superior es el supervisor 1

            Agente agente2 = new Agente();
            agente2.setNombre("Astrid");
            agente2.setApellido("Farnsworth");
            agente2.setNumeroCarnet("AGT-002");
            agente2.setRol(Rol.AGENTE);
            agente2.setGenero(Genero.FEMENINO);
            agente2.setActivo(true);
            agente2.setSuperior(supervisor1); // Su superior es el supervisor 1

            Agente agente3 = new Agente();
            agente3.setNombre("Charlie");
            agente3.setApellido("Francis");
            agente3.setNumeroCarnet("AGT-003");
            agente3.setRol(Rol.AGENTE);
            agente3.setGenero(Genero.MASCULINO);
            agente3.setActivo(true);
            agente3.setSuperior(supervisor2); // Su superior es el supervisor 2
            
            // 4. Guardar todo el personal en la base de datos
            List<Agente> todoElPersonal = Arrays.asList(coordinador, supervisor1, supervisor2, agente1, agente2, agente3);
            agenteRepository.saveAll(todoElPersonal);
            
            System.out.println("--- " + todoElPersonal.size() + " registros de personal creados y jerarquía establecida ---");
        }
    }
}