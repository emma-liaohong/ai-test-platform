package com.aitest.modules.record.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "录制步骤DTO")
public class RecordStepDTO implements Serializable {

    @Schema(description = "步骤序号")
    private int stepOrder;

    @Schema(description = "操作类型: click/input/select/swipe/wait/assert")
    private String action;

    @Schema(description = "目标元素(CSS选择器或坐标)")
    private String target;

    @Schema(description = "输入值")
    private String value;

    @Schema(description = "步骤描述")
    private String description;

    @Schema(description = "截图路径")
    private String screenshot;

    @Schema(description = "时间戳")
    private Long timestamp;
}
