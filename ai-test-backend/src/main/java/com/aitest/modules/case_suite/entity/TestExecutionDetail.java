package com.aitest.modules.case_suite.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Test Execution Detail entity
 */
@Data
@TableName(value = "test_execution_detail", autoResultMap = true)
@Schema(description = "Test Execution Detail")
public class TestExecutionDetail implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "Detail ID")
    private Long id;

    @Schema(description = "Associated execution ID")
    private Long executionId;

    @Schema(description = "Associated case ID")
    private Long caseId;

    @Schema(description = "Status: SUCCESS, FAILED, SKIPPED, ERROR")
    private String status;

    @Schema(description = "Execution start time")
    private LocalDateTime startedAt;

    @Schema(description = "Execution finish time")
    private LocalDateTime finishedAt;

    @Schema(description = "Duration in milliseconds")
    private Long durationMs;

    @Schema(description = "Error message if failed")
    private String errorMessage;

    @Schema(description = "Error step description")
    private String errorStep;

    @Schema(description = "Screenshots (JSON array of paths)")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String screenshots;

    @Schema(description = "Video recording path")
    private String videoPath;

    @Schema(description = "Execution log content")
    private String executionLog;

    @Schema(description = "Comparison result (JSON)")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String compareResult;

    @Schema(description = "AI analysis result")
    private String aiAnalysis;

    @Schema(description = "Create time")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
