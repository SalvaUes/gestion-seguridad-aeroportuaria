package com.aeroseguridad.gestion_seguridad_aeroportuaria.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull; // Importa NotNull
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "vuelos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "aerolinea") // Excluir relaciones perezosas
@EqualsAndHashCode(exclude = "aerolinea")
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
    private LocalDateTime fechaHoraSalida; // Hora programada de salida (ETA si es solo llegada?) Podríamos necesitar ajustar

    @NotNull
    @Column(nullable = false)
    private LocalDateTime fechaHoraLlegada; // Hora programada de llegada (ETD si es solo salida?)

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoVuelo estado;

    // --- NUEVOS CAMPOS ---
    @NotNull(message = "Debe especificar el tipo de operación.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoOperacionVuelo tipoOperacion; // LLEGADA, SALIDA, TRANSITO, RON

    @Column // Puede ser nulo inicialmente, se actualiza al finalizar operación
    private LocalDateTime finOperacionSeguridad; // Hora real/estimada fin cobertura seguridad completa
    // --- FIN NUEVOS CAMPOS ---

    // Podríamos añadir más campos como: Matrícula Aeronave, Puerta Asignada, etc. más adelante.
}