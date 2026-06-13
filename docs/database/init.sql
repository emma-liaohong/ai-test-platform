-- ================================================================
-- AI 全链路自动化测试平台 - 数据库初始化脚本
-- Database: MySQL 8.0+
-- Character Set: utf8mb4
-- ================================================================

CREATE DATABASE IF NOT EXISTS `ai_test_platform` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `ai_test_platform`;

-- ================================================================
-- 1. 系统管理模块
-- ================================================================

-- 系统表 (被管理的目标系统)
CREATE TABLE `sys_system` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `system_code` VARCHAR(50) NOT NULL COMMENT '系统编码',
    `system_name` VARCHAR(100) NOT NULL COMMENT '系统名称',
    `system_type` VARCHAR(20) DEFAULT 'WEB' COMMENT '系统类型: WEB/APP/API',
    `base_url` VARCHAR(500) COMMENT '基础URL',
    `description` TEXT COMMENT '描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    `created_by` BIGINT COMMENT '创建人',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_system_code` (`system_code`)
) ENGINE=InnoDB COMMENT='被管系统';

-- 用户表
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(200) NOT NULL COMMENT '密码(BCrypt)',
    `real_name` VARCHAR(50) COMMENT '真实姓名',
    `email` VARCHAR(100) COMMENT '邮箱',
    `phone` VARCHAR(20) COMMENT '手机号',
    `avatar` VARCHAR(500) COMMENT '头像URL',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    `last_login_at` DATETIME COMMENT '最后登录时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB COMMENT='用户表';

-- 角色表
CREATE TABLE `sys_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
    `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `description` VARCHAR(200) COMMENT '描述',
    `status` TINYINT DEFAULT 1,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB COMMENT='角色表';

-- 权限表
CREATE TABLE `sys_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `perm_code` VARCHAR(100) NOT NULL COMMENT '权限编码',
    `perm_name` VARCHAR(100) NOT NULL COMMENT '权限名称',
    `perm_type` VARCHAR(20) DEFAULT 'MENU' COMMENT '类型: MENU/BUTTON/API',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父级ID',
    `path` VARCHAR(200) COMMENT '路由路径',
    `icon` VARCHAR(50) COMMENT '图标',
    `sort_order` INT DEFAULT 0,
    `status` TINYINT DEFAULT 1,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_perm_code` (`perm_code`)
) ENGINE=InnoDB COMMENT='权限表';

-- 用户角色关联
CREATE TABLE `sys_user_role` (
    `user_id` BIGINT NOT NULL,
    `role_id` BIGINT NOT NULL,
    PRIMARY KEY (`user_id`, `role_id`)
) ENGINE=InnoDB COMMENT='用户角色关联';

-- 角色权限关联
CREATE TABLE `sys_role_permission` (
    `role_id` BIGINT NOT NULL,
    `permission_id` BIGINT NOT NULL,
    PRIMARY KEY (`role_id`, `permission_id`)
) ENGINE=InnoDB COMMENT='角色权限关联';

-- ================================================================
-- 2. 案例管理模块
-- ================================================================

CREATE TABLE `test_case` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `case_code` VARCHAR(50) NOT NULL COMMENT '案例编码',
    `case_name` VARCHAR(200) NOT NULL COMMENT '案例名称',
    `case_type` VARCHAR(20) NOT NULL COMMENT '案例类型: PC/APP/API',
    `system_id` BIGINT NOT NULL COMMENT '所属系统',
    `module_path` VARCHAR(500) COMMENT '模块路径',
    `priority` VARCHAR(10) DEFAULT 'P2' COMMENT '优先级: P0/P1/P2/P3',
    `case_level` VARCHAR(10) COMMENT '案例级别: 冒烟/核心/边界/异常',
    -- PC 端字段
    `record_session_id` VARCHAR(100) COMMENT '录制会话ID',
    `recorded_steps` JSON COMMENT '录制步骤(原始数据)',
    `natural_language_steps` TEXT COMMENT '自然语言步骤',
    `playwright_script` TEXT COMMENT 'Playwright执行脚本',
    -- APP 端字段
    `app_operations` JSON COMMENT 'APP操作步骤',
    -- 接口字段
    `api_url` VARCHAR(500) COMMENT '接口地址',
    `api_method` VARCHAR(10) COMMENT '请求方法: GET/POST/PUT/DELETE',
    `api_request_schema` JSON COMMENT '请求契约',
    `api_response_schema` JSON COMMENT '响应契约',
    `api_headers` JSON COMMENT '请求头',
    -- 通用字段
    `preconditions` TEXT COMMENT '前置条件',
    `expected_result` TEXT COMMENT '预期结果',
    `tags` VARCHAR(500) COMMENT '标签(逗号分隔)',
    `status` VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态: DRAFT/READY/ARCHIVED',
    `is_data_driven` TINYINT DEFAULT 0 COMMENT '是否数据驱动',
    `data_table_id` BIGINT COMMENT '关联测试数据表',
    `created_by` BIGINT,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_by` BIGINT,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_case_code` (`case_code`),
    KEY `idx_system` (`system_id`),
    KEY `idx_type` (`case_type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB COMMENT='测试案例';

CREATE TABLE `test_case_step` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `case_id` BIGINT NOT NULL,
    `step_order` INT NOT NULL COMMENT '步骤顺序',
    `step_action` VARCHAR(50) COMMENT '操作类型: click/input/select/swipe/wait/assert/request',
    `step_target` VARCHAR(500) COMMENT '操作目标(选择器/URL)',
    `step_value` TEXT COMMENT '操作值',
    `step_description` TEXT COMMENT '步骤描述(自然语言)',
    `expected_result` TEXT COMMENT '步骤预期结果',
    `screenshot_path` VARCHAR(500) COMMENT '截图路径',
    `timeout_ms` INT DEFAULT 3000 COMMENT '超时时间(ms)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_case` (`case_id`)
) ENGINE=InnoDB COMMENT='案例步骤';

