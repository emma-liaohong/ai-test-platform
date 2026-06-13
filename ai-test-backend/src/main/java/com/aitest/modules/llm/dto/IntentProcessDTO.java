package com.aitest.modules.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "Intent processing request")
public class IntentProcessDTO implements Serializable {

    @Schema(description = "User query", requiredMode = Schema.RequiredMode.REQUIRED)
    private String query;

    @Schema(description = "Conversation ID (optional)")
    private Long conversationId;
}
