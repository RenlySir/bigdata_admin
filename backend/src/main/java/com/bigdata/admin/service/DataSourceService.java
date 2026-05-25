package com.bigdata.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bigdata.admin.entity.DataSource;
import com.bigdata.admin.mapper.DataSourceMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DataSourceService extends ServiceImpl<DataSourceMapper, DataSource> {

    private static final Logger log = LoggerFactory.getLogger(DataSourceService.class);

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "tidb", "mysql", "postgresql", "mongodb", "kafka", "api", "file"
    );

    private final DataSourceMapper dataSourceMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DataSourceService(DataSourceMapper dataSourceMapper) {
        this.dataSourceMapper = dataSourceMapper;
    }

    public Page<DataSource> getDataSources(int page, int size, String type) {
        Page<DataSource> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<DataSource> wrapper = new LambdaQueryWrapper<>();
        if (type != null && !type.isEmpty()) {
            wrapper.eq(DataSource::getType, type);
        }
        wrapper.orderByDesc(DataSource::getCreatedAt);
        return dataSourceMapper.selectPage(pageParam, wrapper);
    }

    public DataSource getDataSourceById(Long id) {
        return dataSourceMapper.selectById(id);
    }

    @Transactional
    public DataSource createDataSource(DataSource dataSource) {
        validateDataSource(dataSource);
        dataSource.setType(dataSource.getType().trim().toLowerCase());
        dataSource.setStatus(1);
        dataSourceMapper.insert(dataSource);
        return dataSource;
    }

    @Transactional
    public DataSource updateDataSource(Long id, DataSource dataSource) {
        validateDataSource(dataSource);
        dataSource.setId(id);
        dataSource.setType(dataSource.getType().trim().toLowerCase());
        dataSourceMapper.updateById(dataSource);
        return getDataSourceById(id);
    }

    @Transactional
    public void deleteDataSource(Long id) {
        dataSourceMapper.deleteById(id);
    }

    private void validateDataSource(DataSource dataSource) {
        if (dataSource == null) {
            throw new IllegalArgumentException("Data source cannot be null");
        }
        if (!StringUtils.hasText(dataSource.getName()) || dataSource.getName().length() > 100) {
            throw new IllegalArgumentException("Data source name is required and must not exceed 100 characters");
        }
        if (!StringUtils.hasText(dataSource.getType())) {
            throw new IllegalArgumentException("Data source type is required");
        }
        String type = dataSource.getType().trim().toLowerCase();
        if (!ALLOWED_TYPES.contains(type)) {
            throw new IllegalArgumentException("Unsupported data source type: " + dataSource.getType());
        }
        if (!StringUtils.hasText(dataSource.getConnectionConfig())) {
            throw new IllegalArgumentException("Connection configuration is required");
        }
        try {
            objectMapper.readTree(dataSource.getConnectionConfig());
        } catch (Exception e) {
            throw new IllegalArgumentException("Connection configuration must be valid JSON");
        }
    }

    public boolean testConnection(DataSource dataSource) {
        try {
            String config = dataSource.getConnectionConfig();
            log.info("Testing connection for data source: {}", dataSource.getName());
            return true;
        } catch (Exception e) {
            log.error("Connection test failed", e);
            return false;
        }
    }
}
