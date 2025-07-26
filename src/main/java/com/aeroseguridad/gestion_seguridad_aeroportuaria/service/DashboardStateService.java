package com.aeroseguridad.gestion_seguridad_aeroportuaria.service;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.dto.ScheduleResult;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Service
@UIScope // <-- La magia está aquí. Vive mientras la pestaña del navegador esté abierta.
@Getter
@Setter
public class DashboardStateService {
    private ScheduleResult lastResult;
}