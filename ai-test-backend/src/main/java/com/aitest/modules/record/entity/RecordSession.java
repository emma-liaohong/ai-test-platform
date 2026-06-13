package com.aitest.modules.record.entity;

import com.aitest.modules.record.dto.RecordStepDTO;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "record_session", autoResultMap = true)
@Schema(description = "录制会话")
public class RecordSession implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "会话ID")
    private String sessionId;

    @Schema(description = "会话名称")
    private String sessionName;

    @Schema(description = "所属系统ID")
    private Long systemId;

    @Schema(description = "目标URL")
    private String targetUrl;

    @Schema(description = "状态: RECORDING/STOPPED/GENERATED")
    private String status;

    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "录制的操作步骤")
    private List<RecordStepDTO> steps;

    @Schema(description = "步骤数量")
    private Integer stepCount;

    @Schema(description = "录像文件路径")
    private String videoPath;

    @Schema(description = "录制时长(毫秒)")
    private Long durationMs;

    @Schema(description = "生成的用例ID")
    private Long generatedCaseId;

    @Schema(description = "创建人")
    private Long createdBy;

    @Schema(description = "开始时间")
    private LocalDateTime startedAt;

    @Schema(description = "结束时间")
    private LocalDateTime stoppedAt;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
