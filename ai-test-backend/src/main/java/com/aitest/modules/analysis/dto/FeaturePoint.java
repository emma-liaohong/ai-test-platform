package com.aitest.modules.analysis.dto;

import lombok.Data;

import java.util.List;

@Data
public class FeaturePoint {

    private String featureName;

    private String description;

    /** NEW / MODIFIED / REMOVED */
    private String featureType;

    private List<String> businessRules;

    private List<String> relatedModules;

    /** Suggested priority: P0 / P1 / P2 / P3 */
    private String priority;
}
