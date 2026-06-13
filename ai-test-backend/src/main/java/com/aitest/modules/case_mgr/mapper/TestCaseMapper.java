package com.aitest.modules.case_mgr.mapper;

import com.aitest.modules.case_mgr.entity.TestCase;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * TestCase Mapper
 */
@Mapper
public interface TestCaseMapper extends BaseMapper<TestCase> {
}
