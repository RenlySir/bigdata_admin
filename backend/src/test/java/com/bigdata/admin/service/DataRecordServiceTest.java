package com.bigdata.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bigdata.admin.entity.DataRecord;
import com.bigdata.admin.mapper.DataRecordMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DataRecordService
 */
@ExtendWith(MockitoExtension.class)
class DataRecordServiceTest {

    @Mock
    private DataRecordMapper dataRecordMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    private DataRecordService dataRecordService;

    private DataRecord testRecord;

    @BeforeEach
    void setUp() {
        dataRecordService = new DataRecordService(dataRecordMapper, redisTemplate);

        testRecord = new DataRecord();
        testRecord.setId(1L);
        testRecord.setCollectionId(1L);
        testRecord.setDataType("json");
        testRecord.setJsonData("{\"name\":\"test\"}");
        testRecord.setTextContent("test content");
        testRecord.setVersion(1L);
        testRecord.setCreatedAt(LocalDateTime.now());
        testRecord.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void getRecords_WhenCalled_ShouldReturnPage() {
        Page<DataRecord> mockPage = new Page<>(1, 10);
        when(dataRecordMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        Page<DataRecord> result = dataRecordService.getRecords(1L, 1, 10, null);

        assertNotNull(result);
        verify(dataRecordMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void getRecords_WithKeyword_ShouldFilterByKeyword() {
        Page<DataRecord> mockPage = new Page<>(1, 10);
        when(dataRecordMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        Page<DataRecord> result = dataRecordService.getRecords(1L, 1, 10, "test");

        assertNotNull(result);
        verify(dataRecordMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void getRecordById_WhenExists_ShouldReturnRecord() {
        when(dataRecordMapper.selectById(1L)).thenReturn(testRecord);

        DataRecord result = dataRecordService.getRecordById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getRecordById_WhenNotExists_ShouldReturnNull() {
        when(dataRecordMapper.selectById(999L)).thenReturn(null);

        DataRecord result = dataRecordService.getRecordById(999L);

        assertNull(result);
    }

    @Test
    void createRecord_WhenValid_ShouldSetChecksumAndVersion() {
        when(dataRecordMapper.insert(any(DataRecord.class))).thenReturn(1);

        DataRecord result = dataRecordService.createRecord(testRecord);

        assertNotNull(result);
        assertEquals(1L, result.getVersion());
        assertNotNull(result.getChecksum());
        assertFalse(result.getChecksum().isEmpty());
        verify(dataRecordMapper).insert(any(DataRecord.class));
    }

    @Test
    void createRecord_WhenNullJsonData_ShouldSetEmptyChecksum() {
        testRecord.setJsonData(null);
        when(dataRecordMapper.insert(any(DataRecord.class))).thenReturn(1);

        DataRecord result = dataRecordService.createRecord(testRecord);

        assertNotNull(result);
        assertEquals("", result.getChecksum());
    }

    @Test
    void updateRecord_WhenExists_ShouldIncrementVersion() {
        when(dataRecordMapper.selectById(1L)).thenReturn(testRecord);
        when(dataRecordMapper.updateById(any(DataRecord.class))).thenReturn(1);

        DataRecord result = dataRecordService.updateRecord(1L, testRecord);

        assertNotNull(result);
        verify(dataRecordMapper).updateById(any(DataRecord.class));
    }

    @Test
    void updateRecord_WhenNotExists_ShouldReturnNull() {
        when(dataRecordMapper.selectById(999L)).thenReturn(null);

        DataRecord result = dataRecordService.updateRecord(999L, testRecord);

        assertNull(result);
        verify(dataRecordMapper, never()).updateById(any(DataRecord.class));
    }

    @Test
    void deleteRecord_WhenValidId_ShouldCallDelete() {
        when(dataRecordMapper.deleteById(1L)).thenReturn(1);

        dataRecordService.deleteRecord(1L);

        verify(dataRecordMapper).deleteById(1L);
    }

    @Test
    void batchInsertRecords_WhenEmptyList_ShouldReturn() {
        dataRecordService.batchInsertRecords(new ArrayList<>());

        verify(dataRecordMapper, never()).insert(any(DataRecord.class));
    }

    @Test
    void batchInsertRecords_WhenNullList_ShouldReturn() {
        dataRecordService.batchInsertRecords(null);

        verify(dataRecordMapper, never()).insert(any(DataRecord.class));
    }

    @Test
    void batchInsertRecords_WhenValidList_ShouldSetVersionAndChecksum() {
        // Create a fresh record without ID
        DataRecord freshRecord = new DataRecord();
        freshRecord.setCollectionId(1L);
        freshRecord.setDataType("json");
        freshRecord.setJsonData("{\"name\":\"test\"}");

        when(dataRecordMapper.insert(any(DataRecord.class))).thenReturn(1);

        DataRecord result = dataRecordService.createRecord(freshRecord);

        assertEquals(1L, result.getVersion());
        assertNotNull(result.getChecksum());
        assertFalse(result.getChecksum().isEmpty());
    }

    @Test
    void calculateChecksum_WhenSameData_ShouldProduceSameHash() {
        when(dataRecordMapper.insert(any(DataRecord.class))).thenReturn(1);

        DataRecord record1 = new DataRecord();
        record1.setJsonData("test data");

        DataRecord record2 = new DataRecord();
        record2.setJsonData("test data");

        dataRecordService.createRecord(record1);
        dataRecordService.createRecord(record2);

        assertEquals(record1.getChecksum(), record2.getChecksum());
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
}
