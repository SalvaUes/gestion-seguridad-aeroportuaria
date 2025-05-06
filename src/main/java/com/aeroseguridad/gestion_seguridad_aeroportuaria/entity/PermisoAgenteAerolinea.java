package com.aeroseguridad.gestion_seguridad_aeroportuaria.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "permisos_agente_aerolinea", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"id_agente", "id_aerolinea"}) // Un agente solo tiene un estado por aerolínea
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(exclude = {"agente", "aerolinea"}) // Evitar recursión
public class PermisoAgenteAerolinea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPermisoAa;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_agente", nullable = false)
    private Agente agente;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_aerolinea", nullable = false)
    private Aerolinea aerolinea;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPermiso estadoPermiso; // PERMITIDO o RECHAZADO

}