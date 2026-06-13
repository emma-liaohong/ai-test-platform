package com.aitest.modules.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_config")
@Schema(description = "系统配置")
public class SysConfig implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "Config ID")
    private Long id;

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

    @Schema(description = "Create time")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "Update time")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
