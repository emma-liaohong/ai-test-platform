package com.aitest.modules.case_mgr.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * DTO for querying test cases with filters and pagination
 */
@Data
@Schema(description = "Test Case Query DTO")
public class TestCaseQueryDTO implements Serializable {

    @Schema(description = "Case type filter: PC, APP, API")
    private String caseType;

    @Schema(description = "Associated system ID filter")
    private Long systemId;

    @Schema(description = "Priority filter: P0, P1, P2, P3")
    private String priority;

    @Schema(description = "Status filter: draft, ready, deprecated")
    private String status;

    @Schema(description = "Keyword for searching case code or case name")
    private String keyword;

    @Schema(description = "Page number", defaultValue = "1")
    private Long page = 1L;

    @Schema(description = "Page size", defaultValue = "10")
    private Long size = 10L;
}
