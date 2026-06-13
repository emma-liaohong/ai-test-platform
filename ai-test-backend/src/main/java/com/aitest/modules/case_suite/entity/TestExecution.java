package com.aitest.modules.case_suite.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Test Execution entity
 */
@Data
@TableName(value = "test_execution", autoResultMap = true)
@Schema(description = "Test Execution")
public class TestExecution implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "Execution ID")
    private Long id;

    @Schema(description = "Execution name")
    private String executionName;

    @Schema(description = "Associated suite ID")
    private Long suiteId;

    @Schema(description = "Associated system ID")
    private Long systemId;

    @Schema(description = "Environment configuration (JSON)")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String envConfig;

    @Schema(description = "Status: PENDING, RUNNING, SUCCESS, FAILED, CANCELLED")
    private String status;

    @Schema(description = "Trigger type: MANUAL, SCHEDULE, CI")
    private String triggerType;

    @Schema(description = "Total case count")
    private Integer totalCount;

    @Schema(description = "Passed count")
    private Integer passedCount;

    @Schema(description = "Failed count")
    private Integer failedCount;

    @Schema(description = "Skipped count")
    private Integer skippedCount;

    @Schema(description = "Execution start time")
    private LocalDateTime startedAt;

    @Schema(description = "Execution finish time")
    private LocalDateTime finishedAt;

    @Schema(description = "Report content (JSON)")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String reportContent;

    @Schema(description = "Creator ID")
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    @Schema(description = "Create time")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
