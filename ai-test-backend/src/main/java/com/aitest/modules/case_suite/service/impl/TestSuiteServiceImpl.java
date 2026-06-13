package com.aitest.modules.case_suite.service.impl;

import com.aitest.common.exception.BusinessException;
import com.aitest.common.result.PageResult;
import com.aitest.modules.case_mgr.entity.TestCase;
import com.aitest.modules.case_mgr.mapper.TestCaseMapper;
import com.aitest.modules.case_suite.dto.ExecuteRequestDTO;
import com.aitest.modules.case_suite.dto.TestSuiteDTO;
import com.aitest.modules.case_suite.entity.TestExecution;
import com.aitest.modules.case_suite.entity.TestExecutionDetail;
import com.aitest.modules.case_suite.entity.TestSuite;
import com.aitest.modules.case_suite.entity.TestSuiteCase;
import com.aitest.modules.case_suite.mapper.TestExecutionDetailMapper;
import com.aitest.modules.case_suite.mapper.TestExecutionMapper;
import com.aitest.modules.case_suite.mapper.TestSuiteCaseMapper;
import com.aitest.modules.case_suite.mapper.TestSuiteMapper;
import com.aitest.modules.case_suite.service.TestSuiteService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * TestSuite Service implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestSuiteServiceImpl extends ServiceImpl<TestSuiteMapper, TestSuite> implements TestSuiteService {

    private final TestSuiteCaseMapper testSuiteCaseMapper;
    private final TestCaseMapper testCaseMapper;
    private final TestExecutionMapper testExecutionMapper;
    private final TestExecutionDetailMapper testExecutionDetailMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TestSuite createSuite(TestSuiteDTO dto) {
        // Check if suite name already exists for the same system
        LambdaQueryWrapper<TestSuite> nameWrapper = new LambdaQueryWrapper<>();
        nameWrapper.eq(TestSuite::getSuiteName, dto.getSuiteName())
                   .eq(TestSuite::getSystemId, dto.getSystemId());
        if (count(nameWrapper) > 0) {
            throw new BusinessException(400, "Suite name already exists in this system: " + dto.getSuiteName());
        }

        // Build and save the suite
        TestSuite suite = new TestSuite();
        suite.setSuiteName(dto.getSuiteName());
        suite.setSystemId(dto.getSystemId());
        suite.setDescription(dto.getDescription());
        suite.setSuiteType(dto.getSuiteType());
        suite.setDeleted(0);

        save(suite);
        log.info("Test suite created: {} ({})", suite.getId(), suite.getSuiteName());

        // Save case associations if provided
        if (!CollectionUtils.isEmpty(dto.getCaseIds())) {
            saveSuiteCases(suite.getId(), dto.getCaseIds());
        }

        return suite;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TestSuite updateSuite(Long id, TestSuiteDTO dto) {
        TestSuite existing = getById(id);
        if (existing == null) {
            throw new BusinessException(404, "Test suite not found: " + id);
        }

        // Check suite name uniqueness (exclude self)
        if (StringUtils.hasText(dto.getSuiteName()) && !dto.getSuiteName().equals(existing.getSuiteName())) {
            LambdaQueryWrapper<TestSuite> nameWrapper = new LambdaQueryWrapper<>();
            nameWrapper.eq(TestSuite::getSuiteName, dto.getSuiteName())
                       .eq(TestSuite::getSystemId, dto.getSystemId());
            if (count(nameWrapper) > 0) {
                throw new BusinessException(400, "Suite name already exists in this system: " + dto.getSuiteName());
            }
        }

        // Update suite fields
        TestSuite suite = new TestSuite();
        suite.setId(id);
        suite.setSuiteName(dto.getSuiteName());
        suite.setSystemId(dto.getSystemId());
        suite.setDescription(dto.getDescription());
        suite.setSuiteType(dto.getSuiteType());

        updateById(suite);
        log.info("Test suite updated: {} ({})", id, dto.getSuiteName());

        // Replace case associations if caseIds provided
        if (dto.getCaseIds() != null) {
            deleteSuiteCases(id);
            if (!dto.getCaseIds().isEmpty()) {
                saveSuiteCases(id, dto.getCaseIds());
            }
        }

        return suite;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSuite(Long id) {
        TestSuite existing = getById(id);
        if (existing == null) {
            throw new BusinessException(404, "Test suite not found: " + id);
        }
        removeById(id);
        deleteSuiteCases(id);
        log.info("Test suite deleted: {}", id);
    }

    @Override
    public Map<String, Object> getSuiteDetail(Long id) {
        TestSuite suite = getById(id);
        if (suite == null) {
            throw new BusinessException(404, "Test suite not found: " + id);
        }

        // Get associated case IDs
        List<TestSuiteCase> suiteCases = testSuiteCaseMapper.selectList(
                new LambdaQueryWrapper<TestSuiteCase>()
                        .eq(TestSuiteCase::getSuiteId, id)
                        .orderByAsc(TestSuiteCase::getSortOrder));

        // Fetch actual case details
        List<TestCase> cases = Collections.emptyList();
        if (!CollectionUtils.isEmpty(suiteCases)) {
            List<Long> caseIds = suiteCases.stream()
                    .map(TestSuiteCase::getCaseId)
                    .collect(Collectors.toList());
            cases = testCaseMapper.selectBatchIds(caseIds);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("suite", suite);
        result.put("cases", cases);
        result.put("caseCount", suiteCases.size());
        return result;
    }

    @Override
    public PageResult<TestSuite> listSuites(Long systemId, String keyword, Long page, Long size) {
        Page<TestSuite> pageParam = new Page<>(page != null ? page : 1L, size != null ? size : 10L);

        LambdaQueryWrapper<TestSuite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(systemId != null, TestSuite::getSystemId, systemId)
               .and(StringUtils.hasText(keyword), w ->
                       w.like(TestSuite::getSuiteName, keyword)
                        .or()
                        .like(TestSuite::getDescription, keyword))
               .orderByDesc(TestSuite::getCreatedAt);

        Page<TestSuite> result = page(pageParam, wrapper);
        return PageResult.of(
                result.getTotal(), result.getCurrent(), result.getSize(),
                result.getPages(), result.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCasesToSuite(Long suiteId, List<Long> caseIds) {
        TestSuite suite = getById(suiteId);
        if (suite == null) {
            throw new BusinessException(404, "Test suite not found: " + suiteId);
        }
        if (CollectionUtils.isEmpty(caseIds)) {
            throw new BusinessException(400, "Case IDs cannot be empty");
        }

        // Get current max sort order
        List<TestSuiteCase> existing = testSuiteCaseMapper.selectList(
                new LambdaQueryWrapper<TestSuiteCase>()
                        .eq(TestSuiteCase::getSuiteId, suiteId));
        Set<Long> existingCaseIds = existing.stream()
                .map(TestSuiteCase::getCaseId)
                .collect(Collectors.toSet());
        int maxOrder = existing.stream()
                .mapToInt(TestSuiteCase::getSortOrder)
                .max()
                .orElse(0);

        // Add only new cases (skip duplicates)
        int order = maxOrder;
        for (Long caseId : caseIds) {
            if (!existingCaseIds.contains(caseId)) {
                order++;
                TestSuiteCase sc = new TestSuiteCase();
                sc.setSuiteId(suiteId);
                sc.setCaseId(caseId);
                sc.setSortOrder(order);
                testSuiteCaseMapper.insert(sc);
            }
        }
        log.info("Added {} cases to suite {}", caseIds.size(), suiteId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeCasesFromSuite(Long suiteId, List<Long> caseIds) {
        if (CollectionUtils.isEmpty(caseIds)) {
            throw new BusinessException(400, "Case IDs cannot be empty");
        }
        testSuiteCaseMapper.delete(
                new LambdaQueryWrapper<TestSuiteCase>()
                        .eq(TestSuiteCase::getSuiteId, suiteId)
                        .in(TestSuiteCase::getCaseId, caseIds));
        log.info("Removed {} cases from suite {}", caseIds.size(), suiteId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TestExecution executeSuite(ExecuteRequestDTO dto) {
        Long suiteId = dto.getSuiteId();
        TestSuite suite = getById(suiteId);
        if (suite == null) {
            throw new BusinessException(404, "Test suite not found: " + suiteId);
        }

        // Get associated cases
        List<TestSuiteCase> suiteCases = testSuiteCaseMapper.selectList(
                new LambdaQueryWrapper<TestSuiteCase>()
                        .eq(TestSuiteCase::getSuiteId, suiteId)
                        .orderByAsc(TestSuiteCase::getSortOrder));
        if (CollectionUtils.isEmpty(suiteCases)) {
            throw new BusinessException(400, "Suite has no cases to execute");
        }

        // Create execution record
        TestExecution execution = new TestExecution();
        execution.setExecutionName(suite.getSuiteName() + " - " + LocalDateTime.now());
        execution.setSuiteId(suiteId);
        execution.setSystemId(suite.getSystemId());
        execution.setEnvConfig(dto.getEnvConfig());
        execution.setStatus("RUNNING");
        execution.setTriggerType(dto.getTriggerType() != null ? dto.getTriggerType() : "MANUAL");
        execution.setTotalCount(suiteCases.size());
        execution.setPassedCount(0);
        execution.setFailedCount(0);
        execution.setSkippedCount(0);
        execution.setStartedAt(LocalDateTime.now());

        testExecutionMapper.insert(execution);
        log.info("Test execution created: {} for suite {}", execution.getId(), suiteId);

        // Simulate execution for each case
        int passed = 0;
        int failed = 0;
        int skipped = 0;
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (TestSuiteCase sc : suiteCases) {
            TestExecutionDetail detail = new TestExecutionDetail();
            detail.setExecutionId(execution.getId());
            detail.setCaseId(sc.getCaseId());
            detail.setStartedAt(LocalDateTime.now());

            // Simulate: 70% success, 20% failed, 10% skipped
            int rand = random.nextInt(100);
            long durationMs = random.nextLong(500, 15000);
            detail.setFinishedAt(detail.getStartedAt().plusNanos(durationMs * 1_000_000));
            detail.setDurationMs(durationMs);

            if (rand < 70) {
                detail.setStatus("SUCCESS");
                detail.setExecutionLog("Case executed successfully. All steps passed.");
                passed++;
            } else if (rand < 90) {
                detail.setStatus("FAILED");
                detail.setErrorMessage("Assertion failed: Expected element '#submit' to be visible, but it was not found.");
                detail.setErrorStep("Step 3: Click submit button");
                detail.setScreenshots("[\"/screenshots/case_" + sc.getCaseId() + "_error.png\"]");
                detail.setExecutionLog("Case execution failed at step 3.\nScreenshot captured.\nRetrying... still failed.");
                detail.setCompareResult("{\"expected\":\"element visible\",\"actual\":\"element not found\",\"diff\":\"missing_element\"}");
                detail.setAiAnalysis("The test case failed because the submit button was not rendered. This may be caused by a loading timeout or a UI change in the target page.");
                failed++;
            } else {
                detail.setStatus("SKIPPED");
                detail.setExecutionLog("Case skipped due to previous failure in dependency chain.");
                skipped++;
            }

            testExecutionDetailMapper.insert(detail);
        }

        // Update execution summary
        String finalStatus = failed > 0 ? "FAILED" : "SUCCESS";
        execution.setStatus(finalStatus);
        execution.setPassedCount(passed);
        execution.setFailedCount(failed);
        execution.setSkippedCount(skipped);
        execution.setFinishedAt(LocalDateTime.now());
        execution.setReportContent("{\"summary\":\"Execution completed with " + passed + " passed, " + failed + " failed, " + skipped + " skipped.\",\"passRate\":\"" + String.format("%.1f", (double) passed / suiteCases.size() * 100) + "%\"}");
        testExecutionMapper.updateById(execution);

        log.info("Test execution completed: {} - {} (passed={}, failed={}, skipped={})",
                execution.getId(), finalStatus, passed, failed, skipped);

        return execution;
    }

    /**
     * Save case associations for a suite
     */
    private void saveSuiteCases(Long suiteId, List<Long> caseIds) {
        for (int i = 0; i < caseIds.size(); i++) {
            TestSuiteCase sc = new TestSuiteCase();
            sc.setSuiteId(suiteId);
            sc.setCaseId(caseIds.get(i));
            sc.setSortOrder(i + 1);
            testSuiteCaseMapper.insert(sc);
        }
        log.info("Saved {} case associations for suite {}", caseIds.size(), suiteId);
    }

    /**
     * Delete all case associations for a suite
     */
    private void deleteSuiteCases(Long suiteId) {
        testSuiteCaseMapper.delete(
                new LambdaQueryWrapper<TestSuiteCase>()
                        .eq(TestSuiteCase::getSuiteId, suiteId));
    }
}
