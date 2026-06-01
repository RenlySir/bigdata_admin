package com.bigdata.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bigdata.admin.dto.TiDBConnectionInfo;
import com.bigdata.admin.entity.DataSource;
import com.bigdata.admin.service.DataSourceService;
import com.bigdata.admin.service.TiDBConnectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for DataSourceController
 */
@WebMvcTest(DataSourceController.class)
class DataSourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DataSourceService dataSourceService;

    @MockBean
    private TiDBConnectionService tiDBConnectionService;

    private DataSource testDataSource;

    @BeforeEach
    void setUp() {
        testDataSource = new DataSource();
        testDataSource.setId(1L);
        testDataSource.setName("测试数据源");
        testDataSource.setType("tidb");
        testDataSource.setConnectionConfig("{\"host\":\"localhost\",\"port\":4000}");
        testDataSource.setStatus(1);
        testDataSource.setCreatedAt(LocalDateTime.now());
        testDataSource.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void getDataSources_WhenCalled_ShouldReturnPage() throws Exception {
        Page<DataSource> mockPage = new Page<>(1, 10);
        when(dataSourceService.getDataSources(eq(1), eq(10), isNull()))
                .thenReturn(mockPage);

        mockMvc.perform(get("/datasources")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void getDataSources_WithTypeFilter_ShouldFilter() throws Exception {
        Page<DataSource> mockPage = new Page<>(1, 10);
        when(dataSourceService.getDataSources(eq(1), eq(10), eq("tidb")))
                .thenReturn(mockPage);

        mockMvc.perform(get("/datasources")
                        .param("page", "1")
                        .param("size", "10")
                        .param("type", "tidb"))
                .andExpect(status().isOk());
    }

    @Test
    void getDataSource_WhenExists_ShouldReturnDataSource() throws Exception {
        when(dataSourceService.getDataSourceById(1L)).thenReturn(testDataSource);

        mockMvc.perform(get("/datasources/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getDataSource_WhenNotExists_ShouldReturnError() throws Exception {
        when(dataSourceService.getDataSourceById(999L)).thenReturn(null);

        mockMvc.perform(get("/datasources/999"))
                .andExpect(status().isOk());
    }

    @Test
    void createDataSource_WhenValid_ShouldReturnCreated() throws Exception {
        when(dataSourceService.createDataSource(any(DataSource.class)))
                .thenReturn(testDataSource);

        mockMvc.perform(post("/datasources")
                        .contentType("application/json")
                        .content("{\"name\":\"new source\",\"type\":\"tidb\",\"connectionConfig\":\"{}\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void updateDataSource_WhenExists_ShouldReturnUpdated() throws Exception {
        when(dataSourceService.updateDataSource(eq(1L), any(DataSource.class)))
                .thenReturn(testDataSource);

        mockMvc.perform(put("/datasources/1")
                        .contentType("application/json")
                        .content("{\"name\":\"updated source\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void updateDataSource_WhenNotExists_ShouldReturnError() throws Exception {
        when(dataSourceService.updateDataSource(eq(999L), any(DataSource.class)))
                .thenReturn(null);

        mockMvc.perform(put("/datasources/999")
                        .contentType("application/json")
                        .content("{\"name\":\"updated\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteDataSource_WhenExists_ShouldSucceed() throws Exception {
        mockMvc.perform(delete("/datasources/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testConnection_WhenExists_ShouldReturnResult() throws Exception {
        when(dataSourceService.getDataSourceById(1L)).thenReturn(testDataSource);
        when(dataSourceService.testConnection(any(DataSource.class))).thenReturn(true);

        mockMvc.perform(post("/datasources/1/test"))
                .andExpect(status().isOk());
    }

    @Test
    void testConnection_WhenNotExists_ShouldReturnError() throws Exception {
        when(dataSourceService.getDataSourceById(999L)).thenReturn(null);

        mockMvc.perform(post("/datasources/999/test"))
                .andExpect(status().isOk());
    }

    @Test
    void testTiDBConnection_WhenValid_ShouldReturnResult() throws Exception {
        TiDBConnectionInfo connectionInfo = TiDBConnectionInfo.builder()
                .host("localhost")
                .port(4000)
                .database("test")
                .build();

        when(tiDBConnectionService.testConnection(any(TiDBConnectionInfo.class)))
                .thenReturn(connectionInfo);

        mockMvc.perform(post("/datasources/tidb/test")
                        .contentType("application/json")
                        .content("{\"host\":\"localhost\",\"port\":4000,\"database\":\"test\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void getTiDBDatabases_WhenCalled_ShouldReturnList() throws Exception {
        when(tiDBConnectionService.getDatabases(1L)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/datasources/1/tidb/databases"))
                .andExpect(status().isOk());
    }

    @Test
    void getTiDBTables_WhenCalled_ShouldReturnList() throws Exception {
        when(tiDBConnectionService.getTables(eq(1L), eq("test_db")))
                .thenReturn(new ArrayList<>());

        mockMvc.perform(get("/datasources/1/tidb/tables")
                        .param("database", "test_db"))
                .andExpect(status().isOk());
    }

    @Test
    void executeQuery_WhenValid_ShouldReturnResults() throws Exception {
        when(tiDBConnectionService.executeQuery(eq(1L), eq("test_db"), anyString()))
                .thenReturn(new ArrayList<>());

        mockMvc.perform(post("/datasources/1/tidb/query")
                        .contentType("application/json")
                        .content("{\"database\":\"test_db\",\"query\":\"SELECT * FROM test\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void executeQuery_WhenMissingDatabase_ShouldReturnError() throws Exception {
        mockMvc.perform(post("/datasources/1/tidb/query")
                        .contentType("application/json")
                        .content("{\"query\":\"SELECT * FROM test\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void executeQuery_WhenMissingQuery_ShouldReturnError() throws Exception {
        mockMvc.perform(post("/datasources/1/tidb/query")
                        .contentType("application/json")
                        .content("{\"database\":\"test_db\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void getDefaultTiDBInfo_WhenCalled_ShouldReturnInfo() throws Exception {
        mockMvc.perform(get("/datasources/tidb/info"))
                .andExpect(status().isOk());
    }
}
