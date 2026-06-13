package com.aitest.modules.defect.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "Risk Analysis Query DTO")
public class RiskAnalysisQueryDTO implements Serializable {

    @Schema(description = "Page number", defaultValue = "1")
    private int page = 1;

    @Schema(description = "Page size", defaultValue = "10")
    private int size = 10;

    @Schema(description = "Filter by system ID")
    private Long systemId;

    @Schema(description = "Filter by analysis type: REGRESSION/IMPACT/COVERAGE")
    private String analysisType;
}
