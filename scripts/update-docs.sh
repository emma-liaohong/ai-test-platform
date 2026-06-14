#!/usr/bin/env bash
# ═══════════════════════════════════════════════════════════════
# update-docs.sh — 自动扫描代码更新 CLAUDE.md
# ═══════════════════════════════════════════════════════════════

set -e

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
CLAUDE_MD="$ROOT_DIR/CLAUDE.md"
BACKEND_JAVA="$ROOT_DIR/ai-test-backend/src/main/java/com/aitest"
FRONTEND_SRC="$ROOT_DIR/ai-test-frontend/src"
QUIET=false
[ "$1" = "--quiet" ] && QUIET=true

log() { $QUIET || echo "  $1"; }

# ═══ 生成 API 端点表 ═══
generate_api_section() {
  local outfile=$(mktemp)

  # Use awk to process all controllers at once
  find "$BACKEND_JAVA" -name "*Controller.java" -type f 2>/dev/null | sort | \
  while read -r ctrl; do
    local name=$(basename "$ctrl" .java)
    local base=$(grep '@RequestMapping' "$ctrl" | head -1 | sed 's/.*"\(.*\)".*/\1/')
    [ -z "$base" ] && continue

    echo "**${name}** (\`${base}\`)"
    echo ""
    echo "| Method | Path |"
    echo "|--------|------|"

    # Extract endpoints using awk (no nested while-read)
    awk -v base="$base" '
      /@(Get|Post|Put|Delete|Patch)Mapping/ {
        # Extract method
        match($0, /@(Get|Post|Put|Delete|Patch)Mapping/, m)
        method = tolower(m[1])
        # Extract subpath
        if (match($0, /"([^"]*)"/, p)) {
          subpath = p[1]
        } else {
          subpath = ""
        }
        printf "| %s | `%s%s` |\n", method, base, subpath
      }
    ' "$ctrl"

    echo ""
  done > "$outfile"

  cat "$outfile"
  rm -f "$outfile"
}

# ═══ 生成路由表 ═══
generate_routes_section() {
  local router="$FRONTEND_SRC/router/index.ts"
  [ ! -f "$router" ] && echo "路由文件不存在" && return

  echo "| 路径 | 页面 |"
  echo "|------|------|"

  # Extract path + title (they are on adjacent lines: path on one, meta: { title } on next)
  awk '
    /path:/ {
      match($0, /path: *'\''([^'\'']*)'\''/, p)
      current_path = p[1]
    }
    /meta:.*title:/ {
      match($0, /title: *'\''([^'\'']*)'\''/, t)
      title = t[1]
      if (current_path != "" && title != "") {
        path = current_path
        if (path == "/") path = "/dashboard (redirect)"
        if (substr(path,1,1) != "/") path = "/" path
        printf "| `%s` | %s |\n", path, title
      }
      current_path = ""
    }
  ' "$router"
}

# ═══ 技术栈版本 ═══
generate_versions_section() {
  local pom="$ROOT_DIR/ai-test-backend/pom.xml"
  local fpkg="$ROOT_DIR/ai-test-frontend/package.json"
  local ppkg="$ROOT_DIR/ai-test-playwright/package.json"

  local spring=$(grep '<version>' "$pom" 2>/dev/null | head -1 | sed 's/.*<version>\(.*\)<\/version>.*/\1/')
  local java=$(sed -n 's/.*<java.version>\(.*\)<\/java.version>.*/\1/p' "$pom" 2>/dev/null)
  local vue=$(grep '"vue":' "$fpkg" 2>/dev/null | head -1 | sed 's/.*"\^*\([^"]*\)".*/\1/')
  local vite=$(grep '"vite":' "$fpkg" 2>/dev/null | head -1 | sed 's/.*"\^*\([^"]*\)".*/\1/')
  local pw=$(grep '"@playwright/test"' "$ppkg" 2>/dev/null | head -1 | sed 's/.*"\^*\([^"]*\)".*/\1/')

  echo "| 层 | 技术 | 版本 |"
  echo "|----|------|------|"
  echo "| 后端 | Spring Boot | ${spring:-—} |"
  echo "| 语言 | Java | ${java:-17} |"
  echo "| 前端 | Vue | ${vue:-—} |"
  echo "| 构建 | Vite | ${vite:-—} |"
  echo "| E2E | Playwright | ${pw:-—} |"
}

