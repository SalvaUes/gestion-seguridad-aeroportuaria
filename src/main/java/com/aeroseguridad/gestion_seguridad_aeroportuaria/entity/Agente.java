package com.aeroseguridad.gestion_seguridad_aeroportuaria.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "agentes") // Mantenemos el nombre de la tabla por ahora para simplicidad
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
// Excluimos las nuevas relaciones recursivas para evitar StackOverflowError
@ToString(exclude = {"posicionesHabilitadas", "permisosAerolinea", "superior", "subordinados"})
@EqualsAndHashCode(exclude = {"posicionesHabilitadas", "permisosAerolinea", "superior", "subordinados"})
public class Agente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAgente;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Column(nullable = false, length = 50)
    private String nombre;

    @NotBlank(message = "El apellido no puede estar vacío")
    @Column(nullable = false, length = 50)
    private String apellido;

    @NotBlank(message = "El número de carnet no puede estar vacío")
    @Column(nullable = false, unique = true, length = 20)
    private String numeroCarnet;

    @NotNull(message = "Debe seleccionar un género")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Genero genero;

    @Column(length = 255)
    private String direccion;

    @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
    @Column
    private LocalDate fechaNacimiento;

    @Column(length = 20)
    private String telefono;

    @Email(message = "Debe introducir un formato de email válido")
    @Column(length = 100, unique = true)
    private String email;

    @Column(length = 255)
    private String rutaFotografia;

    @NotNull
    @Column(nullable = false)
    private Boolean activo = true;

    // --- CAMBIOS ESTRUCTURALES ---

    // 1. NUEVO CAMPO DE ROL
    @NotNull(message = "Debe especificar un rol")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    // 2. NUEVA RELACIÓN JERÁRQUICA (AUTO-REFERENCIADA)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_superior") // Un agente puede tener un superior (que es otro agente)
    private Agente superior;

    @OneToMany(mappedBy = "superior", fetch = FetchType.LAZY) // Un agente puede tener muchos subordinados
    private Set<Agente> subordinados = new HashSet<>();

    // --- RELACIONES EXISTENTES (SIN CAMBIOS) ---

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "agente_habilidades", joinColumns = @JoinColumn(name = "id_agente"), inverseJoinColumns = @JoinColumn(name = "id_posicion"))
    private Set<PosicionSeguridad> posicionesHabilitadas = new HashSet<>();

    @OneToMany(mappedBy = "agente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<PermisoAgenteAerolinea> permisosAerolinea = new HashSet<>();

    public String getNombreCompleto() {
        return (nombre != null ? nombre : "") + " " + (apellido != null ? apellido : "");
    }
}