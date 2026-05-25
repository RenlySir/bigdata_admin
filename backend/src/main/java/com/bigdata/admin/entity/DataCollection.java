package com.bigdata.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("data_collection")
public class DataCollection extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private Long dataSourceId;

    private String schemaDefinition;

    private Long recordCount;

    private Long sizeInBytes;

    private String tags;

    private Integer status;
}
