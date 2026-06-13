package com.aitest.modules.knowledge.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("kb_category")
@Schema(description = "Knowledge Base Category")
public class KbCategory implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "Category ID")
    private Long id;

    @Schema(description = "Category name")
    private String categoryName;

    @Schema(description = "Category type: CASE/REQUIREMENT/DESIGN/GIT/DEFECT/DATABASE/VIDEO")
    private String categoryType;

    @Schema(description = "Associated system ID")
    private Long systemId;

    @Schema(description = "Parent category ID, 0 for root")
    private Long parentId;

    @Schema(description = "Sort order")
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "Created time")
    private LocalDateTime createdAt;
}
