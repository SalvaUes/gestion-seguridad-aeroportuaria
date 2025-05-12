package com.aeroseguridad.gestion_seguridad_aeroportuaria.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull; // Importar para el enum y boolean
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

    @NotBlank // Para Strings
    @Column(nullable = false, unique = true, length = 100)
    private String nombrePosicion;

    @Column(length = 255)
    private String descripcion;

    @NotNull // Los enums se validan con NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Genero generoRequerido = Genero.CUALQUIERA;

    @Column(nullable = false) // boolean primitivo es not null por defecto
    private boolean requiereEntrenamientoEspecial = false;

    @NotNull // Para el wrapper Boolean
    @Column(nullable = false)
    private Boolean activo = true; // Campo para Soft Delete

    @ManyToMany(mappedBy = "posicionesHabilitadas")
    private Set<Agente> agentes = new HashSet<>();
}