CREATE TABLE `test_data_table` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `table_name` VARCHAR(100) NOT NULL COMMENT '数据表名称',
    `system_id` BIGINT COMMENT '所属系统',
    `description` TEXT,
    `columns` JSON COMMENT '列定义 [{name,type,description}]',
    `created_by` BIGINT,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='测试数据表(数据驱动)';

CREATE TABLE `test_data_row` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `table_id` BIGINT NOT NULL,
    `row_data` JSON COMMENT '行数据',
    `row_index` INT COMMENT '行序号',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_table` (`table_id`)
) ENGINE=InnoDB COMMENT='测试数据行';

-- ================================================================
-- 3. 案例集执行模块
-- ================================================================

CREATE TABLE `test_suite` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `suite_name` VARCHAR(200) NOT NULL,
    `system_id` BIGINT NOT NULL,
    `description` TEXT,
    `suite_type` VARCHAR(20) DEFAULT 'MANUAL' COMMENT 'MANUAL/SMART/REGRESSION/SMOKE',
    `created_by` BIGINT,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_system` (`system_id`)
) ENGINE=InnoDB COMMENT='案例集(测试套件)';

CREATE TABLE `test_suite_case` (
    `suite_id` BIGINT NOT NULL,
    `case_id` BIGINT NOT NULL,
    `sort_order` INT DEFAULT 0,
    PRIMARY KEY (`suite_id`, `case_id`)
) ENGINE=InnoDB COMMENT='案例集-案例关联';

CREATE TABLE `test_execution` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `execution_name` VARCHAR(200),
    `suite_id` BIGINT COMMENT '关联案例集',
    `system_id` BIGINT,
    `env_config` JSON COMMENT '环境配置',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING/RUNNING/SUCCESS/FAILED/CANCELLED',
    `trigger_type` VARCHAR(20) COMMENT 'MANUAL/SCHEDULE/CI',
    `total_count` INT DEFAULT 0,
    `passed_count` INT DEFAULT 0,
    `failed_count` INT DEFAULT 0,
    `skipped_count` INT DEFAULT 0,
    `started_at` DATETIME,
    `finished_at` DATETIME,
    `report_content` JSON COMMENT '测试报告内容',
    `created_by` BIGINT,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_suite` (`suite_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB COMMENT='执行计划';

