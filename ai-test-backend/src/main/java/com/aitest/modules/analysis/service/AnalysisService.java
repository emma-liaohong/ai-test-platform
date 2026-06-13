package com.aitest.modules.analysis.service;

import com.aitest.modules.analysis.dto.AnalysisRequestDTO;
import com.aitest.modules.analysis.dto.AnalysisResultDTO;
import com.aitest.modules.analysis.dto.GeneratedCase;

import java.util.List;

public interface AnalysisService {

    /**
     * Main analysis: parse document → extract features → impact analysis → optionally generate cases.
     */
    AnalysisResultDTO analyzeDocument(AnalysisRequestDTO request);

    /**
     * Persist generated case specs into the test_case table.
     *
     * @param documentId source document id (for logging / tracing)
     * @param cases      case specifications produced by the analysis
     * @param systemId   target system id
     * @return list of newly created case ids
     */
    List<Long> generateCases(Long documentId, List<GeneratedCase> cases, Long systemId);

    /**
     * Retrieve a previously computed analysis result (in-memory cache).
     */
    AnalysisResultDTO getAnalysisResult(Long documentId);
}
