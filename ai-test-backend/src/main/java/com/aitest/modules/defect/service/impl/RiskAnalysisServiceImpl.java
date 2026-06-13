package com.aitest.modules.defect.service.impl;

import com.aitest.common.exception.BusinessException;
import com.aitest.common.result.PageResult;
import com.aitest.modules.defect.dto.RiskAnalysisDTO;
import com.aitest.modules.defect.dto.RiskAnalysisQueryDTO;
import com.aitest.modules.defect.entity.RiskAnalysis;
import com.aitest.modules.defect.mapper.RiskAnalysisMapper;
import com.aitest.modules.defect.service.RiskAnalysisService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskAnalysisServiceImpl extends ServiceImpl<RiskAnalysisMapper, RiskAnalysis> implements RiskAnalysisService {

    private static final Random RANDOM = new Random();
    private static final String[] RISK_LEVELS = {"HIGH", "MEDIUM", "LOW"};

    private static final String[] RISK_TITLES = {
            "Interface compatibility degradation after API v2 migration",
            "Database connection pool exhaustion under high concurrency",
            "Regression risk on payment module after checkout flow changes",
            "Memory leak in long-running batch processing tasks",
            "Insufficient test coverage on user authentication edge cases",
            "Cross-service timeout cascading failure risk",
            "Data inconsistency in distributed transaction scenarios",
            "Third-party service dependency failure impact"
    };

    private static final String[] RISK_DESCRIPTIONS = {
            "After recent API version upgrade, backward compatibility issues may cause existing clients to fail.",
            "Under peak traffic conditions, the connection pool may be exhausted leading to service unavailability.",
            "Changes to the checkout flow may inadvertently affect payment processing logic.",
            "Long-running batch jobs show signs of increasing memory consumption over time.",
            "Edge cases in multi-factor authentication and session management lack adequate test coverage.",
            "A timeout in one downstream service may cascade to upstream services.",
            "Distributed transactions across multiple microservices may lead to data inconsistency.",
            "Failure of a critical third-party API could impact core business functionality."
    };

    private static final String[] RISK_IMPACTS = {
            "High — may affect production stability and user experience",
            "High — potential data loss or financial impact",
            "Medium — limited to specific user flows",
            "Medium — intermittent issues that are hard to reproduce",
            "Low — affects non-critical path",
            "High — could cause complete service outage"
    };

    private static final String[] RISK_SUGGESTIONS = {
            "Run full regression test suite on affected modules before deployment.",
            "Add load testing scenarios to validate connection pool sizing.",
            "Implement canary deployment to limit blast radius.",
            "Add monitoring alerts for memory usage thresholds.",
            "Increase unit test coverage for authentication edge cases.",
            "Implement circuit breaker pattern for external service calls.",
            "Add distributed tracing to identify timeout propagation paths.",
            "Design fallback mechanism for third-party service failures."
    };

    @Override
    public PageResult<RiskAnalysis> listAnalyses(RiskAnalysisQueryDTO query) {
        Page<RiskAnalysis> page = new Page<>(query.getPage(), query.getSize());

        LambdaQueryWrapper<RiskAnalysis> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(query.getSystemId() != null, RiskAnalysis::getSystemId, query.getSystemId())
               .eq(query.getAnalysisType() != null, RiskAnalysis::getAnalysisType, query.getAnalysisType())
               .orderByDesc(RiskAnalysis::getCreatedAt);

        Page<RiskAnalysis> result = page(page, wrapper);
        return PageResult.of(
                result.getTotal(), result.getCurrent(),
                result.getSize(), result.getPages(), result.getRecords());
    }

    @Override
    public RiskAnalysis getAnalysis(Long id) {
        RiskAnalysis analysis = getById(id);
        if (analysis == null) {
            throw new BusinessException(404, "Risk analysis not found: " + id);
        }
        return analysis;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RiskAnalysis createAnalysis(RiskAnalysisDTO dto) {
        RiskAnalysis analysis = new RiskAnalysis();
        analysis.setSystemId(dto.getSystemId());
        analysis.setAnalysisType(dto.getAnalysisType());
        analysis.setTitle(dto.getTitle());

        // Simulate AI analysis
        analysis.setRiskLevel(RISK_LEVELS[RANDOM.nextInt(RISK_LEVELS.length)]);
        analysis.setRiskItems(generateMockRiskItems());
        analysis.setAnalysisResult(generateMockAnalysisResult(dto.getAnalysisType(), dto.getTitle()));
        analysis.setSuggestions(generateMockSuggestions());
        analysis.setAiAnalysis(generateMockAiAnalysis(dto.getAnalysisType(), dto.getTitle()));

        save(analysis);
        log.info("Risk analysis created: {} (type={})", dto.getTitle(), dto.getAnalysisType());
        return analysis;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAnalysis(Long id) {
        RiskAnalysis analysis = getAnalysis(id);
        removeById(id);
        log.info("Risk analysis deleted: {}", analysis.getTitle());
    }

    // ── Mock AI generation helpers ──

    private String generateMockRiskItems() {
        int count = 3 + RANDOM.nextInt(3); // 3-5 items
        StringBuilder sb = new StringBuilder("[");
        Set<Integer> used = new HashSet<>();

        for (int i = 0; i < count; i++) {
            int idx;
            do {
                idx = RANDOM.nextInt(RISK_TITLES.length);
            } while (used.contains(idx) && used.size() < RISK_TITLES.length);
            used.add(idx);

            if (i > 0) sb.append(",");
            sb.append("{")
              .append("\"title\":\"").append(RISK_TITLES[idx]).append("\",")
              .append("\"riskLevel\":\"").append(RISK_LEVELS[RANDOM.nextInt(RISK_LEVELS.length)]).append("\",")
              .append("\"description\":\"").append(RISK_DESCRIPTIONS[idx]).append("\",")
              .append("\"impact\":\"").append(RISK_IMPACTS[RANDOM.nextInt(RISK_IMPACTS.length)]).append("\",")
              .append("\"suggestion\":\"").append(RISK_SUGGESTIONS[RANDOM.nextInt(RISK_SUGGESTIONS.length)]).append("\"")
              .append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    private String generateMockAnalysisResult(String analysisType, String title) {
        return String.format("""
                ## %s Analysis Result: %s

                Based on comprehensive analysis of the system's current state, we have identified several areas of concern that require attention before the next release. The overall risk posture has been evaluated considering historical defect data, code change patterns, and test coverage metrics.

                The analysis reveals that the primary risks are concentrated in areas with recent code changes and complex business logic. Test coverage gaps in critical paths increase the probability of undetected defects reaching production.

                Recommendation: Prioritize addressing HIGH-risk items and ensure all critical test cases pass before proceeding with deployment. A targeted regression testing round focusing on the identified risk areas is strongly recommended.
                """, analysisType, title).trim();
    }

    private String generateMockSuggestions() {
        return """
                1. Execute full regression suite on payment and checkout modules
                2. Add integration tests for cross-service communication paths
                3. Schedule performance testing for peak traffic scenarios
                4. Review and update test cases for edge cases in authentication flow
                5. Implement automated health checks for third-party dependencies
                6. Establish code coverage thresholds as quality gates in CI/CD pipeline
                7. Conduct pair review for all changes touching financial transaction logic
                8. Set up proactive monitoring for memory and connection pool metrics
                """;
    }

    private String generateMockAiAnalysis(String analysisType, String title) {
        return String.format("""
                # AI Risk Analysis Report

                ## Executive Summary
                This automated risk analysis was performed on the target system using historical defect patterns, code complexity metrics, and test coverage data. The analysis type is **%s** with focus on: *%s*.

                ## Risk Overview
                | Category | Count | Severity |
                |----------|-------|----------|
                | Critical | 1-2 | HIGH |
                | Significant | 2-3 | MEDIUM |
                | Minor | 1-2 | LOW |

                ## Key Findings

                ### 1. Code Change Impact
                Recent commits indicate significant changes in core business modules. The change density in payment processing and user authentication modules is above the historical average, suggesting higher defect probability in these areas.

                ### 2. Test Coverage Gaps
                Current test coverage shows gaps in:
                - Error handling paths (estimated 65%% coverage)
                - Concurrent access scenarios (estimated 40%% coverage)
                - Edge cases in input validation (estimated 70%% coverage)

                ### 3. Historical Defect Patterns
                Analysis of past defect data reveals that 40%% of S1/S2 defects originated from modules with recent refactoring. The correlation between code churn and defect rate is statistically significant (p < 0.05).

                ## Recommendations
                - **Immediate**: Add targeted test cases for identified HIGH-risk areas
                - **Short-term**: Increase integration test coverage to >80%% for critical paths
                - **Long-term**: Implement automated risk scoring in CI/CD pipeline

                ## Conclusion
                The overall risk level for this release is assessed as **MODERATE**. With appropriate mitigation actions, the risk can be reduced to an acceptable level for production deployment.
                """, analysisType, title);
    }
}
