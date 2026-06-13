package com.aitest.modules.case_suite.service;

import com.aitest.common.result.PageResult;
import com.aitest.modules.case_suite.entity.TestExecution;
import com.aitest.modules.case_suite.entity.TestExecutionDetail;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * TestExecution Service interface
 */
public interface TestExecutionService extends IService<TestExecution> {

    /**
     * Get execution detail with all case results
     * @return map with "execution" and "details" keys
     */
    Map<String, Object> getExecutionDetail(Long id);

    /**
     * List executions with pagination
     */
    PageResult<TestExecution> listExecutions(Long suiteId, Long systemId, String status, Long page, Long size);

    /**
     * Get execution report (summary statistics)
     * @return map with execution summary and statistics
     */
    Map<String, Object> getExecutionReport(Long id);

    /**
     * Get specific case execution log within an execution
     */
    TestExecutionDetail getCaseExecutionLog(Long executionId, Long caseId);

    /**
     * Cancel a running execution
     */
    void cancelExecution(Long id);
}
