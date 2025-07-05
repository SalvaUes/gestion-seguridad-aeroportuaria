package com.aeroseguridad.gestion_seguridad_aeroportuaria.entity;

public enum Rol {
    AGENTE("Agente de Seguridad"),
    SUPERVISOR("Supervisor de Equipo"),
    COORDINADOR("Coordinador de Operaciones");

    private final String descripcion;

    Rol(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}