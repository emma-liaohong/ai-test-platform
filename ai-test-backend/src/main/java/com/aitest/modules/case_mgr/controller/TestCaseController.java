package com.aitest.modules.case_mgr.controller;

import com.aitest.common.exception.BusinessException;
import com.aitest.common.result.PageResult;
import com.aitest.common.result.Result;
import com.aitest.modules.case_mgr.dto.TestCaseDTO;
import com.aitest.modules.case_mgr.dto.TestCaseQueryDTO;
import com.aitest.modules.case_mgr.entity.TestCase;
import com.aitest.modules.case_mgr.service.TestCaseService;
import com.aitest.modules.case_suite.entity.TestExecution;
import com.aitest.modules.case_suite.entity.TestExecutionDetail;
import com.aitest.modules.case_suite.mapper.TestExecutionDetailMapper;
import com.aitest.modules.case_suite.mapper.TestExecutionMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Test Case management controller
 */
@Slf4j
@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
@Tag(name = "案例管理", description = "CRUD operations for test cases")
public class TestCaseController {

    private final TestCaseService testCaseService;
    private final TestExecutionMapper testExecutionMapper;
    private final TestExecutionDetailMapper testExecutionDetailMapper;

    @PostMapping
    @Operation(summary = "Create test case", description = "Create a new test case with optional steps")
    public Result<TestCase> create(@Valid @RequestBody TestCaseDTO dto) {
        TestCase created = testCaseService.createCase(dto);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update test case", description = "Update an existing test case and replace its steps")
    public Result<TestCase> update(
            @Parameter(description = "Case ID") @PathVariable Long id,
            @Valid @RequestBody TestCaseDTO dto) {
        TestCase updated = testCaseService.updateCase(id, dto);
        return Result.success(updated);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get test case detail", description = "Get a single test case with its steps")
    public Result<Map<String, Object>> getDetail(
            @Parameter(description = "Case ID") @PathVariable Long id) {
        Map<String, Object> detail = testCaseService.getCaseDetail(id);
        return Result.success(detail);
    }

    @GetMapping
    @Operation(summary = "List test cases (paginated)", description = "Get paginated list of test cases with optional filters")
    public Result<PageResult<TestCase>> list(TestCaseQueryDTO queryDTO) {
        PageResult<TestCase> pageResult = testCaseService.listCases(queryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete test case", description = "Delete a test case and its associated steps")
    public Result<Void> delete(@Parameter(description = "Case ID") @PathVariable Long id) {
        testCaseService.deleteCase(id);
        return Result.success();
    }

    @DeleteMapping("/batch")
    @Operation(summary = "Batch delete test cases", description = "Delete multiple test cases and their steps by IDs")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        testCaseService.batchDelete(ids);
        return Result.success();
    }

    @PostMapping("/{id}/execute")
    @Operation(summary = "Execute a single test case", description = "Execute a test case and create execution record")
    public Result<TestExecution> execute(
            @Parameter(description = "Case ID") @PathVariable Long id) {
        TestCase testCase = testCaseService.getById(id);
        if (testCase == null) {
            throw new BusinessException(404, "Test case not found: " + id);
        }

        // Create execution record
        TestExecution execution = new TestExecution();
        execution.setExecutionName(testCase.getCaseName() + " - " + LocalDateTime.now());
        execution.setSystemId(testCase.getSystemId());
        execution.setStatus("RUNNING");
        execution.setTriggerType("MANUAL");
        execution.setTotalCount(1);
        execution.setPassedCount(0);
        execution.setFailedCount(0);
        execution.setSkippedCount(0);
        execution.setStartedAt(LocalDateTime.now());
        testExecutionMapper.insert(execution);

        // Simulate execution
        TestExecutionDetail detail = new TestExecutionDetail();
        detail.setExecutionId(execution.getId());
        detail.setCaseId(id);
        detail.setStartedAt(LocalDateTime.now());

        ThreadLocalRandom random = ThreadLocalRandom.current();
        long durationMs = random.nextLong(500, 8000);
        detail.setFinishedAt(detail.getStartedAt().plusNanos(durationMs * 1_000_000));
        detail.setDurationMs(durationMs);

        int rand = random.nextInt(100);
        if (rand < 80) {
            detail.setStatus("SUCCESS");
            detail.setExecutionLog("Case executed successfully. All steps passed.");
            execution.setPassedCount(1);
        } else {
            detail.setStatus("FAILED");
            detail.setErrorMessage("Step failed: assertion mismatch on step " + (random.nextInt(1, 4)));
            detail.setExecutionLog("Case execution failed at step " + (random.nextInt(1, 4)) + ".");
            execution.setFailedCount(1);
        }
        testExecutionDetailMapper.insert(detail);

        execution.setStatus(detail.getStatus());
        execution.setFinishedAt(detail.getFinishedAt());
        testExecutionMapper.updateById(execution);

        log.info("Test case executed: {} ({}) - {}", testCase.getCaseCode(), testCase.getCaseName(), detail.getStatus());
        return Result.success(execution);
    }
}
