# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

AI 全链路自动化测试平台 — 支持 PC (Playwright)、APP、API 测试案例的录制、执行和管理。

## 常用命令

```bash
# 后端
cd ai-test-backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql  # 启动 (MySQL 持久化)
./mvnw spring-boot:run -Dspring-boot.run.profiles=test   # 启动 (H2 内存库, 适合测试)
./mvnw test -Dspring.profiles.active=test                 # 运行测试
./mvnw compile -DskipTests                                # 编译检查

# MySQL
net start MySQL84              # 启动 MySQL 服务 (Windows)
mysql -u root -paitest123      # 连接 MySQL

# 前端
cd ai-test-frontend
npm run dev          # 开发服务器 :3000
npm run build        # 类型检查 + 构建

# E2E 测试
cd ai-test-playwright
npx playwright test          # 运行全部测试
npx playwright test --ui     # UI 模式
npx playwright test tests/03-system-management.spec.ts  # 运行单个文件

# 基础设施
docker-compose up -d         # MySQL + Redis + MinIO
```

## 测试账号

- 用户名: `admin`
- 密码: `admin123`
- 角色: admin

## 架构概览

### 目录结构
```
ai-test-platform/
├── ai-test-backend/       # Spring Boot 后端 (Java 17)
│   └── src/main/java/com/aitest/
│       ├── config/        # Security, CORS, JWT, MyBatis 配置
│       ├── common/        # Result<T>, BusinessException, JwtUtils
│       └── modules/       # 9 个业务模块 (system, case_mgr, case_suite, knowledge, skill, llm, defect, record, analysis)
├── ai-test-frontend/      # Vue 3 + Element Plus 前端
│   └── src/
│       ├── api/           # 11 个 API 模块
│       ├── views/         # 页面组件
│       ├── store/         # Pinia 状态管理
│       └── router/        # Vue Router 路由
├── ai-test-playwright/    # Playwright E2E 测试
│   ├── tests/             # 15 个测试文件, 61 个测试用例
│   └── src/pages/         # 15 个 Page Object
├── docs/                  # 数据库脚本、原型
└── .github/workflows/     # CI 流水线
```

### 关键设计
- **认证**: JWT (Bearer token) + BCrypt 密码 + Spring Security
- **数据库**: MySQL 8.4 (生产/开发, profile=mysql) / H2 内存库 (测试, profile=test)
- **API 响应**: 统一 Result<T> 包装 {code, message, data}
- **分页**: PageResult<T> {total, records, current, size, pages}
- **逻辑删除**: 所有实体 deleted 字段 (0=正常, 1=删除)

### 前后端 API 路径对应
前端 baseURL = `/api`，后端 Controller 路径以 `/api/` 开头。
例: 前端 `request.get('/system/systems')` → 后端 `/api/system/systems`

<!-- AUTO:START -->
<!-- 以下由 scripts/update-docs.sh 自动生成，请勿手动编辑 -->

## 项目规模

| 指标 | 数量 |
|------|------|
| Java 源文件 | 147 |
| Vue 组件 | 24 |
| API Controller | 18 |
| E2E 测试用例文件 | 15 |
| Page Object | 15 |

## API 端点清单

**AnalysisController** (`/api/analysis`)

| Method | Path |
|--------|------|
| post | `/api/analysis/analyze` |
| get | `/api/analysis/result/{documentId}` |
| post | `/api/analysis/generate-cases` |

**TestCaseController** (`/api/cases`)

| Method | Path |
|--------|------|
| post | `/api/cases` |
| put | `/api/cases/{id}` |
| get | `/api/cases/{id}` |
| get | `/api/cases` |
| delete | `/api/cases/{id}` |
| delete | `/api/cases/batch` |

**TestDataController** (`/api/test-data`)

| Method | Path |
|--------|------|
| post | `/api/test-data/tables` |
| get | `/api/test-data/tables` |
| get | `/api/test-data/tables/{id}` |
| put | `/api/test-data/tables/{id}` |
| delete | `/api/test-data/tables/{id}` |
| post | `/api/test-data/tables/{tableId}/rows` |
| get | `/api/test-data/tables/{tableId}/rows` |
| delete | `/api/test-data/tables/{tableId}/rows/{rowId}` |

**TestExecutionController** (`/api/executions`)

| Method | Path |
|--------|------|
| get | `/api/executions` |
| get | `/api/executions/{id}` |
| get | `/api/executions/{id}/report` |
| get | `/api/executions/{id}/cases/{caseId}/log` |
| post | `/api/executions/{id}/cancel` |

**TestSuiteController** (`/api/suites`)

| Method | Path |
|--------|------|
| post | `/api/suites` |
| put | `/api/suites/{id}` |
| get | `/api/suites/{id}` |
| get | `/api/suites` |
| delete | `/api/suites/{id}` |
| post | `/api/suites/{id}/cases` |
| delete | `/api/suites/{id}/cases` |
| post | `/api/suites/{id}/execute` |

**DefectController** (`/api/defects`)

| Method | Path |
|--------|------|
| get | `/api/defects` |
| get | `/api/defects/{id}` |
| post | `/api/defects` |
| put | `/api/defects/{id}` |
| delete | `/api/defects/{id}` |
| put | `/api/defects/{id}/status` |
| get | `/api/defects/statistics` |

**RiskAnalysisController** (`/api/risk-analysis`)

