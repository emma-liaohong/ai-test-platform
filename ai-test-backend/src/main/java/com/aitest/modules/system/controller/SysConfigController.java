package com.aitest.modules.system.controller;

import com.aitest.common.result.Result;
import com.aitest.modules.system.dto.SysConfigDTO;
import com.aitest.modules.system.entity.SysConfig;
import com.aitest.modules.system.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
@Tag(name = "System Config", description = "System configuration management APIs")
public class SysConfigController {

    private final SysConfigService sysConfigService;

    @GetMapping
    @Operation(summary = "Get all configs grouped by category")
    public Result<Map<String, List<SysConfig>>> getAllGrouped() {
        return Result.success(sysConfigService.getConfigsByCategory());
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get configs for a specific category")
    public Result<List<SysConfig>> getByCategory(@PathVariable String category) {
        return Result.success(sysConfigService.getConfigsByCategory(category));
    }

    @GetMapping("/value/{key}")
    @Operation(summary = "Get single config value by key")
    public Result<Map<String, String>> getValue(@PathVariable String key) {
        String value = sysConfigService.getConfigValue(key);
        return Result.success(Map.of("key", key, "value", value != null ? value : ""));
    }

    @PutMapping("/value/{key}")
    @Operation(summary = "Update config value by key")
    public Result<Void> updateValue(@PathVariable String key, @RequestBody Map<String, String> body) {
        sysConfigService.updateConfigValue(key, body.get("value"));
        return Result.success();
    }

    @PutMapping("/batch")
    @Operation(summary = "Batch update configs")
    public Result<Void> batchUpdate(@RequestBody Map<String, String> configs) {
        sysConfigService.batchUpdateConfigs(configs);
        return Result.success();
    }

    @PostMapping
    @Operation(summary = "Create config")
    public Result<SysConfig> create(@Valid @RequestBody SysConfigDTO dto) {
        return Result.success(sysConfigService.createConfig(dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete config")
    public Result<Void> delete(@PathVariable Long id) {
        sysConfigService.deleteConfig(id);
        return Result.success();
    }
}
