package com.aitest.modules.system.controller;

import com.aitest.common.result.Result;
import com.aitest.modules.case_mgr.entity.TestCase;
import com.aitest.modules.case_mgr.mapper.TestCaseMapper;
import com.aitest.modules.case_suite.mapper.TestSuiteMapper;
import com.aitest.modules.defect.mapper.DefectMapper;
import com.aitest.modules.llm.mapper.LlmConversationMapper;
import com.aitest.modules.skill.mapper.AiSkillMapper;
import com.aitest.modules.system.entity.SysSystem;
import com.aitest.modules.system.mapper.SysSystemMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
    private final SysSystemMapper sysSystemMapper;

    @GetMapping("/stats")
    @Operation(summary = "Get dashboard statistics")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("caseCount", testCaseMapper.selectCount(null));
        stats.put("suiteCount", testSuiteMapper.selectCount(null));
        stats.put("defectCount", defectMapper.selectCount(null));
        stats.put("skillCount", aiSkillMapper.selectCount(null));
        stats.put("conversationCount", llmConversationMapper.selectCount(null));
        return Result.success(stats);
    }

    @GetMapping("/chart/case-distribution")
    @Operation(summary = "Get case distribution data for pie charts")
    public Result<Map<String, Object>> getCaseDistribution() {
        List<TestCase> allCases = testCaseMapper.selectList(null);
        Map<String, Object> result = new HashMap<>();

        // By case type
        Map<String, Long> byType = allCases.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getCaseType() != null ? c.getCaseType() : "未知",
                        Collectors.counting()));
        result.put("byType", mapToList(byType));

        // By priority
        Map<String, Long> byPriority = allCases.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getPriority() != null ? c.getPriority() : "未设置",
                        Collectors.counting()));
        result.put("byPriority", mapToList(byPriority));

        // By system
        List<SysSystem> systems = sysSystemMapper.selectList(null);
        Map<Long, String> systemNameMap = systems.stream()
                .collect(Collectors.toMap(SysSystem::getId, SysSystem::getName, (a, b) -> a));
        Map<String, Long> bySystem = allCases.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getSystemId() != null ? systemNameMap.getOrDefault(c.getSystemId(), "未关联") : "未关联",
                        Collectors.counting()));
        result.put("bySystem", mapToList(bySystem));

        return Result.success(result);
    }

    @GetMapping("/chart/execution-trend")
    @Operation(summary = "Get execution trend data for line chart")
    public Result<Map<String, Object>> getExecutionTrend() {
        // Use case creation dates as trend data (since execution data may not exist)
        List<TestCase> allCases = testCaseMapper.selectList(null);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");

        // Group by creation date (last 7 days)
        Map<String, Long> trendMap = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            String dateKey = today.minusDays(i).format(fmt);
            trendMap.put(dateKey, 0L);
        }

        for (TestCase tc : allCases) {
            if (tc.getCreatedAt() != null) {
                String dateKey = tc.getCreatedAt().toLocalDate().format(fmt);
                trendMap.merge(dateKey, 1L, Long::sum);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("dates", new ArrayList<>(trendMap.keySet()));
        result.put("values", new ArrayList<>(trendMap.values()));
        return Result.success(result);
    }

    private List<Map<String, Object>> mapToList(Map<String, Long> map) {
        return map.entrySet().stream()
                .map(e -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", e.getKey());
                    item.put("value", e.getValue());
                    return item;
                })
                .collect(Collectors.toList());
    }
}
