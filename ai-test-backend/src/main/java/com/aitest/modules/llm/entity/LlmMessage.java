package com.aitest.modules.llm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName(value = "llm_message", autoResultMap = true)
@Schema(description = "LLM Message")
public class LlmMessage implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "Message ID")
    private Long id;

    @Schema(description = "Conversation ID")
    private Long conversationId;

    @Schema(description = "Role: user/assistant/system/tool")
    private String role;

    @Schema(description = "Message content")
    private String content;

    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "Tool call records (JSON)")
    private String toolCalls;

    @Schema(description = "Invoked skill code")
    private String skillInvoked;

    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "Token usage (JSON)")
    private String tokenUsage;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "Created time")
    private LocalDateTime createdAt;
}
