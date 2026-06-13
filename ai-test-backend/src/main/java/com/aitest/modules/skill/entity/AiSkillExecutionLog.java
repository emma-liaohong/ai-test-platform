package com.aitest.modules.skill.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName(value = "ai_skill_execution_log", autoResultMap = true)
@Schema(description = "Skill Execution Log")
public class AiSkillExecutionLog implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "Log ID")
    private Long id;

    @Schema(description = "Skill ID")
    private Long skillId;

    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "Input parameters (JSON)")
    private String inputParams;

    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "Output result (JSON)")
    private String outputResult;

    @Schema(description = "Status: SUCCESS/FAILED")
    private String status;

    @Schema(description = "Execution duration in milliseconds")
    private Long durationMs;

    @Schema(description = "Error message")
    private String errorMessage;

    @Schema(description = "User feedback: GOOD/BAD/NONE")
    private String userFeedback;

    @Schema(description = "Created time")
    private LocalDateTime createdAt;
}
