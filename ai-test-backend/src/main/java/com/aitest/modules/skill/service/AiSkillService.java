package com.aitest.modules.skill.service;

import com.aitest.common.result.PageResult;
import com.aitest.modules.skill.dto.AiSkillDTO;
import com.aitest.modules.skill.dto.AiSkillQueryDTO;
import com.aitest.modules.skill.dto.SkillInvokeDTO;
import com.aitest.modules.skill.entity.AiSkill;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface AiSkillService extends IService<AiSkill> {

    PageResult<AiSkill> listSkills(AiSkillQueryDTO query);

    AiSkill getSkill(Long id);

    AiSkill createSkill(AiSkillDTO dto);

    AiSkill updateSkill(Long id, AiSkillDTO dto);

    void deleteSkill(Long id);

    Map<String, Object> invokeSkill(SkillInvokeDTO dto);

    List<AiSkill> listActiveSkills();

    Map<String, Object> matchIntent(String userInput);
}
