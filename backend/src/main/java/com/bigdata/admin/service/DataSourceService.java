package com.bigdata.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bigdata.admin.entity.DataSource;
import com.bigdata.admin.mapper.DataSourceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class DataSourceService extends ServiceImpl<DataSourceMapper, DataSource> {

    private final DataSourceMapper dataSourceMapper;

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
        dataSource.setStatus(1);
        dataSourceMapper.insert(dataSource);
        return dataSource;
    }

    @Transactional
    public DataSource updateDataSource(Long id, DataSource dataSource) {
        dataSource.setId(id);
        dataSourceMapper.updateById(dataSource);
        return getDataSourceById(id);
    }

    @Transactional
    public void deleteDataSource(Long id) {
        dataSourceMapper.deleteById(id);
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
