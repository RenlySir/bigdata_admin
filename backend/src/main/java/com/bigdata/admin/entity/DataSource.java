package com.bigdata.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("data_source")
public class DataSource extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String type;

    private String connectionConfig;

    private String description;

    private Integer status;

    private Long createdBy;
}
