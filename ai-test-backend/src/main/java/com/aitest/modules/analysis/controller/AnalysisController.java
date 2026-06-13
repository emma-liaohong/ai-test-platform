package com.aitest.modules.analysis.controller;

import com.aitest.common.result.Result;
import com.aitest.modules.analysis.dto.AnalysisRequestDTO;
import com.aitest.modules.analysis.dto.AnalysisResultDTO;
import com.aitest.modules.analysis.service.AnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
@Tag(name = "需求分析", description = "文档解析、功能提取、影响分析、案例生成")
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping("/analyze")
    @Operation(summary = "分析需求文档", description = "解析文档 → 提取功能点 → 影响分析 → 可选自动生成测试案例")
    public Result<AnalysisResultDTO> analyze(
            @Valid @RequestBody AnalysisRequestDTO request) {
        AnalysisResultDTO result = analysisService.analyzeDocument(request);
        return Result.success(result);
    }

    @GetMapping("/result/{documentId}")
    @Operation(summary = "获取分析结果", description = "获取指定文档的分析结果缓存")
    public Result<AnalysisResultDTO> getResult(
            @Parameter(description = "知识库文档ID") @PathVariable Long documentId) {
        AnalysisResultDTO result = analysisService.getAnalysisResult(documentId);
        return Result.success(result);
    }

    @PostMapping("/generate-cases")
    @Operation(summary = "从分析结果生成案例", description = "基于之前的分析结果批量生成测试案例")
    public Result<Map<String, Object>> generateCases(
            @Parameter(description = "知识库文档ID") @RequestParam Long documentId,
            @Parameter(description = "目标系统ID") @RequestParam Long systemId) {
        AnalysisResultDTO analysis = analysisService.getAnalysisResult(documentId);
        List<Long> ids = analysisService.generateCases(documentId, analysis.getGeneratedCases(), systemId);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("createdCount", ids.size());
        data.put("createdCaseIds", ids);
        return Result.success(data);
    }
}
