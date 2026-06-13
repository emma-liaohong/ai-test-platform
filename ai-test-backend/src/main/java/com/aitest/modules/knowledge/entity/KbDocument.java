package com.aitest.modules.knowledge.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName(value = "kb_document", autoResultMap = true)
@Schema(description = "Knowledge Base Document")
public class KbDocument implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "Document ID")
    private Long id;

    @Schema(description = "Document code (unique)")
    private String docCode;

    @Schema(description = "Document name")
    private String docName;

    @Schema(description = "Category ID")
    private Long categoryId;

    @Schema(description = "Associated system ID")
    private Long systemId;

    @Schema(description = "Document type: PDF/WORD/MARKDOWN/EXCEL/IMAGE/VIDEO")
    private String docType;

    @Schema(description = "File storage path")
    private String filePath;

    @Schema(description = "File size in bytes")
    private Long fileSize;

    @Schema(description = "Text content extracted from document")
    private String content;

    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "Content vector for similarity search")
    private String contentVector;

    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "Additional metadata in JSON")
    private String metadata;

    @Schema(description = "Parse status: PENDING/PARSING/DONE/FAILED")
    private String parseStatus;

    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "Parse result in JSON")
    private String parseResult;

    @Schema(description = "Created by user ID")
    private Long createdBy;

    @Schema(description = "Created time")
    private LocalDateTime createdAt;

    @Schema(description = "Updated time")
    private LocalDateTime updatedAt;
}
