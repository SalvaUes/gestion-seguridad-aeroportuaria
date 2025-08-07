package com.aeroseguridad.gestion_seguridad_aeroportuaria.entity;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Data
@Table(name = "assignments")
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vuelo_id", nullable = false)
    private Vuelo vuelo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posicion_seguridad_id", nullable = false)
    private PosicionSeguridad posicionSeguridad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agente_id") // Puede ser nulo si hay conflicto inicial
    private Agente agente;
    
    @Column(nullable = false)
    private LocalDate fechaAsignacion;

    // --- CAMBIO CLAVE: De String a Enum para el estado ---
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoAsignacion estado;

    // --- NUEVO CAMPO: Para guardar detalles del conflicto ---
    @Column(name = "detalle_conflicto", length = 500)
    private String detalleConflicto;
}