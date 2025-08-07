package com.aeroseguridad.gestion_seguridad_aeroportuaria.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "reglas_turno")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReglaDeTurno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- CAMBIO CLAVE: De un solo día a un conjunto de días ---
    @ElementCollection(targetClass = DayOfWeek.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "regla_dias", joinColumns = @JoinColumn(name = "regla_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false)
    private Set<DayOfWeek> diasDeLaSemana = new HashSet<>();

    @Column(nullable = false)
    private LocalTime horaInicio;

    @Column(nullable = false)
    private LocalTime horaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoTurno tipoTurno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plantilla_turno_id", nullable = false)
    private PlantillaTurno plantillaTurno;

    public ReglaDeTurno(ReglaDeTurno source) {
        this.diasDeLaSemana = new HashSet<>(source.getDiasDeLaSemana()); // Copiamos el conjunto
        this.horaInicio = source.getHoraInicio();
        this.horaFin = source.getHoraFin();
        this.tipoTurno = source.getTipoTurno();
    }
}