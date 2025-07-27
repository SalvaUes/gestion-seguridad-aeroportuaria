package com.aeroseguridad.gestion_seguridad_aeroportuaria.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.HashSet;
import java.util.Set;
import java.time.LocalDateTime;

@Entity
@Table(name = "vuelos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"aerolinea", "necesidades", "assignments"}) // Excluir todas las colecciones
@EqualsAndHashCode(exclude = {"aerolinea", "necesidades", "assignments"}) // Excluir todas las colecciones
public class Vuelo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idVuelo;

    @NotNull
    @Column(nullable = false, length = 10)
    private String numeroVuelo;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_aerolinea", nullable = false)
    private Aerolinea aerolinea;

    @NotNull
    @Column(nullable = false, length = 100)
    private String origen;

    @NotNull
    @Column(nullable = false, length = 100)
    private String destino;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime fechaHoraSalida;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime fechaHoraLlegada;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoVuelo estado;

    @NotNull(message = "Debe especificar el tipo de operación.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoOperacionVuelo tipoOperacion;

    @Column
    private LocalDateTime finOperacionSeguridad;
    
    // --- RELACIONES ---

    @OneToMany(mappedBy = "vuelo", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<NecesidadVuelo> necesidades = new HashSet<>();

    /**
     * CAMBIO CLAVE: Relación bidireccional con Assignment.
     * - cascade = CascadeType.ALL: Cualquier cambio en Vuelo (guardar, borrar) se propaga a sus Assignments.
     * - orphanRemoval = true: Si un Assignment se elimina de esta colección, se borrará de la base de datos.
     * Esto centraliza la gestión de la persistencia en la entidad Vuelo.
     */
    @OneToMany(mappedBy = "vuelo", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Assignment> assignments = new HashSet<>();

    // --- MÉTODOS DE AYUDA PARA SINCRONIZAR LA RELACIÓN ---
    
    public void addAssignment(Assignment assignment) {
        assignments.add(assignment);
        assignment.setVuelo(this);
    }

    public void removeAssignment(Assignment assignment) {
        assignments.remove(assignment);
        assignment.setVuelo(null);
    }
}