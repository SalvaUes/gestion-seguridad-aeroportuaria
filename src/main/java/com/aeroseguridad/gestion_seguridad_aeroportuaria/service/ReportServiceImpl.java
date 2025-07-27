package com.aeroseguridad.gestion_seguridad_aeroportuaria.service;

import com.aeroseguridad.gestion_seguridad_aeroportuaria.dto.ScheduleResult;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Assignment;
import com.aeroseguridad.gestion_seguridad_aeroportuaria.entity.Vuelo;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Override
    public byte[] generatePdfReport(ScheduleResult data) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Document document = new Document(PageSize.A4)) {
            PdfWriter.getInstance(document, out);
            document.open();

            com.lowagie.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLUE);
            Paragraph title = new Paragraph("Reporte de Horario Operacional", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            Paragraph date = new Paragraph("Fecha del Reporte: " + LocalDate.now().format(dateFormatter));
            date.setAlignment(Element.ALIGN_CENTER);
            document.add(date);
            document.add(Chunk.NEWLINE);

            Map<Vuelo, List<Assignment>> assignmentsByVuelo = data.getAssignments().stream()
                    .collect(Collectors.groupingBy(Assignment::getVuelo));

            for (Map.Entry<Vuelo, List<Assignment>> entry : assignmentsByVuelo.entrySet()) {
                Vuelo vuelo = entry.getKey();
                List<Assignment> assignments = entry.getValue();
                
                PdfPTable table = new PdfPTable(2);
                table.setWidthPercentage(100);
                table.setSpacingAfter(20f);

                com.lowagie.text.Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);
                PdfPCell headerCell = new PdfPCell(new Phrase("Vuelo: " + vuelo.getNumeroVuelo() + " (" + vuelo.getAerolinea().getNombre() + ")", headerFont));
                headerCell.setColspan(2);
                headerCell.setBackgroundColor(new Color(0, 90, 156));
                headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                headerCell.setPadding(8f);
                table.addCell(headerCell);

                com.lowagie.text.Font subHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
                table.addCell(new Phrase("Posición Requerida", subHeaderFont));
                table.addCell(new Phrase("Agente Asignado", subHeaderFont));

                for (Assignment assignment : assignments) {
                    table.addCell(assignment.getPosicionSeguridad().getNombrePosicion());
                    table.addCell(assignment.getAgente().getNombreCompleto());
                }
                
                document.add(table);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    @Override
    public byte[] generateExcelReport(ScheduleResult data) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Horario");

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"Vuelo", "Aerolínea", "Origen", "Destino", "Fecha/Hora Llegada", "Posición", "Agente Asignado"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowNum = 1;
            for (var assignment : data.getAssignments()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(assignment.getVuelo().getNumeroVuelo());
                row.createCell(1).setCellValue(assignment.getVuelo().getAerolinea().getNombre());
                row.createCell(2).setCellValue(assignment.getVuelo().getOrigen());
                row.createCell(3).setCellValue(assignment.getVuelo().getDestino());
                row.createCell(4).setCellValue(assignment.getVuelo().getFechaHoraLlegada().toString());
                row.createCell(5).setCellValue(assignment.getPosicionSeguridad().getNombrePosicion());
                row.createCell(6).setCellValue(assignment.getAgente().getNombreCompleto());
            }
            
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}