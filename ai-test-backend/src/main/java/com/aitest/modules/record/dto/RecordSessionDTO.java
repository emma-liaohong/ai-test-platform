package com.aitest.modules.record.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "录制会话创建DTO")
public class RecordSessionDTO implements Serializable {

    @NotBlank(message = "会话名称不能为空")
    @Schema(description = "会话名称")
    private String sessionName;

    @Schema(description = "所属系统ID")
    private Long systemId;

    @NotBlank(message = "目标URL不能为空")
    @Schema(description = "目标URL")
    private String targetUrl;
}
