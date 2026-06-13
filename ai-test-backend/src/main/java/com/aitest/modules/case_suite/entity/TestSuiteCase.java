package com.aitest.modules.case_suite.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * Test Suite - Case association entity (composite PK: suiteId + caseId)
 */
@Data
@TableName("test_suite_case")
@Schema(description = "Test Suite Case Association")
public class TestSuiteCase implements Serializable {

    @Schema(description = "Suite ID")
    private Long suiteId;

    @Schema(description = "Case ID")
    private Long caseId;

    @Schema(description = "Sort order within the suite")
    private Integer sortOrder;
}
