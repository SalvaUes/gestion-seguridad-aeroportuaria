package com.aeroseguridad.gestion_seguridad_aeroportuaria.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "plantillas_turno")
@Getter
@Setter
public class PlantillaTurno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombrePlantilla;

    private LocalDate fechaInicioVigencia;

    private LocalDate fechaFinVigencia; // Nulo significa "indefinido"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agente_id", nullable = false)
    private Agente agente;

    @OneToMany(
        mappedBy = "plantillaTurno",
        cascade = CascadeType.ALL, // Si se guarda/borra una plantilla, sus reglas también.
        orphanRemoval = true,      // Si quitas una regla de la lista, se borra de la DB.
        fetch = FetchType.EAGER    // Eager para que las reglas siempre vengan con la plantilla.
    )
    private List<ReglaDeTurno> reglas = new ArrayList<>();

    // Método de conveniencia para añadir reglas de forma segura
    public void addRegla(ReglaDeTurno regla) {
        reglas.add(regla);
        regla.setPlantillaTurno(this);
    }
}