package com.aitest.modules.system.service;

import com.aitest.modules.system.entity.SysSystem;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * SysSystem Service interface
 */
public interface SysSystemService extends IService<SysSystem> {

    /**
     * Find system by code
     *
     * @param code system code
     * @return SysSystem or null
     */
    SysSystem getByCode(String code);
}
