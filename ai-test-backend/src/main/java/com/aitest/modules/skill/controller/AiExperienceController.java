package com.aitest.modules.skill.controller;

import com.aitest.common.result.PageResult;
import com.aitest.common.result.Result;
import com.aitest.modules.skill.dto.ExperienceDTO;
import com.aitest.modules.skill.dto.ExperienceQueryDTO;
import com.aitest.modules.skill.entity.AiExperience;
import com.aitest.modules.skill.service.AiExperienceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/experiences")
@RequiredArgsConstructor
@Tag(name = "经验管理", description = "CRUD and promotion for AI experiences")
public class AiExperienceController {

    private final AiExperienceService aiExperienceService;

    @GetMapping
    @Operation(summary = "List experiences (paginated)")
    public Result<PageResult<AiExperience>> list(ExperienceQueryDTO query) {
        return Result.success(aiExperienceService.listExperiences(query));
    }

    @PostMapping
    @Operation(summary = "Create experience")
    public Result<AiExperience> create(@RequestBody ExperienceDTO dto) {
        return Result.success(aiExperienceService.createExperience(dto));
    }

    @PostMapping("/{id}/promote")
    @Operation(summary = "Promote experience to skill")
    public Result<Void> promote(
            @Parameter(description = "Experience ID") @PathVariable Long id) {
        aiExperienceService.promoteToSkill(id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete experience")
    public Result<Void> delete(
            @Parameter(description = "Experience ID") @PathVariable Long id) {
        aiExperienceService.deleteExperience(id);
        return Result.success();
    }
}
