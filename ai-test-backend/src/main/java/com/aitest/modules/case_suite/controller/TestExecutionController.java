package com.aitest.modules.case_suite.controller;

import com.aitest.common.result.PageResult;
import com.aitest.common.result.Result;
import com.aitest.modules.case_suite.entity.TestExecution;
import com.aitest.modules.case_suite.entity.TestExecutionDetail;
import com.aitest.modules.case_suite.service.TestExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Test Execution management controller
 */
@Slf4j
@RestController
@RequestMapping("/api/executions")
@RequiredArgsConstructor
@Tag(name = "执行管理", description = "Query and manage test execution records")
public class TestExecutionController {

    private final TestExecutionService testExecutionService;

    @GetMapping
    @Operation(summary = "List executions (paginated)", description = "Get paginated list of test executions with optional filters")
    public Result<PageResult<TestExecution>> list(
            @Parameter(description = "Suite ID filter") @RequestParam(required = false) Long suiteId,
            @Parameter(description = "System ID filter") @RequestParam(required = false) Long systemId,
            @Parameter(description = "Status filter: PENDING, RUNNING, SUCCESS, FAILED, CANCELLED") @RequestParam(required = false) String status,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") Long page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") Long size) {
        PageResult<TestExecution> pageResult = testExecutionService.listExecutions(suiteId, systemId, status, page, size);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get execution detail", description = "Get execution detail with all case results")
    public Result<Map<String, Object>> getDetail(
            @Parameter(description = "Execution ID") @PathVariable Long id) {
        Map<String, Object> detail = testExecutionService.getExecutionDetail(id);
        return Result.success(detail);
    }

    @GetMapping("/{id}/report")
    @Operation(summary = "Get execution report", description = "Get execution report with summary statistics")
    public Result<Map<String, Object>> getReport(
            @Parameter(description = "Execution ID") @PathVariable Long id) {
        Map<String, Object> report = testExecutionService.getExecutionReport(id);
        return Result.success(report);
    }

    @GetMapping("/{id}/cases/{caseId}/log")
    @Operation(summary = "Get case execution log", description = "Get specific case execution log within an execution")
    public Result<TestExecutionDetail> getCaseLog(
            @Parameter(description = "Execution ID") @PathVariable Long id,
            @Parameter(description = "Case ID") @PathVariable Long caseId) {
        TestExecutionDetail detail = testExecutionService.getCaseExecutionLog(id, caseId);
        return Result.success(detail);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel execution", description = "Cancel a running or pending execution")
    public Result<Void> cancel(
            @Parameter(description = "Execution ID") @PathVariable Long id) {
        testExecutionService.cancelExecution(id);
        return Result.success();
    }
}
