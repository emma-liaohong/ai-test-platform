package com.aitest.modules.knowledge.controller;

import com.aitest.common.result.PageResult;
import com.aitest.common.result.Result;
import com.aitest.modules.knowledge.entity.KbVideo;
import com.aitest.modules.knowledge.service.KbVideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/knowledge/videos")
@RequiredArgsConstructor
@Tag(name = "知识库视频管理", description = "CRUD operations for knowledge base videos")
public class KbVideoController {

    private final KbVideoService kbVideoService;

    @GetMapping
    @Operation(summary = "分页查询视频", description = "List videos with pagination and optional filters")
    public Result<PageResult<KbVideo>> list(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "System ID filter") @RequestParam(required = false) Long systemId,
            @Parameter(description = "Video type filter: RECORD/EXECUTION") @RequestParam(required = false) String videoType) {
        PageResult<KbVideo> pageResult = kbVideoService.listVideos(page, size, systemId, videoType);
        return Result.success(pageResult);
    }

    @PostMapping
    @Operation(summary = "创建视频记录", description = "Register a new video record in knowledge base")
    public Result<KbVideo> create(@RequestBody KbVideo video) {
        KbVideo created = kbVideoService.createVideo(video);
        return Result.success(created);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除视频", description = "Delete a video record from knowledge base")
    public Result<Void> delete(
            @Parameter(description = "Video ID") @PathVariable Long id) {
        kbVideoService.deleteVideo(id);
        return Result.success();
    }
}
