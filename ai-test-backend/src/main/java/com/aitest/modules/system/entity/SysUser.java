package com.aitest.modules.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * System User entity
 */
@Data
@TableName("sys_user")
@Schema(description = "System User")
public class SysUser implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "User ID")
    private Long id;

    @Schema(description = "Username")
    private String username;

    @Schema(description = "Password (encrypted)")
    private String password;

    @Schema(description = "Real name")
    private String realName;

    @Schema(description = "Email")
    private String email;

    @Schema(description = "Phone number")
    private String phone;

    @Schema(description = "Avatar URL")
    private String avatar;

    @Schema(description = "Status: 0=disabled, 1=enabled")
    private Integer status;

    @Schema(description = "Role: admin, tester, developer")
    private String role;

    @Schema(description = "Creator ID")
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @Schema(description = "Create time")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "Updater ID")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    @Schema(description = "Update time")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "Deleted flag: 0=not deleted, 1=deleted")
    @TableLogic
    private Integer deleted;
}
