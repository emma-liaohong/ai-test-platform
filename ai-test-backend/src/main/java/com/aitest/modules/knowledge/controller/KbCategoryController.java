package com.aitest.modules.knowledge.controller;

import com.aitest.common.result.Result;
import com.aitest.modules.knowledge.dto.KbCategoryDTO;
import com.aitest.modules.knowledge.entity.KbCategory;
import com.aitest.modules.knowledge.service.KbCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/knowledge/categories")
@RequiredArgsConstructor
@Tag(name = "知识库分类管理", description = "CRUD operations for knowledge base categories")
public class KbCategoryController {

    private final KbCategoryService kbCategoryService;

    @GetMapping
    @Operation(summary = "获取分类列表", description = "Get category list, optionally filtered by systemId")
    public Result<List<KbCategory>> list(
            @Parameter(description = "System ID filter") @RequestParam(required = false) Long systemId) {
        List<KbCategory> categories = kbCategoryService.getCategoryTree(systemId);
        return Result.success(categories);
    }

    @PostMapping
    @Operation(summary = "创建分类", description = "Create a new knowledge category")
    public Result<KbCategory> create(@Valid @RequestBody KbCategoryDTO dto) {
        KbCategory created = kbCategoryService.createCategory(dto);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新分类", description = "Update an existing category")
    public Result<KbCategory> update(
            @Parameter(description = "Category ID") @PathVariable Long id,
            @Valid @RequestBody KbCategoryDTO dto) {
        KbCategory updated = kbCategoryService.updateCategory(id, dto);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类", description = "Delete a category (must have no documents or sub-categories)")
    public Result<Void> delete(
            @Parameter(description = "Category ID") @PathVariable Long id) {
        kbCategoryService.deleteCategory(id);
        return Result.success();
    }
}
