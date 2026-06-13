package com.aitest.modules.case_suite.controller;

import com.aitest.common.result.PageResult;
import com.aitest.common.result.Result;
import com.aitest.modules.case_suite.dto.ExecuteRequestDTO;
import com.aitest.modules.case_suite.dto.TestSuiteDTO;
import com.aitest.modules.case_suite.entity.TestExecution;
import com.aitest.modules.case_suite.entity.TestSuite;
import com.aitest.modules.case_suite.service.TestSuiteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Test Suite management controller
 */
@Slf4j
@RestController
@RequestMapping("/api/suites")
@RequiredArgsConstructor
@Tag(name = "案例集管理", description = "CRUD and execution operations for test suites")
public class TestSuiteController {

    private final TestSuiteService testSuiteService;

    @PostMapping
    @Operation(summary = "Create test suite", description = "Create a new test suite with optional case associations")
    public Result<TestSuite> create(@Valid @RequestBody TestSuiteDTO dto) {
        TestSuite created = testSuiteService.createSuite(dto);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update test suite", description = "Update an existing test suite")
    public Result<TestSuite> update(
            @Parameter(description = "Suite ID") @PathVariable Long id,
            @Valid @RequestBody TestSuiteDTO dto) {
        TestSuite updated = testSuiteService.updateSuite(id, dto);
        return Result.success(updated);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get test suite detail", description = "Get a single test suite with its case list")
    public Result<Map<String, Object>> getDetail(
            @Parameter(description = "Suite ID") @PathVariable Long id) {
        Map<String, Object> detail = testSuiteService.getSuiteDetail(id);
        return Result.success(detail);
    }

    @GetMapping
    @Operation(summary = "List test suites (paginated)", description = "Get paginated list of test suites with optional filters")
    public Result<PageResult<TestSuite>> list(
            @Parameter(description = "System ID filter") @RequestParam(required = false) Long systemId,
            @Parameter(description = "Keyword search") @RequestParam(required = false) String keyword,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") Long page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") Long size) {
        PageResult<TestSuite> pageResult = testSuiteService.listSuites(systemId, keyword, page, size);
        return Result.success(pageResult);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete test suite", description = "Delete a test suite and its case associations")
    public Result<Void> delete(@Parameter(description = "Suite ID") @PathVariable Long id) {
        testSuiteService.deleteSuite(id);
        return Result.success();
    }

    @PostMapping("/{id}/cases")
    @Operation(summary = "Add cases to suite", description = "Add test cases to an existing suite")
    public Result<Void> addCases(
            @Parameter(description = "Suite ID") @PathVariable Long id,
            @RequestBody List<Long> caseIds) {
        testSuiteService.addCasesToSuite(id, caseIds);
        return Result.success();
    }

    @DeleteMapping("/{id}/cases")
    @Operation(summary = "Remove cases from suite", description = "Remove test cases from a suite")
    public Result<Void> removeCases(
            @Parameter(description = "Suite ID") @PathVariable Long id,
            @RequestBody List<Long> caseIds) {
        testSuiteService.removeCasesFromSuite(id, caseIds);
        return Result.success();
    }

    @PostMapping("/{id}/execute")
    @Operation(summary = "Execute test suite", description = "Execute all cases in a test suite and create execution records")
    public Result<TestExecution> execute(
            @Parameter(description = "Suite ID") @PathVariable Long id,
            @Valid @RequestBody ExecuteRequestDTO dto) {
        dto.setSuiteId(id);
        TestExecution execution = testSuiteService.executeSuite(dto);
        return Result.success(execution);
    }
}
