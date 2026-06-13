package com.aitest.modules.knowledge.service.impl;

import com.aitest.common.exception.BusinessException;
import com.aitest.modules.knowledge.dto.KbCategoryDTO;
import com.aitest.modules.knowledge.entity.KbCategory;
import com.aitest.modules.knowledge.entity.KbDocument;
import com.aitest.modules.knowledge.mapper.KbDocumentMapper;
import com.aitest.modules.knowledge.mapper.KbCategoryMapper;
import com.aitest.modules.knowledge.service.KbCategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KbCategoryServiceImpl extends ServiceImpl<KbCategoryMapper, KbCategory> implements KbCategoryService {

    private final KbDocumentMapper kbDocumentMapper;

    @Override
    public List<KbCategory> getCategoryTree(Long systemId) {
        LambdaQueryWrapper<KbCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(systemId != null, KbCategory::getSystemId, systemId)
               .orderByAsc(KbCategory::getSortOrder)
               .orderByAsc(KbCategory::getId);
        return list(wrapper);
    }

    @Override
    public KbCategory createCategory(KbCategoryDTO dto) {
        KbCategory category = new KbCategory();
        category.setCategoryName(dto.getCategoryName());
        category.setCategoryType(dto.getCategoryType());
        category.setSystemId(dto.getSystemId());
        category.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        category.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);

        save(category);
        log.info("Category created: {} (type={})", category.getCategoryName(), category.getCategoryType());
        return category;
    }

    @Override
    public KbCategory updateCategory(Long id, KbCategoryDTO dto) {
        KbCategory category = getById(id);
        if (category == null) {
            throw new BusinessException(404, "Category not found: " + id);
        }

        category.setCategoryName(dto.getCategoryName());
        category.setCategoryType(dto.getCategoryType());
        category.setSystemId(dto.getSystemId());
        if (dto.getParentId() != null) {
            category.setParentId(dto.getParentId());
        }
        if (dto.getSortOrder() != null) {
            category.setSortOrder(dto.getSortOrder());
        }

        updateById(category);
        log.info("Category updated: {} -> {}", id, category.getCategoryName());
        return category;
    }

    @Override
    public void deleteCategory(Long id) {
        KbCategory category = getById(id);
        if (category == null) {
            throw new BusinessException(404, "Category not found: " + id);
        }

        // Check if any documents exist under this category
        long docCount = kbDocumentMapper.selectCount(
                new LambdaQueryWrapper<KbDocument>()
                        .eq(KbDocument::getCategoryId, id)
        );
        if (docCount > 0) {
            throw new BusinessException(400, "Cannot delete category with " + docCount + " document(s) under it");
        }

        // Check child categories
        long childCount = count(
                new LambdaQueryWrapper<KbCategory>()
                        .eq(KbCategory::getParentId, id)
        );
        if (childCount > 0) {
            throw new BusinessException(400, "Cannot delete category with " + childCount + " sub-category(ies)");
        }

        removeById(id);
        log.info("Category deleted: {} ({})", id, category.getCategoryName());
    }
}
