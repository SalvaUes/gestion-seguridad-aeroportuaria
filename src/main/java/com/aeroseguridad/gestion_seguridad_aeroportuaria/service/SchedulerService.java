package com.aeroseguridad.gestion_seguridad_aeroportuaria.service;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.dto.ScheduleRequest;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.dto.ScheduleResult;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Assignment;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.NecesidadVuelo;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Vuelo;

import java.util.List;
import java.util.concurrent.Future;

public interface SchedulerService {

    Future<ScheduleResult> generateSchedule(ScheduleRequest request);
    
    /**
     * CAMBIO CLAVE: La firma ahora recibe el ID del vuelo en lugar de depender
     * de la entidad NecesidadVuelo desconectada para encontrar el vuelo.
     */
    List<Agente> findCandidates(Long idVuelo, NecesidadVuelo necesidad, List<Assignment> currentAssignments);
    
    void updateAssignmentsForVuelo(Vuelo vuelo, List<Assignment> newAssignments);
}