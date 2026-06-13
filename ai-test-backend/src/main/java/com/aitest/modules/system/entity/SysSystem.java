package com.aitest.modules.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * System entity - represents an AI system under test
 */
@Data
@TableName("sys_system")
@Schema(description = "AI System")
public class SysSystem implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "System ID")
    private Long id;

    @Schema(description = "System name")
    private String name;

    @Schema(description = "System code (unique identifier)")
    private String code;

    @Schema(description = "System description")
    private String description;

    @Schema(description = "Base URL / API endpoint")
    private String baseUrl;

    @Schema(description = "System type: llm, agent, rag, etc.")
    private String type;

    @Schema(description = "Status: 0=disabled, 1=enabled")
    private Integer status;

    @Schema(description = "Creator ID")
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @Schema(description = "Create time")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "Updater ID")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    @Schema(description = "Update time")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "Deleted flag: 0=not deleted, 1=deleted")
    @TableLogic
    private Integer deleted;
}
