package com.aitest.modules.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "Knowledge Category Create/Update DTO")
public class KbCategoryDTO implements Serializable {

    @NotBlank(message = "Category name is required")
    @Schema(description = "Category name")
    private String categoryName;

    @NotBlank(message = "Category type is required")
    @Schema(description = "Category type: CASE/REQUIREMENT/DESIGN/GIT/DEFECT/DATABASE/VIDEO")
    private String categoryType;

    @Schema(description = "Associated system ID")
    private Long systemId;

    @Schema(description = "Parent category ID, 0 for root")
    private Long parentId;

    @Schema(description = "Sort order")
    private Integer sortOrder;
}
