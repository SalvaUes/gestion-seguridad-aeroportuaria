package com.aeroseguridad.gestion_seguridad_aeroportuaria.entity;

public enum EstadoPermiso {
    PERMITIDO, // El agente puede trabajar para la aerolínea
    RECHAZADO  // La aerolínea rechaza específicamente a este agente
    // Podríamos añadir más estados si es necesario
}