package com.aitest.modules.system.controller;

import com.aitest.common.exception.BusinessException;
import com.aitest.common.result.Result;
import com.aitest.common.utils.JwtUtils;
import com.aitest.modules.system.dto.LoginDTO;
import com.aitest.modules.system.dto.LoginVO;
import com.aitest.modules.system.entity.SysUser;
import com.aitest.modules.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login / Logout / User Info APIs")
public class AuthController {

    private final SysUserService sysUserService;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate with username and password, returns JWT token")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        SysUser user = sysUserService.getByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new BusinessException(401, "User not found");
        }

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "Invalid password");
        }

        if (user.getStatus() != 1) {
            throw new BusinessException(403, "Account is disabled");
        }

        // Generate JWT token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());

        LoginVO.UserInfo userInfo = LoginVO.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .build();

        LoginVO loginVO = LoginVO.builder()
                .token(token)
                .tokenType("Bearer")
                .userInfo(userInfo)
                .build();

        log.info("User logged in: {}", user.getUsername());
        return Result.success(loginVO);
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout and invalidate token (client-side token removal)")
    public Result<Void> logout() {
        // TODO: Add token to Redis blacklist for server-side invalidation
        log.info("User logged out");
        return Result.success();
    }

    @GetMapping("/info")
    @Operation(summary = "Get current user info", description = "Get the authenticated user's information")
    public Result<LoginVO.UserInfo> getUserInfo() {
        // TODO: Extract user from SecurityContext after JWT filter is implemented
        // For now, return a placeholder
        return Result.success();
    }
}
