package com.aitest.modules.system.controller;

import com.aitest.common.result.PageResult;
import com.aitest.common.result.Result;
import com.aitest.modules.system.entity.SysUser;
import com.aitest.modules.system.service.SysUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User management controller
 */
@Slf4j
@RestController
@RequestMapping("/api/system/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "CRUD operations for system users")
public class SysUserController {

    private final SysUserService sysUserService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    @Operation(summary = "List users (paginated)", description = "Get paginated list of users with optional filters")
    public Result<PageResult<SysUser>> list(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "Username filter") @RequestParam(required = false) String username,
            @Parameter(description = "Status filter") @RequestParam(required = false) Integer status) {

        Page<SysUser> page = new Page<>(current, size);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(username != null, SysUser::getUsername, username)
               .eq(status != null, SysUser::getStatus, status)
               .orderByDesc(SysUser::getCreateTime);

        Page<SysUser> result = sysUserService.page(page, wrapper);
        PageResult<SysUser> pageResult = PageResult.of(
                result.getTotal(), result.getCurrent(), result.getSize(),
                result.getPages(), result.getRecords());

        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Get a single user by their ID")
    public Result<SysUser> getById(@Parameter(description = "User ID") @PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        return Result.success(user);
    }

    @PostMapping
    @Operation(summary = "Create user", description = "Register a new system user")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SysUser> create(@RequestBody SysUser user) {
        SysUser saved = sysUserService.register(user);
        return Result.success(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update an existing user's information")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SysUser> update(@PathVariable Long id, @RequestBody SysUser user) {
        user.setId(id);
        // Don't allow password update through this endpoint
        user.setPassword(null);
        sysUserService.updateById(user);
        return Result.success(user);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Soft delete a user by ID")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        sysUserService.removeById(id);
        return Result.success();
    }

    @DeleteMapping("/batch")
    @Operation(summary = "Batch delete users", description = "Soft delete multiple users by IDs")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        sysUserService.removeByIds(ids);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update user status", description = "Enable or disable a user account")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateStatus(
            @PathVariable Long id,
            @Parameter(description = "Status: 0=disabled, 1=enabled") @RequestParam Integer status) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setStatus(status);
        sysUserService.updateById(user);
        return Result.success();
    }

    @PutMapping("/{id}/reset-password")
    @Operation(summary = "Reset user password", description = "Reset a user's password to default")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> resetPassword(@PathVariable Long id) {
        SysUser user = new SysUser();
        user.setId(id);
        // TODO: Use a configurable default password
        user.setPassword(passwordEncoder.encode("123456"));
        sysUserService.updateById(user);
        return Result.success();
    }
}
