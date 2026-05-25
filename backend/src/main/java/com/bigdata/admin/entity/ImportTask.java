package com.bigdata.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Import Task Entity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("import_task")
public class ImportTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long collectionId;

    private String sourceType; // csv, json, excel

    private String sourceConfig; // JSON configuration

    private String status; // pending, running, completed, failed

    private Integer totalRecords;

    private Integer processedRecords;

    private Integer failedRecords;

    private String errorMessage;

    private Integer progress; // 0-100

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
