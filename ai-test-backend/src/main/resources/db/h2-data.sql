-- H2 initial data for testing

-- Default admin (password: admin123, BCrypt)
INSERT INTO sys_user (username, password, real_name, email, status, role) VALUES
('admin', '$2a$10$0Vz8Na/Zivoa.SaRUEs9Mun.SIctn02bvoWALwCdj8YTlxmoYfNwy', '管理员', 'admin@aitest.com', 1, 'admin');

-- Default roles
INSERT INTO sys_role (role_code, role_name, description) VALUES
('ADMIN', '系统管理员', '拥有全部权限'),
('TEST_MGR', '测试经理', '管理案例、案例集、知识库'),
('TESTER', '测试工程师', '执行案例、提交缺陷'),
('VIEWER', '只读用户', '仅查看权限');

-- Admin role association
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- Default system configs
INSERT INTO sys_config (config_key, config_value, config_type, category, description) VALUES
('llm.default_model', 'qwen-max', 'STRING', 'LLM', '默认LLM模型'),
('llm.api_base_url', 'https://dashscope.aliyuncs.com', 'STRING', 'LLM', 'LLM API地址'),
('llm.temperature', '0.7', 'NUMBER', 'LLM', '默认温度'),
('playwright.default_browser', 'chromium', 'STRING', 'PLAYWRIGHT', '默认浏览器'),
('playwright.default_timeout', '30000', 'NUMBER', 'PLAYWRIGHT', '默认超时(ms)'),
('storage.type', 'local', 'STRING', 'STORAGE', '存储类型');

-- Default knowledge categories
INSERT INTO kb_category (category_name, category_type, parent_id, sort_order) VALUES
('案例库', 'CASE', 0, 1),
('需求文档库', 'REQUIREMENT', 0, 2),
('开发设计文档', 'DESIGN', 0, 3),
('GIT代码库', 'GIT', 0, 4),
('缺陷库', 'DEFECT', 0, 5),
('数据库表清单', 'DATABASE', 0, 6),
('案例视频库', 'VIDEO', 0, 7);

-- Default managed systems
INSERT INTO sys_system (code, name, type, base_url, description, status) VALUES
('ORDER', '订单系统', 'WEB', 'https://order.example.com', '订单管理系统', 1),
('PAY', '支付系统', 'WEB', 'https://pay.example.com', '支付网关系统', 1),
('USER', '用户系统', 'WEB', 'https://user.example.com', '用户中心系统', 1);

-- Default permissions
INSERT INTO sys_permission (perm_code, perm_name, perm_type, parent_id, path, icon, sort_order) VALUES
('dashboard', '首页概览', 'MENU', 0, '/', 'Odometer', 1),
('case', '案例管理', 'MENU', 0, '/cases', 'Document', 2),
('suite', '案例集执行', 'MENU', 0, '/suites', 'FolderOpened', 3),
('knowledge', '知识库', 'MENU', 0, '/knowledge', 'Collection', 5),
('defect', '缺陷库', 'MENU', 0, '/defects', 'WarningFilled', 6),
('skill', 'SKILL管理', 'MENU', 0, '/skills', 'MagicStick', 7),
('chat', 'AI对话', 'MENU', 0, '/chat', 'ChatDotRound', 8),
('record', '操作录制', 'MENU', 0, '/record', 'Monitor', 9),
('system', '系统管理', 'MENU', 0, '/systems', 'Platform', 10);

-- Sample SKILLs (trigger_keywords stored as JSON string, not array)
INSERT INTO ai_skill (skill_code, skill_name, skill_type, category, description, executor_type, trigger_keywords, status, confidence, usage_count, success_count) VALUES
('case-generator', '智能案例生成', 'BUILTIN', 'GENERATION', '根据需求描述自动生成测试案例', 'LLM', '"[\"生成案例\",\"创建用例\",\"测试案例\"]"', 'ACTIVE', 0.95, 156, 145),
('requirement-parser', '需求解析', 'BUILTIN', 'ANALYSIS', '解析需求文档提取功能点', 'LLM', '"[\"分析需求\",\"解析文档\",\"功能提取\"]"', 'ACTIVE', 0.88, 89, 76),
('defect-analyzer', '缺陷分析', 'BUILTIN', 'ANALYSIS', '分析缺陷根因并给出修复建议', 'WORKFLOW', '"[\"分析缺陷\",\"排查问题\",\"Bug分析\"]"', 'ACTIVE', 0.92, 67, 62);
