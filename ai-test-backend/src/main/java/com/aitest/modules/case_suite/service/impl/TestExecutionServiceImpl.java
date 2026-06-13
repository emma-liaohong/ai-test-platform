package com.aitest.modules.case_suite.service.impl;

import com.aitest.common.exception.BusinessException;
import com.aitest.common.result.PageResult;
import com.aitest.modules.case_suite.entity.TestExecution;
import com.aitest.modules.case_suite.entity.TestExecutionDetail;
import com.aitest.modules.case_suite.mapper.TestExecutionDetailMapper;
import com.aitest.modules.case_suite.mapper.TestExecutionMapper;
import com.aitest.modules.case_suite.service.TestExecutionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TestExecution Service implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestExecutionServiceImpl extends ServiceImpl<TestExecutionMapper, TestExecution> implements TestExecutionService {

    private final TestExecutionDetailMapper testExecutionDetailMapper;

    @Override
    public Map<String, Object> getExecutionDetail(Long id) {
        TestExecution execution = getById(id);
        if (execution == null) {
            throw new BusinessException(404, "Test execution not found: " + id);
        }

        // Get all case execution details
        List<TestExecutionDetail> details = testExecutionDetailMapper.selectList(
                new LambdaQueryWrapper<TestExecutionDetail>()
                        .eq(TestExecutionDetail::getExecutionId, id)
                        .orderByAsc(TestExecutionDetail::getId));

        Map<String, Object> result = new HashMap<>();
        result.put("execution", execution);
        result.put("details", details);
        result.put("totalCases", details.size());
        return result;
    }

    @Override
    public PageResult<TestExecution> listExecutions(Long suiteId, Long systemId, String status, Long page, Long size) {
        Page<TestExecution> pageParam = new Page<>(page != null ? page : 1L, size != null ? size : 10L);

        LambdaQueryWrapper<TestExecution> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(suiteId != null, TestExecution::getSuiteId, suiteId)
               .eq(systemId != null, TestExecution::getSystemId, systemId)
               .eq(StringUtils.hasText(status), TestExecution::getStatus, status)
               .orderByDesc(TestExecution::getCreatedAt);

        Page<TestExecution> result = page(pageParam, wrapper);
        return PageResult.of(
                result.getTotal(), result.getCurrent(), result.getSize(),
                result.getPages(), result.getRecords());
    }

    @Override
    public Map<String, Object> getExecutionReport(Long id) {
        TestExecution execution = getById(id);
        if (execution == null) {
            throw new BusinessException(404, "Test execution not found: " + id);
        }

        // Get all details for statistical analysis
        List<TestExecutionDetail> details = testExecutionDetailMapper.selectList(
                new LambdaQueryWrapper<TestExecutionDetail>()
                        .eq(TestExecutionDetail::getExecutionId, id));

        // Calculate statistics
        long totalDuration = details.stream()
                .filter(d -> d.getDurationMs() != null)
                .mapToLong(TestExecutionDetail::getDurationMs)
                .sum();
        long avgDuration = details.isEmpty() ? 0 : totalDuration / details.size();
        long maxDuration = details.stream()
                .filter(d -> d.getDurationMs() != null)
                .mapToLong(TestExecutionDetail::getDurationMs)
                .max()
                .orElse(0);
        long minDuration = details.stream()
                .filter(d -> d.getDurationMs() != null)
                .mapToLong(TestExecutionDetail::getDurationMs)
                .min()
                .orElse(0);

        // Calculate pass rate
        double passRate = execution.getTotalCount() != null && execution.getTotalCount() > 0
                ? (double) execution.getPassedCount() / execution.getTotalCount() * 100
                : 0.0;

        // Calculate execution duration
        long executionDurationMs = 0;
        if (execution.getStartedAt() != null && execution.getFinishedAt() != null) {
            executionDurationMs = Duration.between(execution.getStartedAt(), execution.getFinishedAt()).toMillis();
        }

        Map<String, Object> report = new HashMap<>();
        report.put("execution", execution);
        report.put("passRate", String.format("%.1f%%", passRate));
        report.put("totalDurationMs", totalDuration);
        report.put("avgDurationMs", avgDuration);
        report.put("maxDurationMs", maxDuration);
        report.put("minDurationMs", minDuration);
        report.put("executionDurationMs", executionDurationMs);
        report.put("passedCount", execution.getPassedCount());
        report.put("failedCount", execution.getFailedCount());
        report.put("skippedCount", execution.getSkippedCount());
        report.put("totalCount", execution.getTotalCount());
        report.put("status", execution.getStatus());
        report.put("errorCases", details.stream()
                .filter(d -> "FAILED".equals(d.getStatus()) || "ERROR".equals(d.getStatus()))
                .map(d -> {
                    Map<String, Object> errInfo = new HashMap<>();
                    errInfo.put("caseId", d.getCaseId());
                    errInfo.put("errorMessage", d.getErrorMessage());
                    errInfo.put("errorStep", d.getErrorStep());
                    return errInfo;
                })
                .toList());

        return report;
    }

    @Override
    public TestExecutionDetail getCaseExecutionLog(Long executionId, Long caseId) {
        TestExecution execution = getById(executionId);
        if (execution == null) {
            throw new BusinessException(404, "Test execution not found: " + executionId);
        }

        TestExecutionDetail detail = testExecutionDetailMapper.selectOne(
                new LambdaQueryWrapper<TestExecutionDetail>()
                        .eq(TestExecutionDetail::getExecutionId, executionId)
                        .eq(TestExecutionDetail::getCaseId, caseId));
        if (detail == null) {
            throw new BusinessException(404, "Case execution detail not found: executionId=" + executionId + ", caseId=" + caseId);
        }

        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelExecution(Long id) {
        TestExecution execution = getById(id);
        if (execution == null) {
            throw new BusinessException(404, "Test execution not found: " + id);
        }
        if (!"RUNNING".equals(execution.getStatus()) && !"PENDING".equals(execution.getStatus())) {
            throw new BusinessException(400, "Cannot cancel execution in status: " + execution.getStatus());
        }

        // Mark running/pending details as SKIPPED
        List<TestExecutionDetail> pendingDetails = testExecutionDetailMapper.selectList(
                new LambdaQueryWrapper<TestExecutionDetail>()
                        .eq(TestExecutionDetail::getExecutionId, id)
                        .and(w -> w.isNull(TestExecutionDetail::getStatus)
                                .or()
                                .in(TestExecutionDetail::getStatus, "PENDING")));

        for (TestExecutionDetail detail : pendingDetails) {
            detail.setStatus("SKIPPED");
            detail.setExecutionLog("Execution cancelled by user.");
            detail.setFinishedAt(LocalDateTime.now());
            testExecutionDetailMapper.updateById(detail);
        }

        // Update execution status
        execution.setStatus("CANCELLED");
        execution.setFinishedAt(LocalDateTime.now());

        // Recalculate counts
        List<TestExecutionDetail> allDetails = testExecutionDetailMapper.selectList(
                new LambdaQueryWrapper<TestExecutionDetail>()
                        .eq(TestExecutionDetail::getExecutionId, id));
        long passed = allDetails.stream().filter(d -> "SUCCESS".equals(d.getStatus())).count();
        long failed = allDetails.stream().filter(d -> "FAILED".equals(d.getStatus()) || "ERROR".equals(d.getStatus())).count();
        long skipped = allDetails.stream().filter(d -> "SKIPPED".equals(d.getStatus())).count();
        execution.setPassedCount((int) passed);
        execution.setFailedCount((int) failed);
        execution.setSkippedCount((int) skipped);

        updateById(execution);
        log.info("Test execution cancelled: {}", id);
    }
}
