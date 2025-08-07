package com.aeroseguridad.gestion_seguridad_aeroportuaria.entity;

/**
 * Enum que define los posibles estados de una Asignación de Vuelo.
 * Fundamental para la implementación del Principio de Conciencia de Asignación (PCA).
 */
public enum EstadoAsignacion {
    /**
     * La asignación es correcta y está confirmada.
     */
    ASIGNADO,

    /**
     * Conflicto: El turno del agente fue modificado o eliminado, dejando esta asignación sin cobertura horaria.
     */
    CONFLICTO_AGENTE_NO_DISPONIBLE,

    /**
     * Conflicto: Se aprobó un permiso o incapacidad para el agente durante el horario de esta asignación.
     */
    CONFLICTO_PERMISO_APROBADO,

    /**
     * Conflicto: El agente ya no posee una habilidad o certificación requerida para esta posición.
     */
    CONFLICTO_SIN_CUALIFICACION,

    /**
     * Conflicto: El agente fue desactivado o se le revocó el permiso para la aerolínea de este vuelo.
     */
    CONFLICTO_AGENTE_INACTIVO_O_SIN_PERMISO,

    /**
     * Conflicto general: No se pudo encontrar un agente que cumpliera todos los requisitos durante la planificación.
     */
    CONFLICTO_NO_CUBIERTO
}