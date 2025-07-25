package com.aeroseguridad.gestion_seguridad_aeroportuaria.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRequest {
    private LocalDate startDate;
    private LocalDate endDate;
}