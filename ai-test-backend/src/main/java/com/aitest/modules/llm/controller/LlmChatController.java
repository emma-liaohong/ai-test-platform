package com.aitest.modules.llm.controller;

import com.aitest.common.result.Result;
import com.aitest.modules.llm.dto.ChatMessageDTO;
import com.aitest.modules.llm.dto.ConversationDTO;
import com.aitest.modules.llm.dto.IntentProcessDTO;
import com.aitest.modules.llm.engine.LlmEngine;
import com.aitest.modules.llm.entity.LlmConversation;
import com.aitest.modules.llm.entity.LlmMessage;
import com.aitest.modules.llm.service.LlmConversationService;
import com.aitest.modules.llm.service.LlmMessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/llm")
@RequiredArgsConstructor
@Tag(name = "LLM 对话", description = "LLM Chat backend — conversations, messages, and intent processing")
public class LlmChatController {

    private final LlmConversationService conversationService;
    private final LlmMessageService messageService;
    private final LlmEngine llmEngine;
    private final ObjectMapper objectMapper;

    // ======================== Conversation endpoints ========================

    @GetMapping("/conversations")
    @Operation(summary = "List conversations for a user")
    public Result<List<Map<String, Object>>> listConversations(
            @Parameter(description = "User ID (default: 1)") @RequestParam(defaultValue = "1") Long userId) {
        return Result.success(conversationService.listConversations(userId));
    }

    @PostMapping("/conversations")
    @Operation(summary = "Create a new conversation")
    public Result<LlmConversation> createConversation(@RequestBody ConversationDTO dto) {
        // Default userId = 1
        return Result.success(conversationService.createConversation(dto, 1L));
    }

    @GetMapping("/conversations/{id}")
    @Operation(summary = "Get conversation with its messages")
    public Result<Map<String, Object>> getConversation(
            @Parameter(description = "Conversation ID") @PathVariable Long id) {
        LlmConversation conversation = conversationService.getConversation(id);
        List<LlmMessage> messages = messageService.getMessages(id);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("conversation", conversation);
        result.put("messages", messages);
        return Result.success(result);
    }

    @DeleteMapping("/conversations/{id}")
    @Operation(summary = "Delete conversation and its messages")
    public Result<Void> deleteConversation(
            @Parameter(description = "Conversation ID") @PathVariable Long id) {
        conversationService.deleteConversation(id);
        return Result.success();
    }

    @PutMapping("/conversations/{id}/title")
    @Operation(summary = "Update conversation title")
    public Result<Void> updateTitle(
            @Parameter(description = "Conversation ID") @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String title = body.get("title");
        conversationService.updateTitle(id, title);
        return Result.success();
    }

    // ======================== Chat endpoint ========================

    @PostMapping("/chat")
    @Operation(summary = "Send a message and get AI response (key endpoint)")
    public Result<Map<String, Object>> chat(@RequestBody ChatMessageDTO dto) {
        Long conversationId = dto.getConversationId();

        // Step 1: If conversationId is null, auto-create a conversation
        if (conversationId == null) {
            ConversationDTO convDto = new ConversationDTO();
            // Title = first 20 chars of content + "..."
            String content = dto.getContent();
            String title = content.length() > 20 ? content.substring(0, 20) + "..." : content;
            convDto.setTitle(title);
            LlmConversation newConv = conversationService.createConversation(convDto, 1L);
            conversationId = newConv.getId();
        } else {
            // Verify conversation exists
            conversationService.getConversation(conversationId);
        }

        // Step 2: Save user message
        LlmMessage userMsg = new LlmMessage();
        userMsg.setConversationId(conversationId);
        userMsg.setRole("user");
        userMsg.setContent(dto.getContent());
        userMsg.setCreatedAt(LocalDateTime.now());
        messageService.saveMessage(userMsg);

        // Step 3: Load conversation history (recent 20 messages)
        List<LlmMessage> allMessages = messageService.getMessages(conversationId);
        List<LlmMessage> history = allMessages.size() > 20
                ? allMessages.subList(allMessages.size() - 20, allMessages.size())
                : allMessages;

        // Step 4: Call LlmEngine
        LlmEngine.ChatResponse engineResponse = llmEngine.processMessage(
                dto.getContent(), conversationId, history);

        // Step 5: Save assistant message
        LlmMessage assistantMsg = new LlmMessage();
        assistantMsg.setConversationId(conversationId);
        assistantMsg.setRole(engineResponse.getRole());
        assistantMsg.setContent(engineResponse.getContent());
        assistantMsg.setSkillInvoked(engineResponse.getSkillInvoked());
        assistantMsg.setCreatedAt(LocalDateTime.now());

        // Serialize JSON fields
        try {
            if (engineResponse.getToolCalls() != null) {
                assistantMsg.setToolCalls(objectMapper.writeValueAsString(engineResponse.getToolCalls()));
            }
            if (engineResponse.getTokenUsage() != null) {
                assistantMsg.setTokenUsage(objectMapper.writeValueAsString(engineResponse.getTokenUsage()));
            }
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize JSON fields for assistant message: {}", e.getMessage());
        }

        messageService.saveMessage(assistantMsg);

        // Update conversation updatedAt
        LlmConversation conv = conversationService.getConversation(conversationId);
        conv.setUpdatedAt(LocalDateTime.now());
        conversationService.updateById(conv);

        // Step 6: Build response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("conversationId", conversationId);
        response.put("userMessage", userMsg);
        response.put("assistantMessage", assistantMsg);
        response.put("toolCalls", engineResponse.getToolCalls());
        response.put("skillInvoked", engineResponse.getSkillInvoked());
        response.put("tokenUsage", engineResponse.getTokenUsage());

        return Result.success(response);
    }

    // ======================== Intent endpoint ========================

    @PostMapping("/intent")
    @Operation(summary = "Process intent directly (without saving to conversation)")
    public Result<Map<String, Object>> processIntent(@RequestBody IntentProcessDTO dto) {
        // Process through engine without persisting
        LlmEngine.ChatResponse engineResponse = llmEngine.processMessage(
                dto.getQuery(), dto.getConversationId(), List.of());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("query", dto.getQuery());
        response.put("role", engineResponse.getRole());
        response.put("content", engineResponse.getContent());
        response.put("toolCalls", engineResponse.getToolCalls());
        response.put("skillInvoked", engineResponse.getSkillInvoked());
        response.put("tokenUsage", engineResponse.getTokenUsage());

        return Result.success(response);
    }
}
