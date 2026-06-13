package com.aitest.modules.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login response VO containing token and user info
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Login Response")
public class LoginVO {

    @Schema(description = "JWT access token")
    private String token;

    @Schema(description = "Token type")
    private String tokenType;

    @Schema(description = "User info")
    private UserInfo userInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "User Info")
    public static class UserInfo {

        @Schema(description = "User ID")
        private Long id;

        @Schema(description = "Username")
        private String username;

        @Schema(description = "Real name")
        private String realName;

        @Schema(description = "Email")
        private String email;

        @Schema(description = "Avatar URL")
        private String avatar;

        @Schema(description = "Role")
        private String role;
    }
}
