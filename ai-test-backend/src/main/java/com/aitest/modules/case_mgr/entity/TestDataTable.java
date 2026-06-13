package com.aitest.modules.case_mgr.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Test Data Table entity
 */
@Data
@TableName("test_data_table")
@Schema(description = "Test Data Table")
public class TestDataTable implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "Data table ID")
    private Long id;

    @Schema(description = "Table name")
    private String tableName;

    @Schema(description = "Table description")
    private String description;

    @Schema(description = "Column definitions (JSON array of column names)")
    private String columns;

    @Schema(description = "Associated system ID")
    private Long systemId;

    @Schema(description = "Status: 0=disabled, 1=enabled")
    private Integer status;

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
