package com.aitest.modules.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "Knowledge Document Upload DTO")
public class KbDocumentDTO implements Serializable {

    @NotBlank(message = "Document name is required")
    @Schema(description = "Document name")
    private String docName;

    @NotNull(message = "Category ID is required")
    @Schema(description = "Category ID")
    private Long categoryId;

    @Schema(description = "Associated system ID")
    private Long systemId;

    @Schema(description = "Document type: PDF/WORD/MARKDOWN/EXCEL/IMAGE/VIDEO")
    private String docType;

    @Schema(description = "Description or remarks")
    private String description;
}
