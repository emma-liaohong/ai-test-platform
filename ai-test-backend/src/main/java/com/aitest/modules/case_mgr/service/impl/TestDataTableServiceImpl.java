package com.aitest.modules.case_mgr.service.impl;

import com.aitest.modules.case_mgr.entity.TestDataTable;
import com.aitest.modules.case_mgr.mapper.TestDataTableMapper;
import com.aitest.modules.case_mgr.service.TestDataTableService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * TestDataTable Service implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestDataTableServiceImpl extends ServiceImpl<TestDataTableMapper, TestDataTable> implements TestDataTableService {
}
