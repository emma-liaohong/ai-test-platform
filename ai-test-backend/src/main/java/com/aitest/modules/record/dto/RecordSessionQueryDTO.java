package com.aitest.modules.record.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "录制会话查询DTO")
public class RecordSessionQueryDTO implements Serializable {

    @Schema(description = "页码", defaultValue = "1")
    private int page = 1;

    @Schema(description = "每页大小", defaultValue = "10")
    private int size = 10;

    @Schema(description = "关键字搜索(会话名称/会话ID)")
    private String keyword;

    @Schema(description = "所属系统ID")
    private Long systemId;

    @Schema(description = "状态过滤: RECORDING/STOPPED/GENERATED")
    private String status;
}
