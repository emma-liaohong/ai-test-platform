package com.aitest.modules.knowledge.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("kb_video")
@Schema(description = "Knowledge Base Video")
public class KbVideo implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "Video ID")
    private Long id;

    @Schema(description = "Video name")
    private String videoName;

    @Schema(description = "Video type: RECORD/EXECUTION")
    private String videoType;

    @Schema(description = "Associated system ID")
    private Long systemId;

    @Schema(description = "Associated test case ID")
    private Long caseId;

    @Schema(description = "Associated execution ID")
    private Long executionId;

    @Schema(description = "File storage path")
    private String filePath;

    @Schema(description = "File size in bytes")
    private Long fileSize;

    @Schema(description = "Duration in milliseconds")
    private Long durationMs;

    @Schema(description = "Video resolution, e.g. 1920x1080")
    private String resolution;

    @Schema(description = "Thumbnail file path")
    private String thumbnailPath;

    @Schema(description = "Execution status")
    private String executionStatus;

    @Schema(description = "Video description")
    private String description;

    @Schema(description = "Created by user ID")
    private Long createdBy;

    @Schema(description = "Created time")
    private LocalDateTime createdAt;
}
