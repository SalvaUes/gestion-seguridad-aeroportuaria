package com.aeroseguridad.gestion_seguridad_aeroportuaria.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode; // IMPORTACIÓN NUEVA
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "aerolineas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
// --- ANOTACIÓN AÑADIDA: Define cómo se comparan los objetos Aerolinea ---
// Le decimos a Lombok que solo use los campos que marquemos explícitamente.
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Aerolinea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAerolinea;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    // --- ANOTACIÓN AÑADIDA: Marcamos 'codigoIata' como el campo clave para las comparaciones ---
    // Esto es como decir: "Dos objetos Aerolinea son iguales si su código IATA es el mismo".
    @EqualsAndHashCode.Include
    @Column(name = "codigo_iata", length = 3, unique = true)
    private String codigoIata;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}