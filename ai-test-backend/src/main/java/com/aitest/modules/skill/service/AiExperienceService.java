package com.aitest.modules.skill.service;

import com.aitest.common.result.PageResult;
import com.aitest.modules.skill.dto.ExperienceDTO;
import com.aitest.modules.skill.dto.ExperienceQueryDTO;
import com.aitest.modules.skill.entity.AiExperience;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AiExperienceService extends IService<AiExperience> {

    PageResult<AiExperience> listExperiences(ExperienceQueryDTO query);

    AiExperience createExperience(ExperienceDTO dto);

    void promoteToSkill(Long experienceId);

    void deleteExperience(Long id);
}
