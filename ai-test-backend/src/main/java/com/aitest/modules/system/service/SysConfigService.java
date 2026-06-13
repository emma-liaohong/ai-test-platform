package com.aitest.modules.system.service;

import com.aitest.modules.system.dto.SysConfigDTO;
import com.aitest.modules.system.entity.SysConfig;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface SysConfigService extends IService<SysConfig> {

    /**
     * Get all configs grouped by category
     */
    Map<String, List<SysConfig>> getConfigsByCategory();

    /**
     * Get configs for a specific category
     */
    List<SysConfig> getConfigsByCategory(String category);

    /**
     * Get single config value by key
     */
    String getConfigValue(String key);

    /**
     * Get config with default value
     */
    String getConfigValue(String key, String defaultValue);

    /**
     * Update config value by key
     */
    void updateConfigValue(String key, String value);

    /**
     * Batch update configs (for settings page save)
     */
    void batchUpdateConfigs(Map<String, String> configs);

    /**
     * Create new config
     */
    SysConfig createConfig(SysConfigDTO dto);

    /**
     * Delete config
     */
    void deleteConfig(Long id);
}
