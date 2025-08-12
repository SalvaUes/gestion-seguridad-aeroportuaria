package com.aeroseguridad.gestion_seguridad_aeroportuaria.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import jakarta.validation.constraints.AssertTrue;

import java.time.LocalDateTime;

@Entity
@Table(name = "necesidades_vuelo", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"id_vuelo", "id_posicion"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"vuelo", "posicion"})
@EqualsAndHashCode(exclude = {"vuelo", "posicion"})
public class NecesidadVuelo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idNecesidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vuelo", nullable = true)
    private Vuelo vuelo;

    @NotNull(message = "Se debe especificar una posición de seguridad.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_posicion", nullable = false)
    private PosicionSeguridad posicion;

    @Min(value = 1, message = "La cantidad de agentes debe ser al menos 1.")
    @Column(nullable = false)
    private int cantidadAgentes;

    @NotNull(message = "La hora de inicio de cobertura es obligatoria.")
    @Column(nullable = false)
    private LocalDateTime inicioCobertura;

    @NotNull(message = "La hora de fin de cobertura es obligatoria.")
    @Column(nullable = false)
    private LocalDateTime finCobertura;

    // --- NUEVOS CAMPOS PARA ASIGNACIONES SECUENCIALES ---

    @Min(value = 1, message = "La secuencia debe ser al menos 1.")
    @Column(nullable = false)
    private int secuencia = 1;

    @Column(nullable = false)
    private boolean permiteAgenteAsignado = false;

    // --- FIN DE NUEVOS CAMPOS ---

    @AssertTrue(message = "La hora de fin de cobertura debe ser posterior a la hora de inicio.")
    private boolean isFinDespuesDeInicio() {
        return inicioCobertura == null || finCobertura == null || finCobertura.isAfter(inicioCobertura);
    }
}