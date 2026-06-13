package com.aitest.modules.defect.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName(value = "defect", autoResultMap = true)
@Schema(description = "Defect")
public class Defect implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "Defect ID")
    private Long id;

    @Schema(description = "Defect code (auto-generated)")
    private String defectCode;

    @Schema(description = "Defect title")
    private String defectTitle;

    @Schema(description = "System ID")
    private Long systemId;

    @Schema(description = "Severity: S1/S2/S3/S4")
    private String severity;

    @Schema(description = "Priority: P0/P1/P2/P3")
    private String priority;

    @Schema(description = "Status: OPEN/FIXED/VERIFIED/CLOSED/REOPENED")
    private String status;

    @Schema(description = "Related test case ID")
    private Long relatedCaseId;

    @Schema(description = "Related execution ID")
    private Long relatedExecutionId;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Steps to reproduce")
    private String stepsToReproduce;

    @Schema(description = "Expected result")
    private String expectedResult;

    @Schema(description = "Actual result")
    private String actualResult;

    @Schema(description = "Environment info")
    private String environment;

    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "Screenshot URLs (JSON array)")
    private String screenshots;

    @Schema(description = "Assigned to user ID")
    private Long assignedTo;

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
