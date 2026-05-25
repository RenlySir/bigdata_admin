package com.bigdata.admin.service;

import com.bigdata.admin.entity.DataCollection;
import com.bigdata.admin.mapper.DataCollectionMapper;
import com.bigdata.admin.mapper.DataRecordMapper;
import com.bigdata.admin.exception.ResourceNotFoundException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DataCollectionService
 */
@ExtendWith(MockitoExtension.class)
class DataCollectionServiceTest {

    @Mock
    private DataCollectionMapper dataCollectionMapper;

    @Mock
    private DataRecordMapper dataRecordMapper;

    @InjectMocks
    private DataCollectionService dataCollectionService;

    private DataCollection testCollection;

    @BeforeEach
    void setUp() {
        testCollection = new DataCollection();
        testCollection.setId(1L);
        testCollection.setName("测试集合");
        testCollection.setDescription("测试描述");
        testCollection.setStatus(1);
        testCollection.setRecordCount(0L);
        testCollection.setSizeInBytes(0L);
    }

    @Test
    void getCollections_WhenValidParams_ShouldReturnPage() {
        Page<DataCollection> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(testCollection));
        mockPage.setTotal(1);

        when(dataCollectionMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
            .thenReturn(mockPage);

        Page<DataCollection> result = dataCollectionService.getCollections(1, 10, null);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        assertEquals("测试集合", result.getRecords().get(0).getName());
    }

    @Test
    void getCollections_WhenInvalidPageNumber_ShouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            dataCollectionService.getCollections(0, 10, null);
        });

        assertTrue(exception.getMessage().contains("Page number must be >= 1"));
    }

    @Test
    void getCollections_WhenInvalidPageSize_ShouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            dataCollectionService.getCollections(1, 101, null);
        });

        assertTrue(exception.getMessage().contains("Page size must be between 1 and"));
    }

    @Test
    void getCollectionById_WhenExists_ShouldReturnCollection() {
        when(dataCollectionMapper.selectById(1L)).thenReturn(testCollection);

        DataCollection result = dataCollectionService.getCollectionById(1L);

        assertNotNull(result);
        assertEquals("测试集合", result.getName());
    }

    @Test
    void getCollectionById_WhenNotExists_ShouldThrowException() {
        when(dataCollectionMapper.selectById(999L)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> {
            dataCollectionService.getCollectionById(999L);
        });
    }

    @Test
    void getCollectionById_WhenInvalidId_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            dataCollectionService.getCollectionById(0L);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            dataCollectionService.getCollectionById(null);
        });
    }

    @Test
    void createCollection_WhenValidData_ShouldSucceed() {
        when(dataCollectionMapper.insert(any(DataCollection.class))).thenReturn(1);

        DataCollection result = dataCollectionService.createCollection(testCollection);

        assertNotNull(result);
        assertEquals(0L, result.getRecordCount());
        assertEquals(0L, result.getSizeInBytes());
        assertEquals(1, result.getStatus());
    }

    @Test
    void createCollection_WhenNameTooLong_ShouldThrowException() {
        char[] longName = new char[101];
        Arrays.fill(longName, 'A');
        testCollection.setName(new String(longName));

        assertThrows(IllegalArgumentException.class, () -> {
            dataCollectionService.createCollection(testCollection);
        });
    }

    @Test
    void createCollection_WhenEmptyName_ShouldThrowException() {
        testCollection.setName("");

        assertThrows(IllegalArgumentException.class, () -> {
            dataCollectionService.createCollection(testCollection);
        });
    }

    @Test
    void updateCollection_WhenValidData_ShouldSucceed() {
        when(dataCollectionMapper.selectById(1L)).thenReturn(testCollection);
        when(dataCollectionMapper.updateById(any(DataCollection.class))).thenReturn(1);

        testCollection.setDescription("更新描述");
        DataCollection result = dataCollectionService.updateCollection(1L, testCollection);

        assertNotNull(result);
        assertEquals("更新描述", result.getDescription());
    }

    @Test
    void updateCollection_WhenInvalidId_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            dataCollectionService.updateCollection(0L, testCollection);
        });
    }

    @Test
    void deleteCollection_WhenValidId_ShouldSucceed() {
        when(dataCollectionMapper.deleteById(1L)).thenReturn(1);
        when(dataRecordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(dataRecordMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(0);

        dataCollectionService.deleteCollection(1L);

        verify(dataCollectionMapper).deleteById(1L);
    }

    @Test
    void updateRecordCount_WhenValidId_ShouldSucceed() {
        when(dataRecordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(100L);
        when(dataCollectionMapper.updateById(any(DataCollection.class))).thenReturn(1);

        Long count = dataCollectionService.updateRecordCount(1L);

        assertEquals(100L, count);
        verify(dataCollectionMapper).updateById(any(DataCollection.class));
    }
}
