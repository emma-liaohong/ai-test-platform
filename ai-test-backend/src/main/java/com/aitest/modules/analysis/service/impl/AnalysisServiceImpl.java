package com.aitest.modules.analysis.service.impl;

import com.aitest.common.exception.BusinessException;
import com.aitest.modules.analysis.dto.*;
import com.aitest.modules.analysis.service.AnalysisService;
import com.aitest.modules.case_mgr.dto.TestCaseDTO;
import com.aitest.modules.case_mgr.entity.TestCase;
import com.aitest.modules.case_mgr.entity.TestCaseStep;
import com.aitest.modules.case_mgr.service.TestCaseService;
import com.aitest.modules.knowledge.entity.KbDocument;
import com.aitest.modules.knowledge.service.KbDocumentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisServiceImpl implements AnalysisService {

    private final KbDocumentService kbDocumentService;

    @Lazy
    private final TestCaseService testCaseService;

    private final ObjectMapper objectMapper;

    /** In-memory cache for analysis results (in production, would use Redis or DB). */
    private final Map<Long, AnalysisResultDTO> analysisCache = new ConcurrentHashMap<>();

    // ──────────────────────────────────────────────────────────────────────────
    // Public API
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    public AnalysisResultDTO analyzeDocument(AnalysisRequestDTO request) {
        // 1. Load the document
        KbDocument document = kbDocumentService.getDocument(request.getDocumentId());
        if (document == null) {
            throw new BusinessException(404, "Document not found: " + request.getDocumentId());
        }

        // 2. Extract feature points (simulated AI)
        List<FeaturePoint> features = extractFeatures(document);

        // 3. Impact analysis (simulated)
        List<ImpactItem> impacts = analyzeImpact(document, request.getSystemId(), features);

        // 4. Generate test cases if requested
        List<GeneratedCase> generatedCases = new ArrayList<>();
        List<Long> createdCaseIds = new ArrayList<>();
        if (Boolean.TRUE.equals(request.getGenerateCases())) {
            generatedCases = generateCaseSpecs(features, document);
            if (request.getSystemId() != null) {
                createdCaseIds = generateCases(request.getDocumentId(), generatedCases, request.getSystemId());
            }
        }

        // 5. Build result
        AnalysisResultDTO result = new AnalysisResultDTO();
        result.setDocumentId(document.getId());
        result.setDocumentName(document.getDocName());
        result.setSystemId(request.getSystemId());
        result.setAnalysisType(request.getAnalysisType() != null ? request.getAnalysisType() : "FULL");
        result.setFeatures(features);
        result.setImpactAnalysis(impacts);
        result.setGeneratedCases(generatedCases);
        result.setCreatedCaseIds(createdCaseIds);
        result.setSummary(buildSummary(features, impacts, generatedCases));
        result.setAiAnalysis(buildAiAnalysis(document, features, impacts));
        result.setAnalyzedAt(LocalDateTime.now());

        // Cache result
        analysisCache.put(document.getId(), result);

        log.info("Analysis completed for document {}: {} features, {} impacts, {} cases",
                document.getDocCode(), features.size(), impacts.size(), generatedCases.size());

        return result;
    }

    @Override
    public List<Long> generateCases(Long documentId, List<GeneratedCase> caseSpecs, Long systemId) {
        List<Long> createdIds = new ArrayList<>();

        for (GeneratedCase spec : caseSpecs) {
            try {
                TestCaseDTO dto = new TestCaseDTO();
                dto.setCaseCode("TC-GEN-" + System.currentTimeMillis() + "-" + createdIds.size());
                dto.setCaseName(spec.getCaseName());
                dto.setCaseType(spec.getCaseType());
                dto.setSystemId(systemId);
                dto.setPriority(spec.getPriority());
                dto.setStatus("draft");
                dto.setPreconditions(spec.getPreconditions());
                dto.setExpectedResult(spec.getExpectedResult());
                dto.setTags("AI生成,需求分析");

                // Build steps
                List<TestCaseStep> steps = new ArrayList<>();
                for (Map<String, Object> stepMap : spec.getSteps()) {
                    TestCaseStep step = new TestCaseStep();
                    step.setStepOrder((Integer) stepMap.getOrDefault("stepOrder", steps.size() + 1));
                    step.setStepAction((String) stepMap.getOrDefault("stepAction", "click"));
                    step.setStepTarget((String) stepMap.getOrDefault("stepTarget", ""));
                    step.setStepValue((String) stepMap.get("stepValue"));
                    step.setStepDescription((String) stepMap.get("stepDescription"));
                    step.setExpectedResult((String) stepMap.get("expectedResult"));
                    steps.add(step);
                }
                dto.setSteps(steps);

                TestCase created = testCaseService.createCase(dto);
                createdIds.add(created.getId());
            } catch (Exception e) {
                log.warn("Failed to generate case '{}': {}", spec.getCaseName(), e.getMessage());
            }
        }

        log.info("Generated {} test cases from document {}", createdIds.size(), documentId);
        return createdIds;
    }

    @Override
    public AnalysisResultDTO getAnalysisResult(Long documentId) {
        AnalysisResultDTO cached = analysisCache.get(documentId);
        if (cached != null) {
            return cached;
        }
        throw new BusinessException(404, "No analysis result found for document: " + documentId);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Private helpers — simulated AI logic
    // ──────────────────────────────────────────────────────────────────────────

    private List<FeaturePoint> extractFeatures(KbDocument document) {
        List<FeaturePoint> features = new ArrayList<>();
        String docName = document.getDocName();

        // Mock feature data — in production this would call an LLM
        String[][] mockFeatures = {
                {"用户登录", "支持用户名密码登录，包含记住密码和自动登录功能", "NEW", "P0"},
                {"用户注册", "新用户注册流程，包含邮箱验证和手机验证", "NEW", "P0"},
                {"密码找回", "通过邮箱或手机找回密码，包含验证码校验", "NEW", "P1"},
                {"第三方登录", "支持微信、QQ、GitHub第三方登录", "NEW", "P2"},
                {"个人信息管理", "用户查看和修改个人信息，包含头像上传", "MODIFIED", "P1"},
                {"权限控制", "基于角色的访问控制，区分管理员和普通用户", "MODIFIED", "P0"},
                {"操作日志", "记录用户关键操作日志，支持查询和导出", "NEW", "P2"},
        };

        int count = 4 + new Random().nextInt(4); // 4–7 features
        for (int i = 0; i < Math.min(count, mockFeatures.length); i++) {
            FeaturePoint fp = new FeaturePoint();
            fp.setFeatureName(docName + " - " + mockFeatures[i][0]);
            fp.setDescription(mockFeatures[i][1]);
            fp.setFeatureType(mockFeatures[i][2]);
            fp.setBusinessRules(List.of(
                    "规则1: " + mockFeatures[i][0] + "需要输入有效参数",
                    "规则2: 操作成功后返回正确状态码",
                    "规则3: 异常情况返回友好错误提示"
            ));
            fp.setRelatedModules(List.of("前端页面", "后端接口", "数据库"));
            fp.setPriority(mockFeatures[i][3]);
            features.add(fp);
        }

        return features;
    }

    private List<ImpactItem> analyzeImpact(KbDocument document, Long systemId, List<FeaturePoint> features) {
        List<ImpactItem> impacts = new ArrayList<>();

        // Impact on existing test cases
        ImpactItem caseImpact = new ImpactItem();
        caseImpact.setAffectedModule("测试案例");
        caseImpact.setImpactLevel("HIGH");
        caseImpact.setDescription("新增/修改的功能点可能影响现有 " + (5 + new Random().nextInt(10)) + " 个测试案例");
        caseImpact.setAffectedCaseIds(List.of(1L, 2L, 3L, 5L, 8L));
        caseImpact.setSuggestion("建议重新执行受影响的测试案例，验证新功能是否引入回归问题");
        impacts.add(caseImpact);

        // Impact on API contracts
        ImpactItem apiImpact = new ImpactItem();
        apiImpact.setAffectedModule("接口契约");
        apiImpact.setImpactLevel("MEDIUM");
        apiImpact.setDescription("可能需要新增或修改 " + features.size() + " 个API接口");
        apiImpact.setAffectedCaseIds(List.of(10L, 11L, 12L));
        apiImpact.setSuggestion("更新接口文档和API测试案例的响应契约");
        impacts.add(apiImpact);

        // Impact on database
        ImpactItem dbImpact = new ImpactItem();
        dbImpact.setAffectedModule("数据库");
        dbImpact.setImpactLevel(features.size() > 5 ? "HIGH" : "LOW");
        dbImpact.setDescription("新功能可能需要新增数据表或修改现有表结构");
        dbImpact.setAffectedCaseIds(Collections.emptyList());
        dbImpact.setSuggestion("检查数据库迁移脚本，确保表结构与需求一致");
        impacts.add(dbImpact);

        return impacts;
    }

    private List<GeneratedCase> generateCaseSpecs(List<FeaturePoint> features, KbDocument document) {
        List<GeneratedCase> cases = new ArrayList<>();

        for (FeaturePoint fp : features) {
            // ── Normal flow case ──
            GeneratedCase normalCase = new GeneratedCase();
            normalCase.setCaseName(fp.getFeatureName() + " - 正常流程");
            normalCase.setCaseType("PC");
            normalCase.setPriority(fp.getPriority());
            normalCase.setPreconditions("1. 系统已部署最新版本\n2. 测试账号已准备就绪");
            normalCase.setSteps(List.of(
                    Map.of("stepOrder", 1, "stepAction", "click", "stepTarget", "导航到功能页面",
                            "stepDescription", "打开" + fp.getFeatureName() + "页面",
                            "expectedResult", "页面正常加载"),
                    Map.of("stepOrder", 2, "stepAction", "input", "stepTarget", "输入表单",
                            "stepDescription", "填写必要信息",
                            "expectedResult", "表单数据正确填入"),
                    Map.of("stepOrder", 3, "stepAction", "click", "stepTarget", "提交按钮",
                            "stepDescription", "点击提交/确认",
                            "expectedResult", "操作成功，显示成功提示"),
                    Map.of("stepOrder", 4, "stepAction", "assert", "stepTarget", "结果区域",
                            "stepDescription", "验证操作结果",
                            "expectedResult", fp.getFeatureName() + "功能正常执行")
            ));
            normalCase.setExpectedResult(fp.getFeatureName() + "功能正常完成");
            normalCase.setSourceFeature(fp.getFeatureName());
            cases.add(normalCase);

            // ── Exception case ──
            GeneratedCase exceptionCase = new GeneratedCase();
            exceptionCase.setCaseName(fp.getFeatureName() + " - 异常场景");
            exceptionCase.setCaseType("PC");
            exceptionCase.setPriority("P0".equals(fp.getPriority()) ? "P1" : fp.getPriority());
            exceptionCase.setPreconditions("1. 系统已部署最新版本");
            exceptionCase.setSteps(List.of(
                    Map.of("stepOrder", 1, "stepAction", "click", "stepTarget", "导航到功能页面",
                            "stepDescription", "打开" + fp.getFeatureName() + "页面",
                            "expectedResult", "页面正常加载"),
                    Map.of("stepOrder", 2, "stepAction", "input", "stepTarget", "输入表单",
                            "stepValue", "无效数据/空值",
                            "stepDescription", "填写无效或不完整的数据",
                            "expectedResult", "表单校验提示错误"),
                    Map.of("stepOrder", 3, "stepAction", "click", "stepTarget", "提交按钮",
                            "stepDescription", "点击提交",
                            "expectedResult", "显示友好的错误提示信息")
            ));
            exceptionCase.setExpectedResult("系统正确处理异常，显示错误提示");
            exceptionCase.setSourceFeature(fp.getFeatureName());
            cases.add(exceptionCase);
        }

        return cases;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Summary / report builders
    // ──────────────────────────────────────────────────────────────────────────

    private String buildSummary(List<FeaturePoint> features, List<ImpactItem> impacts, List<GeneratedCase> cases) {
        long highImpact = impacts.stream().filter(i -> "HIGH".equals(i.getImpactLevel())).count();
        return String.format(
                "文档分析完成：提取 %d 个功能点，识别 %d 个影响项（其中 %d 个高风险），生成 %d 个测试案例。",
                features.size(), impacts.size(), highImpact, cases.size()
        );
    }

    private String buildAiAnalysis(KbDocument document, List<FeaturePoint> features, List<ImpactItem> impacts) {
        StringBuilder sb = new StringBuilder();
        sb.append("## 需求分析报告\n\n");
        sb.append("### 文档: ").append(document.getDocName()).append("\n\n");

        // Feature section
        sb.append("### 功能点提取\n\n");
        sb.append("共识别 **").append(features.size()).append("** 个功能点：\n\n");
        for (int i = 0; i < features.size(); i++) {
            FeaturePoint fp = features.get(i);
            String typeIcon = "NEW".equals(fp.getFeatureType()) ? "🆕" : "🔄";
            sb.append(String.format("%d. %s **%s** [%s] — %s\n",
                    i + 1, typeIcon, fp.getFeatureName(), fp.getPriority(), fp.getDescription()));
        }

        // Impact section
        sb.append("\n### 影响分析\n\n");
        for (ImpactItem item : impacts) {
            String levelIcon = switch (item.getImpactLevel()) {
                case "HIGH"   -> "🔴";
                case "MEDIUM" -> "🟠";
                default       -> "🟢";
            };
            sb.append(String.format("- %s **%s** (%s): %s\n",
                    levelIcon, item.getAffectedModule(), item.getImpactLevel(), item.getDescription()));
            sb.append("  - 建议: ").append(item.getSuggestion()).append("\n");
        }

        // Test recommendations
        sb.append("\n### 测试建议\n\n");
        sb.append("- P0案例: 核心功能正常流程必须覆盖\n");
        sb.append("- P1案例: 异常场景和边界条件\n");
        sb.append("- P2案例: 兼容性和性能相关\n");
        sb.append("- 建议执行全量回归测试确保无回归问题\n");
        return sb.toString();
    }
}
