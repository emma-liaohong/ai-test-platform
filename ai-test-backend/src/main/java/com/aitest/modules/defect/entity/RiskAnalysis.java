package com.aitest.modules.defect.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName(value = "risk_analysis", autoResultMap = true)
@Schema(description = "Risk Analysis")
public class RiskAnalysis implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "Analysis ID")
    private Long id;

    @Schema(description = "System ID")
    private Long systemId;

    @Schema(description = "Analysis type: REGRESSION/IMPACT/COVERAGE")
    private String analysisType;

    @Schema(description = "Analysis title")
    private String title;

    @Schema(description = "Risk level: HIGH/MEDIUM/LOW")
    private String riskLevel;

    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "Risk items (JSON array)")
    private String riskItems;

    @Schema(description = "Analysis result text")
    private String analysisResult;

    @Schema(description = "Suggestions")
    private String suggestions;

    @Schema(description = "AI analysis content (markdown)")
    private String aiAnalysis;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "Created by user ID")
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "Created time")
    private LocalDateTime createdAt;
}
