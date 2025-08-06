package com.aeroseguridad.gestion_seguridad_aeroportuaria.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "plantillas_vuelo")
@Getter
@Setter
@NoArgsConstructor
public class PlantillaVuelo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false, length = 50)
    private String nombrePlantilla;

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
    private LocalTime horaSalida;

    @NotNull
    @Column(nullable = false)
    private LocalTime horaLlegada;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoOperacionVuelo tipoOperacion;
    
    // --- CAMBIO: Nuevo campo añadido ---
    @Column
    private LocalTime horaFinOperacionSeguridad;

    @NotNull
    @ElementCollection(targetClass = DayOfWeek.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "plantilla_dias_operacion", joinColumns = @JoinColumn(name = "plantilla_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false)
    private Set<DayOfWeek> diasOperacion = new HashSet<>();

    @NotNull
    @Column(nullable = false)
    private LocalDate fechaInicioContrato;

    @NotNull
    @Column(nullable = false)
    private LocalDate fechaFinContrato;
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "plantilla_vuelo_id")
    private Set<NecesidadVuelo> necesidadesEstandar = new HashSet<>();

    public void addNecesidadEstandar(NecesidadVuelo necesidad) {
        necesidadesEstandar.add(necesidad);
    }

    public void removeNecesidadEstandar(NecesidadVuelo necesidad) {
        necesidadesEstandar.remove(necesidad);
    }
}