package com.aeroseguridad.gestion_seguridad_aeroportuaria.entity;

public enum TipoConflicto {
    FUERA_DE_TURNO("Fuera de Turno"),
    SIN_PERMISO_AEROLINEA("Sin Permiso de Aerolínea"),
    SIN_HABILIDAD_REQUERIDA("Sin Habilidad Requerida"),
    NO_CUMPLE_GENERO("No Cumple Requisito de Género"),
    CON_PERMISO_APROBADO("Con Permiso/Ausencia"),
    SIN_PERSONAL_DISPONIBLE("Sin Personal Disponible");

    private final String descripcion;

    TipoConflicto(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}