| Method | Path |
|--------|------|
| get | `/api/risk-analysis` |
| get | `/api/risk-analysis/{id}` |
| post | `/api/risk-analysis` |
| delete | `/api/risk-analysis/{id}` |

**KbCategoryController** (`/api/knowledge/categories`)

| Method | Path |
|--------|------|
| get | `/api/knowledge/categories` |
| post | `/api/knowledge/categories` |
| put | `/api/knowledge/categories/{id}` |
| delete | `/api/knowledge/categories/{id}` |

**KbDocumentController** (`/api/knowledge/documents`)

| Method | Path |
|--------|------|
| get | `/api/knowledge/documents` |
| get | `/api/knowledge/documents/{id}` |
| post | `/api/knowledge/documents/upload` |
| post | `/api/knowledge/documents/{id}/parse` |
| delete | `/api/knowledge/documents/{id}` |
| get | `/api/knowledge/documents/search` |

**KbVideoController** (`/api/knowledge/videos`)

| Method | Path |
|--------|------|
| get | `/api/knowledge/videos` |
| post | `/api/knowledge/videos` |
| delete | `/api/knowledge/videos/{id}` |

**LlmChatController** (`/api/llm`)

| Method | Path |
|--------|------|
| get | `/api/llm/conversations` |
| post | `/api/llm/conversations` |
| get | `/api/llm/conversations/{id}` |
| delete | `/api/llm/conversations/{id}` |
| put | `/api/llm/conversations/{id}/title` |
| post | `/api/llm/chat` |
| post | `/api/llm/intent` |

**RecordController** (`/api/record`)

| Method | Path |
|--------|------|
| get | `/api/record/sessions` |
| get | `/api/record/sessions/{id}` |
| get | `/api/record/sessions/by-id/{sessionId}` |
| post | `/api/record/sessions/start` |
| post | `/api/record/sessions/{id}/stop` |
| post | `/api/record/sessions/{id}/steps` |
| delete | `/api/record/sessions/{id}` |
| post | `/api/record/sessions/{id}/generate` |

**AiExperienceController** (`/api/experiences`)

| Method | Path |
|--------|------|
| get | `/api/experiences` |
| post | `/api/experiences` |
| post | `/api/experiences/{id}/promote` |
| delete | `/api/experiences/{id}` |

**AiSkillController** (`/api/skills`)

| Method | Path |
|--------|------|
| get | `/api/skills` |
| get | `/api/skills/{id}` |
| post | `/api/skills` |
| put | `/api/skills/{id}` |
| delete | `/api/skills/{id}` |
| post | `/api/skills/invoke` |
| get | `/api/skills/intent` |
| get | `/api/skills/active` |
| get | `/api/skills/{id}/logs` |
| post | `/api/skills/logs/{logId}/feedback` |

**AuthController** (`/api/auth`)

| Method | Path |
|--------|------|
| post | `/api/auth/login` |
| post | `/api/auth/logout` |
| get | `/api/auth/info` |

**SysConfigController** (`/api/config`)

| Method | Path |
|--------|------|
| get | `/api/config` |
| get | `/api/config/category/{category}` |
| get | `/api/config/value/{key}` |
| put | `/api/config/value/{key}` |
| put | `/api/config/batch` |
| post | `/api/config` |
| delete | `/api/config/{id}` |

**SysSystemController** (`/api/system/systems`)

| Method | Path |
|--------|------|
| get | `/api/system/systems` |
| get | `/api/system/systems/{id}` |
| get | `/api/system/systems/code/{code}` |
| post | `/api/system/systems` |
| put | `/api/system/systems/{id}` |
| delete | `/api/system/systems/{id}` |
| delete | `/api/system/systems/batch` |
| get | `/api/system/systems/all` |

**SysUserController** (`/api/system/users`)

| Method | Path |
|--------|------|
| get | `/api/system/users` |
| get | `/api/system/users/{id}` |
| post | `/api/system/users` |
| put | `/api/system/users/{id}` |
| delete | `/api/system/users/{id}` |
| delete | `/api/system/users/batch` |
| put | `/api/system/users/{id}/status` |
| put | `/api/system/users/{id}/reset-password` |

## 前端路由

| 路径 | 页面 |
|------|------|
| `/login` | 登录 |
| `/dashboard` | 工作台 |
| `/cases` | 测试用例 |
| `/cases/create` | 新建用例 |
| `/cases/:id` | 用例详情 |
| `/cases/:id/data-table` | 测试数据管理 |
| `/suites` | 用例套件 |
| `/suites/:id` | 套件详情 |
| `/executions` | 执行记录 |
| `/executions/:id` | 执行详情 |
| `/knowledge` | 知识库 |
| `/defects` | 缺陷管理 |
| `/risk-analysis` | 风险分析 |
| `/analysis` | 需求分析 |
| `/skills` | AI 技能 |
| `/chat` | AI 对话 |
| `/record` | 录制回放 |
| `/record/:sessionId` | 录制详情 |
| `/systems` | 系统管理 |
| `/users` | 用户管理 |
| `/roles` | 角色管理 |
| `/settings` | 系统设置 |

## 技术栈版本

| 层 | 技术 | 版本 |
|----|------|------|
| 后端 | Spring Boot | 3.2.5 |
| 语言 | Java | 17 |
| 前端 | Vue | 3.4.38 |
| 构建 | Vite | 5.4.3 |
| E2E | Playwright | 1.48.0 |

---
_自动更新于 2026-06-14 18:05_
<!-- AUTO:END -->
