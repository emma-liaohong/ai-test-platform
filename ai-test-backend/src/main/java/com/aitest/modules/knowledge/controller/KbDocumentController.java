package com.aitest.modules.knowledge.controller;

import com.aitest.common.result.PageResult;
import com.aitest.common.result.Result;
import com.aitest.modules.knowledge.dto.KbDocumentDTO;
import com.aitest.modules.knowledge.dto.KbDocumentQueryDTO;
import com.aitest.modules.knowledge.entity.KbDocument;
import com.aitest.modules.knowledge.service.KbDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/knowledge/documents")
@RequiredArgsConstructor
@Tag(name = "知识库文档管理", description = "Upload, parse, search and manage knowledge documents")
public class KbDocumentController {

    private final KbDocumentService kbDocumentService;

    @GetMapping
    @Operation(summary = "分页查询文档", description = "List documents with pagination and filters")
    public Result<PageResult<KbDocument>> list(KbDocumentQueryDTO query) {
        PageResult<KbDocument> pageResult = kbDocumentService.listDocuments(query);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取文档详情", description = "Get a single document with its parsed chunks")
    public Result<Map<String, Object>> getDetail(
            @Parameter(description = "Document ID") @PathVariable Long id) {
        Map<String, Object> detail = kbDocumentService.getDocumentDetail(id);
        return Result.success(detail);
    }

    @PostMapping("/upload")
    @Operation(summary = "上传文档", description = "Upload a document file with metadata")
    public Result<KbDocument> upload(
            @Valid @RequestPart("dto") KbDocumentDTO dto,
            @RequestPart("file") MultipartFile file) {
        KbDocument document = kbDocumentService.uploadDocument(dto, file);
        return Result.success(document);
    }

    @PostMapping("/{id}/parse")
    @Operation(summary = "解析文档", description = "Trigger document parsing (simulated)")
    public Result<Void> parse(
            @Parameter(description = "Document ID") @PathVariable Long id) {
        kbDocumentService.parseDocument(id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除文档", description = "Delete a document and its associated chunks")
    public Result<Void> delete(
            @Parameter(description = "Document ID") @PathVariable Long id) {
        kbDocumentService.deleteDocument(id);
        return Result.success();
    }

    @GetMapping("/search")
    @Operation(summary = "搜索知识库", description = "Search documents by keyword across chunks")
    public Result<List<Map<String, Object>>> search(
            @Parameter(description = "Search query") @RequestParam String query,
            @Parameter(description = "Category ID filter") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "System ID filter") @RequestParam(required = false) Long systemId) {
        List<Map<String, Object>> results = kbDocumentService.searchDocuments(query, categoryId, systemId);
        return Result.success(results);
    }
}
