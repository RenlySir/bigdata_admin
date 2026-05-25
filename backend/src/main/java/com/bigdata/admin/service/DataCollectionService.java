package com.bigdata.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bigdata.admin.entity.DataCollection;
import com.bigdata.admin.entity.DataRecord;
import com.bigdata.admin.mapper.DataCollectionMapper;
import com.bigdata.admin.mapper.DataRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataCollectionService extends ServiceImpl<DataCollectionMapper, DataCollection> {

    private final DataCollectionMapper dataCollectionMapper;
    private final DataRecordMapper dataRecordMapper;
    private final ObjectMapper objectMapper;

    public Page<DataCollection> getCollections(int page, int size, String keyword) {
        Page<DataCollection> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<DataCollection> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(DataCollection::getName, keyword)
                   .or()
                   .like(DataCollection::getDescription, keyword);
        }
        wrapper.orderByDesc(DataCollection::getCreatedAt);
        return dataCollectionMapper.selectPage(pageParam, wrapper);
    }

    public DataCollection getCollectionById(Long id) {
        return dataCollectionMapper.selectById(id);
    }

    @Transactional
    public DataCollection createCollection(DataCollection collection) {
        collection.setRecordCount(0L);
        collection.setSizeInBytes(0L);
        collection.setStatus(1);
        dataCollectionMapper.insert(collection);
        return collection;
    }

    @Transactional
    public DataCollection updateCollection(Long id, DataCollection collection) {
        collection.setId(id);
        dataCollectionMapper.updateById(collection);
        return getCollectionById(id);
    }

    @Transactional
    public void deleteCollection(Long id) {
        dataCollectionMapper.deleteById(id);
        LambdaQueryWrapper<DataRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DataRecord::getCollectionId, id);
        dataRecordMapper.delete(wrapper);
    }

    public Long updateRecordCount(Long collectionId) {
        LambdaQueryWrapper<DataRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DataRecord::getCollectionId, collectionId);
        Long count = dataRecordMapper.selectCount(wrapper);
        DataCollection collection = new DataCollection();
        collection.setId(collectionId);
        collection.setRecordCount(count);
        dataCollectionMapper.updateById(collection);
        return count;
    }
}
