package com.bigdata.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bigdata.admin.entity.DataRecord;
import com.bigdata.admin.entity.ImportTask;
import com.bigdata.admin.mapper.DataRecordMapper;
import com.bigdata.admin.mapper.ImportTaskMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Data Import Service
 * Handles CSV, Excel, and JSON imports
 */
@Service
public class DataImportService {

    private static final Logger log = LoggerFactory.getLogger(DataImportService.class);

    private final ImportTaskMapper importTaskMapper;
    private final DataRecordMapper dataRecordMapper;
    private final ObjectMapper objectMapper;

    public DataImportService(ImportTaskMapper importTaskMapper, DataRecordMapper dataRecordMapper, ObjectMapper objectMapper) {
        this.importTaskMapper = importTaskMapper;
        this.dataRecordMapper = dataRecordMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * Create a new import task
     * @param collectionId Target collection ID
     * @param sourceType Source type (csv, excel, json)
     * @param sourceConfig Source configuration
     * @return Import task ID
     */
    @Transactional
    public Long createImportTask(Long collectionId, String sourceType, String sourceConfig) {
        ImportTask task = new ImportTask();
        task.setCollectionId(collectionId);
        task.setSourceType(sourceType);
        task.setSourceConfig(sourceConfig);
        task.setStatus("pending");
        task.setProgress(0);
        task.setProcessedRecords(0);
        task.setFailedRecords(0);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        importTaskMapper.insert(task);
        return task.getId();
    }

    /**
     * Process import from uploaded file
     * @param taskId Import task ID
     * @param file Uploaded file
     */
    @Async
    public void processImport(Long taskId, MultipartFile file) {
        ImportTask task = importTaskMapper.selectById(taskId);
        if (task == null) {
            log.error("Import task not found: {}", taskId);
            return;
        }

        try {
            // Update task status to running
            task.setStatus("running");
            task.setStartedAt(LocalDateTime.now());
            importTaskMapper.updateById(task);

            // Process based on source type
            switch (task.getSourceType().toLowerCase()) {
                case "csv":
                    processCsvImport(task, file);
                    break;
                case "excel":
                case "xlsx":
                    processExcelImport(task, file);
                    break;
                case "json":
                    processJsonImport(task, file);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported source type: " + task.getSourceType());
            }

            // Mark as completed
            task.setStatus("completed");
            task.setProgress(100);
            task.setCompletedAt(LocalDateTime.now());

        } catch (Exception e) {
            log.error("Import failed for task: {}", taskId, e);
            task.setStatus("failed");
            task.setErrorMessage(e.getMessage());
        } finally {
            task.setUpdatedAt(LocalDateTime.now());
            importTaskMapper.updateById(task);
        }
    }

    /**
     * Process CSV import
     */
    private void processCsvImport(ImportTask task, MultipartFile file) throws IOException {
        List<DataRecord> records = new ArrayList<>();
        AtomicInteger recordCount = new AtomicInteger(0);

        // Read CSV file
        try (var inputStream = file.getInputStream()) {
            String content = new String(inputStream.readAllBytes());
            String[] lines = content.split("\n");

            if (lines.length < 2) {
                throw new IllegalArgumentException("CSV file must have at least a header and one data row");
            }

            // Parse header
            String[] headers = parseCsvLine(lines[0]);
            task.setTotalRecords(lines.length - 1);

            // Parse data rows
            for (int i = 1; i < lines.length; i++) {
                try {
                    String[] values = parseCsvLine(lines[i]);
                    String jsonData = buildJsonFromCsv(headers, values);

                    DataRecord record = new DataRecord();
                    record.setCollectionId(task.getCollectionId());
                    record.setDataType("json");
                    record.setJsonData(jsonData);
                    record.setTextContent(String.join(", ", values));
                    records.add(record);

                    // Batch insert every 1000 records
                    if (records.size() >= 1000) {
                        batchInsertWithUpdate(task, records, recordCount);
                        records.clear();
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse row {}: {}", i, e.getMessage());
                    task.setFailedRecords(task.getFailedRecords() + 1);
                }
            }

            // Insert remaining records
            if (!records.isEmpty()) {
                batchInsertWithUpdate(task, records, recordCount);
            }
        }
    }

    /**
     * Process Excel import
     */
    private void processExcelImport(ImportTask task, MultipartFile file) throws IOException {
        List<DataRecord> records = new ArrayList<>();
        AtomicInteger recordCount = new AtomicInteger(0);

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int totalRows = sheet.getPhysicalNumberOfRows() - 1; // Exclude header
            task.setTotalRecords(totalRows);

            // Read header row
            Row headerRow = sheet.getRow(0);
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(getCellValueAsString(cell));
            }

            // Read data rows
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                try {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    List<String> values = new ArrayList<>();
                    for (int j = 0; j < headers.size(); j++) {
                        Cell cell = row.getCell(j);
                        values.add(cell != null ? getCellValueAsString(cell) : "");
                    }

                    String jsonData = buildJsonFromCsv(headers.toArray(new String[0]), values.toArray(new String[0]));

                    DataRecord record = new DataRecord();
                    record.setCollectionId(task.getCollectionId());
                    record.setDataType("json");
                    record.setJsonData(jsonData);
                    record.setTextContent(String.join(", ", values));
                    records.add(record);

                    // Batch insert every 1000 records
                    if (records.size() >= 1000) {
                        batchInsertWithUpdate(task, records, recordCount);
                        records.clear();
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse row {}: {}", i, e.getMessage());
                    task.setFailedRecords(task.getFailedRecords() + 1);
                }
            }

            // Insert remaining records
            if (!records.isEmpty()) {
                batchInsertWithUpdate(task, records, recordCount);
            }
        }
    }

