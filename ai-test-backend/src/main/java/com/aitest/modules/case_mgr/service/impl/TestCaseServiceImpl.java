package com.aitest.modules.case_mgr.service.impl;

import com.aitest.common.exception.BusinessException;
import com.aitest.common.result.PageResult;
import com.aitest.modules.case_mgr.dto.TestCaseDTO;
import com.aitest.modules.case_mgr.dto.TestCaseQueryDTO;
import com.aitest.modules.case_mgr.entity.TestCase;
import com.aitest.modules.case_mgr.entity.TestCaseStep;
import com.aitest.modules.case_mgr.mapper.TestCaseMapper;
import com.aitest.modules.case_mgr.mapper.TestCaseStepMapper;
import com.aitest.modules.case_mgr.service.TestCaseService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TestCase Service implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestCaseServiceImpl extends ServiceImpl<TestCaseMapper, TestCase> implements TestCaseService {

    private final TestCaseStepMapper testCaseStepMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TestCase createCase(TestCaseDTO dto) {
        // Check if case code already exists
        LambdaQueryWrapper<TestCase> codeWrapper = new LambdaQueryWrapper<>();
        codeWrapper.eq(TestCase::getCaseCode, dto.getCaseCode());
        if (count(codeWrapper) > 0) {
            throw new BusinessException(400, "Case code already exists: " + dto.getCaseCode());
        }

        // Build and save the test case
        TestCase testCase = new TestCase();
        testCase.setCaseCode(dto.getCaseCode());
        testCase.setCaseName(dto.getCaseName());
        testCase.setCaseType(dto.getCaseType());
        testCase.setSystemId(dto.getSystemId());
        testCase.setModulePath(dto.getModulePath());
        testCase.setPriority(dto.getPriority());
        testCase.setCaseLevel(dto.getCaseLevel());
        testCase.setRecordSessionId(dto.getRecordSessionId());
        testCase.setRecordedSteps(dto.getRecordedSteps());
        testCase.setNaturalLanguageSteps(dto.getNaturalLanguageSteps());
        testCase.setPlaywrightScript(dto.getPlaywrightScript());
        testCase.setAppOperations(dto.getAppOperations());
        testCase.setApiUrl(dto.getApiUrl());
        testCase.setApiMethod(dto.getApiMethod());
        testCase.setApiRequestSchema(dto.getApiRequestSchema());
        testCase.setApiResponseSchema(dto.getApiResponseSchema());
        testCase.setApiHeaders(dto.getApiHeaders());
        testCase.setPreconditions(dto.getPreconditions());
        testCase.setExpectedResult(dto.getExpectedResult());
        testCase.setTags(dto.getTags());
        testCase.setStatus(dto.getStatus() != null ? dto.getStatus() : "draft");
        testCase.setIsDataDriven(dto.getIsDataDriven());
        testCase.setDataTableId(dto.getDataTableId());
        testCase.setDeleted(0);

        save(testCase);
        log.info("Test case created: {} ({})", testCase.getCaseCode(), testCase.getCaseName());

        // Save steps if provided
        saveSteps(testCase.getId(), dto.getSteps());

        return testCase;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TestCase updateCase(Long id, TestCaseDTO dto) {
        TestCase existing = getById(id);
        if (existing == null) {
            throw new BusinessException(404, "Test case not found: " + id);
        }

        // Check case code uniqueness (exclude self)
        if (StringUtils.hasText(dto.getCaseCode()) && !dto.getCaseCode().equals(existing.getCaseCode())) {
            LambdaQueryWrapper<TestCase> codeWrapper = new LambdaQueryWrapper<>();
            codeWrapper.eq(TestCase::getCaseCode, dto.getCaseCode());
            if (count(codeWrapper) > 0) {
                throw new BusinessException(400, "Case code already exists: " + dto.getCaseCode());
            }
        }

        // Update test case fields
        TestCase testCase = new TestCase();
        testCase.setId(id);
        testCase.setCaseCode(dto.getCaseCode());
        testCase.setCaseName(dto.getCaseName());
        testCase.setCaseType(dto.getCaseType());
        testCase.setSystemId(dto.getSystemId());
        testCase.setModulePath(dto.getModulePath());
        testCase.setPriority(dto.getPriority());
        testCase.setCaseLevel(dto.getCaseLevel());
        testCase.setRecordSessionId(dto.getRecordSessionId());
        testCase.setRecordedSteps(dto.getRecordedSteps());
        testCase.setNaturalLanguageSteps(dto.getNaturalLanguageSteps());
        testCase.setPlaywrightScript(dto.getPlaywrightScript());
        testCase.setAppOperations(dto.getAppOperations());
        testCase.setApiUrl(dto.getApiUrl());
        testCase.setApiMethod(dto.getApiMethod());
        testCase.setApiRequestSchema(dto.getApiRequestSchema());
        testCase.setApiResponseSchema(dto.getApiResponseSchema());
        testCase.setApiHeaders(dto.getApiHeaders());
        testCase.setPreconditions(dto.getPreconditions());
        testCase.setExpectedResult(dto.getExpectedResult());
        testCase.setTags(dto.getTags());
        testCase.setStatus(dto.getStatus());
        testCase.setIsDataDriven(dto.getIsDataDriven());
        testCase.setDataTableId(dto.getDataTableId());

        updateById(testCase);
        log.info("Test case updated: {} ({})", id, dto.getCaseName());

        // Replace steps: delete old + save new
        deleteStepsByCaseId(id);
        saveSteps(id, dto.getSteps());

        return testCase;
    }

    @Override
    public Map<String, Object> getCaseDetail(Long id) {
        TestCase testCase = getById(id);
        if (testCase == null) {
            throw new BusinessException(404, "Test case not found: " + id);
        }

        List<TestCaseStep> steps = testCaseStepMapper.selectList(
                new LambdaQueryWrapper<TestCaseStep>()
                        .eq(TestCaseStep::getCaseId, id)
                        .orderByAsc(TestCaseStep::getStepOrder));

        Map<String, Object> result = new HashMap<>();
        result.put("case", testCase);
        result.put("steps", steps);
        return result;
    }

    @Override
    public PageResult<TestCase> listCases(TestCaseQueryDTO queryDTO) {
        Page<TestCase> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());

        LambdaQueryWrapper<TestCase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(queryDTO.getCaseType()), TestCase::getCaseType, queryDTO.getCaseType())
               .eq(queryDTO.getSystemId() != null, TestCase::getSystemId, queryDTO.getSystemId())
               .eq(StringUtils.hasText(queryDTO.getPriority()), TestCase::getPriority, queryDTO.getPriority())
               .eq(StringUtils.hasText(queryDTO.getStatus()), TestCase::getStatus, queryDTO.getStatus())
               .and(StringUtils.hasText(queryDTO.getKeyword()), w ->
                       w.like(TestCase::getCaseCode, queryDTO.getKeyword())
                        .or()
                        .like(TestCase::getCaseName, queryDTO.getKeyword()))
               .orderByDesc(TestCase::getCreatedAt);

        Page<TestCase> result = page(page, wrapper);
        return PageResult.of(
                result.getTotal(), result.getCurrent(), result.getSize(),
                result.getPages(), result.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCase(Long id) {
        TestCase existing = getById(id);
        if (existing == null) {
            throw new BusinessException(404, "Test case not found: " + id);
        }
        removeById(id);
        deleteStepsByCaseId(id);
        log.info("Test case deleted: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        removeByIds(ids);
        // Delete steps for all cases
        testCaseStepMapper.delete(
                new LambdaQueryWrapper<TestCaseStep>()
                        .in(TestCaseStep::getCaseId, ids));
        log.info("Test cases batch deleted: {} cases", ids.size());
    }

    /**
     * Save steps for a test case
     */
    private void saveSteps(Long caseId, List<TestCaseStep> steps) {
        if (CollectionUtils.isEmpty(steps)) {
            return;
        }
        for (int i = 0; i < steps.size(); i++) {
            TestCaseStep step = steps.get(i);
            step.setId(null); // Ensure new insert
            step.setCaseId(caseId);
            step.setStepOrder(i + 1);
            testCaseStepMapper.insert(step);
        }
        log.info("Saved {} steps for case {}", steps.size(), caseId);
    }

    /**
     * Delete all steps belonging to a test case
     */
    private void deleteStepsByCaseId(Long caseId) {
        testCaseStepMapper.delete(
                new LambdaQueryWrapper<TestCaseStep>()
                        .eq(TestCaseStep::getCaseId, caseId));
    }
}
