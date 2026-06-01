package com.bigdata.admin.service;

import com.bigdata.admin.entity.DataRecord;
import com.bigdata.admin.entity.ImportTask;
import com.bigdata.admin.mapper.DataRecordMapper;
import com.bigdata.admin.mapper.ImportTaskMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DataImportService
 */
@ExtendWith(MockitoExtension.class)
class DataImportServiceTest {

    @Mock
    private ImportTaskMapper importTaskMapper;

    @Mock
    private DataRecordMapper dataRecordMapper;

    @Mock
    private MultipartFile multipartFile;

    private DataImportService dataImportService;

    private ImportTask testTask;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        dataImportService = new DataImportService(importTaskMapper, dataRecordMapper, objectMapper);

        testTask = new ImportTask();
        testTask.setId(1L);
        testTask.setCollectionId(1L);
        testTask.setSourceType("csv");
        testTask.setSourceConfig("{}");
        testTask.setStatus("pending");
        testTask.setProgress(0);
        testTask.setTotalRecords(0);
        testTask.setProcessedRecords(0);
        testTask.setFailedRecords(0);
        testTask.setCreatedAt(LocalDateTime.now());
        testTask.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void createImportTask_WhenValid_ShouldReturnTaskId() {
        when(importTaskMapper.insert(any(ImportTask.class))).thenAnswer(invocation -> {
            ImportTask task = invocation.getArgument(0);
            task.setId(1L);
            return 1;
        });

        Long taskId = dataImportService.createImportTask(1L, "csv", "{}");

        assertNotNull(taskId);
        verify(importTaskMapper).insert(any(ImportTask.class));
    }

    @Test
    void createImportTask_WhenValid_ShouldSetPendingStatus() {
        when(importTaskMapper.insert(any(ImportTask.class))).thenAnswer(invocation -> {
            ImportTask task = invocation.getArgument(0);
            task.setId(1L);
            return 1;
        });

        Long taskId = dataImportService.createImportTask(1L, "json", "{\"format\":\"array\"}");

        assertNotNull(taskId);
        verify(importTaskMapper).insert(argThat(task ->
            "pending".equals(task.getStatus()) &&
            task.getProgress() == 0 &&
            task.getProcessedRecords() == 0 &&
            task.getFailedRecords() == 0
        ));
    }

    @Test
    void getTask_WhenExists_ShouldReturnTask() {
        when(importTaskMapper.selectById(1L)).thenReturn(testTask);

        ImportTask result = dataImportService.getTask(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("csv", result.getSourceType());
    }

    @Test
    void getTask_WhenNotExists_ShouldReturnNull() {
        when(importTaskMapper.selectById(999L)).thenReturn(null);

        ImportTask result = dataImportService.getTask(999L);

        assertNull(result);
    }

    @Test
    void getTasksByCollection_WhenCalled_ShouldReturnList() {
        when(importTaskMapper.selectList(any())).thenReturn(List.of(testTask));

        var result = dataImportService.getTasksByCollection(1L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(importTaskMapper).selectList(any());
    }

    @Test
    void processImport_WhenTaskNotExists_ShouldReturnEarly() {
        when(importTaskMapper.selectById(999L)).thenReturn(null);

        dataImportService.processImport(999L, multipartFile);

        verify(importTaskMapper, never()).updateById(any());
        verify(dataRecordMapper, never()).insert(any());
    }

    @Test
    void createImportTask_WithDifferentSourceTypes_ShouldSucceed() {
        when(importTaskMapper.insert(any(ImportTask.class))).thenAnswer(invocation -> {
            ImportTask task = invocation.getArgument(0);
            task.setId(1L);
            return 1;
        });

        Long csvTaskId = dataImportService.createImportTask(1L, "csv", "{}");
        Long excelTaskId = dataImportService.createImportTask(2L, "xlsx", "{}");
        Long jsonTaskId = dataImportService.createImportTask(3L, "json", "{}");

        assertNotNull(csvTaskId);
        assertNotNull(excelTaskId);
        assertNotNull(jsonTaskId);
        verify(importTaskMapper, times(3)).insert(any(ImportTask.class));
    }

    @Test
    void processImport_WhenUnsupportedSourceType_ShouldHandleGracefully() {
        testTask.setSourceType("xml");
        when(importTaskMapper.selectById(1L)).thenReturn(testTask);

        // The method should handle unsupported types gracefully
        // It will mark the task as failed rather than throwing an exception
        dataImportService.processImport(1L, multipartFile);

        // Verify the task was updated
        verify(importTaskMapper, atLeastOnce()).updateById(any());
    }

    private void assertNotNull(Object obj) {
        if (obj == null) {
            throw new AssertionError("Expected non-null value");
        }
    }

    private void assertNull(Object obj) {
        if (obj != null) {
            throw new AssertionError("Expected null but was: " + obj);
        }
    }

    private void assertEquals(Long expected, Long actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected " + expected + " but was " + actual);
        }
    }

    private void assertEquals(String expected, String actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected '" + expected + "' but was '" + actual + "'");
        }
    }

    private void assertFalse(boolean condition) {
        if (condition) {
            throw new AssertionError("Expected false but was true");
        }
    }

    private void assertThrows(Class<? extends Exception> expectedException, Runnable runnable) {
        try {
            runnable.run();
            throw new AssertionError("Expected " + expectedException.getSimpleName() + " to be thrown, but nothing was thrown");
        } catch (Exception e) {
            if (!expectedException.isInstance(e)) {
                throw new AssertionError("Expected " + expectedException.getSimpleName() + " but got " + e.getClass().getSimpleName());
            }
        }
    }
}
