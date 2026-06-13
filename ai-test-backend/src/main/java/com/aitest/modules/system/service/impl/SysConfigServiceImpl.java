package com.aitest.modules.system.service.impl;

import com.aitest.common.exception.BusinessException;
import com.aitest.modules.system.dto.SysConfigDTO;
import com.aitest.modules.system.entity.SysConfig;
import com.aitest.modules.system.mapper.SysConfigMapper;
import com.aitest.modules.system.service.SysConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements SysConfigService {

    @Override
    public Map<String, List<SysConfig>> getConfigsByCategory() {
        List<SysConfig> allConfigs = list();
        if (allConfigs.isEmpty()) {
            return Collections.emptyMap();
        }
        return allConfigs.stream()
                .collect(Collectors.groupingBy(
                        config -> config.getCategory() != null ? config.getCategory() : "UNCATEGORIZED"));
    }

    @Override
    public List<SysConfig> getConfigsByCategory(String category) {
        return list(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getCategory, category)
                .orderByAsc(SysConfig::getConfigKey));
    }

    @Override
    public String getConfigValue(String key) {
        SysConfig config = getOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, key));
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    public String getConfigValue(String key, String defaultValue) {
        String value = getConfigValue(key);
        return value != null ? value : defaultValue;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfigValue(String key, String value) {
        SysConfig config = getOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, key));
        if (config == null) {
            throw new BusinessException(404, "Config key not found: " + key);
        }
        config.setConfigValue(value);
        updateById(config);
        log.info("Config updated: {} = {}", key, value);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateConfigs(Map<String, String> configs) {
        configs.forEach(this::updateConfigValue);
        log.info("Batch updated {} configs", configs.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysConfig createConfig(SysConfigDTO dto) {
        // Check key uniqueness
        SysConfig existing = getOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, dto.getConfigKey()));
        if (existing != null) {
            throw new BusinessException(400, "Config key already exists: " + dto.getConfigKey());
        }

        SysConfig config = new SysConfig();
        config.setConfigKey(dto.getConfigKey());
        config.setConfigValue(dto.getConfigValue());
        config.setConfigType(dto.getConfigType() != null ? dto.getConfigType() : "STRING");
        config.setCategory(dto.getCategory());
        config.setDescription(dto.getDescription());
        save(config);
        log.info("Config created: {}", dto.getConfigKey());
        return config;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfig(Long id) {
        SysConfig config = getById(id);
        if (config == null) {
            throw new BusinessException(404, "Config not found with id: " + id);
        }
        removeById(id);
        log.info("Config deleted: {}", config.getConfigKey());
    }
}
