package com.aitest.modules.analysis.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnalysisRequestDTO {

    @NotNull(message = "documentId cannot be null")
    private Long documentId;

    private Long systemId;

    /** FULL / FEATURE_ONLY / IMPACT_ONLY */
    private String analysisType;

    /** Whether to auto-generate test cases */
    private Boolean generateCases;

    /** Default priority for generated cases: P0 / P1 / P2 / P3 */
    private String casePriority;
}
