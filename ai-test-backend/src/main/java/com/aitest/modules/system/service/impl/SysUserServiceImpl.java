package com.aitest.modules.system.service.impl;

import com.aitest.common.exception.BusinessException;
import com.aitest.modules.system.entity.SysUser;
import com.aitest.modules.system.mapper.SysUserMapper;
import com.aitest.modules.system.service.SysUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * SysUser Service implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final PasswordEncoder passwordEncoder;

    @Override
    public SysUser getByUsername(String username) {
        return getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
    }

    @Override
    public SysUser register(SysUser user) {
        // Check if username already exists
        SysUser existing = getByUsername(user.getUsername());
        if (existing != null) {
            throw new BusinessException(400, "Username already exists");
        }

        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(1);
        user.setDeleted(0);

        save(user);
        log.info("User registered: {}", user.getUsername());
        return user;
    }
}
