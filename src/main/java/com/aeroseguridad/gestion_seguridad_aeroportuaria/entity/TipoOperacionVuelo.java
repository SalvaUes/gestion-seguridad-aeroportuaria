package com.aeroseguridad.gestion_seguridad_aeroportuaria.entity;

public enum TipoOperacionVuelo {
    LLEGADA_SOLO,    // Solo llega, no sale pronto (ej. mantenimiento, pernocta inicial)
    SALIDA_SOLO,     // Solo sale (ej. fin de mantenimiento, inicio de día)
    TRANSITO,        // Llega y sale relativamente pronto (Turnaround)
    PERNOCTA_RON     // Remain Overnight - Llega un día y sale otro
}