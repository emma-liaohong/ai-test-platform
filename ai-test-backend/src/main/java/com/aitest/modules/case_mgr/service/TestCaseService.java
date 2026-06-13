package com.aitest.modules.case_mgr.service;

import com.aitest.common.result.PageResult;
import com.aitest.modules.case_mgr.dto.TestCaseDTO;
import com.aitest.modules.case_mgr.dto.TestCaseQueryDTO;
import com.aitest.modules.case_mgr.entity.TestCase;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * TestCase Service interface
 */
public interface TestCaseService extends IService<TestCase> {

    /**
     * Create a new test case with steps
     *
     * @param dto test case data including steps
     * @return created test case
     */
    TestCase createCase(TestCaseDTO dto);

    /**
     * Update a test case and replace its steps
     *
     * @param id  test case ID
     * @param dto test case data including new steps
     * @return updated test case
     */
    TestCase updateCase(Long id, TestCaseDTO dto);

    /**
     * Get test case detail including steps
     *
     * @param id test case ID
     * @return map with "case" and "steps" keys
     */
    Map<String, Object> getCaseDetail(Long id);

    /**
     * List test cases with filters and pagination
     *
     * @param queryDTO query filters and pagination params
     * @return paginated result
     */
    PageResult<TestCase> listCases(TestCaseQueryDTO queryDTO);

    /**
     * Delete a test case and its steps
     *
     * @param id test case ID
     */
    void deleteCase(Long id);

    /**
     * Batch delete test cases and their steps
     *
     * @param ids list of test case IDs
     */
    void batchDelete(List<Long> ids);
}
