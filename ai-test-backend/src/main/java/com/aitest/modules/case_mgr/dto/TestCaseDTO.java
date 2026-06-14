package com.aitest.modules.case_mgr.dto;

import com.aitest.modules.case_mgr.entity.TestCaseStep;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for creating/updating a test case
 */
@Data
@Schema(description = "Test Case Create/Update DTO")
public class TestCaseDTO implements Serializable {

    @NotBlank(message = "Case code is required")
    @Schema(description = "Case code (unique identifier)")
    private String caseCode;

    @NotBlank(message = "Case name is required")
    @Schema(description = "Case name")
    private String caseName;

    @NotBlank(message = "Case type is required")
    @Schema(description = "Case type: PC, APP, API")
    private String caseType;

    @NotNull(message = "System ID is required")
    @Schema(description = "Associated system ID")
    private Long systemId;

    @Schema(description = "Module path")
    private String modulePath;

    @Schema(description = "Priority: P0, P1, P2, P3")
    private String priority;

    @Schema(description = "Case level: smoke, regression, full")
    private String caseLevel;

    @Schema(description = "Recorded session ID")
    private Long recordSessionId;

    @Schema(description = "Recorded steps (JSON string)")
    private String recordedSteps;

    @Schema(description = "Natural language steps")
    private String naturalLanguageSteps;

    @Schema(description = "Generated Playwright script")
    private String playwrightScript;

    @Schema(description = "App operations (JSON string)")
    private String appOperations;

    @Schema(description = "API URL")
    private String apiUrl;

    @Schema(description = "API method: GET, POST, PUT, DELETE")
    private String apiMethod;

    @Schema(description = "API request schema (JSON string)")
    private String apiRequestSchema;

    @Schema(description = "API response schema (JSON string)")
    private String apiResponseSchema;

    @Schema(description = "API headers (JSON string)")
    private String apiHeaders;

    @Schema(description = "Preconditions for test execution")
    private String preconditions;

    @Schema(description = "Expected result")
    private String expectedResult;

    @Schema(description = "Tags (comma-separated)")
    private String tags;

    @Schema(description = "Status: draft, ready, deprecated")
    private String status;

    @Schema(description = "Whether data-driven: 0=no, 1=yes")
    private Integer isDataDriven;

    @Schema(description = "Associated data table ID")
    private Long dataTableId;

    @Schema(description = "List of test case steps")
    private List<TestCaseStep> steps;
}
