package com.aitest.modules.defect.controller;

import com.aitest.common.result.PageResult;
import com.aitest.common.result.Result;
import com.aitest.modules.defect.dto.RiskAnalysisDTO;
import com.aitest.modules.defect.dto.RiskAnalysisQueryDTO;
import com.aitest.modules.defect.entity.RiskAnalysis;
import com.aitest.modules.defect.service.RiskAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/risk-analysis")
@RequiredArgsConstructor
@Tag(name = "风险分析", description = "AI-powered risk analysis management")
public class RiskAnalysisController {

    private final RiskAnalysisService riskAnalysisService;

    @GetMapping
    @Operation(summary = "List risk analyses (paginated)")
    public Result<PageResult<RiskAnalysis>> list(RiskAnalysisQueryDTO query) {
        return Result.success(riskAnalysisService.listAnalyses(query));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get risk analysis detail")
    public Result<RiskAnalysis> getDetail(@Parameter(description = "Analysis ID") @PathVariable Long id) {
        return Result.success(riskAnalysisService.getAnalysis(id));
    }

    @PostMapping
    @Operation(summary = "Create and trigger risk analysis (simulates AI analysis)")
    public Result<RiskAnalysis> create(@RequestBody RiskAnalysisDTO dto) {
        return Result.success(riskAnalysisService.createAnalysis(dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete risk analysis")
    public Result<Void> delete(@PathVariable Long id) {
        riskAnalysisService.deleteAnalysis(id);
        return Result.success();
    }
}
