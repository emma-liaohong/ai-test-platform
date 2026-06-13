package com.aitest.modules.llm.engine;

import com.aitest.modules.llm.entity.LlmMessage;
import com.aitest.modules.skill.dto.SkillInvokeDTO;
import com.aitest.modules.skill.service.AiSkillService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmEngine {

    @Lazy
    private final AiSkillService aiSkillService;

    /**
     * Process a chat message and generate a response.
     * Steps:
     * 1. Try intent matching against SKILLs
     * 2. If match found, invoke the SKILL and format response
     * 3. If no match, generate a general conversational response
     */
    @SuppressWarnings("unchecked")
    public ChatResponse processMessage(String userMessage, Long conversationId, List<LlmMessage> history) {
        ChatResponse response = new ChatResponse();

        // Step 1: Try intent matching
        Map<String, Object> intentResult = aiSkillService.matchIntent(userMessage);

        boolean matched = intentResult != null
                && intentResult.get("matched") != null
                && (boolean) intentResult.get("matched");

        if (matched && intentResult.get("topMatch") != null) {
            // Extract top match details
            Map<String, Object> topMatch = (Map<String, Object>) intentResult.get("topMatch");
            String skillCode = (String) topMatch.get("skillCode");
            Long skillId = ((Number) topMatch.get("skillId")).longValue();
            double confidence = ((Number) topMatch.get("confidence")).doubleValue();

            // Build input params from user message
            Map<String, Object> inputParams = new LinkedHashMap<>();
            inputParams.put("userInput", userMessage);
            inputParams.put("conversationId", conversationId);
            inputParams.put("history", history.stream()
                    .map(m -> Map.of("role", m.getRole(), "content", m.getContent()))
                    .collect(Collectors.toList()));

            // Invoke SKILL
            SkillInvokeDTO invokeDTO = new SkillInvokeDTO();
            invokeDTO.setSkillId(skillId);
            invokeDTO.setInputParams(inputParams);

            Map<String, Object> skillResult = aiSkillService.invokeSkill(invokeDTO);

            response.setRole("assistant");
            response.setContent(formatSkillResponse(skillCode, skillResult, confidence));
            response.setSkillInvoked(skillCode);
            response.setToolCalls(buildToolCalls(skillCode, inputParams, skillResult));
            response.setTokenUsage(generateTokenUsage(userMessage, response.getContent()));
        } else {
            // No intent match — general conversation
            response.setRole("assistant");
            response.setContent(generateGeneralResponse(userMessage, history));
            response.setTokenUsage(generateTokenUsage(userMessage, response.getContent()));
        }

        return response;
    }

    @SuppressWarnings("unchecked")
    private String formatSkillResponse(String skillCode, Map<String, Object> skillResult, double confidence) {
        StringBuilder sb = new StringBuilder();
        sb.append("已为您调用 **").append(skillCode).append("** 技能（置信度: ")
                .append(String.format("%.0f%%", confidence * 100)).append("）\n\n");

        // Format based on executor type
        String executorType = (String) skillResult.getOrDefault("executorType", "LLM");
        switch (executorType) {
            case "LLM":
                sb.append("### AI 分析结果\n\n");
                sb.append(skillResult.getOrDefault("response", "处理完成")).append("\n\n");
                Map<String, Object> tokens = (Map<String, Object>) skillResult.getOrDefault("tokens", Map.of());
                sb.append("*模型: ").append(skillResult.getOrDefault("model", "unknown"))
                        .append(" | Token: ").append(tokens.getOrDefault("totalTokens", 0))
                        .append("*");
                break;
            case "CODE":
                sb.append("### 脚本执行结果\n\n");
                sb.append("```\n").append(skillResult.getOrDefault("stdout", "")).append("\n```\n");
                break;
            case "API":
                sb.append("### API 调用结果\n\n");
                sb.append("状态码: ").append(skillResult.getOrDefault("responseCode", "unknown")).append("\n\n");
                sb.append("```json\n").append(skillResult.getOrDefault("responseBody", "")).append("\n```");
                break;
            case "WORKFLOW":
                sb.append("### 工作流执行结果\n\n");
                List<Map<String, Object>> steps = (List<Map<String, Object>>) skillResult.getOrDefault("steps", List.of());
                for (Map<String, Object> step : steps) {
                    String status = "SUCCESS".equals(step.get("status").toString()) ? "✅" : "❌";
                    sb.append(status).append(" 步骤").append(step.get("step")).append(": ")
                            .append(step.get("action")).append(" — ").append(step.get("status")).append("\n");
                }
                sb.append("\n**最终输出**: ").append(skillResult.getOrDefault("finalOutput", ""));
                break;
            default:
                sb.append("### 执行结果\n\n");
                sb.append(skillResult.getOrDefault("response", "处理完成"));
                break;
        }
        return sb.toString();
    }

    private String generateGeneralResponse(String userMessage, List<LlmMessage> history) {
        // Simulated general conversation
        String msg = userMessage.toLowerCase();

        if (msg.contains("你好") || msg.contains("hi") || msg.contains("hello")) {
            return "你好！我是 AI 测试助手 🤖\n\n" +
                    "我可以帮助你：\n" +
                    "- 🧪 **生成测试案例** — 输入需求描述，自动生成测试用例\n" +
                    "- 🔍 **分析需求文档** — 上传文档，提取功能点\n" +
                    "- 🐛 **分析缺陷** — 描述问题，分析根因\n" +
                    "- 📊 **评估回归风险** — 分析代码变更的影响范围\n" +
                    "- 🎬 **执行测试** — 调用案例执行技能\n\n" +
                    "请告诉我你需要什么帮助？";
        } else if (msg.contains("帮助") || msg.contains("help") || msg.contains("功能")) {
            return "### 我的能力\n\n" +
                    "| 功能 | 触发方式 | 示例 |\n" +
                    "|------|---------|------|\n" +
                    "| 案例生成 | 描述需求 | \"为用户登录功能生成测试案例\" |\n" +
                    "| 需求分析 | 提到需求/文档 | \"分析这份需求文档\" |\n" +
                    "| 缺陷分析 | 描述Bug | \"支付超时问题怎么排查\" |\n" +
                    "| 回归评估 | 提到变更/回归 | \"评估这次代码变更的回归风险\" |\n" +
                    "| 测试执行 | 提到执行/运行 | \"执行登录冒烟测试\" |\n\n" +
                    "你也可以直接输入自然语言，我会自动匹配最合适的技能来处理！";
        } else if (msg.contains("案例") || msg.contains("测试") || msg.contains("用例")) {
            return "我来帮你处理测试相关的任务！\n\n" +
                    "根据你的描述，我建议使用 **案例生成** 技能。请提供更详细的需求描述，例如：\n\n" +
                    "1. **功能名称** — 例如：用户登录、订单支付\n" +
                    "2. **系统名称** — 例如：订单系统、用户系统\n" +
                    "3. **测试范围** — 例如：正常流程、异常场景、边界条件\n\n" +
                    "我会自动为你生成完整的测试案例集 📋";
        } else {
            return "收到你的消息。我正在分析你的意图...\n\n" +
                    "当前我匹配到了一些相关技能，但置信度不够高。你可以：\n\n" +
                    "1. **更具体地描述需求** — 包含功能名称和系统信息\n" +
                    "2. **直接指定技能** — 例如 \"调用案例生成技能\"\n" +
                    "3. **查看可用技能** — 输入 \"帮助\" 查看所有功能\n\n" +
                    "需要我为你做什么？";
        }
    }

    private List<Map<String, Object>> buildToolCalls(String skillCode, Map<String, Object> input, Map<String, Object> output) {
        Map<String, Object> toolCall = new LinkedHashMap<>();
        toolCall.put("id", "call_" + System.currentTimeMillis());
        toolCall.put("type", "function");
        toolCall.put("function", Map.of(
                "name", skillCode,
                "arguments", input,
                "result", output
        ));
        toolCall.put("status", "completed");
        toolCall.put("durationMs", output.getOrDefault("durationMs", 0));
        return List.of(toolCall);
    }

    private Map<String, Object> generateTokenUsage(String input, String output) {
        int promptTokens = input.length() + 200; // simulate system prompt overhead
        int completionTokens = output.length();
        return Map.of(
                "promptTokens", promptTokens,
                "completionTokens", completionTokens,
                "totalTokens", promptTokens + completionTokens
        );
    }

    /**
     * Inner class for chat response
     */
    @Data
    public static class ChatResponse {
        private String role;
        private String content;
        private String skillInvoked;
        private List<Map<String, Object>> toolCalls;
        private Map<String, Object> tokenUsage;
    }
}
