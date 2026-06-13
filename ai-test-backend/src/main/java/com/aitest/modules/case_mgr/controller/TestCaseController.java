package com.aitest.modules.case_mgr.controller;

import com.aitest.common.result.PageResult;
import com.aitest.common.result.Result;
import com.aitest.modules.case_mgr.dto.TestCaseDTO;
import com.aitest.modules.case_mgr.dto.TestCaseQueryDTO;
import com.aitest.modules.case_mgr.entity.TestCase;
import com.aitest.modules.case_mgr.service.TestCaseService;
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
 * Test Case management controller
 */
@Slf4j
@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
@Tag(name = "案例管理", description = "CRUD operations for test cases")
public class TestCaseController {

    private final TestCaseService testCaseService;

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
}
