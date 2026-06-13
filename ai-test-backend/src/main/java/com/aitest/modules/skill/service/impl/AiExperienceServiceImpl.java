package com.aitest.modules.skill.service.impl;

import com.aitest.common.exception.BusinessException;
import com.aitest.common.result.PageResult;
import com.aitest.modules.skill.dto.ExperienceDTO;
import com.aitest.modules.skill.dto.ExperienceQueryDTO;
import com.aitest.modules.skill.entity.AiExperience;
import com.aitest.modules.skill.entity.AiSkill;
import com.aitest.modules.skill.mapper.AiExperienceMapper;
import com.aitest.modules.skill.service.AiExperienceService;
import com.aitest.modules.skill.service.AiSkillService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiExperienceServiceImpl extends ServiceImpl<AiExperienceMapper, AiExperience>
        implements AiExperienceService {

    @Lazy
    private final AiSkillService aiSkillService;
    private final ObjectMapper objectMapper;

    @Override
    public PageResult<AiExperience> listExperiences(ExperienceQueryDTO query) {
        Page<AiExperience> page = new Page<>(query.getPage(), query.getSize());

        LambdaQueryWrapper<AiExperience> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(query.getExperienceType()), AiExperience::getExperienceType, query.getExperienceType())
                .eq(query.getIsPromoted() != null, AiExperience::getIsPromoted, Boolean.TRUE.equals(query.getIsPromoted()) ? 1 : 0)
                .and(StringUtils.hasText(query.getKeyword()), w ->
                        w.like(AiExperience::getTitle, query.getKeyword())
                                .or()
                                .like(AiExperience::getContent, query.getKeyword()))
                .orderByDesc(AiExperience::getCreatedAt);

        Page<AiExperience> result = page(page, wrapper);
        return PageResult.of(
                result.getTotal(), result.getCurrent(),
                result.getSize(), result.getPages(), result.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiExperience createExperience(ExperienceDTO dto) {
        AiExperience experience = new AiExperience();
        experience.setExperienceType(dto.getExperienceType());
        experience.setTitle(dto.getTitle());
        experience.setContent(dto.getContent());
        experience.setRelatedSkillId(dto.getRelatedSkillId());
        experience.setRelatedCaseId(dto.getRelatedCaseId());
        experience.setIsPromoted(0);

        // Convert JSON fields
        experience.setContext(toJsonString(dto.getContext()));
        experience.setTags(toJsonString(dto.getTags()));

        save(experience);
        return experience;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void promoteToSkill(Long experienceId) {
        AiExperience experience = getById(experienceId);
        if (experience == null) {
            throw new BusinessException(404, "Experience not found: " + experienceId);
        }
        if (experience.getIsPromoted() != null && experience.getIsPromoted() == 1) {
            throw new BusinessException(400, "Experience already promoted to skill");
        }

        // Create a new LEARNED skill from the experience
        AiSkill newSkill = new AiSkill();
        newSkill.setSkillCode("SKILL-" + System.currentTimeMillis());
        newSkill.setSkillName("Learned: " + (experience.getTitle() != null ? experience.getTitle() : "Untitled"));
        newSkill.setSkillType("LEARNED");
        newSkill.setSourceType("LEARNED");
        newSkill.setLearnedFrom(experienceId);
        newSkill.setDescription(experience.getContent());
        newSkill.setExecutorType("LLM");
        newSkill.setPromptTemplate(experience.getContent());
        newSkill.setStatus("TESTING");
        newSkill.setVersion(1);
        newSkill.setConfidence(new BigDecimal("0.50"));
        newSkill.setUsageCount(0);
        newSkill.setSuccessCount(0);

        // Copy tags from experience as trigger keywords
        if (StringUtils.hasText(experience.getTags())) {
            newSkill.setTriggerKeywords(experience.getTags());
        }

        // Use AiSkillService to save (to leverage its code uniqueness check)
        aiSkillService.save(newSkill);

        // Update experience: set is_promoted=1 and promoted_skill_id
        experience.setIsPromoted(1);
        experience.setPromotedSkillId(newSkill.getId());
        updateById(experience);

        log.info("Promoted experience {} to skill {} ({})", experienceId, newSkill.getSkillCode(), newSkill.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteExperience(Long id) {
        AiExperience experience = getById(id);
        if (experience == null) {
            throw new BusinessException(404, "Experience not found: " + id);
        }
        removeById(id);
        log.info("Deleted experience: {}", id);
    }

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
