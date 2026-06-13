# AI 全链路自动化测试平台

> AI-driven Full-Chain Automation Testing Platform

基于 AI 大模型驱动的全链路自动化测试平台，支持 PC 端（Playwright）、APP 端、API 接口三类测试案例的录制、执行和管理。

## 🏗 技术栈

| 层次 | 技术 |
|------|------|
| 后端 | Spring Boot 3.2 + Java 17 + MyBatis-Plus 3.5 |
| 前端 | Vue 3 + Vite + TypeScript + Element Plus |
| 数据库 | MySQL 8.0 + Redis |
| API文档 | Knife4j (Swagger/OpenAPI 3) |
| 认证 | JWT + Spring Security |
| 浏览器自动化 | Playwright |
| 容器化 | Docker Compose |

## ✨ 功能模块

### 📋 案例管理
- PC / APP / API 三类测试案例
- 数据驱动测试（测试数据表管理）
- Playwright 脚本预览
- 自然语言步骤描述

### 📦 案例集执行
- 批量执行测试套件
- 6 Tab 执行详情：步骤截图 / 测试报告 / 执行日志 / 截图对比 / 视频回放 / AI 分析

### 📚 知识库管理
- 分类树管理（案例库 / 需求文档 / 设计文档 / GIT / 缺陷 / 数据库 / 视频库）
- 文档上传与解析（PDF / Word / Markdown / Excel / 图片）
- 案例视频库
- AI 语义搜索

### 🤖 SKILL 管理
- 4 种执行器类型：LLM / CODE / API / WORKFLOW
- 意图匹配与自动路由
- 自学习进化（从经验生成新 SKILL）
- SKILL 实验场（在线测试）

### 💬 AI 对话
- 自然语言驱动 SKILL 调用
- 工具调用可视化（Tool Call Cards）
- Markdown 渲染
- 快捷指令（/生成案例 /分析需求 /执行测试 /查询知识库）

### 🐛 缺陷管理
- 缺陷状态流转（OPEN → FIXED → VERIFIED → CLOSED）
- 统计面板（按严重度 / 状态 / 优先级）
- AI 风险分析报告

### 🎬 录制回放
- 浏览器预览 + 操作步骤实时记录
- 步骤高亮动画
- 回放模式（逐步动画重放）
- 一键生成 Playwright 脚本

### 📊 需求分析
- 知识库文档 → 功能点提取
- 影响分析（对现有案例 / 接口 / 数据库的影响）
- 自动生成测试案例
- AI 分析报告导出

### ⚙️ 系统配置
- LLM 模型配置
- Playwright 配置
- 存储配置
- 通知配置

## 🚀 快速开始

### 前置条件
- Java 17+
- Node.js 18+
- Docker & Docker Compose
- Maven 3.8+

### 1. 启动基础设施
```bash
docker-compose up -d
```
- MySQL: `localhost:3306` (root / aitest123)
- Redis: `localhost:6379` (password: aitest123)
- MinIO: `localhost:9000` (aitest / aitest123)

### 2. 启动后端
```bash
cd ai-test-backend
mvn spring-boot:run
```
后端运行在 `http://localhost:8080`  
API 文档: `http://localhost:8080/doc.html`

### 3. 启动前端
```bash
cd ai-test-frontend
npm install
npm run dev
```
前端运行在 `http://localhost:5173`

### 4. 登录
- 用户名: `admin`
- 密码: `admin123`

## 📁 项目结构

```
ai-test-platform/
├── ai-test-backend/          # Spring Boot 后端
│   └── src/main/java/com/aitest/
│       ├── common/           # 公共模块（Result, Exception, JWT）
│       ├── config/           # 全局配置（Security, CORS, MyBatis）
│       ├── modules/
│       │   ├── system/       # 系统管理（用户/角色/系统/配置）
│       │   ├── case_mgr/     # 案例管理
│       │   ├── case_suite/   # 案例集执行
│       │   ├── knowledge/    # 知识库
│       │   ├── skill/        # SKILL 管理 + 执行引擎
│       │   ├── llm/          # LLM 对话
│       │   ├── defect/       # 缺陷 + 风险分析
│       │   ├── record/       # 录制回放
│       │   └── analysis/     # 需求分析 + 案例生成
│       └── integration/      # 外部集成
│
├── ai-test-frontend/         # Vue 3 前端
│   └── src/
│       ├── api/              # API 接口封装
│       ├── views/            # 页面组件
│       ├── layouts/          # 布局组件
│       ├── store/            # Pinia 状态管理
│       ├── router/           # 路由
│       └── utils/            # 工具函数
│
├── docs/
│   ├── database/init.sql     # 数据库建表脚本（28张表）
│   └── frontend-prototype.html  # 前端交互原型
│
└── docker-compose.yml        # 基础设施编排
```

## 📊 数据库
- 28 张表，涵盖系统管理、案例、执行、知识库、SKILL、对话、缺陷、录制、配置
- 完整建表脚本 + 初始数据（管理员账号、角色、权限、默认配置）

## 📄 License
MIT
