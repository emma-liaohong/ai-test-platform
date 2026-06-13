package com.aitest.modules.case_mgr.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Test Case Step entity
 */
@Data
@TableName("test_case_step")
@Schema(description = "Test Case Step")
public class TestCaseStep implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "Step ID")
    private Long id;

    @Schema(description = "Associated case ID")
    private Long caseId;

    @Schema(description = "Step order (1-based)")
    private Integer stepOrder;

    @Schema(description = "Step action (e.g. click, input, assert)")
    private String stepAction;

    @Schema(description = "Step target (e.g. CSS selector, element ID)")
    private String stepTarget;

    @Schema(description = "Step value (e.g. input text)")
    private String stepValue;

    @Schema(description = "Step description")
    private String stepDescription;

    @Schema(description = "Expected result for this step")
    private String expectedResult;

    @Schema(description = "Screenshot path after step execution")
    private String screenshotPath;

    @Schema(description = "Timeout in milliseconds")
    private Integer timeoutMs;

    @Schema(description = "Create time")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
