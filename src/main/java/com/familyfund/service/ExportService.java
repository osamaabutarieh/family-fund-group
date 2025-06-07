package com.familyfund.service;

import com.familyfund.model.Transaction;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private static final String[] HEADERS = {"Date", "Member", "Type", "Amount", "Note"};

    public byte[] exportToExcel(List<Transaction> transactions, String title) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(title);

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Create amount style
            CellStyle amountStyle = workbook.createCellStyle();
            amountStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

            // Create header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // Create data rows
            int rowNum = 1;
            for (Transaction transaction : transactions) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(transaction.getDate().format(DATE_FORMATTER));
                row.createCell(1).setCellValue(transaction.getMember() != null ? 
                    transaction.getMember().getName() : "N/A");
                row.createCell(2).setCellValue(transaction.getType().toString());
                
                Cell amountCell = row.createCell(3);
                amountCell.setCellValue(transaction.getAmount().doubleValue());
                amountCell.setCellStyle(amountStyle);
                
                row.createCell(4).setCellValue(transaction.getNote());
            }

            // Autosize columns
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to ByteArrayOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public byte[] exportToPdf(List<Transaction> transactions, String title) throws Exception {
        Document document = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, outputStream);

        document.open();

        // Add title
        com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph titlePara = new Paragraph(title, titleFont);
        titlePara.setAlignment(Element.ALIGN_CENTER);
        titlePara.setSpacingAfter(20);
        document.add(titlePara);

        // Create table
        PdfPTable table = new PdfPTable(HEADERS.length);
        table.setWidthPercentage(100);

        // Add header cells
        for (String header : HEADERS) {
            PdfPCell cell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setPadding(5);
            table.addCell(cell);
        }

        // Add data rows
        for (Transaction transaction : transactions) {
            table.addCell(transaction.getDate().format(DATE_FORMATTER));
            table.addCell(transaction.getMember() != null ? transaction.getMember().getName() : "N/A");
            table.addCell(transaction.getType().toString());
            table.addCell(String.format("$%.2f", transaction.getAmount()));
            table.addCell(transaction.getNote());
        }

        document.add(table);
        document.close();

        return outputStream.toByteArray();
    }
} 