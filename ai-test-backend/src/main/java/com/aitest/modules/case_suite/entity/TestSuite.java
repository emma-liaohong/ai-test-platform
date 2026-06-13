package com.aitest.modules.case_suite.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Test Suite entity
 */
@Data
@TableName("test_suite")
@Schema(description = "Test Suite")
public class TestSuite implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "Suite ID")
    private Long id;

    @Schema(description = "Suite name")
    private String suiteName;

    @Schema(description = "Associated system ID")
    private Long systemId;

    @Schema(description = "Suite description")
    private String description;

    @Schema(description = "Suite type: MANUAL, SMART, REGRESSION, SMOKE")
    private String suiteType;

    @Schema(description = "Creator ID")
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    @Schema(description = "Create time")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "Update time")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @Schema(description = "Deleted flag: 0=not deleted, 1=deleted")
    @TableLogic
    private Integer deleted;
}
