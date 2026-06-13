package com.aitest.modules.record.service;

import com.aitest.common.result.PageResult;
import com.aitest.modules.record.dto.RecordSessionDTO;
import com.aitest.modules.record.dto.RecordSessionQueryDTO;
import com.aitest.modules.record.dto.RecordStepDTO;
import com.aitest.modules.record.entity.RecordSession;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

public interface RecordSessionService extends IService<RecordSession> {

    PageResult<RecordSession> listSessions(RecordSessionQueryDTO query);

    RecordSession getSession(Long id);

    RecordSession getSessionBySessionId(String sessionId);

    RecordSession startSession(RecordSessionDTO dto);

    RecordSession stopSession(Long id);

    void addStep(Long id, RecordStepDTO step);

    void deleteSession(Long id);

    Map<String, Object> generateCase(Long id);
}
