package com.aitest.modules.defect.service;

import com.aitest.common.result.PageResult;
import com.aitest.modules.defect.dto.DefectDTO;
import com.aitest.modules.defect.dto.DefectQueryDTO;
import com.aitest.modules.defect.entity.Defect;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

public interface DefectService extends IService<Defect> {

    PageResult<Defect> listDefects(DefectQueryDTO query);

    Defect getDefect(Long id);

    Defect createDefect(DefectDTO dto);

    Defect updateDefect(Long id, DefectDTO dto);

    void deleteDefect(Long id);

    Defect updateStatus(Long id, String status);

    Map<String, Object> getStatistics(Long systemId);
}