# ═══ 项目统计 ═══
generate_stats_section() {
  local java_n=$(find "$BACKEND_JAVA" -name "*.java" 2>/dev/null | wc -l | tr -d ' ')
  local vue_n=$(find "$FRONTEND_SRC" -name "*.vue" 2>/dev/null | wc -l | tr -d ' ')
  local ctrl_n=$(find "$BACKEND_JAVA" -name "*Controller.java" 2>/dev/null | wc -l | tr -d ' ')
  local e2e_n=$(find "$ROOT_DIR/ai-test-playwright/tests" -name "*.spec.ts" 2>/dev/null | wc -l | tr -d ' ')
  local page_n=$(find "$ROOT_DIR/ai-test-playwright/src/pages" -name "*.ts" 2>/dev/null | wc -l | tr -d ' ')

  echo "| 指标 | 数量 |"
  echo "|------|------|"
  echo "| Java 源文件 | ${java_n} |"
  echo "| Vue 组件 | ${vue_n} |"
  echo "| API Controller | ${ctrl_n} |"
  echo "| E2E 测试用例文件 | ${e2e_n} |"
  echo "| Page Object | ${page_n} |"
}

# ═══ 主流程 ═══
main() {
  log "🤖 开始扫描代码更新 CLAUDE.md..."

  local api=$(generate_api_section)
  local routes=$(generate_routes_section)
  local versions=$(generate_versions_section)
  local stats=$(generate_stats_section)
  local ts=$(date '+%Y-%m-%d %H:%M')

  local auto_block="<!-- AUTO:START -->
<!-- 以下由 scripts/update-docs.sh 自动生成，请勿手动编辑 -->

## 项目规模

${stats}

## API 端点清单

${api}

## 前端路由

${routes}

## 技术栈版本

${versions}

---
_自动更新于 ${ts}_
<!-- AUTO:END -->"

  if [ ! -f "$CLAUDE_MD" ]; then
    log "📝 创建 CLAUDE.md..."
    cat > "$CLAUDE_MD" <<'HEADER'
# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

AI 全链路自动化测试平台 — 支持 PC (Playwright)、APP、API 测试案例的录制、执行和管理。

## 常用命令

```bash
# 后端
cd ai-test-backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=test   # 启动 (H2 内存库)
./mvnw test -Dspring.profiles.active=test                 # 运行测试
./mvnw compile -DskipTests                                # 编译检查

# 前端
cd ai-test-frontend
npm run dev          # 开发服务器 :3000
npm run build        # 类型检查 + 构建

# E2E 测试
cd ai-test-playwright
npx playwright test          # 运行全部测试
npx playwright test --ui     # UI 模式
npx playwright test tests/03-system-management.spec.ts  # 运行单个文件
```

## 测试账号

- 用户名: `admin` / 密码: `admin123` / 角色: admin

## 架构概览

```
ai-test-platform/
├── ai-test-backend/       # Spring Boot 后端 (Java 17)
│   └── src/main/java/com/aitest/
│       ├── config/        # Security, CORS, JWT, MyBatis
│       ├── common/        # Result<T>, BusinessException, JwtUtils
│       └── modules/       # 9 个业务模块
├── ai-test-frontend/      # Vue 3 + Element Plus 前端
│   └── src/
│       ├── api/           # API 模块
│       ├── views/         # 页面组件
│       ├── store/         # Pinia
│       └── router/        # Vue Router
├── ai-test-playwright/    # Playwright E2E 测试
├── docs/                  # 数据库脚本、原型
└── .github/workflows/     # CI 流水线
```

### 关键设计
- **认证**: JWT + BCrypt + Spring Security
- **数据库**: MySQL (生产) / H2 内存库 (测试 profile=test)
- **API 响应**: `Result<T>` → `{code, message, data}`
- **分页**: `PageResult<T>` → `{total, records, current, size, pages}`
- **逻辑删除**: `deleted` 字段 (0/1)
- **路径映射**: 前端 baseURL=`/api`，后端 Controller 以 `/api/` 开头

HEADER
    echo "$auto_block" >> "$CLAUDE_MD"
    log "✅ CLAUDE.md 创建完成"
    return
  fi

  # 更新已有文件的自动区域
  if grep -q "<!-- AUTO:START -->" "$CLAUDE_MD"; then
    local tmp=$(mktemp)
    awk -v block="$auto_block" '
      /<!-- AUTO:START -->/ { print block; skip=1; next }
      /<!-- AUTO:END -->/   { skip=0; next }
      !skip                  { print }
    ' "$CLAUDE_MD" > "$tmp"
    mv "$tmp" "$CLAUDE_MD"
    log "✅ 自动区域已更新"
  else
    echo "" >> "$CLAUDE_MD"
    echo "$auto_block" >> "$CLAUDE_MD"
    log "✅ 自动区域已追加"
  fi
}

main "$@"
