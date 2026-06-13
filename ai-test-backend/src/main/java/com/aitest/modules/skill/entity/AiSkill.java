package com.aitest.modules.skill.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName(value = "ai_skill", autoResultMap = true)
@Schema(description = "AI Skill")
public class AiSkill implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "Skill ID")
    private Long id;

    @Schema(description = "Skill code")
    private String skillCode;

    @Schema(description = "Skill name")
    private String skillName;

    @Schema(description = "Skill type: BUILTIN/CUSTOM/LEARNED")
    private String skillType;

    @Schema(description = "Category: TESTING/ANALYSIS/GENERATION/EXECUTION")
    private String category;

    @Schema(description = "Description")
    private String description;

    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "Trigger intent config (JSON)")
    private String triggerIntent;

    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "Trigger keywords (JSON array)")
    private String triggerKeywords;

    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "Input schema (JSON)")
    private String inputSchema;

    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "Output schema (JSON)")
    private String outputSchema;

    @Schema(description = "Executor type: LLM/CODE/API/WORKFLOW")
    private String executorType;

    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "Executor config (JSON)")
    private String executorConfig;

    @Schema(description = "Prompt template")
    private String promptTemplate;

    @Schema(description = "Script content")
    private String scriptContent;

    @Schema(description = "Source type: MANUAL/LEARNED/EVOLVED")
    private String sourceType;

    @Schema(description = "Learned from skill/experience ID")
    private Long learnedFrom;

    @Schema(description = "Version")
    private Integer version;

    @Schema(description = "Confidence score")
    private BigDecimal confidence;

    @Schema(description = "Usage count")
    private Integer usageCount;

    @Schema(description = "Success count")
    private Integer successCount;

    @Schema(description = "Status: ACTIVE/DISABLED/TESTING")
    private String status;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "Created by user ID")
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "Created time")
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "Updated time")
    private LocalDateTime updatedAt;
}
