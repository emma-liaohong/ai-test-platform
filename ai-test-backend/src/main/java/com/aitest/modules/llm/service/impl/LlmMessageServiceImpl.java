package com.aitest.modules.llm.service.impl;

import com.aitest.modules.llm.entity.LlmMessage;
import com.aitest.modules.llm.mapper.LlmMessageMapper;
import com.aitest.modules.llm.service.LlmMessageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmMessageServiceImpl extends ServiceImpl<LlmMessageMapper, LlmMessage>
        implements LlmMessageService {

    @Override
    public List<LlmMessage> getMessages(Long conversationId) {
        LambdaQueryWrapper<LlmMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LlmMessage::getConversationId, conversationId)
                .orderByAsc(LlmMessage::getCreatedAt);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LlmMessage saveMessage(LlmMessage message) {
        if (message.getCreatedAt() == null) {
            message.setCreatedAt(LocalDateTime.now());
        }
        save(message);
        return message;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMessagesByConversationId(Long conversationId) {
        LambdaQueryWrapper<LlmMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LlmMessage::getConversationId, conversationId);
        remove(wrapper);
        log.info("Deleted all messages for conversation: {}", conversationId);
    }
}
