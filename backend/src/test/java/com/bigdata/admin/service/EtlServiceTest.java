package com.bigdata.admin.service;

import com.bigdata.admin.dto.EtlExecutionDto;
import com.bigdata.admin.dto.EtlTransformationDto;
import com.bigdata.admin.entity.DataCollection;
import com.bigdata.admin.entity.DataRecord;
import com.bigdata.admin.entity.EtlExecution;
import com.bigdata.admin.entity.EtlTransformation;
import com.bigdata.admin.mapper.DataCollectionMapper;
import com.bigdata.admin.mapper.DataRecordMapper;
import com.bigdata.admin.mapper.EtlExecutionMapper;
import com.bigdata.admin.mapper.EtlTransformationMapper;
import com.bigdata.admin.exception.ResourceNotFoundException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EtlService
 */
@ExtendWith(MockitoExtension.class)
class EtlServiceTest {

    @Mock
    private EtlTransformationMapper transformationMapper;

    @Mock
    private EtlExecutionMapper executionMapper;

    @Mock
    private DataCollectionMapper collectionMapper;

    @Mock
    private DataRecordMapper recordMapper;

    private ObjectMapper objectMapper;

    private EtlService etlService;

    private EtlTransformation testTransformation;
    private DataCollection testCollection;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        etlService = new EtlService(transformationMapper, executionMapper, collectionMapper, recordMapper, objectMapper);

        testCollection = new DataCollection();
        testCollection.setId(1L);
        testCollection.setName("测试集合");
        testCollection.setStatus(1);

        testTransformation = new EtlTransformation();
        testTransformation.setId(1L);
        testTransformation.setName("测试转换");
        testTransformation.setDescription("测试描述");
        testTransformation.setSourceCollectionId(1L);
        testTransformation.setTargetCollectionId(2L);
        testTransformation.setTransformationType("mapping");
        testTransformation.setTransformationRules("{\"fieldMapping\":{\"source\":\"target\"}}");
        testTransformation.setStatus("active");
        testTransformation.setTotalExecutions(0);
        testTransformation.setSuccessExecutions(0);
        testTransformation.setFailureExecutions(0);
    }

    @Test
    void getTransformations_WhenValidParams_ShouldReturnPage() {
        Page<EtlTransformation> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(testTransformation));
        mockPage.setTotal(1);

        when(transformationMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        Page<EtlTransformationDto> result = etlService.getTransformations(1, 10, null);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        assertEquals("测试转换", result.getRecords().get(0).getName());
    }

    @Test
    void getTransformations_WhenInvalidPageNumber_ShouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            etlService.getTransformations(0, 10, null);
        });

        assertTrue(exception.getMessage().contains("Page number must be >= 1"));
    }

    @Test
    void getTransformationById_WhenExists_ShouldReturnTransformation() {
        when(transformationMapper.selectById(1L)).thenReturn(testTransformation);

        EtlTransformationDto result = etlService.getTransformationById(1L);

        assertNotNull(result);
        assertEquals("测试转换", result.getName());
    }

    @Test
    void getTransformationById_WhenNotExists_ShouldThrowException() {
        when(transformationMapper.selectById(999L)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> {
            etlService.getTransformationById(999L);
        });
    }

    @Test
    void createTransformation_WhenValidData_ShouldSucceed() {
        when(collectionMapper.selectById(1L)).thenReturn(testCollection);
        when(collectionMapper.selectById(2L)).thenReturn(testCollection);
        when(transformationMapper.insert(any(EtlTransformation.class))).thenReturn(1);

        EtlTransformationDto dto = new EtlTransformationDto();
        dto.setName("新转换");
        dto.setSourceCollectionId(1L);
        dto.setTargetCollectionId(2L);
        dto.setTransformationType("mapping");
        dto.setTransformationRules("{\"fieldMapping\":{}}");

        EtlTransformationDto result = etlService.createTransformation(dto);

        assertNotNull(result);
        assertEquals("新转换", result.getName());
    }

    @Test
    void createTransformation_WhenNameTooLong_ShouldThrowException() {
        when(collectionMapper.selectById(1L)).thenReturn(testCollection);

        char[] longName = new char[101];
        Arrays.fill(longName, 'A');

        EtlTransformationDto dto = new EtlTransformationDto();
        dto.setName(new String(longName));
        dto.setSourceCollectionId(1L);
        dto.setTransformationType("mapping");

        assertThrows(IllegalArgumentException.class, () -> {
            etlService.createTransformation(dto);
        });
    }

    @Test
    void createTransformation_WhenInvalidType_ShouldThrowException() {
        when(collectionMapper.selectById(1L)).thenReturn(testCollection);

        EtlTransformationDto dto = new EtlTransformationDto();
        dto.setName("测试转换");
        dto.setSourceCollectionId(1L);
        dto.setTransformationType("invalid_type");

        assertThrows(IllegalArgumentException.class, () -> {
            etlService.createTransformation(dto);
        });
    }

    @Test
    void updateTransformation_WhenValidData_ShouldSucceed() {
        when(transformationMapper.selectById(1L)).thenReturn(testTransformation);
        when(collectionMapper.selectById(1L)).thenReturn(testCollection);
        when(transformationMapper.updateById(any(EtlTransformation.class))).thenReturn(1);

        EtlTransformationDto dto = new EtlTransformationDto();
        dto.setName("更新转换");
        dto.setSourceCollectionId(1L);
        dto.setTransformationType("mapping");

        EtlTransformationDto result = etlService.updateTransformation(1L, dto);

        assertNotNull(result);
        assertEquals("更新转换", result.getName());
    }

    @Test
    void deleteTransformation_WhenValidId_ShouldSucceed() {
        when(transformationMapper.deleteById(1L)).thenReturn(1);

        etlService.deleteTransformation(1L);

        verify(transformationMapper).deleteById(1L);
    }

    @Test
    void getExecutions_WhenValidParams_ShouldReturnPage() {
        EtlExecution execution = new EtlExecution();
        execution.setId(1L);
        execution.setTransformationId(1L);
        execution.setStatus("completed");

        Page<EtlExecution> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(execution));
        mockPage.setTotal(1);

        when(executionMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);
        when(transformationMapper.selectBatchIds(any())).thenReturn(Arrays.asList(testTransformation));

        Page<EtlExecutionDto> result = etlService.getExecutions(1L, 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
    }
}
