package com.aitest.modules.llm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName(value = "llm_conversation", autoResultMap = true)
@Schema(description = "LLM Conversation")
public class LlmConversation implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "Conversation ID")
    private Long id;

    @Schema(description = "Unique session ID")
    private String sessionId;

    @Schema(description = "User ID")
    private Long userId;

    @Schema(description = "Conversation title")
    private String title;

    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "Context config (JSON)")
    private String context;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "Created time")
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "Updated time")
    private LocalDateTime updatedAt;
}
