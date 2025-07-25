package com.aeroseguridad.gestion_seguridad_aeroportuaria.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;


@Entity
@Data
@Table(name = "assignments")
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vuelo_id", nullable = false)
    private Vuelo vuelo;

    @ManyToOne
    @JoinColumn(name = "posicion_seguridad_id", nullable = false) // <-- CORRECCIÓN CLAVE
    private PosicionSeguridad posicionSeguridad; // <-- CORRECCIÓN CLAVE

    @ManyToOne
    @JoinColumn(name = "agente_id") // Puede ser nulo si hay conflicto
    private Agente agente;
    
    @Column(nullable = false)
    private LocalDate fechaAsignacion;

    @Column(nullable = false)
    private String estado; // ej: "ASIGNADO", "CONFLICTO_NO_CUBIERTO"
}