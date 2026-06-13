package com.aitest.modules.skill.engine;

import com.aitest.common.exception.BusinessException;
import com.aitest.modules.skill.entity.AiSkill;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkillEngine {

    public Map<String, Object> execute(AiSkill skill, Map<String, Object> inputParams) {
        String executorType = skill.getExecutorType();
        Map<String, Object> result = new LinkedHashMap<>();
        long start = System.currentTimeMillis();

        switch (executorType) {
            case "LLM":
                result = executeLLM(skill, inputParams);
                break;
            case "CODE":
                result = executeCode(skill, inputParams);
                break;
            case "API":
                result = executeAPI(skill, inputParams);
                break;
            case "WORKFLOW":
                result = executeWorkflow(skill, inputParams);
                break;
            default:
                throw new BusinessException(400, "Unknown executor type: " + executorType);
        }

        result.put("durationMs", System.currentTimeMillis() - start);
        result.put("executorType", executorType);
        return result;
    }

    private Map<String, Object> executeLLM(AiSkill skill, Map<String, Object> inputParams) {
        // Simulate LLM call: substitute prompt template variables with input params
        String prompt = skill.getPromptTemplate() != null ? skill.getPromptTemplate() : "Default prompt";
        for (Map.Entry<String, Object> entry : inputParams.entrySet()) {
            prompt = prompt.replace("{{" + entry.getKey() + "}}", String.valueOf(entry.getValue()));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("type", "LLM");
        result.put("prompt", prompt);
        result.put("model", "qwen-max (simulated)");
        result.put("response", "这是AI模拟的LLM响应。基于输入参数: " + inputParams + "，生成的结果为: 测试案例已生成，共3条。");
        result.put("tokens", Map.of("promptTokens", prompt.length(), "completionTokens", 150, "totalTokens", prompt.length() + 150));
        return result;
    }

    private Map<String, Object> executeCode(AiSkill skill, Map<String, Object> inputParams) {
        // Simulate code execution
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("type", "CODE");
        result.put("script", skill.getScriptContent() != null ? skill.getScriptContent() : "// no script");
        result.put("stdout", "脚本执行成功，处理了 " + inputParams.size() + " 个参数");
        result.put("exitCode", 0);
        return result;
    }

    private Map<String, Object> executeAPI(AiSkill skill, Map<String, Object> inputParams) {
        // Simulate API call
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("simulated", true);
        config.put("inputParams", inputParams);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("type", "API");
        result.put("request", config);
        result.put("responseCode", 200);
        result.put("responseBody", Map.of("status", "success", "data", "API调用成功（模拟）"));
        return result;
    }

    private Map<String, Object> executeWorkflow(AiSkill skill, Map<String, Object> inputParams) {
        // Simulate workflow: chain of sub-steps
        List<Map<String, Object>> steps = new ArrayList<>();
        steps.add(Map.of("step", 1, "action", "解析输入", "status", "SUCCESS"));
        steps.add(Map.of("step", 2, "action", "执行核心逻辑", "status", "SUCCESS"));
        steps.add(Map.of("step", 3, "action", "生成输出", "status", "SUCCESS"));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("type", "WORKFLOW");
        result.put("steps", steps);
        result.put("finalOutput", "工作流执行完成，共3个步骤");
        return result;
    }
}
