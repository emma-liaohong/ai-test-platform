-- H2-compatible schema for testing (MySQL mode)
-- Removes: ENGINE=InnoDB, COMMENT, FULLTEXT, ON UPDATE CURRENT_TIMESTAMP
-- Replaces: JSON -> TEXT, LONGTEXT -> CLOB

CREATE TABLE IF NOT EXISTS sys_system (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    description TEXT,
    base_url VARCHAR(500),
    type VARCHAR(20) DEFAULT 'WEB',
    status INT DEFAULT 1,
    create_by BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(200) NOT NULL,
    real_name VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20),
    avatar VARCHAR(500),
    status INT DEFAULT 1,
    role VARCHAR(50) DEFAULT 'viewer',
    create_by BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(50) NOT NULL,
    role_name VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    status TINYINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    perm_code VARCHAR(100) NOT NULL,
    perm_name VARCHAR(100) NOT NULL,
    perm_type VARCHAR(20) DEFAULT 'MENU',
    parent_id BIGINT DEFAULT 0,
    path VARCHAR(200),
    icon VARCHAR(50),
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS sys_role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE IF NOT EXISTS test_case (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    case_code VARCHAR(50) NOT NULL,
    case_name VARCHAR(200) NOT NULL,
    case_type VARCHAR(20) NOT NULL,
    system_id BIGINT NOT NULL,
    module_path VARCHAR(500),
    priority VARCHAR(10) DEFAULT 'P2',
    case_level VARCHAR(10),
    record_session_id VARCHAR(100),
    recorded_steps TEXT,
    natural_language_steps TEXT,
    playwright_script TEXT,
    app_operations TEXT,
    api_url VARCHAR(500),
    api_method VARCHAR(10),
    api_request_schema TEXT,
    api_response_schema TEXT,
    api_headers TEXT,
    preconditions TEXT,
    expected_result TEXT,
    tags VARCHAR(500),
    status VARCHAR(20) DEFAULT 'DRAFT',
    is_data_driven TINYINT DEFAULT 0,
    data_table_id BIGINT,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS test_case_step (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    case_id BIGINT NOT NULL,
    step_order INT NOT NULL,
    step_action VARCHAR(50),
    step_target VARCHAR(500),
    step_value TEXT,
    step_description TEXT,
    expected_result TEXT,
    screenshot_path VARCHAR(500),
    timeout_ms INT DEFAULT 3000,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS test_data_table (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_name VARCHAR(100) NOT NULL,
    system_id BIGINT,
    description TEXT,
    columns_def TEXT,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS test_data_row (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_id BIGINT NOT NULL,
    row_data TEXT,
    row_index INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS test_suite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    suite_name VARCHAR(200) NOT NULL,
    system_id BIGINT NOT NULL,
    description TEXT,
    suite_type VARCHAR(20) DEFAULT 'MANUAL',
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS test_suite_case (
    suite_id BIGINT NOT NULL,
    case_id BIGINT NOT NULL,
    sort_order INT DEFAULT 0,
    PRIMARY KEY (suite_id, case_id)
);

CREATE TABLE IF NOT EXISTS test_execution (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    execution_name VARCHAR(200),
    suite_id BIGINT,
    system_id BIGINT,
    env_config TEXT,
    status VARCHAR(20) DEFAULT 'PENDING',
    trigger_type VARCHAR(20),
    total_count INT DEFAULT 0,
    passed_count INT DEFAULT 0,
    failed_count INT DEFAULT 0,
    skipped_count INT DEFAULT 0,
    started_at TIMESTAMP,
    finished_at TIMESTAMP,
    report_content TEXT,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS test_execution_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    execution_id BIGINT NOT NULL,
    case_id BIGINT NOT NULL,
    status VARCHAR(20),
    started_at TIMESTAMP,
    finished_at TIMESTAMP,
    duration_ms BIGINT,
    error_message TEXT,
    error_step INT,
    screenshots TEXT,
    video_path VARCHAR(500),
    execution_log TEXT,
    compare_result TEXT,
    ai_analysis TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS kb_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL,
    category_type VARCHAR(50) NOT NULL,
    system_id BIGINT,
    parent_id BIGINT DEFAULT 0,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS kb_document (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doc_code VARCHAR(50),
    doc_name VARCHAR(200) NOT NULL,
    category_id BIGINT NOT NULL,
    system_id BIGINT,
    doc_type VARCHAR(50),
    file_path VARCHAR(500),
    file_size BIGINT,
    content CLOB,
    content_vector TEXT,
    metadata TEXT,
    parse_status VARCHAR(20) DEFAULT 'PENDING',
    parse_result TEXT,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS kb_chunk (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_id BIGINT NOT NULL,
    chunk_index INT,
    content TEXT NOT NULL,
    content_type VARCHAR(20),
    embedding TEXT,
    metadata TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS kb_video (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    video_name VARCHAR(200) NOT NULL,
    video_type VARCHAR(20) NOT NULL,
    system_id BIGINT,
    case_id BIGINT,
    execution_id BIGINT,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    duration_ms BIGINT,
    resolution VARCHAR(20),
    thumbnail_path VARCHAR(500),
    execution_status VARCHAR(20),
    description TEXT,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ai_skill (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    skill_code VARCHAR(50) NOT NULL,
    skill_name VARCHAR(100) NOT NULL,
    skill_type VARCHAR(30) NOT NULL,
    category VARCHAR(50),
    description TEXT,
    trigger_intent TEXT,
    trigger_keywords TEXT,
    input_schema TEXT,
    output_schema TEXT,
    executor_type VARCHAR(30),
    executor_config TEXT,
    prompt_template TEXT,
    script_content TEXT,
    source_type VARCHAR(20) DEFAULT 'MANUAL',
    learned_from BIGINT,
    version INT DEFAULT 1,
    confidence DECIMAL(3,2) DEFAULT 1.00,
    usage_count INT DEFAULT 0,
    success_count INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ai_skill_execution_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    skill_id BIGINT NOT NULL,
    input_params TEXT,
    output_result TEXT,
    status VARCHAR(20),
    duration_ms BIGINT,
    error_message TEXT,
    user_feedback VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ai_experience (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    experience_type VARCHAR(30),
    title VARCHAR(200),
    context TEXT,
    content TEXT,
    related_skill_id BIGINT,
    related_case_id BIGINT,
    tags TEXT,
    is_promoted TINYINT DEFAULT 0,
    promoted_skill_id BIGINT,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS defect (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    defect_code VARCHAR(50),
    defect_title VARCHAR(200) NOT NULL,
    system_id BIGINT NOT NULL,
    severity VARCHAR(10),
    priority VARCHAR(10),
    status VARCHAR(20) DEFAULT 'OPEN',
    related_case_id BIGINT,
    related_execution_id BIGINT,
    description TEXT,
    steps_to_reproduce TEXT,
    expected_result TEXT,
    actual_result TEXT,
    environment TEXT,
    screenshots TEXT,
    assigned_to BIGINT,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS risk_analysis (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    system_id BIGINT NOT NULL,
    analysis_type VARCHAR(30),
    title VARCHAR(200),
    risk_level VARCHAR(10),
    risk_items TEXT,
    analysis_result TEXT,
    suggestions TEXT,
    ai_analysis TEXT,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS llm_conversation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(100),
    user_id BIGINT NOT NULL,
    title VARCHAR(200),
    context TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS llm_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    tool_calls TEXT,
    skill_invoked VARCHAR(50),
    token_usage TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS record_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(100) NOT NULL,
    session_name VARCHAR(200),
    system_id BIGINT,
    target_url VARCHAR(500),
    status VARCHAR(20) DEFAULT 'RECORDING',
    steps TEXT,
    step_count INT DEFAULT 0,
    video_path VARCHAR(500),
    duration_ms BIGINT,
    generated_case_id BIGINT,
    created_by BIGINT,
    started_at TIMESTAMP,
    stopped_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL,
    config_value TEXT,
    config_type VARCHAR(20) DEFAULT 'STRING',
    category VARCHAR(50),
    description VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
