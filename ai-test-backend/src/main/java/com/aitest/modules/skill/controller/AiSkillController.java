package com.aitest.modules.skill.controller;

import com.aitest.common.result.PageResult;
import com.aitest.common.result.Result;
import com.aitest.modules.skill.dto.AiSkillDTO;
import com.aitest.modules.skill.dto.AiSkillQueryDTO;
import com.aitest.modules.skill.dto.SkillInvokeDTO;
import com.aitest.modules.skill.entity.AiSkill;
import com.aitest.modules.skill.entity.AiSkillExecutionLog;
import com.aitest.modules.skill.service.AiSkillExecutionLogService;
import com.aitest.modules.skill.service.AiSkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
@Tag(name = "技能管理", description = "CRUD, invoke and intent matching for AI skills")
public class AiSkillController {

    private final AiSkillService aiSkillService;
    private final AiSkillExecutionLogService executionLogService;

    @GetMapping
    @Operation(summary = "List skills (paginated)")
    public Result<PageResult<AiSkill>> list(AiSkillQueryDTO query) {
        return Result.success(aiSkillService.listSkills(query));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get skill detail")
    public Result<AiSkill> getDetail(
            @Parameter(description = "Skill ID") @PathVariable Long id) {
        return Result.success(aiSkillService.getSkill(id));
    }

    @PostMapping
    @Operation(summary = "Create skill")
    public Result<AiSkill> create(@RequestBody AiSkillDTO dto) {
        return Result.success(aiSkillService.createSkill(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update skill")
    public Result<AiSkill> update(
            @Parameter(description = "Skill ID") @PathVariable Long id,
            @RequestBody AiSkillDTO dto) {
        return Result.success(aiSkillService.updateSkill(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete skill")
    public Result<Void> delete(
            @Parameter(description = "Skill ID") @PathVariable Long id) {
        aiSkillService.deleteSkill(id);
        return Result.success();
    }

    @PostMapping("/invoke")
    @Operation(summary = "Invoke skill execution")
    public Result<Map<String, Object>> invoke(@RequestBody SkillInvokeDTO dto) {
        return Result.success(aiSkillService.invokeSkill(dto));
    }

    @GetMapping("/intent")
    @Operation(summary = "Match intent from user input")
    public Result<Map<String, Object>> matchIntent(
            @Parameter(description = "User input query") @RequestParam String query) {
        return Result.success(aiSkillService.matchIntent(query));
    }

    @GetMapping("/active")
    @Operation(summary = "List all active skills")
    public Result<List<AiSkill>> listActive() {
        return Result.success(aiSkillService.listActiveSkills());
    }

    @GetMapping("/{id}/logs")
    @Operation(summary = "Get execution logs for a skill")
    public Result<PageResult<AiSkillExecutionLog>> getLogs(
            @Parameter(description = "Skill ID") @PathVariable Long id,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        return Result.success(executionLogService.listLogs(id, page, size));
    }

    @PostMapping("/logs/{logId}/feedback")
    @Operation(summary = "Submit feedback for an execution log")
    public Result<Void> submitFeedback(
            @Parameter(description = "Execution log ID") @PathVariable Long logId,
            @RequestBody Map<String, String> body) {
        String feedback = body.get("feedback");
        executionLogService.submitFeedback(logId, feedback);
        return Result.success();
    }
}
