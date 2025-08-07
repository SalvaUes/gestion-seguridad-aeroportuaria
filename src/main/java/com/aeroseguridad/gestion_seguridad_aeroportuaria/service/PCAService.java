package com.aeroseguridad.gestion_seguridad_aeroportuaria.service;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Permiso;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.PosicionSeguridad;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Turno;

public interface PCAService {

    void verificarConflictoPorCambioDeTurno(Turno turnoOriginal, Turno turnoModificado);

    void verificarConflictoPorPermiso(Permiso permiso);
    
    // --- MÉTODOS COMPLETADOS ---
    void verificarConflictoPorCambioDeHabilidad(Agente agente, PosicionSeguridad habilidad);

    void verificarConflictoPorRevocacionPermisoAerolinea(Agente agente, Long aerolineaId);

    void verificarConflictoPorDesactivacionAgente(Agente agente);
}