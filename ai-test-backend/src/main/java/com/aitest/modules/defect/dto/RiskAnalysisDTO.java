package com.aitest.modules.defect.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "Risk Analysis Create DTO")
public class RiskAnalysisDTO implements Serializable {

    @Schema(description = "System ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long systemId;

    @Schema(description = "Analysis type: REGRESSION/IMPACT/COVERAGE", requiredMode = Schema.RequiredMode.REQUIRED)
    private String analysisType;

    @Schema(description = "Analysis title", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;
}
