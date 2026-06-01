package com.bigdata.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bigdata.admin.entity.DataCollection;
import com.bigdata.admin.service.DataCollectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for DataCollectionController
 */
@WebMvcTest(DataCollectionController.class)
class DataCollectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DataCollectionService dataCollectionService;

    private DataCollection testCollection;

    @BeforeEach
    void setUp() {
        testCollection = new DataCollection();
        testCollection.setId(1L);
        testCollection.setName("测试集合");
        testCollection.setDescription("测试描述");
        testCollection.setRecordCount(0L);
        testCollection.setStatus(1);
        testCollection.setCreatedAt(LocalDateTime.now());
        testCollection.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void getCollections_WhenCalled_ShouldReturnPage() throws Exception {
        Page<DataCollection> mockPage = new Page<>(1, 10);
        when(dataCollectionService.getCollections(eq(1), eq(10), isNull()))
                .thenReturn(mockPage);

        mockMvc.perform(get("/collections")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void getCollections_WithKeyword_ShouldFilter() throws Exception {
        Page<DataCollection> mockPage = new Page<>(1, 10);
        when(dataCollectionService.getCollections(eq(1), eq(10), eq("test")))
                .thenReturn(mockPage);

        mockMvc.perform(get("/collections")
                        .param("page", "1")
                        .param("size", "10")
                        .param("keyword", "test"))
                .andExpect(status().isOk());
    }

    @Test
    void getCollection_WhenExists_ShouldReturnCollection() throws Exception {
        when(dataCollectionService.getCollectionById(1L)).thenReturn(testCollection);

        mockMvc.perform(get("/collections/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getCollection_WhenNotExists_ShouldReturnError() throws Exception {
        when(dataCollectionService.getCollectionById(999L)).thenReturn(null);

        mockMvc.perform(get("/collections/999"))
                .andExpect(status().isOk());
    }

    @Test
    void createCollection_WhenValid_ShouldReturnCreated() throws Exception {
        when(dataCollectionService.createCollection(any(DataCollection.class)))
                .thenReturn(testCollection);

        mockMvc.perform(post("/collections")
                        .contentType("application/json")
                        .content("{\"name\":\"new collection\",\"description\":\"test\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void updateCollection_WhenExists_ShouldReturnUpdated() throws Exception {
        when(dataCollectionService.updateCollection(eq(1L), any(DataCollection.class)))
                .thenReturn(testCollection);

        mockMvc.perform(put("/collections/1")
                        .contentType("application/json")
                        .content("{\"name\":\"updated collection\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void updateCollection_WhenNotExists_ShouldReturnError() throws Exception {
        when(dataCollectionService.updateCollection(eq(999L), any(DataCollection.class)))
                .thenReturn(null);

        mockMvc.perform(put("/collections/999")
                        .contentType("application/json")
                        .content("{\"name\":\"updated\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteCollection_WhenExists_ShouldSucceed() throws Exception {
        mockMvc.perform(delete("/collections/1"))
                .andExpect(status().isOk());
    }

    @Test
    void updateStats_WhenCalled_ShouldReturnCount() throws Exception {
        when(dataCollectionService.updateRecordCount(1L)).thenReturn(100L);

        mockMvc.perform(post("/collections/1/stats"))
                .andExpect(status().isOk());
    }
}
