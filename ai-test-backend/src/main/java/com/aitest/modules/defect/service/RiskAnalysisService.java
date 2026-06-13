package com.aitest.modules.defect.service;

import com.aitest.common.result.PageResult;
import com.aitest.modules.defect.dto.RiskAnalysisDTO;
import com.aitest.modules.defect.dto.RiskAnalysisQueryDTO;
import com.aitest.modules.defect.entity.RiskAnalysis;
import com.baomidou.mybatisplus.extension.service.IService;

public interface RiskAnalysisService extends IService<RiskAnalysis> {

    PageResult<RiskAnalysis> listAnalyses(RiskAnalysisQueryDTO query);

    RiskAnalysis getAnalysis(Long id);

    RiskAnalysis createAnalysis(RiskAnalysisDTO dto);

    void deleteAnalysis(Long id);
}
