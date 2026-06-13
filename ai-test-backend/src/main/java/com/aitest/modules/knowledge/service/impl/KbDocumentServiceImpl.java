package com.aitest.modules.knowledge.service.impl;

import com.aitest.common.exception.BusinessException;
import com.aitest.common.result.PageResult;
import com.aitest.modules.knowledge.dto.KbDocumentDTO;
import com.aitest.modules.knowledge.dto.KbDocumentQueryDTO;
import com.aitest.modules.knowledge.entity.KbChunk;
import com.aitest.modules.knowledge.entity.KbDocument;
import com.aitest.modules.knowledge.mapper.KbChunkMapper;
import com.aitest.modules.knowledge.mapper.KbDocumentMapper;
import com.aitest.modules.knowledge.service.KbDocumentService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KbDocumentServiceImpl extends ServiceImpl<KbDocumentMapper, KbDocument> implements KbDocumentService {

    private final KbChunkMapper kbChunkMapper;

    private static final String UPLOAD_DIR = "uploads/knowledge/";

    @Override
    public PageResult<KbDocument> listDocuments(KbDocumentQueryDTO query) {
        Page<KbDocument> page = new Page<>(query.getPage(), query.getSize());

        LambdaQueryWrapper<KbDocument> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(query.getCategoryId() != null, KbDocument::getCategoryId, query.getCategoryId())
               .eq(query.getSystemId() != null, KbDocument::getSystemId, query.getSystemId())
               .eq(StringUtils.hasText(query.getDocType()), KbDocument::getDocType, query.getDocType())
               .eq(StringUtils.hasText(query.getParseStatus()), KbDocument::getParseStatus, query.getParseStatus())
               .and(StringUtils.hasText(query.getKeyword()), w ->
                       w.like(KbDocument::getDocCode, query.getKeyword())
                        .or()
                        .like(KbDocument::getDocName, query.getKeyword()))
               .orderByDesc(KbDocument::getCreatedAt);

        Page<KbDocument> result = page(page, wrapper);
        return PageResult.of(
                result.getTotal(), result.getCurrent(), result.getSize(),
                result.getPages(), result.getRecords());
    }

    @Override
    public KbDocument getDocument(Long id) {
        KbDocument document = getById(id);
        if (document == null) {
            throw new BusinessException(404, "Document not found: " + id);
        }
        return document;
    }

    @Override
    public Map<String, Object> getDocumentDetail(Long id) {
        KbDocument document = getDocument(id);

        // Fetch associated chunks
        List<KbChunk> chunks = kbChunkMapper.selectList(
                new LambdaQueryWrapper<KbChunk>()
                        .eq(KbChunk::getDocumentId, id)
                        .orderByAsc(KbChunk::getChunkIndex)
        );

        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("document", document);
        detail.put("chunks", chunks);
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KbDocument uploadDocument(KbDocumentDTO dto, MultipartFile file) {
        // Create upload directory if needed
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = UUID.randomUUID().toString().replace("-", "") + extension;
        String filePath = UPLOAD_DIR + uniqueFilename;

        // Save file to disk
        try {
            file.transferTo(new File(filePath));
            log.info("File saved: {} -> {}", originalFilename, filePath);
        } catch (IOException e) {
            throw new BusinessException(500, "Failed to save file: " + e.getMessage());
        }

        // Generate doc_code
        String docCode = "DOC-" + System.currentTimeMillis();

        // Determine doc_type from file extension if not provided
        String docType = dto.getDocType();
        if (!StringUtils.hasText(docType) && originalFilename != null) {
            docType = inferDocType(originalFilename);
        }

        // Create document record
        KbDocument document = new KbDocument();
        document.setDocCode(docCode);
        document.setDocName(dto.getDocName());
        document.setCategoryId(dto.getCategoryId());
        document.setSystemId(dto.getSystemId());
        document.setDocType(docType);
        document.setFilePath(filePath);
        document.setFileSize(file.getSize());
        document.setParseStatus("PENDING");

        // Store description in metadata if provided
        if (StringUtils.hasText(dto.getDescription())) {
            document.setMetadata("{\"description\":\"" + dto.getDescription().replace("\"", "\\\"") + "\"}");
        }

        save(document);
        log.info("Document uploaded: {} ({})", docCode, dto.getDocName());
        return document;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void parseDocument(Long id) {
        KbDocument document = getDocument(id);

        // Update status to PARSING
        document.setParseStatus("PARSING");
        updateById(document);
        log.info("Document parsing started: {}", document.getDocCode());

        // Simulate parsing: generate mock chunks from document name
        List<KbChunk> chunks = generateMockChunks(document);

        // Delete old chunks if re-parsing
        kbChunkMapper.delete(
                new LambdaQueryWrapper<KbChunk>()
                        .eq(KbChunk::getDocumentId, id)
        );

        // Save new chunks
        for (KbChunk chunk : chunks) {
            kbChunkMapper.insert(chunk);
        }

        // Update parse result and status
        Map<String, Object> parseResult = new LinkedHashMap<>();
        parseResult.put("chunkCount", chunks.size());
        parseResult.put("parsedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        parseResult.put("features", chunks.stream()
                .map(c -> "功能点" + c.getChunkIndex() + ": " + document.getDocName())
                .collect(Collectors.toList()));

        document.setParseStatus("DONE");
        document.setParseResult(mapToJson(parseResult));
        document.setContent(chunks.stream()
                .map(KbChunk::getContent)
                .collect(Collectors.joining("\n\n")));
        updateById(document);

        log.info("Document parsing completed: {} -> {} chunks", document.getDocCode(), chunks.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDocument(Long id) {
        KbDocument document = getById(id);
        if (document == null) {
            throw new BusinessException(404, "Document not found: " + id);
        }

        // Delete associated chunks
        kbChunkMapper.delete(
                new LambdaQueryWrapper<KbChunk>()
                        .eq(KbChunk::getDocumentId, id)
        );

        // Delete file from disk
        if (StringUtils.hasText(document.getFilePath())) {
            File file = new File(document.getFilePath());
            if (file.exists()) {
                boolean deleted = file.delete();
                log.info("File deleted: {} -> {}", document.getFilePath(), deleted);
            }
        }

        removeById(id);
        log.info("Document deleted: {} ({})", id, document.getDocName());
    }

    @Override
    public List<Map<String, Object>> searchDocuments(String query, Long categoryId, Long systemId) {
        // Search chunks using LIKE (fallback from FULLTEXT)
        LambdaQueryWrapper<KbChunk> chunkWrapper = new LambdaQueryWrapper<>();
        chunkWrapper.like(KbChunk::getContent, query)
                    .orderByDesc(KbChunk::getCreatedAt)
                    .last("LIMIT 50");
        List<KbChunk> matchingChunks = kbChunkMapper.selectList(chunkWrapper);

        if (matchingChunks.isEmpty()) {
            return Collections.emptyList();
        }

        // Collect unique document IDs
        Set<Long> docIds = matchingChunks.stream()
                .map(KbChunk::getDocumentId)
                .collect(Collectors.toSet());

        // Fetch documents with optional filters
        LambdaQueryWrapper<KbDocument> docWrapper = new LambdaQueryWrapper<>();
        docWrapper.in(KbDocument::getId, docIds)
                  .eq(categoryId != null, KbDocument::getCategoryId, categoryId)
                  .eq(systemId != null, KbDocument::getSystemId, systemId);
        List<KbDocument> documents = list(docWrapper);

        // Build set of valid document IDs (after filtering)
        Set<Long> validDocIds = documents.stream()
                .map(KbDocument::getId)
                .collect(Collectors.toSet());

        // Group chunks by document
        Map<Long, List<KbChunk>> chunksByDoc = matchingChunks.stream()
                .filter(c -> validDocIds.contains(c.getDocumentId()))
                .collect(Collectors.groupingBy(KbChunk::getDocumentId));

        // Build result
        List<Map<String, Object>> results = new ArrayList<>();
        for (KbDocument doc : documents) {
            if (!chunksByDoc.containsKey(doc.getId())) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("document", doc);
            item.put("matchingChunks", chunksByDoc.get(doc.getId()));
            item.put("matchCount", chunksByDoc.get(doc.getId()).size());
            results.add(item);
        }

        // Sort by match count descending
        results.sort((a, b) -> Integer.compare(
                (int) b.get("matchCount"), (int) a.get("matchCount")));

        return results;
    }

    // ===================== Private helpers =====================

    /**
     * Generate mock chunks from document content for simulation.
     */
    private List<KbChunk> generateMockChunks(KbDocument document) {
        List<KbChunk> chunks = new ArrayList<>();
        int chunkCount = 3 + new Random().nextInt(3); // 3-5 chunks

        for (int i = 0; i < chunkCount; i++) {
            KbChunk chunk = new KbChunk();
            chunk.setDocumentId(document.getId());
            chunk.setChunkIndex(i + 1);
            chunk.setContentType("TEXT");
            chunk.setContent("功能点" + (i + 1) + ": " + document.getDocName()
                    + " - 这是第" + (i + 1) + "个解析片段，包含相关测试知识点和内容摘要。");
            chunk.setCreatedAt(LocalDateTime.now());
            chunks.add(chunk);
        }

        return chunks;
    }

    /**
     * Infer document type from file extension.
     */
    private String inferDocType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".pdf")) return "PDF";
        if (lower.endsWith(".doc") || lower.endsWith(".docx")) return "WORD";
        if (lower.endsWith(".md") || lower.endsWith(".markdown")) return "MARKDOWN";
        if (lower.endsWith(".xls") || lower.endsWith(".xlsx")) return "EXCEL";
        if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                || lower.endsWith(".gif") || lower.endsWith(".bmp")) return "IMAGE";
        if (lower.endsWith(".mp4") || lower.endsWith(".avi") || lower.endsWith(".mov")
                || lower.endsWith(".mkv")) return "VIDEO";
        return "UNKNOWN";
    }

    /**
     * Simple map-to-JSON conversion (avoids extra dependency on ObjectMapper in service).
     */
    private String mapToJson(Map<String, Object> map) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsString(map);
        } catch (Exception e) {
            log.warn("Failed to serialize map to JSON: {}", e.getMessage());
            return "{}";
        }
    }
}
