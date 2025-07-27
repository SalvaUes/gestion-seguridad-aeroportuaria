package com.aeroseguridad.gestion_seguridad_aeroportuaria.service;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.dto.ScheduleResult;

public interface ReportService {

    /**
     * Crea un reporte de horario en formato PDF.
     * @param data El resultado del horario generado.
     * @return Un array de bytes con el contenido del archivo PDF.
     */
    byte[] generatePdfReport(ScheduleResult data);

    /**
     * Crea un reporte de horario en formato Excel.
     * @param data El resultado del horario generado.
     * @return Un array de bytes con el contenido del archivo Excel.
     */
    byte[] generateExcelReport(ScheduleResult data);
}