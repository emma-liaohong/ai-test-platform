package com.aitest.modules.case_mgr.controller;

import com.aitest.common.exception.BusinessException;
import com.aitest.common.result.PageResult;
import com.aitest.common.result.Result;
import com.aitest.modules.case_mgr.entity.TestDataTable;
import com.aitest.modules.case_mgr.entity.TestDataRow;
import com.aitest.modules.case_mgr.mapper.TestDataRowMapper;
import com.aitest.modules.case_mgr.service.TestDataTableService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Test Data management controller
 */
@Slf4j
@RestController
@RequestMapping("/api/test-data")
@RequiredArgsConstructor
@Tag(name = "测试数据管理", description = "CRUD operations for test data tables and rows")
public class TestDataController {

    private final TestDataTableService testDataTableService;
    private final TestDataRowMapper testDataRowMapper;

    // ==================== Data Table APIs ====================

    @PostMapping("/tables")
    @Operation(summary = "Create data table", description = "Create a new test data table")
    public Result<TestDataTable> createTable(@RequestBody TestDataTable table) {
        table.setStatus(table.getStatus() != null ? table.getStatus() : 1);
        table.setDeleted(0);
        testDataTableService.save(table);
        log.info("Data table created: {} ({})", table.getTableName(), table.getId());
        return Result.success(table);
    }

    @GetMapping("/tables")
    @Operation(summary = "List data tables (paginated)", description = "Get paginated list of data tables")
    public Result<PageResult<TestDataTable>> listTables(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "Table name filter") @RequestParam(required = false) String tableName,
            @Parameter(description = "System ID filter") @RequestParam(required = false) Long systemId) {

        Page<TestDataTable> page = new Page<>(current, size);
        LambdaQueryWrapper<TestDataTable> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(tableName != null, TestDataTable::getTableName, tableName)
               .eq(systemId != null, TestDataTable::getSystemId, systemId)
               .orderByDesc(TestDataTable::getCreatedAt);

        Page<TestDataTable> result = testDataTableService.page(page, wrapper);
        PageResult<TestDataTable> pageResult = PageResult.of(
                result.getTotal(), result.getCurrent(), result.getSize(),
                result.getPages(), result.getRecords());

        return Result.success(pageResult);
    }

    @GetMapping("/tables/{id}")
    @Operation(summary = "Get data table by ID", description = "Get a single data table by its ID")
    public Result<TestDataTable> getTable(
            @Parameter(description = "Data table ID") @PathVariable Long id) {
        TestDataTable table = testDataTableService.getById(id);
        if (table == null) {
            throw new BusinessException(404, "Data table not found: " + id);
        }
        return Result.success(table);
    }

    @PutMapping("/tables/{id}")
    @Operation(summary = "Update data table", description = "Update an existing data table")
    public Result<TestDataTable> updateTable(
            @Parameter(description = "Data table ID") @PathVariable Long id,
            @RequestBody TestDataTable table) {
        TestDataTable existing = testDataTableService.getById(id);
        if (existing == null) {
            throw new BusinessException(404, "Data table not found: " + id);
        }
        table.setId(id);
        testDataTableService.updateById(table);
        return Result.success(table);
    }

    @DeleteMapping("/tables/{id}")
    @Operation(summary = "Delete data table", description = "Soft delete a data table by ID")
    public Result<Void> deleteTable(
            @Parameter(description = "Data table ID") @PathVariable Long id) {
        TestDataTable existing = testDataTableService.getById(id);
        if (existing == null) {
            throw new BusinessException(404, "Data table not found: " + id);
        }
        testDataTableService.removeById(id);
        log.info("Data table deleted: {}", id);
        return Result.success();
    }

    // ==================== Data Row APIs ====================

    @PostMapping("/tables/{tableId}/rows")
    @Operation(summary = "Add data row", description = "Add a new row to a data table")
    public Result<TestDataRow> addRow(
            @Parameter(description = "Data table ID") @PathVariable Long tableId,
            @RequestBody TestDataRow row) {
        TestDataTable table = testDataTableService.getById(tableId);
        if (table == null) {
            throw new BusinessException(404, "Data table not found: " + tableId);
        }

        // Determine next row index
        Long existingCount = testDataRowMapper.selectCount(
                new LambdaQueryWrapper<TestDataRow>().eq(TestDataRow::getTableId, tableId));
        row.setTableId(tableId);
        row.setRowIndex(existingCount.intValue());
        row.setCreatedAt(LocalDateTime.now());
        testDataRowMapper.insert(row);

        log.info("Data row added to table {}: row {}", tableId, row.getRowIndex());
        return Result.success(row);
    }

    @GetMapping("/tables/{tableId}/rows")
    @Operation(summary = "List data rows", description = "Get all rows for a data table, ordered by row index")
    public Result<List<TestDataRow>> listRows(
            @Parameter(description = "Data table ID") @PathVariable Long tableId) {
        List<TestDataRow> rows = testDataRowMapper.selectList(
                new LambdaQueryWrapper<TestDataRow>()
                        .eq(TestDataRow::getTableId, tableId)
                        .orderByAsc(TestDataRow::getRowIndex));
        return Result.success(rows);
    }

    @DeleteMapping("/tables/{tableId}/rows/{rowId}")
    @Operation(summary = "Delete data row", description = "Delete a single row from a data table")
    public Result<Void> deleteRow(
            @Parameter(description = "Data table ID") @PathVariable Long tableId,
            @Parameter(description = "Row ID") @PathVariable Long rowId) {
        testDataRowMapper.deleteById(rowId);
        log.info("Data row {} deleted from table {}", rowId, tableId);
        return Result.success();
    }
}
