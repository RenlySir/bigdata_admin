package com.bigdata.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Data Record DTO with validation
 */
@Data
public class DataRecordDto {

    private Long id;

    @NotBlank(message = "数据类型不能为空")
    @Pattern(regexp = "json|text|binary|document", message = "数据类型必须是 json, text, binary 或 document")
    private String dataType;

    @NotBlank(message = "JSON数据不能为空", groups = {Create.class, Update.class})
    @Size(max = 10485760, message = "JSON数据不能超过10MB", groups = {Create.class, Update.class})
    private String jsonData;

    @Size(max = 1048576, message = "文本内容不能超过1MB")
    private String textContent;

    @Size(max = 1048576, message = "元数据不能超过1MB")
    private String metadata;

    public interface Create {}
    public interface Update {}
}
