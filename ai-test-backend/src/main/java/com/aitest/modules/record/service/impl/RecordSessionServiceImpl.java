package com.aitest.modules.record.service.impl;

import com.aitest.common.exception.BusinessException;
import com.aitest.common.result.PageResult;
import com.aitest.modules.record.dto.RecordSessionDTO;
import com.aitest.modules.record.dto.RecordSessionQueryDTO;
import com.aitest.modules.record.dto.RecordStepDTO;
import com.aitest.modules.record.entity.RecordSession;
import com.aitest.modules.record.mapper.RecordSessionMapper;
import com.aitest.modules.record.service.RecordSessionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecordSessionServiceImpl extends ServiceImpl<RecordSessionMapper, RecordSession>
        implements RecordSessionService {

    private final ObjectMapper objectMapper;

    @Override
    public PageResult<RecordSession> listSessions(RecordSessionQueryDTO query) {
        Page<RecordSession> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<RecordSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(query.getSystemId() != null, RecordSession::getSystemId, query.getSystemId())
               .eq(StringUtils.hasText(query.getStatus()), RecordSession::getStatus, query.getStatus())
               .and(StringUtils.hasText(query.getKeyword()), w ->
                       w.like(RecordSession::getSessionName, query.getKeyword())
                        .or()
                        .like(RecordSession::getSessionId, query.getKeyword()))
               .orderByDesc(RecordSession::getCreatedAt);
        Page<RecordSession> result = page(page, wrapper);
        return PageResult.of(
                result.getTotal(), result.getCurrent(), result.getSize(),
                result.getPages(), result.getRecords());
    }

    @Override
    public RecordSession getSession(Long id) {
        RecordSession session = getById(id);
        if (session == null) {
            throw new BusinessException(404, "录制会话不存在: " + id);
        }
        return session;
    }

    @Override
    public RecordSession getSessionBySessionId(String sessionId) {
        LambdaQueryWrapper<RecordSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecordSession::getSessionId, sessionId);
        RecordSession session = getOne(wrapper);
        if (session == null) {
            throw new BusinessException(404, "录制会话不存在: " + sessionId);
        }
        return session;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RecordSession startSession(RecordSessionDTO dto) {
        RecordSession session = new RecordSession();
        session.setSessionId("REC-" + UUID.randomUUID().toString().substring(0, 8));
        session.setSessionName(dto.getSessionName());
        session.setSystemId(dto.getSystemId());
        session.setTargetUrl(dto.getTargetUrl());
        session.setStatus("RECORDING");
        session.setStepCount(0);
        session.setStartedAt(LocalDateTime.now());
        session.setCreatedAt(LocalDateTime.now());
        save(session);
        log.info("录制会话已创建: {}", session.getSessionId());
        return session;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RecordSession stopSession(Long id) {
        RecordSession session = getById(id);
        if (session == null) {
            throw new BusinessException(404, "录制会话不存在: " + id);
        }
        if (!"RECORDING".equals(session.getStatus())) {
            throw new BusinessException(400, "会话状态不是录制中，无法停止: " + session.getStatus());
        }

        LocalDateTime now = LocalDateTime.now();
        session.setStatus("STOPPED");
        session.setStoppedAt(now);

        // 计算录制时长
        if (session.getStartedAt() != null) {
            long durationMs = Duration.between(session.getStartedAt(), now).toMillis();
            session.setDurationMs(durationMs);
        }

        // 模拟生成 5-8 个录制步骤
        List<RecordStepDTO> mockSteps = generateMockSteps();
        session.setSteps(mockSteps);
        session.setStepCount(mockSteps.size());

        updateById(session);
        log.info("录制会话已停止: {}, 步骤数: {}", session.getSessionId(), mockSteps.size());
        return session;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addStep(Long id, RecordStepDTO step) {
        RecordSession session = getById(id);
        if (session == null) {
            throw new BusinessException(404, "录制会话不存在: " + id);
        }
        if (!"RECORDING".equals(session.getStatus())) {
            throw new BusinessException(400, "会话状态不是录制中，无法添加步骤");
        }

        List<RecordStepDTO> steps = session.getSteps();
        if (steps == null) {
            steps = new ArrayList<>();
        }

        // 自动设置步骤序号
        step.setStepOrder(steps.size() + 1);
        if (step.getTimestamp() == null) {
            step.setTimestamp(System.currentTimeMillis());
        }
        steps.add(step);

        session.setSteps(steps);
        session.setStepCount(steps.size());
        updateById(session);
        log.info("步骤已添加到会话 {}: stepOrder={}, action={}", session.getSessionId(), step.getStepOrder(), step.getAction());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSession(Long id) {
        RecordSession session = getById(id);
        if (session == null) {
            throw new BusinessException(404, "录制会话不存在: " + id);
        }
        removeById(id);
        log.info("录制会话已删除: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> generateCase(Long id) {
        RecordSession session = getById(id);
        if (session == null) {
            throw new BusinessException(404, "录制会话不存在: " + id);
        }
        if (session.getSteps() == null || session.getSteps().isEmpty()) {
            throw new BusinessException(400, "会话没有录制步骤，无法生成用例");
        }

        // Convert LinkedHashMap to RecordStepDTO (JacksonTypeHandler loses generic type info)
        List<RecordStepDTO> typedSteps = new ArrayList<>();
        for (Object step : session.getSteps()) {
            if (step instanceof RecordStepDTO) {
                typedSteps.add((RecordStepDTO) step);
            } else {
                typedSteps.add(objectMapper.convertValue(step, RecordStepDTO.class));
            }
        }

        // 更新状态为已生成
        long mockCaseId = 10000 + new Random().nextInt(90000);
        session.setStatus("GENERATED");
        session.setGeneratedCaseId(mockCaseId);
        updateById(session);

        // 构建自然语言步骤描述
        List<String> stepDescriptions = new ArrayList<>();
        for (RecordStepDTO step : typedSteps) {
            String desc = String.format("步骤%d: %s", step.getStepOrder(),
                    StringUtils.hasText(step.getDescription()) ? step.getDescription() : step.getAction() + " " + step.getTarget());
            stepDescriptions.add(desc);
        }

        // 生成模拟 Playwright 脚本
        String playwrightScript = generateMockPlaywrightScript(session, typedSteps);

        // 构建返回结果
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("generatedCaseId", mockCaseId);
        result.put("caseName", "【自动生成】" + session.getSessionName());
        result.put("caseType", "PC");
        result.put("steps", stepDescriptions);
        result.put("playwrightScript", playwrightScript);

        log.info("测试用例已生成: 会话={}, 用例ID={}", session.getSessionId(), mockCaseId);
        return result;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 模拟生成 5-8 个录制步骤
     */
    private List<RecordStepDTO> generateMockSteps() {
        Random random = new Random();
        int stepCount = 5 + random.nextInt(4); // 5-8 steps
        long baseTime = System.currentTimeMillis();

        // 预定义的模拟步骤模板
        String[][] templates = {
                {"click",   "#login-btn",              "",              "点击登录按钮"},
                {"input",   "#username",               "admin",         "输入用户名 admin"},
                {"input",   "#password",               "******",        "输入密码"},
                {"click",   "#submit-btn",             "",              "点击提交按钮"},
                {"wait",    ".dashboard-container",    "",              "等待仪表盘页面加载"},
                {"assert",  ".welcome-message",        "欢迎",          "断言欢迎消息显示"},
                {"click",   "#menu-test-case",         "",              "点击测试用例菜单"},
                {"input",   "#search-input",           "登录测试",       "在搜索框输入关键字"},
                {"select",  "#env-select",             "staging",       "选择测试环境为 staging"},
                {"click",   "#run-btn",                "",              "点击执行按钮"},
                {"assert",  ".result-status",          "通过",          "断言测试结果状态为通过"},
                {"swipe",   ".sidebar",                "down",          "向下滚动侧边栏"},
        };

        // 随机选取步骤
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < templates.length; i++) {
            indices.add(i);
        }
        Collections.shuffle(indices, random);

        List<RecordStepDTO> steps = new ArrayList<>();
        for (int i = 0; i < stepCount; i++) {
            String[] template = templates[indices.get(i)];
            RecordStepDTO step = new RecordStepDTO();
            step.setStepOrder(i + 1);
            step.setAction(template[0]);
            step.setTarget(template[1]);
            step.setValue(template[2]);
            step.setDescription(template[3]);
            step.setScreenshot("/screenshots/step_" + (i + 1) + ".png");
            step.setTimestamp(baseTime + (long) i * 2000);
            steps.add(step);
        }

        return steps;
    }

    /**
     * 生成模拟的 Playwright 测试脚本
     */
    private String generateMockPlaywrightScript(RecordSession session, List<RecordStepDTO> steps) {
        StringBuilder sb = new StringBuilder();
        sb.append("import { test, expect } from '@playwright/test';\n\n");
        sb.append("test('").append(session.getSessionName()).append("', async ({ page }) => {\n");
        sb.append("  // 目标地址: ").append(session.getTargetUrl()).append("\n");
        sb.append("  await page.goto('").append(session.getTargetUrl()).append("');\n\n");

        for (RecordStepDTO step : steps) {
            sb.append("  // 步骤").append(step.getStepOrder()).append(": ").append(step.getDescription()).append("\n");
            switch (step.getAction()) {
                case "click":
                    sb.append("  await page.click('").append(step.getTarget()).append("');\n");
                    break;
                case "input":
                    sb.append("  await page.fill('").append(step.getTarget()).append("', '").append(step.getValue()).append("');\n");
                    break;
                case "select":
                    sb.append("  await page.selectOption('").append(step.getTarget()).append("', '").append(step.getValue()).append("');\n");
                    break;
                case "swipe":
                    sb.append("  // swipe 操作需自定义实现\n");
                    sb.append("  await page.mouse.wheel(0, 300);\n");
                    break;
                case "wait":
                    sb.append("  await page.waitForSelector('").append(step.getTarget()).append("');\n");
                    break;
                case "assert":
                    sb.append("  await expect(page.locator('").append(step.getTarget()).append("')).toContainText('").append(step.getValue()).append("');\n");
                    break;
                default:
                    sb.append("  // 未知操作: ").append(step.getAction()).append("\n");
                    break;
            }
            sb.append("\n");
        }

        sb.append("});\n");
        return sb.toString();
    }
}