CREATE TABLE `test_execution_detail` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `execution_id` BIGINT NOT NULL,
    `case_id` BIGINT NOT NULL,
    `status` VARCHAR(20) COMMENT 'SUCCESS/FAILED/SKIPPED/ERROR',
    `started_at` DATETIME,
    `finished_at` DATETIME,
    `duration_ms` BIGINT,
    `error_message` TEXT,
    `error_step` INT COMMENT '失败步骤序号',
    `screenshots` JSON COMMENT '截图列表 [{step,path,timestamp}]',
    `video_path` VARCHAR(500) COMMENT '执行录像路径',
    `execution_log` TEXT COMMENT '执行日志(完整)',
    `compare_result` JSON COMMENT '截图对比结果 [{step,status,similarity,diff}]',
    `ai_analysis` TEXT COMMENT 'AI分析结果',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_execution` (`execution_id`),
    KEY `idx_case` (`case_id`)
) ENGINE=InnoDB COMMENT='执行详情(每个案例)';

-- ================================================================
-- 4. 知识库管理模块
-- ================================================================

CREATE TABLE `kb_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `category_name` VARCHAR(100) NOT NULL COMMENT '分类名称',
    `category_type` VARCHAR(50) NOT NULL COMMENT 'CASE/REQUIREMENT/DESIGN/GIT/DEFECT/DATABASE/VIDEO',
    `system_id` BIGINT COMMENT '关联系统',
    `parent_id` BIGINT DEFAULT 0,
    `sort_order` INT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_type` (`category_type`),
    KEY `idx_system` (`system_id`)
) ENGINE=InnoDB COMMENT='知识库分类';

CREATE TABLE `kb_document` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `doc_code` VARCHAR(50) UNIQUE,
    `doc_name` VARCHAR(200) NOT NULL,
    `category_id` BIGINT NOT NULL,
    `system_id` BIGINT,
    `doc_type` VARCHAR(50) COMMENT 'PDF/WORD/MARKDOWN/EXCEL/IMAGE/VIDEO',
    `file_path` VARCHAR(500) COMMENT '文件存储路径',
    `file_size` BIGINT,
    `content` LONGTEXT COMMENT '解析后的文本内容',
    `content_vector` JSON COMMENT '向量化内容',
    `metadata` JSON COMMENT '元数据',
    `parse_status` VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING/PARSING/DONE/FAILED',
    `parse_result` JSON COMMENT '解析结果(功能点/表格/图片描述等)',
    `created_by` BIGINT,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category_id`),
    KEY `idx_system` (`system_id`)
) ENGINE=InnoDB COMMENT='知识库文档';

CREATE TABLE `kb_chunk` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `document_id` BIGINT NOT NULL,
    `chunk_index` INT COMMENT '段落序号',
    `content` TEXT NOT NULL,
    `content_type` VARCHAR(20) COMMENT 'TEXT/TABLE/IMAGE/FLOWCHART',
    `embedding` JSON COMMENT '向量嵌入',
    `metadata` JSON COMMENT '段落元数据',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_document` (`document_id`),
    FULLTEXT KEY `ft_content` (`content`)
) ENGINE=InnoDB COMMENT='知识库段落(文档切片)';

-- 案例视频库
CREATE TABLE `kb_video` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `video_name` VARCHAR(200) NOT NULL,
    `video_type` VARCHAR(20) NOT NULL COMMENT 'RECORD(录制)/EXECUTION(执行录制)',
    `system_id` BIGINT COMMENT '所属系统',
    `case_id` BIGINT COMMENT '关联案例',
    `execution_id` BIGINT COMMENT '关联执行记录',
    `file_path` VARCHAR(500) NOT NULL COMMENT '视频文件路径',
    `file_size` BIGINT COMMENT '文件大小(bytes)',
    `duration_ms` BIGINT COMMENT '时长(毫秒)',
    `resolution` VARCHAR(20) COMMENT '分辨率',
    `thumbnail_path` VARCHAR(500) COMMENT '缩略图路径',
    `execution_status` VARCHAR(20) COMMENT '执行结果: SUCCESS/FAILED (执行录制时)',
    `description` TEXT,
    `created_by` BIGINT,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_system` (`system_id`),
    KEY `idx_case` (`case_id`),
    KEY `idx_type` (`video_type`)
) ENGINE=InnoDB COMMENT='案例视频库';

-- ================================================================
-- 5. SKILL 管理模块
-- ================================================================

CREATE TABLE `ai_skill` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `skill_code` VARCHAR(50) NOT NULL COMMENT 'SKILL编码',
    `skill_name` VARCHAR(100) NOT NULL COMMENT 'SKILL名称',
    `skill_type` VARCHAR(30) NOT NULL COMMENT 'BUILTIN/CUSTOM/LEARNED',
    `category` VARCHAR(50) COMMENT 'TESTING/ANALYSIS/GENERATION/EXECUTION',
    `description` TEXT COMMENT 'SKILL描述(LLM意图匹配用)',
    `trigger_intent` JSON COMMENT '触发意图模板',
    `trigger_keywords` JSON COMMENT '触发关键词',
    `input_schema` JSON COMMENT '输入参数定义',
    `output_schema` JSON COMMENT '输出结果定义',
    `executor_type` VARCHAR(30) COMMENT '执行器: LLM/CODE/API/WORKFLOW',
    `executor_config` JSON COMMENT '执行器配置',
    `prompt_template` TEXT COMMENT 'LLM提示词模板',
    `script_content` TEXT COMMENT '脚本内容(CODE类型)',
    `source_type` VARCHAR(20) DEFAULT 'MANUAL' COMMENT 'MANUAL/LEARNED/EVOLVED',
    `learned_from` BIGINT COMMENT '从哪个经验学习',
    `version` INT DEFAULT 1,
    `confidence` DECIMAL(3,2) DEFAULT 1.00 COMMENT '置信度',
    `usage_count` INT DEFAULT 0,
    `success_count` INT DEFAULT 0,
    `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT 'ACTIVE/DISABLED/TESTING',
    `created_by` BIGINT,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_skill_code` (`skill_code`),
    KEY `idx_type` (`skill_type`),
    KEY `idx_category` (`category`)
) ENGINE=InnoDB COMMENT='SKILL定义';

