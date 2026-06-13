package com.aitest.modules.case_suite.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for creating/updating a test suite
 */
@Data
@Schema(description = "Test Suite Create/Update DTO")
public class TestSuiteDTO implements Serializable {

    @NotBlank(message = "Suite name is required")
    @Schema(description = "Suite name")
    private String suiteName;

    @NotNull(message = "System ID is required")
    @Schema(description = "Associated system ID")
    private Long systemId;

    @Schema(description = "Suite description")
    private String description;

    @NotBlank(message = "Suite type is required")
    @Schema(description = "Suite type: MANUAL, SMART, REGRESSION, SMOKE")
    private String suiteType;

    @Schema(description = "List of case IDs to include in the suite")
    private List<Long> caseIds;
}
