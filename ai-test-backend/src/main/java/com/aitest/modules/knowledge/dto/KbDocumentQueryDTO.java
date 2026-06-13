package com.aitest.modules.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "Knowledge Document Query DTO")
public class KbDocumentQueryDTO implements Serializable {

    @Schema(description = "Page number", defaultValue = "1")
    private Long page = 1L;

    @Schema(description = "Page size", defaultValue = "10")
    private Long size = 10L;

    @Schema(description = "Keyword for searching document code or name")
    private String keyword;

    @Schema(description = "Category ID filter")
    private Long categoryId;

    @Schema(description = "Associated system ID filter")
    private Long systemId;

    @Schema(description = "Document type filter: PDF/WORD/MARKDOWN/EXCEL/IMAGE/VIDEO")
    private String docType;

    @Schema(description = "Parse status filter: PENDING/PARSING/DONE/FAILED")
    private String parseStatus;
}