CREATE TABLE `ai_skill_execution_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `skill_id` BIGINT NOT NULL,
    `input_params` JSON,
    `output_result` JSON,
    `status` VARCHAR(20) COMMENT 'SUCCESS/FAILED',
    `duration_ms` BIGINT,
    `error_message` TEXT,
    `user_feedback` VARCHAR(20) COMMENT 'GOOD/BAD/NONE',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_skill` (`skill_id`)
) ENGINE=InnoDB COMMENT='SKILL执行日志';

CREATE TABLE `ai_experience` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `experience_type` VARCHAR(30) COMMENT 'SUCCESS_CASE/FAILED_CASE/PATTERN/INSIGHT',
    `title` VARCHAR(200),
    `context` JSON COMMENT '上下文',
    `content` TEXT COMMENT '经验内容',
    `related_skill_id` BIGINT,
    `related_case_id` BIGINT,
    `tags` JSON,
    `is_promoted` TINYINT DEFAULT 0 COMMENT '是否已提升为SKILL',
    `promoted_skill_id` BIGINT,
    `created_by` BIGINT,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_type` (`experience_type`)
) ENGINE=InnoDB COMMENT='经验库(自学习)';

-- ================================================================
-- 6. 缺陷库 & 风险分析
-- ================================================================

CREATE TABLE `defect` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `defect_code` VARCHAR(50) UNIQUE,
    `defect_title` VARCHAR(200) NOT NULL,
    `system_id` BIGINT NOT NULL,
    `severity` VARCHAR(10) COMMENT 'S1/S2/S3/S4',
    `priority` VARCHAR(10) COMMENT 'P0/P1/P2/P3',
    `status` VARCHAR(20) DEFAULT 'OPEN' COMMENT 'OPEN/FIXED/VERIFIED/CLOSED/REOPENED',
    `related_case_id` BIGINT,
    `related_execution_id` BIGINT,
    `description` TEXT,
    `steps_to_reproduce` TEXT,
    `expected_result` TEXT,
    `actual_result` TEXT,
    `environment` TEXT,
    `screenshots` JSON,
    `assigned_to` BIGINT,
    `created_by` BIGINT,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_system` (`system_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB COMMENT='缺陷';

CREATE TABLE `risk_analysis` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `system_id` BIGINT NOT NULL,
    `analysis_type` VARCHAR(30) COMMENT 'REGRESSION/IMPACT/COVERAGE',
    `title` VARCHAR(200),
    `risk_level` VARCHAR(10) COMMENT 'HIGH/MEDIUM/LOW',
    `risk_items` JSON,
    `analysis_result` TEXT,
    `suggestions` TEXT,
    `ai_analysis` TEXT,
    `created_by` BIGINT,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_system` (`system_id`)
) ENGINE=InnoDB COMMENT='风险分析';

-- ================================================================
-- 7. LLM 对话模块
-- ================================================================

CREATE TABLE `llm_conversation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `session_id` VARCHAR(100) UNIQUE,
    `user_id` BIGINT NOT NULL,
    `title` VARCHAR(200),
    `context` JSON COMMENT '对话上下文配置',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB COMMENT='对话会话';

CREATE TABLE `llm_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `conversation_id` BIGINT NOT NULL,
    `role` VARCHAR(20) NOT NULL COMMENT 'user/assistant/system/tool',
    `content` TEXT NOT NULL,
    `tool_calls` JSON COMMENT '工具调用记录',
    `skill_invoked` VARCHAR(50) COMMENT '调用的SKILL编码',
    `token_usage` JSON COMMENT 'token使用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_conversation` (`conversation_id`)
) ENGINE=InnoDB COMMENT='对话消息';

-- ================================================================
-- 8. PC端录制模块
-- ================================================================

