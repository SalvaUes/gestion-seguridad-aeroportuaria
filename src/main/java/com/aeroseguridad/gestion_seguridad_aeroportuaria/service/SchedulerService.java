package com.aeroseguridad.gestion_seguridad_aeroportuaria.service;

// IMPORTACIONES CORREGIDAS (sin el subpaquete "scheduling")
import com.aeroseguridad.gestion_seguridad_aeroportuaria.dto.ScheduleRequest;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.dto.ScheduleResult;
import org.springframework.scheduling.annotation.Async;
import java.util.concurrent.Future;

public interface SchedulerService {

    @Async("taskExecutor")
    Future<ScheduleResult> generateSchedule(ScheduleRequest request);
}