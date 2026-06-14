package com.aitest.modules.llm.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.aitest.modules.system.service.SysConfigService;

/**
 * LLM Service - calls OpenAI-compatible API (DashScope / Alibaba Bailian)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmService {

    private final SysConfigService sysConfigService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Call LLM with a system prompt and user message, return the response text.
     */
    public String chat(String systemPrompt, String userMessage) {
        String apiKey = sysConfigService.getConfigValue("llm.api_key", "");
        String baseUrl = sysConfigService.getConfigValue("llm.api_base_url", "https://dashscope.aliyuncs.com");
        String model = sysConfigService.getConfigValue("llm.default_model", "qwen-max");
        String temperature = sysConfigService.getConfigValue("llm.temperature", "0.7");

        if (apiKey.isEmpty()) {
            log.warn("LLM API key not configured, returning mock response");
            return "[LLM未配置API Key，请在系统设置中配置 llm.api_key]";
        }

        // Build OpenAI-compatible request body
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", model);
        requestBody.put("temperature", Double.parseDouble(temperature));

        ArrayNode messages = requestBody.putArray("messages");

        // System message
        ObjectNode systemMsg = messages.addObject();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);

        // User message
        ObjectNode userMsg = messages.addObject();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);

        // Call API
        String url = baseUrl + "/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        try {
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode choices = root.get("choices");
                if (choices != null && choices.isArray() && choices.size() > 0) {
                    String content = choices.get(0).get("message").get("content").asText();
                    log.info("LLM response received (model={}, tokens={})", model,
                            root.has("usage") ? root.get("usage").get("total_tokens").asInt() : "?");
                    return content;
                }
            }
            log.error("LLM API returned unexpected response: {}", response.getBody());
            return "[LLM响应格式异常]";
        } catch (Exception e) {
            log.error("LLM API call failed: {}", e.getMessage());
            return "[LLM调用失败: " + e.getMessage() + "]";
        }
    }

    /**
     * Generate Playwright TypeScript code from natural language test steps.
     */
    public String generatePlaywrightCode(String caseName, String naturalLanguageSteps, String preconditions) {
        String systemPrompt = """
                你是一个 Playwright 自动化测试专家。根据用户提供的自然语言测试步骤，生成完整的 Playwright TypeScript 测试代码。

                这是一个 AI 测试平台，前端使用 Vue 3 + Element Plus 组件库。

                关键定位规则（必须遵守）：
                1. Element Plus 输入框用 placeholder 定位：page.getByPlaceholder('请输入用户名')
                2. 按钮用文字定位：page.getByRole('button', { name: /登录/ })
                3. 链接/菜单用文字：page.getByText('工作台')
                4. 绝对不要用 input[name="..."] 或 #id 选择器，因为 Element Plus 组件不生成这些属性
                5. 使用 getByPlaceholder、getByRole、getByText、getByLabel 等语义化定位器

                登录固定代码：
                await page.goto('/login');
                await page.getByPlaceholder('请输入用户名').fill('admin');
                await page.getByPlaceholder('请输入密码').fill('admin123');
                await page.getByRole('button', { name: /登\\s*录/ }).click();
                await page.waitForURL(/\\/dashboard/);

                要求：
                1. 使用 @playwright/test 框架
                2. 必须是完整可运行的 .spec.ts 文件
                3. 使用 test.describe 和 test() 结构
                4. 适当使用 expect() 进行断言
                5. 只输出代码，不要输出解释文字
                6. 代码必须用 ```typescript 和 ``` 包裹
                """;

        String userMessage = String.format("""
                用例名称: %s
                前置条件: %s
                测试步骤:
                %s

                请生成完整的 Playwright 测试代码。记住使用 getByPlaceholder、getByRole、getByText 定位器。
                """, caseName,
                preconditions != null ? preconditions : "无",
                naturalLanguageSteps);

        String response = chat(systemPrompt, userMessage);
        return extractCodeFromResponse(response);
    }

    /**
     * Extract TypeScript code from LLM response (handles markdown code blocks).
     */
    private String extractCodeFromResponse(String response) {
        if (response == null) return "";

        // Try to extract from ```typescript ... ``` code block
        int startIdx = response.indexOf("```typescript");
        if (startIdx == -1) startIdx = response.indexOf("```ts");
        if (startIdx == -1) startIdx = response.indexOf("```");

        if (startIdx != -1) {
            // Skip the opening ``` line
            int codeStart = response.indexOf('\n', startIdx) + 1;
            int codeEnd = response.indexOf("```", codeStart);
            if (codeEnd > codeStart) {
                return response.substring(codeStart, codeEnd).trim();
            }
        }

        // If no code block found, return the raw response (might be pure code)
        return response.trim();
    }
}