CREATE TABLE `record_session` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `session_id` VARCHAR(100) NOT NULL UNIQUE,
    `session_name` VARCHAR(200),
    `system_id` BIGINT COMMENT '所属系统',
    `target_url` VARCHAR(500) COMMENT '录制目标URL',
    `status` VARCHAR(20) DEFAULT 'RECORDING' COMMENT 'RECORDING/STOPPED/GENERATED',
    `steps` JSON COMMENT '录制的操作步骤',
    `step_count` INT DEFAULT 0,
    `video_path` VARCHAR(500) COMMENT '录制视频路径',
    `duration_ms` BIGINT COMMENT '录制时长',
    `generated_case_id` BIGINT COMMENT '生成的案例ID',
    `created_by` BIGINT,
    `started_at` DATETIME,
    `stopped_at` DATETIME,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_system` (`system_id`)
) ENGINE=InnoDB COMMENT='录制会话';

-- ================================================================
-- 9. 系统配置表
-- ================================================================

CREATE TABLE `sys_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `config_type` VARCHAR(20) DEFAULT 'STRING' COMMENT 'STRING/JSON/NUMBER/BOOLEAN',
    `category` VARCHAR(50) COMMENT 'LLM/NOTIFICATION/PLAYWRIGHT/STORAGE/SECURITY',
    `description` VARCHAR(200),
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB COMMENT='系统配置';

-- ================================================================
-- 初始数据
-- ================================================================

-- 默认管理员 (密码: admin123, BCrypt加密)
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `email`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员', 'admin@aitest.com', 1);

-- 默认角色
INSERT INTO `sys_role` (`role_code`, `role_name`, `description`) VALUES
('ADMIN', '系统管理员', '拥有全部权限'),
('TEST_MGR', '测试经理', '管理案例、案例集、知识库'),
('TESTER', '测试工程师', '执行案例、提交缺陷'),
('VIEWER', '只读用户', '仅查看权限');

-- 管理员关联角色
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1);

-- 默认系统配置
INSERT INTO `sys_config` (`config_key`, `config_value`, `config_type`, `category`, `description`) VALUES
('llm.default_model', 'qwen-max', 'STRING', 'LLM', '默认LLM模型'),
('llm.api_base_url', 'https://dashscope.aliyuncs.com', 'STRING', 'LLM', 'LLM API地址'),
('llm.temperature', '0.7', 'NUMBER', 'LLM', '默认温度'),
('playwright.default_browser', 'chromium', 'STRING', 'PLAYWRIGHT', '默认浏览器'),
('playwright.default_timeout', '30000', 'NUMBER', 'PLAYWRIGHT', '默认超时(ms)'),
('playwright.screenshot_on_step', 'true', 'BOOLEAN', 'PLAYWRIGHT', '每步截图'),
('playwright.video_on_execution', 'true', 'BOOLEAN', 'PLAYWRIGHT', '执行录像'),
('storage.type', 'local', 'STRING', 'STORAGE', '存储类型: local/minio'),
('storage.local_path', './uploads', 'STRING', 'STORAGE', '本地存储路径');

-- 默认知识库分类
INSERT INTO `kb_category` (`category_name`, `category_type`, `parent_id`, `sort_order`) VALUES
('案例库', 'CASE', 0, 1),
('需求文档库', 'REQUIREMENT', 0, 2),
('开发设计文档', 'DESIGN', 0, 3),
('GIT代码库', 'GIT', 0, 4),
('缺陷库', 'DEFECT', 0, 5),
('数据库表清单', 'DATABASE', 0, 6),
('案例视频库', 'VIDEO', 0, 7);

-- 默认权限
INSERT INTO `sys_permission` (`perm_code`, `perm_name`, `perm_type`, `parent_id`, `path`, `icon`, `sort_order`) VALUES
('dashboard', '首页概览', 'MENU', 0, '/', '📊', 1),
('case', '案例管理', 'MENU', 0, '/cases', '📋', 2),
('suite', '案例集执行', 'MENU', 0, '/suites', '📦', 3),
('execution', '执行详情', 'MENU', 0, '/executions', '📊', 4),
('knowledge', '知识库', 'MENU', 0, '/knowledge', '📚', 5),
('defect', '缺陷库', 'MENU', 0, '/defects', '🐛', 6),
('skill', 'SKILL管理', 'MENU', 0, '/skills', '🤖', 7),
('chat', 'AI对话', 'MENU', 0, '/chat', '💬', 8),
('record', '操作录制', 'MENU', 0, '/record', '🎬', 9),
('system', '系统管理', 'MENU', 0, '/systems', '⚙️', 10);
