package com.aitest.modules.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "System Config DTO")
public class SysConfigDTO {

    @NotBlank(message = "Config key cannot be empty")
    @Schema(description = "配置键")
    private String configKey;

    @Schema(description = "配置值")
    private String configValue;

    @Schema(description = "值类型: STRING/JSON/NUMBER/BOOLEAN")
    private String configType;

    @Schema(description = "分类: LLM/NOTIFICATION/PLAYWRIGHT/STORAGE/SECURITY")
    private String category;

    @Schema(description = "描述")
    private String description;
}
