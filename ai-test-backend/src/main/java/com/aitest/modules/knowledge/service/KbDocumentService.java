package com.aitest.modules.knowledge.service;

import com.aitest.common.result.PageResult;
import com.aitest.modules.knowledge.dto.KbDocumentDTO;
import com.aitest.modules.knowledge.dto.KbDocumentQueryDTO;
import com.aitest.modules.knowledge.entity.KbDocument;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface KbDocumentService extends IService<KbDocument> {

    /**
     * Paginated document listing with filters.
     */
    PageResult<KbDocument> listDocuments(KbDocumentQueryDTO query);

    /**
     * Get a single document by ID.
     */
    KbDocument getDocument(Long id);

    /**
     * Get document detail including its chunks.
     *
     * @return map with keys: "document", "chunks"
     */
    Map<String, Object> getDocumentDetail(Long id);

    /**
     * Upload a document file and create a PENDING record.
     */
    KbDocument uploadDocument(KbDocumentDTO dto, MultipartFile file);

    /**
     * Trigger document parsing (simulated).
     * Sets status to PARSING, generates mock chunks, then sets to DONE.
     */
    void parseDocument(Long id);

    /**
     * Delete a document and its associated chunks.
     */
    void deleteDocument(Long id);

    /**
     * Search documents by keyword using full-text / LIKE search on chunks.
     *
     * @return list of matching chunks grouped by document
     */
    List<Map<String, Object>> searchDocuments(String query, Long categoryId, Long systemId);
}
