package com.aitest.modules.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "Conversation create/update request")
public class ConversationDTO implements Serializable {

    @Schema(description = "Conversation title")
    private String title;

    @Schema(description = "Context config (JSON string)")
    private String context;
}
