package com.aitest.modules.system.service;

import com.aitest.modules.system.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * SysUser Service interface
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * Find user by username
     *
     * @param username username
     * @return SysUser or null
     */
    SysUser getByUsername(String username);

    /**
     * Register a new user
     *
     * @param user user entity (password will be encoded)
     * @return saved user
     */
    SysUser register(SysUser user);
}
