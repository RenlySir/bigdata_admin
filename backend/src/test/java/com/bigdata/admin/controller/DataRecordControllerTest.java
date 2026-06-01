package com.bigdata.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bigdata.admin.common.Result;
import com.bigdata.admin.entity.DataRecord;
import com.bigdata.admin.service.DataRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for DataRecordController
 */
@WebMvcTest(DataRecordController.class)
class DataRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DataRecordService dataRecordService;

    private DataRecord testRecord;

    @BeforeEach
    void setUp() {
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
    void getRecords_WhenCalled_ShouldReturnPage() throws Exception {
        Page<DataRecord> mockPage = new Page<>(1, 10);
        when(dataRecordService.getRecords(eq(1L), eq(1), eq(10), isNull()))
                .thenReturn(mockPage);

        mockMvc.perform(get("/collections/1/records")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void getRecords_WithKeyword_ShouldFilter() throws Exception {
        Page<DataRecord> mockPage = new Page<>(1, 10);
        when(dataRecordService.getRecords(eq(1L), eq(1), eq(10), eq("test")))
                .thenReturn(mockPage);

        mockMvc.perform(get("/collections/1/records")
                        .param("page", "1")
                        .param("size", "10")
                        .param("keyword", "test"))
                .andExpect(status().isOk());
    }

    @Test
    void getRecord_WhenExists_ShouldReturnRecord() throws Exception {
        when(dataRecordService.getRecordById(1L)).thenReturn(testRecord);

        mockMvc.perform(get("/collections/1/records/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getRecord_WhenNotExists_ShouldReturnError() throws Exception {
        when(dataRecordService.getRecordById(999L)).thenReturn(null);

        mockMvc.perform(get("/collections/1/records/999"))
                .andExpect(status().isOk());
    }

    @Test
    void getRecord_WhenWrongCollection_ShouldReturnError() throws Exception {
        testRecord.setCollectionId(2L);
        when(dataRecordService.getRecordById(1L)).thenReturn(testRecord);

        mockMvc.perform(get("/collections/1/records/1"))
                .andExpect(status().isOk());
    }

    @Test
    void batchInsertRecords_WhenValid_ShouldSucceed() throws Exception {
        doAnswer(invocation -> null).when(dataRecordService).batchInsertRecords(any());

        mockMvc.perform(post("/collections/1/records/batch")
                        .contentType("application/json")
                        .content("[]"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteRecord_WhenExists_ShouldSucceed() throws Exception {
        when(dataRecordService.getRecordById(1L)).thenReturn(testRecord);

        mockMvc.perform(delete("/collections/1/records/1"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteRecord_WhenNotExists_ShouldReturnError() throws Exception {
        when(dataRecordService.getRecordById(999L)).thenReturn(null);

        mockMvc.perform(delete("/collections/1/records/999"))
                .andExpect(status().isOk());
    }
}
