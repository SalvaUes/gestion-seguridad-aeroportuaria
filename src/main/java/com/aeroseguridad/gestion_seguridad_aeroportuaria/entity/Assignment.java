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
    @JoinColumn(name = "agente_id")
    private Agente agente;
    
    @Column(nullable = false)
    private LocalDate fechaAsignacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoAsignacion estado;

    // --- CAMPO REFACTORIZADO: Ahora guardamos el TIPO de conflicto ---
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_conflicto") // Puede ser nulo si no hay conflicto
    private TipoConflicto tipoConflicto;

    @Column(name = "detalle_conflicto", length = 500)
    private String detalleConflicto;
}