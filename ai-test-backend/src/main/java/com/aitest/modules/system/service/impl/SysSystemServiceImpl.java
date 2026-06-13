package com.aitest.modules.system.service.impl;

import com.aitest.modules.system.entity.SysSystem;
import com.aitest.modules.system.mapper.SysSystemMapper;
import com.aitest.modules.system.service.SysSystemService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * SysSystem Service implementation
 */
@Slf4j
@Service
public class SysSystemServiceImpl extends ServiceImpl<SysSystemMapper, SysSystem> implements SysSystemService {

    @Override
    public SysSystem getByCode(String code) {
        return getOne(new LambdaQueryWrapper<SysSystem>()
                .eq(SysSystem::getCode, code));
    }
}
