package com.aitest.modules.analysis.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GeneratedCase {

    private String caseName;

    /** PC / API */
    private String caseType;

    private String priority;

    private String preconditions;

    /** Each map contains: stepOrder, stepAction, stepTarget, stepValue, stepDescription, expectedResult */
    private List<Map<String, Object>> steps;

    private String expectedResult;

    /** Which feature point this case came from */
    private String sourceFeature;
}
