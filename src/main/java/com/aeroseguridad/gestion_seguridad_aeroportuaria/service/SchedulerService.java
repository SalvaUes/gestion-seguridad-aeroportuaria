package com.aeroseguridad.gestion_seguridad_aeroportuaria.service;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.dto.ScheduleRequest;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.dto.ScheduleResult;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Agente; // <-- AÑADIR IMPORT
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Assignment; // <-- AÑADIR IMPORT
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.NecesidadVuelo; // <-- AÑADIR IMPORT
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Vuelo; // <-- AÑADIR IMPORT
import org.springframework.scheduling.annotation.Async;
import java.util.List; // <-- AÑADIR IMPORT
import java.util.concurrent.Future;

public interface SchedulerService {

    @Async("taskExecutor")
    Future<ScheduleResult> generateSchedule(ScheduleRequest request);

    // --- NUEVO MÉTODO: Para encontrar candidatos para una posición ---
    List<Agente> findCandidates(NecesidadVuelo necesidad, List<Assignment> currentAssignments);
    
    // --- NUEVO MÉTODO: Para actualizar las asignaciones de un vuelo ---
    void updateAssignmentsForVuelo(Vuelo vuelo, List<Assignment> newAssignments);
}