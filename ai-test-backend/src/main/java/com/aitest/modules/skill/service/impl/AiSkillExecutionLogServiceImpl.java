package com.aitest.modules.skill.service.impl;

import com.aitest.common.exception.BusinessException;
import com.aitest.common.result.PageResult;
import com.aitest.modules.skill.entity.AiSkillExecutionLog;
import com.aitest.modules.skill.mapper.AiSkillExecutionLogMapper;
import com.aitest.modules.skill.service.AiSkillExecutionLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiSkillExecutionLogServiceImpl extends ServiceImpl<AiSkillExecutionLogMapper, AiSkillExecutionLog>
        implements AiSkillExecutionLogService {

    @Override
    public PageResult<AiSkillExecutionLog> listLogs(Long skillId, int page, int size) {
        Page<AiSkillExecutionLog> pageObj = new Page<>(page, size);

        LambdaQueryWrapper<AiSkillExecutionLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(skillId != null, AiSkillExecutionLog::getSkillId, skillId)
                .orderByDesc(AiSkillExecutionLog::getCreatedAt);

        Page<AiSkillExecutionLog> result = page(pageObj, wrapper);
        return PageResult.of(
                result.getTotal(), result.getCurrent(),
                result.getSize(), result.getPages(), result.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitFeedback(Long logId, String feedback) {
        AiSkillExecutionLog logEntry = getById(logId);
        if (logEntry == null) {
            throw new BusinessException(404, "Execution log not found: " + logId);
        }

        if (!"GOOD".equals(feedback) && !"BAD".equals(feedback)) {
            throw new BusinessException(400, "Invalid feedback value. Must be GOOD or BAD");
        }

        logEntry.setUserFeedback(feedback);
        updateById(logEntry);
        log.info("Submitted feedback '{}' for execution log {}", feedback, logId);
    }
}
