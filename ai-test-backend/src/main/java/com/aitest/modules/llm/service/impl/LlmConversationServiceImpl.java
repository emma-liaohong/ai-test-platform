package com.aitest.modules.llm.service.impl;

import com.aitest.common.exception.BusinessException;
import com.aitest.modules.llm.dto.ConversationDTO;
import com.aitest.modules.llm.entity.LlmConversation;
import com.aitest.modules.llm.entity.LlmMessage;
import com.aitest.modules.llm.mapper.LlmConversationMapper;
import com.aitest.modules.llm.mapper.LlmMessageMapper;
import com.aitest.modules.llm.service.LlmConversationService;
import com.aitest.modules.llm.service.LlmMessageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmConversationServiceImpl extends ServiceImpl<LlmConversationMapper, LlmConversation>
        implements LlmConversationService {

    private final LlmMessageService llmMessageService;
    private final LlmMessageMapper llmMessageMapper;

    @Override
    public List<Map<String, Object>> listConversations(Long userId) {
        LambdaQueryWrapper<LlmConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LlmConversation::getUserId, userId)
                .orderByDesc(LlmConversation::getUpdatedAt);
        List<LlmConversation> conversations = list(wrapper);

        // Enrich with lastMessage and messageCount
        List<Map<String, Object>> result = new ArrayList<>();
        for (LlmConversation conv : conversations) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", conv.getId());
            item.put("sessionId", conv.getSessionId());
            item.put("userId", conv.getUserId());
            item.put("title", conv.getTitle());
            item.put("context", conv.getContext());
            item.put("createdAt", conv.getCreatedAt());
            item.put("updatedAt", conv.getUpdatedAt());

            // Message count
            LambdaQueryWrapper<LlmMessage> countWrapper = new LambdaQueryWrapper<>();
            countWrapper.eq(LlmMessage::getConversationId, conv.getId());
            long messageCount = llmMessageMapper.selectCount(countWrapper);
            item.put("messageCount", messageCount);

            // Last message
            LambdaQueryWrapper<LlmMessage> lastMsgWrapper = new LambdaQueryWrapper<>();
            lastMsgWrapper.eq(LlmMessage::getConversationId, conv.getId())
                    .orderByDesc(LlmMessage::getCreatedAt)
                    .last("LIMIT 1");
            LlmMessage lastMsg = llmMessageMapper.selectOne(lastMsgWrapper);
            item.put("lastMessage", lastMsg != null ? lastMsg.getContent() : null);

            result.add(item);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LlmConversation createConversation(ConversationDTO dto, Long userId) {
        LlmConversation conv = new LlmConversation();
        conv.setSessionId("sess_" + UUID.randomUUID());
        conv.setUserId(userId);
        conv.setTitle(dto.getTitle());
        conv.setContext(dto.getContext());
        conv.setCreatedAt(LocalDateTime.now());
        conv.setUpdatedAt(LocalDateTime.now());
        save(conv);
        log.info("Created conversation: {} (session: {})", conv.getId(), conv.getSessionId());
        return conv;
    }

    @Override
    public LlmConversation getConversation(Long id) {
        LlmConversation conv = getById(id);
        if (conv == null) {
            throw new BusinessException(404, "Conversation not found: " + id);
        }
        return conv;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConversation(Long id) {
        LlmConversation conv = getConversation(id);
        // Delete all messages first
        llmMessageService.deleteMessagesByConversationId(id);
        // Delete conversation
        removeById(id);
        log.info("Deleted conversation: {} (session: {})", conv.getId(), conv.getSessionId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTitle(Long id, String title) {
        LlmConversation conv = getConversation(id);
        conv.setTitle(title);
        conv.setUpdatedAt(LocalDateTime.now());
        updateById(conv);
    }
}
