package com.aitest.modules.analysis.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AnalysisResultDTO {

    private Long documentId;

    private String documentName;

    private Long systemId;

    private String analysisType;

    private List<FeaturePoint> features;

    private List<ImpactItem> impactAnalysis;

    private List<GeneratedCase> generatedCases;

    /** IDs of actually created cases */
    private List<Long> createdCaseIds;

    /** Overall analysis summary */
    private String summary;

    /** Detailed AI analysis text (markdown) */
    private String aiAnalysis;

    private LocalDateTime analyzedAt;
}
