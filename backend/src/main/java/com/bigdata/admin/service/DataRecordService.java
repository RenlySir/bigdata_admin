package com.bigdata.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bigdata.admin.entity.DataRecord;
import com.bigdata.admin.mapper.DataRecordMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class DataRecordService extends ServiceImpl<DataRecordMapper, DataRecord> {

    private static final Logger log = LoggerFactory.getLogger(DataRecordService.class);

    private final DataRecordMapper dataRecordMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public DataRecordService(DataRecordMapper dataRecordMapper, RedisTemplate<String, Object> redisTemplate) {
        this.dataRecordMapper = dataRecordMapper;
        this.redisTemplate = redisTemplate;
    }

    public Page<DataRecord> getRecords(Long collectionId, int page, int size, String keyword) {
        Page<DataRecord> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<DataRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DataRecord::getCollectionId, collectionId);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(DataRecord::getJsonData, keyword)
                   .or()
                   .like(DataRecord::getTextContent, keyword);
        }
        wrapper.orderByDesc(DataRecord::getCreatedAt);
        return dataRecordMapper.selectPage(pageParam, wrapper);
    }

    public DataRecord getRecordById(Long id) {
        return dataRecordMapper.selectById(id);
    }

    @Transactional
    public DataRecord createRecord(DataRecord record) {
        record.setVersion(1L);
        record.setChecksum(calculateChecksum(record.getJsonData()));
        dataRecordMapper.insert(record);
        return record;
    }

    @Transactional
    public DataRecord updateRecord(Long id, DataRecord record) {
        DataRecord existing = getRecordById(id);
        if (existing != null) {
            record.setId(id);
            record.setVersion(existing.getVersion() + 1);
            record.setChecksum(calculateChecksum(record.getJsonData()));
            dataRecordMapper.updateById(record);
            return getRecordById(id);
        }
        return null;
    }

    @Transactional
    public void deleteRecord(Long id) {
        dataRecordMapper.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchInsertRecords(List<DataRecord> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        // Pre-process checksums
        records.forEach(record -> {
            record.setVersion(1L);
            record.setChecksum(calculateChecksum(record.getJsonData()));
        });
        // Use MyBatis Plus batch insert for better performance
        this.saveBatch(records, 1000);
    }

    private String calculateChecksum(String data) {
        if (data == null || data.isEmpty()) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            // Use Hex format for better performance
            StringBuilder hexString = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("Error calculating checksum for data", e);
            return "";
        }
    }
}
