package com.aitest.modules.defect.controller;

import com.aitest.common.result.PageResult;
import com.aitest.common.result.Result;
import com.aitest.modules.defect.dto.DefectDTO;
import com.aitest.modules.defect.dto.DefectQueryDTO;
import com.aitest.modules.defect.entity.Defect;
import com.aitest.modules.defect.service.DefectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/defects")
@RequiredArgsConstructor
@Tag(name = "缺陷管理", description = "CRUD and statistics for defect tracking")
public class DefectController {

    private final DefectService defectService;

    @GetMapping
    @Operation(summary = "List defects (paginated)")
    public Result<PageResult<Defect>> list(DefectQueryDTO query) {
        return Result.success(defectService.listDefects(query));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get defect detail")
    public Result<Defect> getDetail(@Parameter(description = "Defect ID") @PathVariable Long id) {
        return Result.success(defectService.getDefect(id));
    }

    @PostMapping
    @Operation(summary = "Create defect")
    public Result<Defect> create(@RequestBody DefectDTO dto) {
        return Result.success(defectService.createDefect(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update defect")
    public Result<Defect> update(@PathVariable Long id, @RequestBody DefectDTO dto) {
        return Result.success(defectService.updateDefect(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete defect")
    public Result<Void> delete(@PathVariable Long id) {
        defectService.deleteDefect(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update defect status")
    public Result<Defect> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        return Result.success(defectService.updateStatus(id, status));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get defect statistics")
    public Result<Map<String, Object>> getStatistics(
            @Parameter(description = "System ID (optional, omit for global stats)") @RequestParam(required = false) Long systemId) {
        return Result.success(defectService.getStatistics(systemId));
    }
}
