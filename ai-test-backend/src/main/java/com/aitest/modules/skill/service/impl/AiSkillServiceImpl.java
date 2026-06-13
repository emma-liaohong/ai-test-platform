package com.aitest.modules.skill.service.impl;

import com.aitest.common.exception.BusinessException;
import com.aitest.common.result.PageResult;
import com.aitest.modules.skill.dto.AiSkillDTO;
import com.aitest.modules.skill.dto.AiSkillQueryDTO;
import com.aitest.modules.skill.dto.SkillInvokeDTO;
import com.aitest.modules.skill.engine.SkillEngine;
import com.aitest.modules.skill.entity.AiSkill;
import com.aitest.modules.skill.entity.AiSkillExecutionLog;
import com.aitest.modules.skill.mapper.AiSkillExecutionLogMapper;
import com.aitest.modules.skill.mapper.AiSkillMapper;
import com.aitest.modules.skill.service.AiSkillService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiSkillServiceImpl extends ServiceImpl<AiSkillMapper, AiSkill> implements AiSkillService {

    private final SkillEngine skillEngine;
    private final AiSkillExecutionLogMapper executionLogMapper;
    private final ObjectMapper objectMapper;

    @Override
    public PageResult<AiSkill> listSkills(AiSkillQueryDTO query) {
        Page<AiSkill> page = new Page<>(query.getPage(), query.getSize());

        LambdaQueryWrapper<AiSkill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(query.getSkillType()), AiSkill::getSkillType, query.getSkillType())
                .eq(StringUtils.hasText(query.getCategory()), AiSkill::getCategory, query.getCategory())
                .eq(StringUtils.hasText(query.getExecutorType()), AiSkill::getExecutorType, query.getExecutorType())
                .eq(StringUtils.hasText(query.getStatus()), AiSkill::getStatus, query.getStatus())
                .and(StringUtils.hasText(query.getKeyword()), w ->
                        w.like(AiSkill::getSkillCode, query.getKeyword())
                                .or()
                                .like(AiSkill::getSkillName, query.getKeyword()))
                .orderByDesc(AiSkill::getCreatedAt);

        Page<AiSkill> result = page(page, wrapper);
        return PageResult.of(
                result.getTotal(), result.getCurrent(),
                result.getSize(), result.getPages(), result.getRecords());
    }

    @Override
    public AiSkill getSkill(Long id) {
        AiSkill skill = getById(id);
        if (skill == null) {
            throw new BusinessException(404, "Skill not found: " + id);
        }
        return skill;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiSkill createSkill(AiSkillDTO dto) {
        AiSkill skill = new AiSkill();

        // Generate skill_code if not provided
        String skillCode = dto.getSkillCode();
        if (!StringUtils.hasText(skillCode)) {
            skillCode = "SKILL-" + System.currentTimeMillis();
        }

        // Check uniqueness
        LambdaQueryWrapper<AiSkill> codeWrapper = new LambdaQueryWrapper<>();
        codeWrapper.eq(AiSkill::getSkillCode, skillCode);
        if (count(codeWrapper) > 0) {
            throw new BusinessException(400, "Skill code already exists: " + skillCode);
        }

        skill.setSkillCode(skillCode);
        skill.setSkillName(dto.getSkillName());
        skill.setSkillType(dto.getSkillType());
        skill.setCategory(dto.getCategory());
        skill.setDescription(dto.getDescription());
        skill.setExecutorType(dto.getExecutorType());
        skill.setPromptTemplate(dto.getPromptTemplate());
        skill.setScriptContent(dto.getScriptContent());
        skill.setStatus(StringUtils.hasText(dto.getStatus()) ? dto.getStatus() : "ACTIVE");
        skill.setSourceType("MANUAL");
        skill.setVersion(1);
        skill.setConfidence(new BigDecimal("1.00"));
        skill.setUsageCount(0);
        skill.setSuccessCount(0);

        // Convert JSON object fields to strings
        skill.setTriggerIntent(toJsonString(dto.getTriggerIntent()));
        skill.setTriggerKeywords(toJsonString(dto.getTriggerKeywords()));
        skill.setInputSchema(toJsonString(dto.getInputSchema()));
        skill.setOutputSchema(toJsonString(dto.getOutputSchema()));
        skill.setExecutorConfig(toJsonString(dto.getExecutorConfig()));

        save(skill);
        return skill;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiSkill updateSkill(Long id, AiSkillDTO dto) {
        AiSkill skill = getSkill(id);

        // Check code uniqueness if changed
        if (StringUtils.hasText(dto.getSkillCode()) && !dto.getSkillCode().equals(skill.getSkillCode())) {
            LambdaQueryWrapper<AiSkill> codeWrapper = new LambdaQueryWrapper<>();
            codeWrapper.eq(AiSkill::getSkillCode, dto.getSkillCode());
            if (count(codeWrapper) > 0) {
                throw new BusinessException(400, "Skill code already exists: " + dto.getSkillCode());
            }
            skill.setSkillCode(dto.getSkillCode());
        }

        if (dto.getSkillName() != null) skill.setSkillName(dto.getSkillName());
        if (dto.getSkillType() != null) skill.setSkillType(dto.getSkillType());
        if (dto.getCategory() != null) skill.setCategory(dto.getCategory());
        if (dto.getDescription() != null) skill.setDescription(dto.getDescription());
        if (dto.getExecutorType() != null) skill.setExecutorType(dto.getExecutorType());
        if (dto.getPromptTemplate() != null) skill.setPromptTemplate(dto.getPromptTemplate());
        if (dto.getScriptContent() != null) skill.setScriptContent(dto.getScriptContent());
        if (dto.getStatus() != null) skill.setStatus(dto.getStatus());

        // Update JSON fields (set to new value or null)
        if (dto.getTriggerIntent() != null) skill.setTriggerIntent(toJsonString(dto.getTriggerIntent()));
        if (dto.getTriggerKeywords() != null) skill.setTriggerKeywords(toJsonString(dto.getTriggerKeywords()));
        if (dto.getInputSchema() != null) skill.setInputSchema(toJsonString(dto.getInputSchema()));
        if (dto.getOutputSchema() != null) skill.setOutputSchema(toJsonString(dto.getOutputSchema()));
        if (dto.getExecutorConfig() != null) skill.setExecutorConfig(toJsonString(dto.getExecutorConfig()));

        updateById(skill);
        return skill;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSkill(Long id) {
        AiSkill skill = getSkill(id);

        // Delete related execution logs
        LambdaQueryWrapper<AiSkillExecutionLog> logWrapper = new LambdaQueryWrapper<>();
        logWrapper.eq(AiSkillExecutionLog::getSkillId, id);
        executionLogMapper.delete(logWrapper);

        removeById(id);
        log.info("Deleted skill: {} ({})", skill.getSkillCode(), id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> invokeSkill(SkillInvokeDTO dto) {
        // Find skill by ID or code
        AiSkill skill;
        if (dto.getSkillId() != null) {
            skill = getSkill(dto.getSkillId());
        } else if (StringUtils.hasText(dto.getSkillCode())) {
            LambdaQueryWrapper<AiSkill> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AiSkill::getSkillCode, dto.getSkillCode());
            skill = getOne(wrapper);
            if (skill == null) {
                throw new BusinessException(404, "Skill not found with code: " + dto.getSkillCode());
            }
        } else {
            throw new BusinessException(400, "Either skillId or skillCode must be provided");
        }

        // Create execution log
        AiSkillExecutionLog execLog = new AiSkillExecutionLog();
        execLog.setSkillId(skill.getId());
        execLog.setInputParams(toJsonString(dto.getInputParams()));
        execLog.setUserFeedback("NONE");

        Map<String, Object> inputParams = dto.getInputParams() != null ? dto.getInputParams() : new LinkedHashMap<>();
        Map<String, Object> engineResult;
        String status;
        String errorMessage = null;

        try {
            engineResult = skillEngine.execute(skill, inputParams);
            status = "SUCCESS";
        } catch (Exception e) {
            log.error("Skill execution failed: {} ({})", skill.getSkillCode(), e.getMessage(), e);
            status = "FAILED";
            errorMessage = e.getMessage();
            engineResult = new LinkedHashMap<>();
            engineResult.put("error", e.getMessage());
        }

        // Update execution log
        execLog.setOutputResult(toJsonString(engineResult));
        execLog.setStatus(status);
        execLog.setErrorMessage(errorMessage);
        execLog.setCreatedAt(LocalDateTime.now());
        if (engineResult.containsKey("durationMs")) {
            execLog.setDurationMs(((Number) engineResult.get("durationMs")).longValue());
        }
        executionLogMapper.insert(execLog);

        // Update usage_count and success_count
        skill.setUsageCount(skill.getUsageCount() + 1);
        if ("SUCCESS".equals(status)) {
            skill.setSuccessCount(skill.getSuccessCount() + 1);
        }
        updateById(skill);

        // Build response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("skillId", skill.getId());
        response.put("skillCode", skill.getSkillCode());
        response.put("skillName", skill.getSkillName());
        response.put("logId", execLog.getId());
        response.put("status", status);
        response.putAll(engineResult);
        return response;
    }

    @Override
    public List<AiSkill> listActiveSkills() {
        LambdaQueryWrapper<AiSkill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiSkill::getStatus, "ACTIVE");
        wrapper.orderByDesc(AiSkill::getConfidence);
        return list(wrapper);
    }

    @Override
    public Map<String, Object> matchIntent(String userInput) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("query", userInput);

        if (!StringUtils.hasText(userInput)) {
            response.put("matched", false);
            response.put("message", "Empty input");
            return response;
        }

        // Get all active skills
        List<AiSkill> activeSkills = listActiveSkills();

        // Split input into words (support both Chinese and English)
        String normalizedInput = userInput.toLowerCase();
        String[] words = normalizedInput.split("[\\s,;.!?，；。！？、]+");

        // Find matching skills
        List<Map<String, Object>> candidates = new ArrayList<>();

        for (AiSkill skill : activeSkills) {
            if (!StringUtils.hasText(skill.getTriggerKeywords())) {
                continue;
            }

            try {
                List<String> keywords = objectMapper.readValue(
                        skill.getTriggerKeywords(), new TypeReference<List<String>>() {});

                int matchCount = 0;
                List<String> matchedKeywords = new ArrayList<>();

                for (String keyword : keywords) {
                    if (normalizedInput.contains(keyword.toLowerCase())) {
                        matchCount++;
                        matchedKeywords.add(keyword);
                    }
                }

                if (matchCount > 0) {
                    // Score = matchCount * confidence
                    double score = matchCount * skill.getConfidence().doubleValue();
                    Map<String, Object> candidate = new LinkedHashMap<>();
                    candidate.put("skillId", skill.getId());
                    candidate.put("skillCode", skill.getSkillCode());
                    candidate.put("skillName", skill.getSkillName());
                    candidate.put("matchCount", matchCount);
                    candidate.put("matchedKeywords", matchedKeywords);
                    candidate.put("confidence", skill.getConfidence());
                    candidate.put("score", score);
                    candidates.add(candidate);
                }
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse trigger_keywords for skill {}: {}", skill.getSkillCode(), e.getMessage());
            }
        }

        if (candidates.isEmpty()) {
            response.put("matched", false);
            response.put("message", "No matching skill found");
            response.put("candidates", candidates);
        } else {
            // Sort by score descending
            candidates.sort((a, b) -> Double.compare((double) b.get("score"), (double) a.get("score")));

            response.put("matched", true);
            response.put("topMatch", candidates.get(0));
            response.put("candidates", candidates);
        }

        return response;
    }

    /**
     * Convert an object to JSON string. Returns null if input is null.
     */
    private String toJsonString(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            return (String) obj;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize object to JSON: {}", e.getMessage());
            return obj.toString();
        }
    }
}