    /**
     * Process JSON import
     */
    private void processJsonImport(ImportTask task, MultipartFile file) throws IOException {
        List<DataRecord> records = new ArrayList<>();
        AtomicInteger recordCount = new AtomicInteger(0);

        try (var inputStream = file.getInputStream()) {
            String content = new String(inputStream.readAllBytes());

            // Parse JSON array or object
            Object jsonObject = objectMapper.readValue(content, Object.class);

            List<Object> jsonObjects;
            if (jsonObject instanceof List) {
                jsonObjects = (List<Object>) jsonObject;
            } else {
                jsonObjects = List.of(jsonObject);
            }

            task.setTotalRecords(jsonObjects.size());

            for (Object obj : jsonObjects) {
                try {
                    String jsonData = objectMapper.writeValueAsString(obj);

                    DataRecord record = new DataRecord();
                    record.setCollectionId(task.getCollectionId());
                    record.setDataType("json");
                    record.setJsonData(jsonData);
                    record.setTextContent(jsonData);
                    records.add(record);

                    // Batch insert every 1000 records
                    if (records.size() >= 1000) {
                        batchInsertWithUpdate(task, records, recordCount);
                        records.clear();
                    }
                } catch (Exception e) {
                    log.warn("Failed to process JSON record: {}", e.getMessage());
                    task.setFailedRecords(task.getFailedRecords() + 1);
                }
            }

            // Insert remaining records
            if (!records.isEmpty()) {
                batchInsertWithUpdate(task, records, recordCount);
            }
        }
    }

    /**
     * Batch insert records with task progress update
     */
    @Transactional
    protected void batchInsertWithUpdate(ImportTask task, List<DataRecord> records, AtomicInteger recordCount) {
        // Set version and checksum
        records.forEach(r -> {
            r.setVersion(1L);
            r.setChecksum(calculateChecksum(r.getJsonData()));
        });

        // Batch insert
        records.forEach(dataRecordMapper::insert);

        // Update task progress
        int current = recordCount.addAndGet(records.size());
        task.setProcessedRecords(current);
        task.setProgress((int) ((current * 100.0) / task.getTotalRecords()));
        task.setUpdatedAt(LocalDateTime.now());
        importTaskMapper.updateById(task);

        log.info("Imported {} records for task {}", current, task.getId());
    }

    /**
     * Parse CSV line handling quoted values
     */
    private String[] parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                values.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        values.add(current.toString());
        return values.toArray(new String[0]);
    }

    /**
     * Build JSON from CSV headers and values using Jackson for proper escaping
     */
    private String buildJsonFromCsv(String[] headers, String[] values) {
        try {
            // Use Jackson ObjectMapper for proper JSON serialization and escaping
            var jsonNode = objectMapper.createObjectNode();
            for (int i = 0; i < headers.length && i < values.length; i++) {
                // ObjectMapper will properly escape special characters including quotes, backslashes, etc.
                jsonNode.put(headers[i], values[i]);
            }
            return objectMapper.writeValueAsString(jsonNode);
        } catch (Exception e) {
            log.error("Error building JSON from CSV data", e);
            // Fallback to basic escaping if Jackson fails
            return buildJsonWithBasicEscaping(headers, values);
        }
    }

    /**
     * Fallback method with basic JSON escaping
     */
    private String buildJsonWithBasicEscaping(String[] headers, String[] values) {
        StringBuilder json = new StringBuilder("{");
        for (int i = 0; i < headers.length && i < values.length; i++) {
            if (i > 0) json.append(",");
            json.append("\"").append(escapeJsonString(headers[i])).append("\":\"")
                .append(escapeJsonString(values[i])).append("\"");
        }
        json.append("}");
        return json.toString();
    }

    /**
     * Escape special characters in JSON strings
     */
    private String escapeJsonString(String input) {
        if (input == null) {
            return "";
        }
        StringBuilder escaped = new StringBuilder();
        for (char c : input.toCharArray()) {
            switch (c) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '/' -> escaped.append("\\/");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (c <= '\u001F' || (c >= '\u007F' && c <= '\u009F') || (c >= '\u2000' && c <= '\u20FF')) {
                        escaped.append(String.format("\\u%04x", (int) c));
                    } else {
                        escaped.append(c);
                    }
                }
            }
        }
        return escaped.toString();
    }

    /**
     * Get cell value as string
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    /**
     * Calculate SHA-256 checksum
     */
    private String calculateChecksum(String data) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("Error calculating checksum", e);
            return "";
        }
    }

    /**
     * Get import task by ID
     */
    public ImportTask getTask(Long taskId) {
        return importTaskMapper.selectById(taskId);
    }

    /**
     * Get tasks by collection ID
     */
    public List<ImportTask> getTasksByCollection(Long collectionId) {
        LambdaQueryWrapper<ImportTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImportTask::getCollectionId, collectionId);
        wrapper.orderByDesc(ImportTask::getCreatedAt);
        return importTaskMapper.selectList(wrapper);
    }
}
