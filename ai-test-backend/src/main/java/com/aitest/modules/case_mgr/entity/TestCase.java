package com.aitest.modules.case_mgr.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Test Case entity
 */
@Data
@TableName(value = "test_case", autoResultMap = true)
@Schema(description = "Test Case")
public class TestCase implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "Case ID")
    private Long id;

    @Schema(description = "Case code (unique identifier)")
    private String caseCode;

    @Schema(description = "Case name")
    private String caseName;

    @Schema(description = "Case type: PC, APP, API")
    private String caseType;

    @Schema(description = "Associated system ID")
    private Long systemId;

    @Schema(description = "Module path (e.g. /login/auth)")
    private String modulePath;

    @Schema(description = "Priority: P0, P1, P2, P3")
    private String priority;

    @Schema(description = "Case level: smoke, regression, full")
    private String caseLevel;

    @Schema(description = "Recorded session ID")
    private Long recordSessionId;

    @Schema(description = "Recorded steps (JSON)")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String recordedSteps;

    @Schema(description = "Natural language steps")
    private String naturalLanguageSteps;

    @Schema(description = "Generated Playwright script")
    private String playwrightScript;

    @Schema(description = "App operations (JSON)")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String appOperations;

    @Schema(description = "API URL")
    private String apiUrl;

    @Schema(description = "API method: GET, POST, PUT, DELETE")
    private String apiMethod;

    @Schema(description = "API request schema (JSON)")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String apiRequestSchema;

    @Schema(description = "API response schema (JSON)")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String apiResponseSchema;

    @Schema(description = "API headers (JSON)")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String apiHeaders;

    @Schema(description = "Preconditions for test execution")
    private String preconditions;

    @Schema(description = "Expected result")
    private String expectedResult;

    @Schema(description = "Tags (comma-separated)")
    private String tags;

    @Schema(description = "Status: draft, ready, deprecated")
    private String status;

    @Schema(description = "Whether data-driven: 0=no, 1=yes")
    private Integer isDataDriven;

    @Schema(description = "Associated data table ID")
    private Long dataTableId;

    @Schema(description = "Creator ID")
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    @Schema(description = "Create time")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "Updater ID")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;

    @Schema(description = "Update time")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @Schema(description = "Deleted flag: 0=not deleted, 1=deleted")
    @TableLogic
    private Integer deleted;
}
