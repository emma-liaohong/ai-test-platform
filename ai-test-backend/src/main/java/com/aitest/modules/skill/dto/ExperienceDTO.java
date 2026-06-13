package com.aitest.modules.skill.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "Experience Create/Update DTO")
public class ExperienceDTO implements Serializable {

    @Schema(description = "Experience type: SUCCESS_CASE/FAILED_CASE/PATTERN/INSIGHT", requiredMode = Schema.RequiredMode.REQUIRED)
    private String experienceType;

    @Schema(description = "Title")
    private String title;

    @Schema(description = "Content")
    private String content;

    @Schema(description = "Context (JSON object)")
    private Object context;

    @Schema(description = "Related skill ID")
    private Long relatedSkillId;

    @Schema(description = "Related case ID")
    private Long relatedCaseId;

    @Schema(description = "Tags (JSON array)")
    private Object tags;
}
