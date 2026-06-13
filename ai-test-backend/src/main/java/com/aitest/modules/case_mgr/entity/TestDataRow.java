package com.aitest.modules.case_mgr.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Test Data Row entity
 */
@Data
@TableName(value = "test_data_row", autoResultMap = true)
@Schema(description = "Test Data Row")
public class TestDataRow implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "Row ID")
    private Long id;

    @Schema(description = "Associated data table ID")
    private Long tableId;

    @Schema(description = "Row index (0-based)")
    private Integer rowIndex;

    @Schema(description = "Row data (JSON object mapping column name to value)")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String rowData;

    @Schema(description = "Row description")
    private String description;

    @Schema(description = "Create time")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
