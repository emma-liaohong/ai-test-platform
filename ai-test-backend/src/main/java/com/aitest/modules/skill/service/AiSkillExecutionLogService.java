package com.aitest.modules.skill.service;

import com.aitest.common.result.PageResult;
import com.aitest.modules.skill.entity.AiSkillExecutionLog;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AiSkillExecutionLogService extends IService<AiSkillExecutionLog> {

    PageResult<AiSkillExecutionLog> listLogs(Long skillId, int page, int size);

    void submitFeedback(Long logId, String feedback);
}
