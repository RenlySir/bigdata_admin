package com.bigdata.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Data Source DTO with validation
 */
@Data
public class DataSourceDto {

    private Long id;

    @NotBlank(message = "数据源名称不能为空")
    @Size(min = 1, max = 100, message = "数据源名称长度必须在1-100之间")
    private String name;

    @NotBlank(message = "数据源类型不能为空")
    @Pattern(regexp = "mysql|postgresql|mongodb|kafka|file|api", message = "不支持的数据源类型")
    private String type;

    @NotBlank(message = "连接配置不能为空")
    @Size(min = 10, max = 1048576, message = "连接配置长度必须在10-1MB之间")
    @Pattern(regexp = "^\\{.*\\}$", message = "连接配置必须是有效的JSON格式")
    private String connectionConfig;

    @Size(max = 500, message = "描述不能超过500字符")
    private String description;

    public interface Create {}
    public interface Update {}
}
