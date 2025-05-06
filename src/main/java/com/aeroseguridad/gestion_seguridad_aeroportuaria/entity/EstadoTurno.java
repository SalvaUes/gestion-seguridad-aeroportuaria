package com.aeroseguridad.gestion_seguridad_aeroportuaria.entity;

public enum EstadoTurno {
    PROGRAMADO, // Planificado
    COMPLETADO, // Trabajado
    CANCELADO,  // Cancelado antes de iniciar
    AUSENTE,    // No se presentó (sin justificación/permiso previo)
    CON_PERMISO // Cubierto por un permiso (se cruza con tabla Permisos)
}