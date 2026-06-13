package com.aitest.modules.knowledge.service;

import com.aitest.modules.knowledge.dto.KbCategoryDTO;
import com.aitest.modules.knowledge.entity.KbCategory;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface KbCategoryService extends IService<KbCategory> {

    /**
     * Get category list, optionally filtered by systemId.
     * Returns flat list — frontend builds tree structure.
     */
    List<KbCategory> getCategoryTree(Long systemId);

    /**
     * Create a new category.
     */
    KbCategory createCategory(KbCategoryDTO dto);

    /**
     * Update an existing category.
     */
    KbCategory updateCategory(Long id, KbCategoryDTO dto);

    /**
     * Delete a category. Fails if documents exist under it.
     */
    void deleteCategory(Long id);
}
