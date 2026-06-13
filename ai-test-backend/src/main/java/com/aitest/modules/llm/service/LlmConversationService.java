package com.aitest.modules.llm.service;

import com.aitest.modules.llm.dto.ConversationDTO;
import com.aitest.modules.llm.entity.LlmConversation;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface LlmConversationService extends IService<LlmConversation> {

    /**
     * List conversations for a user, enriched with lastMessage and messageCount.
     */
    List<Map<String, Object>> listConversations(Long userId);

    /**
     * Create a new conversation.
     */
    LlmConversation createConversation(ConversationDTO dto, Long userId);

    /**
     * Get a conversation by ID.
     */
    LlmConversation getConversation(Long id);

    /**
     * Delete a conversation and all its messages.
     */
    void deleteConversation(Long id);

    /**
     * Update conversation title.
     */
    void updateTitle(Long id, String title);
}
