package com.aeroseguridad.gestion_seguridad_aeroportuaria.entity;

import jakarta.persistence.*;
// --- Imports para Validación ---
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank; // Para Strings no vacíos
import jakarta.validation.constraints.NotNull; // Para Objetos no nulos
import jakarta.validation.constraints.Past;   // Para Fechas en el pasado
import jakarta.validation.constraints.Size;   // Para tamaño de Strings
// --- Fin Imports Validación ---
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "agentes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"posicionesHabilitadas", "permisosAerolinea"})
@EqualsAndHashCode(exclude = {"posicionesHabilitadas", "permisosAerolinea"})
public class Agente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAgente;

    @NotBlank(message = "El nombre no puede estar vacío") // Validación
    @Size(max = 50, message = "El nombre no puede exceder los 50 caracteres") // Validación
    @Column(nullable = false, length = 50)
    private String nombre;

    @NotBlank(message = "El apellido no puede estar vacío")
    @Size(max = 50, message = "El apellido no puede exceder los 50 caracteres")
    @Column(nullable = false, length = 50)
    private String apellido;

    @NotBlank(message = "El número de carnet no puede estar vacío")
    @Size(max = 20, message = "El carnet no puede exceder los 20 caracteres")
    @Column(nullable = false, unique = true, length = 20)
    private String numeroCarnet;

    @NotNull(message = "Debe seleccionar un género") // Validación para el ComboBox
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Genero genero;

    @Size(max = 255, message = "La dirección no puede exceder los 255 caracteres")
    @Column(length = 255)
    private String direccion;

    @Past(message = "La fecha de nacimiento debe ser una fecha pasada") // Validación
    @Column // Nullable por defecto está bien aquí
    private LocalDate fechaNacimiento;

    @Size(max = 20, message = "El teléfono no puede exceder los 20 caracteres")
    @Column(length = 20)
    private String telefono;

    @Email(message = "Debe introducir un formato de email válido") // Validación de formato
    @Size(max = 100, message = "El email no puede exceder los 100 caracteres")
    @Column(length = 100)
    private String email;

    @Column(length = 255)
    private String rutaFotografia;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false)
    private boolean esBilingue = false;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "agente_habilidades",
            joinColumns = @JoinColumn(name = "id_agente"),
            inverseJoinColumns = @JoinColumn(name = "id_posicion")
    )
    private Set<PosicionSeguridad> posicionesHabilitadas = new HashSet<>();

    @OneToMany(mappedBy = "agente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<PermisoAgenteAerolinea> permisosAerolinea = new HashSet<>();

}