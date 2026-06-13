package com.aitest.modules.case_suite.service;

import com.aitest.common.result.PageResult;
import com.aitest.modules.case_suite.dto.ExecuteRequestDTO;
import com.aitest.modules.case_suite.dto.TestSuiteDTO;
import com.aitest.modules.case_suite.entity.TestExecution;
import com.aitest.modules.case_suite.entity.TestSuite;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * TestSuite Service interface
 */
public interface TestSuiteService extends IService<TestSuite> {

    /**
     * Create a new test suite with optional case associations
     */
    TestSuite createSuite(TestSuiteDTO dto);

    /**
     * Update a test suite
     */
    TestSuite updateSuite(Long id, TestSuiteDTO dto);

    /**
     * Delete a test suite and its case associations
     */
    void deleteSuite(Long id);

    /**
     * Get suite detail including associated case list
     * @return map with "suite" and "cases" keys
     */
    Map<String, Object> getSuiteDetail(Long id);

    /**
     * List suites with pagination
     */
    PageResult<TestSuite> listSuites(Long systemId, String keyword, Long page, Long size);

    /**
     * Add cases to an existing suite
     */
    void addCasesToSuite(Long suiteId, List<Long> caseIds);

    /**
     * Remove cases from a suite
     */
    void removeCasesFromSuite(Long suiteId, List<Long> caseIds);

    /**
     * Execute a test suite: create execution records and simulate case execution
     */
    TestExecution executeSuite(ExecuteRequestDTO dto);
}
