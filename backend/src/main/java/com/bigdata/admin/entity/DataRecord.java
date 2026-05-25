package com.bigdata.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("data_record")
public class DataRecord extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long collectionId;

    private String dataType;

    private String jsonData;

    private String textContent;

    private String metadata;

    private Long version;

    private String checksum;
}
