package com.aitest.modules.knowledge.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName(value = "kb_chunk", autoResultMap = true)
@Schema(description = "Knowledge Base Chunk (document segment)")
public class KbChunk implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "Chunk ID")
    private Long id;

    @Schema(description = "Parent document ID")
    private Long documentId;

    @Schema(description = "Chunk index within the document")
    private Integer chunkIndex;

    @Schema(description = "Chunk text content")
    private String content;

    @Schema(description = "Content type: TEXT/TABLE/IMAGE/FLOWCHART")
    private String contentType;

    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "Embedding vector in JSON")
    private String embedding;

    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "Additional metadata in JSON")
    private String metadata;

    @Schema(description = "Created time")
    private LocalDateTime createdAt;
}
