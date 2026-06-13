package com.aitest.modules.case_suite.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * DTO for executing a test suite
 */
@Data
@Schema(description = "Execute Request DTO")
public class ExecuteRequestDTO implements Serializable {

    @NotNull(message = "Suite ID is required")
    @Schema(description = "Suite ID to execute")
    private Long suiteId;

    @Schema(description = "Environment configuration (JSON string)")
    private String envConfig;

    @Schema(description = "Trigger type: MANUAL, SCHEDULE, CI")
    private String triggerType;
}
