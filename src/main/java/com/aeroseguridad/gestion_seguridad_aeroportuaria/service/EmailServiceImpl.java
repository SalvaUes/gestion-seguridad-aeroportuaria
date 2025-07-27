package com.aeroseguridad.gestion_seguridad_aeroportuaria.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource; // <-- CAMBIO
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void sendReport(String to, String subject, String body, byte[] attachment, String attachmentName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);
            
            if (attachment != null && attachmentName != null) {
                // --- CORRECCIÓN CLAVE: Usar ByteArrayResource en lugar de InputStreamResource ---
                helper.addAttachment(attachmentName, new ByteArrayResource(attachment));
            }

            mailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al enviar el correo: " + e.getMessage());
        }
    }
}