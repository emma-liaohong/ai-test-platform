package com.aitest.modules.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "Knowledge Base Search DTO")
public class KbSearchDTO implements Serializable {

    @Schema(description = "Search query string")
    private String query;

    @Schema(description = "Category ID filter")
    private Long categoryId;

    @Schema(description = "Associated system ID filter")
    private Long systemId;

    @Schema(description = "Maximum number of results", defaultValue = "20")
    private Integer limit;
}
