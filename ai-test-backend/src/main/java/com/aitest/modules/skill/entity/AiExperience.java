package com.aitest.modules.skill.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName(value = "ai_experience", autoResultMap = true)
@Schema(description = "AI Experience")
public class AiExperience implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "Experience ID")
    private Long id;

    @Schema(description = "Experience type: SUCCESS_CASE/FAILED_CASE/PATTERN/INSIGHT")
    private String experienceType;

    @Schema(description = "Title")
    private String title;

    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "Context (JSON)")
    private String context;

    @Schema(description = "Content")
    private String content;

    @Schema(description = "Related skill ID")
    private Long relatedSkillId;

    @Schema(description = "Related case ID")
    private Long relatedCaseId;

    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "Tags (JSON array)")
    private String tags;

    @Schema(description = "Whether promoted to skill: 0=no, 1=yes")
    private Integer isPromoted;

    @Schema(description = "Promoted skill ID")
    private Long promotedSkillId;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "Created by user ID")
    private Long createdBy;

    @Schema(description = "Created time")
    private LocalDateTime createdAt;
}
