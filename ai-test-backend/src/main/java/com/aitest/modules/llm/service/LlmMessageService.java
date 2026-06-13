package com.aitest.modules.llm.service;

import com.aitest.modules.llm.entity.LlmMessage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface LlmMessageService extends IService<LlmMessage> {

    /**
     * Get messages for a conversation, ordered by createdAt ascending.
     */
    List<LlmMessage> getMessages(Long conversationId);

    /**
     * Save a message and return it.
     */
    LlmMessage saveMessage(LlmMessage message);

    /**
     * Delete all messages for a conversation.
     */
    void deleteMessagesByConversationId(Long conversationId);
}
