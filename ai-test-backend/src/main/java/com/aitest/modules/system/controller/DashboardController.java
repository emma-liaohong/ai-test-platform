package com.aitest.modules.system.controller;

import com.aitest.common.result.Result;
import com.aitest.modules.case_mgr.mapper.TestCaseMapper;
import com.aitest.modules.case_suite.mapper.TestSuiteMapper;
import com.aitest.modules.defect.mapper.DefectMapper;
import com.aitest.modules.llm.mapper.LlmConversationMapper;
import com.aitest.modules.skill.mapper.AiSkillMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Dashboard statistics controller - provides real counts from database
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "仪表盘", description = "Dashboard statistics")
public class DashboardController {

    private final TestCaseMapper testCaseMapper;
    private final TestSuiteMapper testSuiteMapper;
    private final DefectMapper defectMapper;
    private final AiSkillMapper aiSkillMapper;
    private final LlmConversationMapper llmConversationMapper;

    @GetMapping("/stats")
    @Operation(summary = "Get dashboard statistics", description = "Returns real counts for all dashboard stat cards")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("caseCount", testCaseMapper.selectCount(null));
        stats.put("suiteCount", testSuiteMapper.selectCount(null));
        stats.put("defectCount", defectMapper.selectCount(null));
        stats.put("skillCount", aiSkillMapper.selectCount(null));
        stats.put("conversationCount", llmConversationMapper.selectCount(null));
        return Result.success(stats);
    }
}
