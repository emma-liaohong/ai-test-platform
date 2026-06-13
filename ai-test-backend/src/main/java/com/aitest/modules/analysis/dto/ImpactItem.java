package com.aitest.modules.analysis.dto;

import lombok.Data;

import java.util.List;

@Data
public class ImpactItem {

    private String affectedModule;

    /** HIGH / MEDIUM / LOW */
    private String impactLevel;

    private String description;

    private List<Long> affectedCaseIds;

    private String suggestion;
}
