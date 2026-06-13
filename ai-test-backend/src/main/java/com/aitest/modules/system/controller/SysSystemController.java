package com.aitest.modules.system.controller;

import com.aitest.common.result.PageResult;
import com.aitest.common.result.Result;
import com.aitest.modules.system.entity.SysSystem;
import com.aitest.modules.system.service.SysSystemService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * System management controller
 */
@Slf4j
@RestController
@RequestMapping("/api/system/systems")
@RequiredArgsConstructor
@Tag(name = "System Management", description = "CRUD operations for AI systems under test")
public class SysSystemController {

    private final SysSystemService sysSystemService;

    @GetMapping
    @Operation(summary = "List systems (paginated)", description = "Get paginated list of systems with optional filters")
    public Result<PageResult<SysSystem>> list(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "Name filter") @RequestParam(required = false) String name,
            @Parameter(description = "Type filter") @RequestParam(required = false) String type,
            @Parameter(description = "Status filter") @RequestParam(required = false) Integer status) {

        Page<SysSystem> page = new Page<>(current, size);
        LambdaQueryWrapper<SysSystem> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(name != null, SysSystem::getName, name)
               .eq(type != null, SysSystem::getType, type)
               .eq(status != null, SysSystem::getStatus, status)
               .orderByDesc(SysSystem::getCreateTime);

        Page<SysSystem> result = sysSystemService.page(page, wrapper);
        PageResult<SysSystem> pageResult = PageResult.of(
                result.getTotal(), result.getCurrent(), result.getSize(),
                result.getPages(), result.getRecords());

        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get system by ID", description = "Get a single system by its ID")
    public Result<SysSystem> getById(@Parameter(description = "System ID") @PathVariable Long id) {
        SysSystem system = sysSystemService.getById(id);
        return Result.success(system);
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get system by code", description = "Get a single system by its unique code")
    public Result<SysSystem> getByCode(@Parameter(description = "System code") @PathVariable String code) {
        SysSystem system = sysSystemService.getByCode(code);
        return Result.success(system);
    }

    @PostMapping
    @Operation(summary = "Create system", description = "Register a new AI system under test")
    public Result<SysSystem> create(@RequestBody SysSystem system) {
        sysSystemService.save(system);
        return Result.success(system);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update system", description = "Update an existing system's configuration")
    public Result<SysSystem> update(@PathVariable Long id, @RequestBody SysSystem system) {
        system.setId(id);
        sysSystemService.updateById(system);
        return Result.success(system);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete system", description = "Soft delete a system by ID")
    public Result<Void> delete(@PathVariable Long id) {
        sysSystemService.removeById(id);
        return Result.success();
    }

    @DeleteMapping("/batch")
    @Operation(summary = "Batch delete systems", description = "Soft delete multiple systems by IDs")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        sysSystemService.removeByIds(ids);
        return Result.success();
    }

    @GetMapping("/all")
    @Operation(summary = "List all systems", description = "Get all enabled systems (no pagination)")
    public Result<List<SysSystem>> listAll() {
        List<SysSystem> list = sysSystemService.list(
                new LambdaQueryWrapper<SysSystem>()
                        .eq(SysSystem::getStatus, 1)
                        .orderByAsc(SysSystem::getName));
        return Result.success(list);
    }
}
