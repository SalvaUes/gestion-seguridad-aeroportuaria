package com.aeroseguridad.gestion_seguridad_aeroportuaria.ui.dialogs;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.dto.ScheduleResult;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.EmailService;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.service.ReportService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.EmailField;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ShareReportDialog extends Dialog {

    private final EmailField emailField;
    private final RadioButtonGroup<String> formatSelector;
    private final Button sendButton;
    private final Button cancelButton;

    private final ScheduleResult scheduleResult;
    private final ReportService reportService;
    private final EmailService emailService;

    public ShareReportDialog(ScheduleResult scheduleResult, ReportService reportService, EmailService emailService) {
        this.scheduleResult = scheduleResult;
        this.reportService = reportService;
        this.emailService = emailService;

        setHeaderTitle("Compartir Reporte por Correo");

        emailField = new EmailField("Correo del Destinatario");
        emailField.setRequiredIndicatorVisible(true);
        emailField.setWidthFull();

        formatSelector = new RadioButtonGroup<>("Formato del Archivo");
        formatSelector.setItems("PDF", "Excel");
        formatSelector.setValue("PDF");

        sendButton = new Button("Enviar", e -> send());
        sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelButton = new Button("Cancelar", e -> close());

        getFooter().add(cancelButton, sendButton);
        add(new VerticalLayout(emailField, formatSelector));
    }

    private void send() {
        if (emailField.isInvalid() || emailField.isEmpty()) {
            Notification.show("Por favor, ingrese un correo válido.", 3000, Notification.Position.BOTTOM_CENTER);
            return;
        }

        String to = emailField.getValue();
        String format = formatSelector.getValue().toLowerCase();
        String attachmentName = "Horario_Seguridad_" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + "." + ("excel".equals(format) ? "xlsx" : "pdf");
        
        byte[] reportBytes = "pdf".equals(format) ?
                reportService.generatePdfReport(scheduleResult) :
                reportService.generateExcelReport(scheduleResult);
        
        try {
            emailService.sendReport(
                to,
                "Reporte de Horario de Seguridad",
                "Adjunto se encuentra el reporte de horario generado.",
                reportBytes,
                attachmentName
            );
            Notification.show("Correo enviado exitosamente a " + to, 3000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            close();
        } catch (Exception e) {
            Notification.show("Error al enviar el correo: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}