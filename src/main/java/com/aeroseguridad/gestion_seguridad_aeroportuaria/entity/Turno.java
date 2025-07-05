package com.aeroseguridad.gestion_seguridad_aeroportuaria.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue; // Para validación de fechas
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "turnos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "agente")
@EqualsAndHashCode(exclude = "agente")
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTurno;

    @NotNull(message = "El turno debe estar asociado a un agente.")
    @ManyToOne(fetch = FetchType.LAZY) // Un turno es de un agente
    @JoinColumn(name = "id_agente", nullable = false)
    private Agente agente;

    @NotNull(message = "La fecha/hora de inicio es obligatoria.")
    @Column(nullable = false)
    private LocalDateTime inicioTurno;

    @NotNull(message = "La fecha/hora de fin es obligatoria.")
    @Column(nullable = false)
    private LocalDateTime finTurno;

    @NotNull(message = "El tipo de turno es obligatorio.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoTurno tipoTurno;

    @NotNull(message = "El estado del turno es obligatorio.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoTurno estadoTurno = EstadoTurno.PROGRAMADO; // Por defecto

    // Validación a nivel de entidad para asegurar fin > inicio
    @AssertTrue(message = "La fecha/hora de fin debe ser posterior a la fecha/hora de inicio.")
    private boolean isFinDespuesDeInicio() {
        // Solo valida si ambas fechas están presentes
        return inicioTurno == null || finTurno == null || finTurno.isAfter(inicioTurno);
    }
}