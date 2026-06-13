package com.aitest.modules.defect.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "Defect Query DTO")
public class DefectQueryDTO implements Serializable {

    @Schema(description = "Page number", defaultValue = "1")
    private int page = 1;

    @Schema(description = "Page size", defaultValue = "10")
    private int size = 10;

    @Schema(description = "Keyword to search defect_code and defect_title")
    private String keyword;

    @Schema(description = "Filter by system ID")
    private Long systemId;

    @Schema(description = "Filter by severity: S1/S2/S3/S4")
    private String severity;

    @Schema(description = "Filter by priority: P0/P1/P2/P3")
    private String priority;

    @Schema(description = "Filter by status: OPEN/FIXED/VERIFIED/CLOSED/REOPENED")
    private String status;
}
