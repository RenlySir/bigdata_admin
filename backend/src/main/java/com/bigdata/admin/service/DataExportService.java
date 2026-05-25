package com.bigdata.admin.service;

import com.bigdata.admin.entity.DataRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Data Export Service
 * Handles exporting data to Excel and JSON formats
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataExportService {

    /**
     * Export records to Excel format
     * @param records Data records to export
     * @return Excel file as byte array
     */
    public byte[] exportToExcel(List<DataRecord> records) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Data Records");

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Data Type", "JSON Data", "Text Content", "Version", "Created At", "Updated At"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Create data rows
            int rowNum = 1;
            for (DataRecord record : records) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(record.getId());
                row.createCell(1).setCellValue(record.getDataType());
                row.createCell(2).setCellValue(truncate(record.getJsonData(), 32767)); // Excel cell limit
                row.createCell(3).setCellValue(truncate(record.getTextContent(), 32767));
                row.createCell(4).setCellValue(record.getVersion());
                row.createCell(5).setCellValue(formatDate(record.getCreatedAt()));
                row.createCell(6).setCellValue(formatDate(record.getUpdatedAt()));
            }

            // Create summary sheet
            Sheet summarySheet = workbook.createSheet("Summary");
            createSummarySheet(summarySheet, records);

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Export records to JSON format
     * @param records Data records to export
     * @return JSON string
     */
    public String exportToJson(List<DataRecord> records) {
        StringBuilder json = new StringBuilder();
        json.append("[\n");

        for (int i = 0; i < records.size(); i++) {
            DataRecord record = records.get(i);
            json.append("  {\n");
            json.append("    \"id\": ").append(record.getId()).append(",\n");
            json.append("    \"collectionId\": ").append(record.getCollectionId()).append(",\n");
            json.append("    \"dataType\": \"").append(record.getDataType()).append("\",\n");
            json.append("    \"jsonData\": ").append(record.getJsonData()).append(",\n");
            json.append("    \"textContent\": \"").append(escapeJson(record.getTextContent())).append("\",\n");
            json.append("    \"version\": ").append(record.getVersion()).append(",\n");
            json.append("    \"createdAt\": \"").append(formatDate(record.getCreatedAt())).append("\",\n");
            json.append("    \"updatedAt\": \"").append(formatDate(record.getUpdatedAt())).append("\"\n");
            json.append("  }");
            if (i < records.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("]");
        return json.toString();
    }

    /**
     * Export records to CSV format
     * @param records Data records to export
     * @return CSV string
     */
    public String exportToCsv(List<DataRecord> records) {
        StringBuilder csv = new StringBuilder();

        // Header
        csv.append("ID,Collection ID,Data Type,JSON Data,Text Content,Version,Created At,Updated At\n");

        // Data rows
        for (DataRecord record : records) {
            csv.append(record.getId()).append(",");
            csv.append(record.getCollectionId()).append(",");
            csv.append(escapeCsv(record.getDataType())).append(",");
            csv.append(escapeCsv(record.getJsonData())).append(",");
            csv.append(escapeCsv(record.getTextContent())).append(",");
            csv.append(record.getVersion()).append(",");
            csv.append(escapeCsv(formatDate(record.getCreatedAt()))).append(",");
            csv.append(escapeCsv(formatDate(record.getUpdatedAt()))).append("\n");
        }

        return csv.toString();
    }

    /**
     * Create summary sheet
     */
    private void createSummarySheet(Sheet sheet, List<DataRecord> records) {
        // Create header
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Metric");
        headerRow.createCell(1).setCellValue("Value");

        // Add summary data
        int rowNum = 1;
        createRow(sheet, rowNum++, "Total Records", String.valueOf(records.size()));

        long jsonCount = records.stream().filter(r -> "json".equals(r.getDataType())).count();
        createRow(sheet, rowNum++, "JSON Records", String.valueOf(jsonCount));

        long textCount = records.stream().filter(r -> "text".equals(r.getDataType())).count();
        createRow(sheet, rowNum++, "Text Records", String.valueOf(textCount));

        long totalSize = records.stream()
                .mapToLong(r -> r.getJsonData() != null ? r.getJsonData().length() : 0)
                .sum();
        createRow(sheet, rowNum++, "Total Data Size (bytes)", String.valueOf(totalSize));

        // Auto-size columns
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createRow(Sheet sheet, int rowNum, String label, String value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        return str.length() > maxLength ? str.substring(0, maxLength) : str;
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String escapeCsv(String str) {
        if (str == null) return "";
        if (str.contains(",") || str.contains("\"") || str.contains("\n")) {
            return "\"" + str.replace("\"", "\"\"") + "\"";
        }
        return str;
    }

    private String formatDate(Object date) {
        if (date == null) return "";
        return date.toString();
    }
}
