package com.aeroseguridad.gestion_seguridad_aeroportuaria.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "posiciones_seguridad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "agentes")
@EqualsAndHashCode(exclude = "agentes")
public class PosicionSeguridad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPosicion;

    @NotBlank
    @Column(nullable = false, unique = true, length = 100)
    private String nombrePosicion;

    @Column(length = 255)
    private String descripcion;

    // --- NUEVO CAMPO ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Genero generoRequerido = Genero.CUALQUIERA; // Por defecto, cualquiera

    // --- NUEVO CAMPO (Opcional) ---
    @Column(nullable = false)
    private boolean requiereEntrenamientoEspecial = false; // Flag genérico

    @ManyToMany(mappedBy = "posicionesHabilitadas")
    private Set<Agente> agentes = new HashSet<>();

}