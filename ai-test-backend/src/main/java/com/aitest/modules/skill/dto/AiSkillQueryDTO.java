package com.aitest.modules.skill.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "Skill Query DTO")
public class AiSkillQueryDTO implements Serializable {

    @Schema(description = "Page number", defaultValue = "1")
    private int page = 1;

    @Schema(description = "Page size", defaultValue = "10")
    private int size = 10;

    @Schema(description = "Keyword to search skill_code and skill_name")
    private String keyword;

    @Schema(description = "Filter by skill type: BUILTIN/CUSTOM/LEARNED")
    private String skillType;

    @Schema(description = "Filter by category: TESTING/ANALYSIS/GENERATION/EXECUTION")
    private String category;

    @Schema(description = "Filter by executor type: LLM/CODE/API/WORKFLOW")
    private String executorType;

    @Schema(description = "Filter by status: ACTIVE/DISABLED/TESTING")
    private String status;
}
