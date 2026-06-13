package com.aitest.modules.record.controller;

import com.aitest.common.result.PageResult;
import com.aitest.common.result.Result;
import com.aitest.modules.record.dto.RecordSessionDTO;
import com.aitest.modules.record.dto.RecordSessionQueryDTO;
import com.aitest.modules.record.dto.RecordStepDTO;
import com.aitest.modules.record.entity.RecordSession;
import com.aitest.modules.record.service.RecordSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/record")
@RequiredArgsConstructor
@Tag(name = "录制回放", description = "CRUD operations for recording/playback sessions")
public class RecordController {

    private final RecordSessionService recordSessionService;

    @GetMapping("/sessions")
    @Operation(summary = "分页查询录制会话", description = "List recording sessions with pagination and filters")
    public Result<PageResult<RecordSession>> listSessions(RecordSessionQueryDTO query) {
        return Result.success(recordSessionService.listSessions(query));
    }

    @GetMapping("/sessions/{id}")
    @Operation(summary = "获取会话详情", description = "Get recording session detail by ID")
    public Result<RecordSession> getSession(
            @Parameter(description = "会话ID") @PathVariable Long id) {
        return Result.success(recordSessionService.getSession(id));
    }

    @GetMapping("/sessions/by-id/{sessionId}")
    @Operation(summary = "根据会话编号获取详情", description = "Get recording session by session_id string")
    public Result<RecordSession> getSessionBySessionId(
            @Parameter(description = "会话编号") @PathVariable String sessionId) {
        return Result.success(recordSessionService.getSessionBySessionId(sessionId));
    }

    @PostMapping("/sessions/start")
    @Operation(summary = "开始录制", description = "Start a new recording session")
    public Result<RecordSession> startSession(@Valid @RequestBody RecordSessionDTO dto) {
        return Result.success(recordSessionService.startSession(dto));
    }

    @PostMapping("/sessions/{id}/stop")
    @Operation(summary = "停止录制", description = "Stop a recording session")
    public Result<RecordSession> stopSession(
            @Parameter(description = "会话ID") @PathVariable Long id) {
        return Result.success(recordSessionService.stopSession(id));
    }

    @PostMapping("/sessions/{id}/steps")
    @Operation(summary = "添加步骤", description = "Add a step to recording session")
    public Result<Void> addStep(
            @Parameter(description = "会话ID") @PathVariable Long id,
            @Valid @RequestBody RecordStepDTO step) {
        recordSessionService.addStep(id, step);
        return Result.success();
    }

    @DeleteMapping("/sessions/{id}")
    @Operation(summary = "删除会话", description = "Delete a recording session")
    public Result<Void> deleteSession(
            @Parameter(description = "会话ID") @PathVariable Long id) {
        recordSessionService.deleteSession(id);
        return Result.success();
    }

    @PostMapping("/sessions/{id}/generate")
    @Operation(summary = "生成测试用例", description = "Generate test case from recorded steps")
    public Result<Map<String, Object>> generateCase(
            @Parameter(description = "会话ID") @PathVariable Long id) {
        return Result.success(recordSessionService.generateCase(id));
    }
}
