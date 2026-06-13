package com.aitest.modules.skill.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "AI Skill Create/Update DTO")
public class AiSkillDTO implements Serializable {

    @Schema(description = "Skill code (auto-generated if not provided)")
    private String skillCode;

    @Schema(description = "Skill name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String skillName;

    @Schema(description = "Skill type: BUILTIN/CUSTOM/LEARNED", requiredMode = Schema.RequiredMode.REQUIRED)
    private String skillType;

    @Schema(description = "Category: TESTING/ANALYSIS/GENERATION/EXECUTION")
    private String category;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Trigger intent config (JSON object)")
    private Object triggerIntent;

    @Schema(description = "Trigger keywords (JSON array)")
    private Object triggerKeywords;

    @Schema(description = "Input schema (JSON object)")
    private Object inputSchema;

    @Schema(description = "Output schema (JSON object)")
    private Object outputSchema;

    @Schema(description = "Executor type: LLM/CODE/API/WORKFLOW")
    private String executorType;

    @Schema(description = "Executor config (JSON object)")
    private Object executorConfig;

    @Schema(description = "Prompt template (for LLM executor)")
    private String promptTemplate;

    @Schema(description = "Script content (for CODE executor)")
    private String scriptContent;

    @Schema(description = "Status: ACTIVE/DISABLED/TESTING")
    private String status;
}
