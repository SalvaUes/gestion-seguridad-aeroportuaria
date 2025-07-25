package com.aeroseguridad.gestion_seguridad_aeroportuaria.dto;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Assignment;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ScheduleResult {
    private List<Assignment> assignments = new ArrayList<>();
    private List<Assignment> conflicts = new ArrayList<>();
    // Correcto. No se necesitan cambios.
}