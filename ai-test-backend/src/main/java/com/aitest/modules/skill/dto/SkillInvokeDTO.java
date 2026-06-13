package com.aitest.modules.skill.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
@Schema(description = "Skill Invoke DTO")
public class SkillInvokeDTO implements Serializable {

    @Schema(description = "Skill ID (provide either skillId or skillCode)")
    private Long skillId;

    @Schema(description = "Skill code (provide either skillId or skillCode)")
    private String skillCode;

    @Schema(description = "Input parameters")
    private Map<String, Object> inputParams;
}
