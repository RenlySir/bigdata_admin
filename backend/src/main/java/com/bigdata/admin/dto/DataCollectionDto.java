package com.bigdata.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Data Collection DTO with validation
 */
@Data
public class DataCollectionDto {

    private Long id;

    @NotBlank(message = "集合名称不能为空")
    @Size(min = 1, max = 100, message = "集合名称长度必须在1-100之间")
    @Pattern(regexp = "^[a-zA-Z0-9_\\-\\u4e00-\\u9fa5]+$", message = "集合名称只能包含字母、数字、下划线、连字符和中文")
    private String name;

    @Size(max = 500, message = "描述不能超过500字符")
    private String description;

    private Long dataSourceId;

    @Size(max = 1048576, message = "Schema定义不能超过1MB")
    private String schemaDefinition;

    @Size(max = 200, message = "标签不能超过200字符")
    @Pattern(regexp = "^[a-zA-Z0-9_\\-\\u4e00-\\u9fa5,\\s]*$", message = "标签格式不正确")
    private String tags;

    public interface Create {}
    public interface Update {}
}
