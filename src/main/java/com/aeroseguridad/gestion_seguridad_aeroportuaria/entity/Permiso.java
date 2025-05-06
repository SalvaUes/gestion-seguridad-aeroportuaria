package com.aeroseguridad.gestion_seguridad_aeroportuaria.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import jakarta.validation.constraints.AssertTrue;

import java.time.LocalDateTime;

@Entity
@Table(name = "permisos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "agente")
@EqualsAndHashCode(exclude = "agente")
public class Permiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPermiso;

    @NotNull(message = "El permiso debe estar asociado a un agente.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_agente", nullable = false)
    private Agente agente;

    @Column(nullable = false)
    private LocalDateTime fechaSolicitud = LocalDateTime.now(); // Se establece al crear

    @NotNull(message = "La fecha/hora de inicio es obligatoria.")
    // @FutureOrPresent(message="La fecha de inicio no puede ser pasada") // Opcional: Validación extra
    @Column(nullable = false)
    private LocalDateTime fechaInicio;

    @NotNull(message = "La fecha/hora de fin es obligatoria.")
    @Column(nullable = false)
    private LocalDateTime fechaFin;

    @NotNull(message = "El tipo de permiso es obligatorio.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoPermiso tipoPermiso;

    @NotBlank(message = "El motivo no puede estar vacío.")
    @Column(nullable = false, columnDefinition = "TEXT") // Columna de texto largo
    private String motivo;

    @Column(length = 255) // Ruta a posible documento adjunto
    private String rutaDocumento;

    @NotNull(message = "El estado es obligatorio.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitudPermiso estadoSolicitud = EstadoSolicitudPermiso.SOLICITADO; // Por defecto

    // Validación: fin > inicio
    @AssertTrue(message = "La fecha/hora de fin debe ser posterior a la fecha/hora de inicio.")
    private boolean isFinDespuesDeInicio() {
        return fechaInicio == null || fechaFin == null || fechaFin.isAfter(fechaInicio);
    }
}