package com.aeroseguridad.gestion_seguridad_aeroportuaria.service;

public interface EmailService {
    void sendReport(String to, String subject, String body, byte[] attachment, String attachmentName);
}