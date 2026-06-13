package com.aitest.modules.defect.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "Defect Create/Update DTO")
public class DefectDTO implements Serializable {

    @Schema(description = "Defect title", requiredMode = Schema.RequiredMode.REQUIRED)
    private String defectTitle;

    @Schema(description = "System ID", requiredMode = Schema.RequiredMode.REQUIRED)
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

    @Schema(description = "Screenshot URLs (JSON array string)")
    private String screenshots;

    @Schema(description = "Assigned to user ID")
    private Long assignedTo;
}
