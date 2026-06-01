package com.bigdata.admin.service;

import com.bigdata.admin.entity.DataRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DataExportService
 */
class DataExportServiceTest {

    private DataExportService dataExportService;

    private List<DataRecord> testRecords;

    @BeforeEach
    void setUp() {
        dataExportService = new DataExportService();

        // Create test records
        testRecords = new ArrayList<>();
        DataRecord record1 = new DataRecord();
        record1.setId(1L);
        record1.setCollectionId(1L);
        record1.setDataType("json");
        record1.setJsonData("{\"name\":\"test\",\"value\":\"data\"}");
        record1.setTextContent("test data");
        record1.setVersion(1L);
        record1.setCreatedAt(LocalDateTime.of(2024, 1, 1, 12, 0));
        record1.setUpdatedAt(LocalDateTime.of(2024, 1, 1, 12, 0));

        DataRecord record2 = new DataRecord();
        record2.setId(2L);
        record2.setCollectionId(1L);
        record2.setDataType("text");
        record2.setJsonData("{\"text\":\"sample\"}");
        record2.setTextContent("sample text");
        record2.setVersion(1L);
        record2.setCreatedAt(LocalDateTime.of(2024, 1, 2, 12, 0));
        record2.setUpdatedAt(LocalDateTime.of(2024, 1, 2, 12, 0));

        testRecords.add(record1);
        testRecords.add(record2);
    }

    @Test
    void exportToJson_WhenRecordsExist_ShouldReturnValidJson() throws Exception {
        String json = dataExportService.exportToJson(testRecords);

        assertNotNull(json);
        assertTrue(json.startsWith("["));
        assertTrue(json.endsWith("]"));
        assertTrue(json.contains("\"id\": 1"));
        assertTrue(json.contains("\"id\": 2"));
    }

    @Test
    void exportToJson_WhenEmptyList_ShouldReturnEmptyArray() {
        String json = dataExportService.exportToJson(new ArrayList<>());

        assertNotNull(json);
        assertEquals("[\n]", json);
    }

    @Test
    void exportToCsv_WhenRecordsExist_ShouldReturnValidCsv() {
        String csv = dataExportService.exportToCsv(testRecords);

        assertNotNull(csv);
        assertTrue(csv.contains("ID,Collection ID,Data Type"));
        assertTrue(csv.contains("1,1,json"));
        assertTrue(csv.contains("2,1,text"));
    }

    @Test
    void exportToCsv_WhenEmptyList_ShouldReturnHeaderOnly() {
        String csv = dataExportService.exportToCsv(new ArrayList<>());

        assertNotNull(csv);
        assertTrue(csv.startsWith("ID,Collection ID,Data Type"));
    }

    @Test
    void exportToExcel_WhenRecordsExist_ShouldReturnByteArray() throws Exception {
        byte[] excelData = dataExportService.exportToExcel(testRecords);

        assertNotNull(excelData);
        assertTrue(excelData.length > 0);
        // Excel files start with PK (ZIP signature)
        assertEquals('P', (char) excelData[0]);
        assertEquals('K', (char) excelData[1]);
    }

    @Test
    void exportToExcel_WhenEmptyList_ShouldReturnValidExcel() throws Exception {
        byte[] excelData = dataExportService.exportToExcel(new ArrayList<>());

        assertNotNull(excelData);
        assertTrue(excelData.length > 0);
    }

    @Test
    void exportToJson_WhenContainsSpecialCharacters_ShouldHandle() {
        DataRecord record = new DataRecord();
        record.setId(1L);
        record.setCollectionId(1L);
        record.setDataType("json");
        record.setJsonData("{\"text\":\"data\"}");
        record.setTextContent("text with special chars");
        record.setVersion(1L);
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());

        List<DataRecord> records = List.of(record);
        String json = dataExportService.exportToJson(records);

        assertNotNull(json);
        assertTrue(json.contains("\"id\": 1"));
        assertTrue(json.contains("special chars"));
    }

    @Test
    void exportToCsv_WhenContainsCommas_ShouldQuote() {
        DataRecord record = new DataRecord();
        record.setId(1L);
        record.setCollectionId(1L);
        record.setDataType("text");
        record.setJsonData("{}");
        record.setTextContent("data, with, commas");
        record.setVersion(1L);

        List<DataRecord> records = List.of(record);
        String csv = dataExportService.exportToCsv(records);

        assertNotNull(csv);
        assertTrue(csv.contains("\"data, with, commas\""));
    }

    private void assertNotNull(Object obj) {
        if (obj == null) {
            throw new AssertionError("Expected non-null value");
        }
    }

    private void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Expected true but was false");
        }
    }

    private void assertEquals(String expected, String actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected '" + expected + "' but was '" + actual + "'");
        }
    }

    private void assertEquals(char expected, char actual) {
        if (expected != actual) {
            throw new AssertionError("Expected '" + expected + "' but was '" + actual + "'");
        }
    }
}
