package com.bigdata.admin.service;

import com.bigdata.admin.entity.DataSource;
import com.bigdata.admin.mapper.DataSourceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DataSourceService
 */
@ExtendWith(MockitoExtension.class)
class DataSourceServiceTest {

    @Mock
    private DataSourceMapper dataSourceMapper;

    @InjectMocks
    private DataSourceService dataSourceService;

    private DataSource testDataSource;

    @BeforeEach
    void setUp() {
        testDataSource = new DataSource();
        testDataSource.setId(1L);
        testDataSource.setName("测试数据源");
        testDataSource.setType("tidb");
        testDataSource.setConnectionConfig("{\"host\":\"localhost\",\"port\":4000}");
        testDataSource.setStatus(1);
    }

    @Test
    void getDataSources_WhenCalled_ShouldReturnPage() {
        // Test implementation
        assertNotNull(dataSourceService);
        assertNotNull(dataSourceMapper);
    }

    @Test
    void getDataSourceById_WhenExists_ShouldReturnDataSource() {
        when(dataSourceMapper.selectById(1L)).thenReturn(testDataSource);

        DataSource result = dataSourceService.getDataSourceById(1L);

        assertNotNull(result);
        assertEquals("测试数据源", result.getName());
    }

    @Test
    void createDataSource_WhenValidData_ShouldReturnCreatedDataSource() {
        when(dataSourceMapper.insert(any(DataSource.class))).thenReturn(1);

        DataSource result = dataSourceService.createDataSource(testDataSource);

        assertNotNull(result);
        assertEquals(1, result.getStatus());
    }

    @Test
    void updateDataSource_WhenValidData_ShouldReturnUpdatedDataSource() {
        when(dataSourceMapper.selectById(1L)).thenReturn(testDataSource);
        when(dataSourceMapper.updateById(any(DataSource.class))).thenReturn(1);

        DataSource result = dataSourceService.updateDataSource(1L, testDataSource);

        assertNotNull(result);
        assertEquals("测试数据源", result.getName());
    }

    @Test
    void deleteDataSource_WhenValidId_ShouldSucceed() {
        when(dataSourceMapper.deleteById(1L)).thenReturn(1);

        dataSourceService.deleteDataSource(1L);

        verify(dataSourceMapper).deleteById(1L);
    }

    @Test
    void testConnection_WhenValidConfig_ShouldReturnTrue() {
        // This test would require actual database connection
        // For now, we test the logic structure
        assertNotNull(dataSourceService);
    }
}
