package com.aitest.modules.skill.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "Experience Query DTO")
public class ExperienceQueryDTO implements Serializable {

    @Schema(description = "Page number", defaultValue = "1")
    private int page = 1;

    @Schema(description = "Page size", defaultValue = "10")
    private int size = 10;

    @Schema(description = "Keyword to search title and content")
    private String keyword;

    @Schema(description = "Filter by experience type: SUCCESS_CASE/FAILED_CASE/PATTERN/INSIGHT")
    private String experienceType;

    @Schema(description = "Filter by promoted status")
    private Boolean isPromoted;
}
