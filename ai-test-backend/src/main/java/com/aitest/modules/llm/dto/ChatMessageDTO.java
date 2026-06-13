package com.aitest.modules.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "Chat message request")
public class ChatMessageDTO implements Serializable {

    @Schema(description = "Conversation ID (optional, creates new conversation if null)")
    private Long conversationId;

    @Schema(description = "Message content", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "Session ID (optional)")
    private String sessionId;
}
