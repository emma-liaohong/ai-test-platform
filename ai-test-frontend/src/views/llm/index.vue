<template>
  <div class="chat-page">
    <!-- ==================== Left Sidebar ==================== -->
    <div class="chat-sidebar">
      <div class="sidebar-header">
        <el-button type="primary" class="new-chat-btn" @click="handleNewConversation">
          <el-icon><Plus /></el-icon>
          <span>新建会话</span>
        </el-button>
      </div>

      <div class="sidebar-search">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索会话..."
          prefix-icon="Search"
          clearable
          size="small"
        />
      </div>

      <div class="conversation-list">
        <template v-for="group in groupedConversations" :key="group.label">
          <div class="date-group-label">{{ group.label }}</div>
          <div
            v-for="conv in group.items"
            :key="conv.id"
            class="conversation-item"
            :class="{ active: currentConversationId === conv.id }"
            @click="switchConversation(conv)"
          >
            <div class="conv-main">
              <div class="conv-title text-ellipsis">{{ conv.title }}</div>
              <div class="conv-preview text-ellipsis">{{ conv.lastMessage || '暂无消息' }}</div>
            </div>
            <div class="conv-meta">
              <span class="conv-time">{{ formatRelativeTime(conv.updatedAt) }}</span>
              <el-icon
                class="conv-delete"
                @click.stop="handleDeleteConversation(conv)"
              >
                <Delete />
              </el-icon>
            </div>
          </div>
        </template>

        <div v-if="filteredConversations.length === 0" class="empty-conversations">
          <el-empty :description="searchKeyword ? '未找到匹配会话' : '暂无会话'" :image-size="60" />
        </div>
      </div>
    </div>

    <!-- ==================== Right Chat Area ==================== -->
    <div class="chat-main">
      <!-- Header -->
      <div class="chat-header">
        <div class="chat-header-left">
          <el-icon :size="20" color="var(--el-color-primary)"><ChatDotRound /></el-icon>
          <template v-if="currentConversation">
            <span v-if="!isEditingTitle" class="chat-title" @dblclick="startEditTitle">
              {{ currentConversation.title }}
            </span>
            <el-input
              v-else
              ref="titleInputRef"
              v-model="editingTitle"
              size="small"
              class="title-edit-input"
              @blur="finishEditTitle"
              @keyup.enter="finishEditTitle"
            />
          </template>
          <span v-else class="chat-title-placeholder">选择一个会话或新建会话开始聊天</span>
        </div>
        <div class="chat-header-right">
          <el-button
            v-if="currentConversation"
            text
            type="danger"
            size="small"
            @click="handleDeleteConversation(currentConversation)"
          >
            <el-icon><Delete /></el-icon>
            删除会话
          </el-button>
        </div>
      </div>

      <!-- Messages Area -->
      <div ref="messagesContainerRef" class="messages-container">
        <!-- Welcome message when no conversation -->
        <div v-if="!currentConversation && messages.length === 0" class="welcome-screen">
          <el-icon :size="64" color="var(--el-color-primary)"><ChatDotRound /></el-icon>
          <h2>🤖 AI 测试助手</h2>
          <p>你好！我是 AI 测试助手。你可以：</p>
          <ul>
            <li>🧪 让我根据需求生成测试案例</li>
            <li>🔍 让我分析某个功能的测试范围</li>
            <li>🐛 让我帮你排查缺陷原因</li>
            <li>📊 让我评估回归测试风险</li>
          </ul>
          <p class="welcome-hint">新建一个会话开始聊天吧 👆</p>
        </div>

        <!-- Messages -->
        <template v-for="(msg, index) in messages" :key="msg.id || index">
          <!-- Date separator -->
          <div
            v-if="shouldShowDateSeparator(index)"
            class="date-separator"
          >
            <span>{{ formatDateSeparator(msg.createdAt) }}</span>
          </div>

          <!-- System message -->
          <div v-if="msg.role === 'system'" class="message-row system-message">
            <div class="system-bubble">
              <el-icon :size="12"><InfoFilled /></el-icon>
              <span>{{ msg.content }}</span>
            </div>
          </div>

          <!-- User message -->
          <div v-else-if="msg.role === 'user'" class="message-row user-message">
            <div class="user-bubble">
              <div class="message-content" v-html="simpleMarkdown(msg.content)" />
              <div class="message-time">{{ formatMessageTime(msg.createdAt) }}</div>
            </div>
            <div class="avatar user-avatar">
              <el-icon :size="18"><User /></el-icon>
            </div>
          </div>

          <!-- Assistant message -->
          <div v-else-if="msg.role === 'assistant'" class="message-row assistant-message">
            <div class="avatar assistant-avatar">
              <el-icon :size="18"><Monitor /></el-icon>
            </div>
            <div class="assistant-bubble-wrapper">
              <!-- Tool call cards -->
              <ToolCallCard
                v-for="tc in msg.toolCalls"
                :key="tc.id"
                :tool-call="tc"
              />
              <!-- Skill invoked badge -->
              <div v-if="msg.skillInvoked" class="skill-badge">
                <el-tag size="small" type="info" effect="plain">
                  <el-icon><MagicStick /></el-icon>
                  SKILL: {{ msg.skillInvoked }}
                </el-tag>
              </div>
              <!-- Content -->
              <div class="assistant-bubble">
                <div class="message-content" v-html="simpleMarkdown(msg.content)" />
                <div class="message-footer">
                  <span class="message-time">{{ formatMessageTime(msg.createdAt) }}</span>
                  <span v-if="msg.tokenUsage" class="token-info">
                    Tokens: {{ msg.tokenUsage.totalTokens }}
                    (提示 {{ msg.tokenUsage.promptTokens }} + 补全 {{ msg.tokenUsage.completionTokens }})
                  </span>
                  <span class="feedback-buttons">
                    <el-button
                      text
                      size="small"
                      :type="msgFeedback[msg.id || index] === 'up' ? 'success' : ''"
                      @click="submitFeedback(msg, 'up')"
                    >👍</el-button>
                    <el-button
                      text
                      size="small"
                      :type="msgFeedback[msg.id || index] === 'down' ? 'danger' : ''"
                      @click="submitFeedback(msg, 'down')"
                    >👎</el-button>
                    <el-button text size="small" @click="copyMessage(msg.content)">📋</el-button>
                  </span>
                </div>
              </div>
            </div>
          </div>
        </template>

        <!-- Typing indicator -->
        <div v-if="isLoading" class="message-row assistant-message">
          <div class="avatar assistant-avatar">
            <el-icon :size="18"><Monitor /></el-icon>
          </div>
          <div class="assistant-bubble-wrapper">
            <div class="assistant-bubble typing-indicator">
              <span class="typing-text">AI 正在思考</span>
              <span class="typing-dots">
                <span class="dot">.</span><span class="dot">.</span><span class="dot">.</span>
              </span>
            </div>
          </div>
        </div>

        <!-- Scroll anchor -->
        <div ref="scrollAnchorRef" class="scroll-anchor" />
      </div>

      <!-- Input Area -->
      <div class="chat-input-area">
        <!-- Quick action chips -->
        <div class="quick-actions">
          <el-tag
            v-for="action in quickActions"
            :key="action.label"
            size="small"
            effect="plain"
            class="quick-action-chip"
            @click="fillQuickAction(action.text)"
          >
            {{ action.label }}
          </el-tag>
        </div>

        <div class="input-row">
          <el-input
            v-model="inputText"
            type="textarea"
            placeholder="输入消息..."
            :autosize="{ minRows: 1, maxRows: 6 }"
            :disabled="isLoading"
            resize="none"
            @keydown="handleInputKeydown"
          />
          <el-button
            type="primary"
            class="send-btn"
            :disabled="!inputText.trim() || isLoading"
            @click="handleSend"
          >
            <el-icon><Promotion /></el-icon>
            发送
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, nextTick, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import {
  getConversations,
  createConversation,
  deleteConversation as deleteConversationApi,
  updateConversationTitle,
  sendMessage as sendMessageApi,
  type LlmConversation,
  type LlmMessage,
  type ToolCall,
  type ChatResponse,
} from '@/api/llm'

// ==================== Sub-component: ToolCallCard ====================

const ToolCallCard = {
  name: 'ToolCallCard',
  props: {
    toolCall: { type: Object as () => ToolCall, required: true },
  },
  setup(props: { toolCall: ToolCall }) {
    const isExpanded = ref(false)

    const statusType = computed(() => {
      switch (props.toolCall.status) {
        case 'success': return 'success' as const
        case 'failed': return 'danger' as const
        case 'running': return 'warning' as const
        default: return 'info' as const
      }
    })

    const statusLabel = computed(() => {
      switch (props.toolCall.status) {
        case 'success': return '✅ 成功'
        case 'failed': return '❌ 失败'
        case 'running': return '⏳ 运行中'
        default: return props.toolCall.status
      }
    })

    const typeIcon = computed(() => {
      switch (props.toolCall.type?.toUpperCase()) {
        case 'LLM': return '🧠'
        case 'CODE': return '💻'
        case 'WORKFLOW': return '🔄'
        default: return '🔧'
      }
    })

    const formattedArgs = computed(() => {
      try {
        return JSON.stringify(props.toolCall.function.arguments, null, 2)
      } catch {
        return '{}'
      }
    })

    const formattedResult = computed(() => {
      try {
        return JSON.stringify(props.toolCall.function.result, null, 2)
      } catch {
        return '{}'
      }
    })

    return { isExpanded, statusType, statusLabel, typeIcon, formattedArgs, formattedResult }
  },
  template: `
    <div class="tool-call-card" :class="{ expanded: isExpanded }">
      <div class="tool-call-header" @click="isExpanded = !isExpanded">
        <span class="tool-call-icon">{{ typeIcon }}</span>
        <span class="tool-call-name">工具调用: {{ toolCall.function.name }}</span>
        <el-tag size="small" :type="statusType" effect="light">{{ statusLabel }}</el-tag>
        <span v-if="toolCall.durationMs" class="tool-call-duration">
          ⏱ {{ (toolCall.durationMs / 1000).toFixed(1) }}s
        </span>
        <el-icon class="tool-call-expand-icon" :class="{ rotated: isExpanded }">
          <ArrowRight />
        </el-icon>
      </div>
      <div v-show="isExpanded" class="tool-call-body">
        <div class="tool-call-section">
          <div class="section-label">📥 输入参数</div>
          <pre class="code-block">{{ formattedArgs }}</pre>
        </div>
        <div class="tool-call-section">
          <div class="section-label">📤 输出结果</div>
          <pre class="code-block">{{ formattedResult }}</pre>
        </div>
      </div>
    </div>
  `,
}

// ==================== Types for internal use ====================

interface ConversationGroup {
  label: string
  items: LlmConversation[]
}

interface QuickAction {
  label: string
  text: string
}

// ==================== State ====================

const conversations = ref<LlmConversation[]>([])
const currentConversationId = ref<number | null>(null)
const messages = ref<LlmMessage[]>([])
const inputText = ref('')
const isLoading = ref(false)
const searchKeyword = ref('')
const msgFeedback = reactive<Record<number, 'up' | 'down'>>({})
const mockMessageCounter = ref(0)

// Title editing
const isEditingTitle = ref(false)
const editingTitle = ref('')
const titleInputRef = ref<any>(null)

// DOM refs
const messagesContainerRef = ref<HTMLElement | null>(null)
const scrollAnchorRef = ref<HTMLElement | null>(null)

// Quick actions
const quickActions: QuickAction[] = [
  { label: '/生成案例', text: '请为以下功能生成测试案例：' },
  { label: '/分析需求', text: '请分析以下需求文档：' },
  { label: '/执行测试', text: '请执行以下测试套件：' },
  { label: '/查询知识库', text: '在知识库中搜索：' },
]

// ==================== Computed ====================

const currentConversation = computed(() => {
  return conversations.value.find(c => c.id === currentConversationId.value) || null
})

const filteredConversations = computed(() => {
  if (!searchKeyword.value.trim()) return conversations.value
  const kw = searchKeyword.value.toLowerCase()
  return conversations.value.filter(
    c => c.title.toLowerCase().includes(kw) || (c.lastMessage || '').toLowerCase().includes(kw)
  )
})

const groupedConversations = computed<ConversationGroup[]>(() => {
  const today = dayjs().startOf('day')
  const yesterday = today.subtract(1, 'day')
  const groups: Record<string, LlmConversation[]> = {
    '今天': [],
    '昨天': [],
    '更早': [],
  }

  for (const conv of filteredConversations.value) {
    const date = conv.updatedAt ? dayjs(conv.updatedAt).startOf('day') : today
    if (date.isSame(today, 'day')) {
      groups['今天'].push(conv)
    } else if (date.isSame(yesterday, 'day')) {
      groups['昨天'].push(conv)
    } else {
      groups['更早'].push(conv)
    }
  }

  const result: ConversationGroup[] = []
  for (const [label, items] of Object.entries(groups)) {
    if (items.length > 0) {
      result.push({ label, items })
    }
  }
  return result
})

// ==================== Simple Markdown Parser ====================

function simpleMarkdown(text: string): string {
  if (!text) return ''

  let html = text
    // Escape HTML first
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')

  // Code blocks (```...```)
  html = html.replace(/```(\w*)\n([\s\S]*?)```/g, (_match, lang, code) => {
    return `<pre class="md-code-block"><code class="language-${lang}">${code.trim()}</code></pre>`
  })

  // Inline code
  html = html.replace(/`([^`]+)`/g, '<code class="md-inline-code">$1</code>')

  // Headers
  html = html.replace(/^### (.+)$/gm, '<h4 class="md-h4">$1</h4>')
  html = html.replace(/^## (.+)$/gm, '<h3 class="md-h3">$1</h3>')
  html = html.replace(/^# (.+)$/gm, '<h2 class="md-h2">$1</h2>')

  // Bold
  html = html.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')

  // Italic
  html = html.replace(/\*([^*]+)\*/g, '<em>$1</em>')

  // Tables
  html = html.replace(/(?:^|\n)((?:\|.*\|\n)+)/g, (_match, tableBlock: string) => {
    const rows = tableBlock.trim().split('\n').filter((r: string) => !r.match(/^\|[\s-:|]+\|$/))
    if (rows.length === 0) return tableBlock

    let table = '<table class="md-table"><thead><tr>'
    const headerCells = rows[0].split('|').filter((c: string) => c.trim())
    for (const cell of headerCells) {
      table += `<th>${cell.trim()}</th>`
    }
    table += '</tr></thead><tbody>'
    for (let i = 1; i < rows.length; i++) {
      table += '<tr>'
      const cells = rows[i].split('|').filter((c: string) => c.trim())
      for (const cell of cells) {
        table += `<td>${cell.trim()}</td>`
      }
      table += '</tr>'
    }
    table += '</tbody></table>'
    return table
  })

  // Unordered list items
  html = html.replace(/(?:^|\n)((?:- .+\n?)+)/g, (_match, listBlock: string) => {
    const items = listBlock.trim().split('\n')
    let list = '<ul class="md-list">'
    for (const item of items) {
      const content = item.replace(/^- /, '')
      list += `<li>${content}</li>`
    }
    list += '</ul>'
    return list
  })

  // Line breaks (but not inside block elements)
  html = html.replace(/\n/g, '<br>')

  return html
}

// ==================== Time Formatting ====================

function formatRelativeTime(dateStr?: string): string {
  if (!dateStr) return ''
  const date = dayjs(dateStr)
  const now = dayjs()
  const diffMin = now.diff(date, 'minute')

  if (diffMin < 1) return '刚刚'
  if (diffMin < 60) return `${diffMin}分钟前`
  const diffHour = now.diff(date, 'hour')
  if (diffHour < 24) return `${diffHour}小时前`
  const diffDay = now.diff(date, 'day')
  if (diffDay < 7) return `${diffDay}天前`
  return date.format('MM/DD')
}

function formatMessageTime(dateStr?: string): string {
  if (!dateStr) return ''
  const date = dayjs(dateStr)
  const now = dayjs()
  if (now.diff(date, 'day') === 0) {
    return date.format('HH:mm')
  }
  return date.format('MM/DD HH:mm')
}

function shouldShowDateSeparator(index: number): boolean {
  if (index === 0) return true
  const curr = messages.value[index]
  const prev = messages.value[index - 1]
  if (!curr.createdAt || !prev.createdAt) return false
  return !dayjs(curr.createdAt).isSame(dayjs(prev.createdAt), 'day')
}

function formatDateSeparator(dateStr?: string): string {
  if (!dateStr) return ''
  const date = dayjs(dateStr)
  const now = dayjs()
  if (now.diff(date, 'day') === 0) return '今天'
  if (now.diff(date, 'day') === 1) return '昨天'
  return date.format('YYYY年MM月DD日')
}

// ==================== Mock Data ====================

function generateMockData(): void {
  const now = dayjs()

  conversations.value = [
    {
      id: 1,
      title: '测试案例设计 - 登录模块',
      lastMessage: '已为用户登录功能生成8条测试案例',
      messageCount: 6,
      createdAt: now.subtract(2, 'hour').toISOString(),
      updatedAt: now.subtract(10, 'minute').toISOString(),
    },
    {
      id: 2,
      title: '回归风险分析',
      lastMessage: '根据代码变更分析，建议重点关注支付模块',
      messageCount: 4,
      createdAt: now.subtract(5, 'hour').toISOString(),
      updatedAt: now.subtract(1, 'hour').toISOString(),
    },
    {
      id: 3,
      title: 'API 测试辅助',
      lastMessage: '已生成用户管理的 REST API 测试脚本',
      messageCount: 8,
      createdAt: now.subtract(1, 'day').subtract(3, 'hour').toISOString(),
      updatedAt: now.subtract(1, 'day').subtract(2, 'hour').toISOString(),
    },
    {
      id: 4,
      title: '需求文档解析',
      lastMessage: '从需求文档中提取了15个测试点',
      messageCount: 3,
      createdAt: now.subtract(3, 'day').toISOString(),
      updatedAt: now.subtract(3, 'day').toISOString(),
    },
    {
      id: 5,
      title: '性能测试建议',
      lastMessage: '建议在高峰期对接口进行压力测试',
      messageCount: 5,
      createdAt: now.subtract(7, 'day').toISOString(),
      updatedAt: now.subtract(6, 'day').toISOString(),
    },
  ]

  // Pre-load messages for conversation 1
  if (currentConversationId.value === 1) {
    messages.value = [
      {
        id: 101,
        conversationId: 1,
        role: 'system',
        content: '会话开始',
        createdAt: now.subtract(2, 'hour').toISOString(),
      },
      {
        id: 102,
        conversationId: 1,
        role: 'user',
        content: '你好，请帮我分析一下用户登录功能需要哪些测试案例',
        createdAt: now.subtract(110, 'minute').toISOString(),
      },
      {
        id: 103,
        conversationId: 1,
        role: 'assistant',
        skillInvoked: 'case-generator',
        toolCalls: [
          {
            id: 'tc-001',
            type: 'LLM',
            function: {
              name: 'case-generator',
              arguments: { userInput: '用户登录功能测试案例', module: 'auth' },
              result: { casesGenerated: 8, coverage: '95%' },
            },
            status: 'success',
            durationMs: 2340,
          },
        ],
        tokenUsage: { promptTokens: 520, completionTokens: 1840, totalTokens: 2360 },
        content: `已为「用户登录」生成 **8 条测试案例**：

### 正向用例
- 正确的用户名和密码登录成功
- 记住密码功能验证
- 自动登录 token 过期后重新登录

### 异常用例
- 用户名不存在
- 密码错误（含连续错误锁定）
- 用户名或密码为空
- SQL 注入测试
- XSS 注入测试

所有案例覆盖了主要测试场景，覆盖率约 **95%**。`,
        createdAt: now.subtract(108, 'minute').toISOString(),
      },
      {
        id: 104,
        conversationId: 1,
        role: 'user',
        content: '请为密码错误的场景增加更多边界测试',
        createdAt: now.subtract(15, 'minute').toISOString(),
      },
      {
        id: 105,
        conversationId: 1,
        role: 'assistant',
        content: `好的，以下是密码错误场景的补充边界测试用例：

### 密码边界测试
- 密码长度为1位（最小长度）
- 密码长度为最大限制（如128位）
- 密码包含特殊字符 \`!@#$%^&*()\`
- 密码包含空格
- 密码包含中文字符
- 密码大小写敏感性验证
- 密码前后空格处理

| 用例编号 | 测试场景 | 预期结果 |
| --- | --- | --- |
| TC-AUTH-09 | 1位密码 | 提示密码长度不足 |
| TC-AUTH-10 | 超长密码 | 截断或提示超限 |
| TC-AUTH-11 | 特殊字符密码 | 正常登录 |
| TC-AUTH-12 | 密码含空格 | 保留空格验证 |

建议同时验证前端的输入长度限制与后端的一致性。`,
        createdAt: now.subtract(10, 'minute').toISOString(),
      },
    ]
  }
}

// ==================== Mock Response Generator ====================

function generateMockResponse(userContent: string): ChatResponse {
  mockMessageCounter.value++
  const convId = currentConversationId.value || 1
  const now = dayjs().toISOString()
  const isSkillCall = mockMessageCounter.value % 2 === 1

  if (isSkillCall) {
    return {
      conversationId: convId,
      userMessage: {
        id: Date.now(),
        conversationId: convId,
        role: 'user',
        content: userContent,
        createdAt: now,
      },
      assistantMessage: {
        id: Date.now() + 1,
        conversationId: convId,
        role: 'assistant',
        content: `已匹配 SKILL: [意图分析]\n\n根据你输入的内容，我分析了以下关键信息：\n\n**输入摘要：** ${userContent.slice(0, 50)}${userContent.length > 50 ? '...' : ''}\n\n### 分析结果\n- 识别到测试相关意图\n- 建议关联模块：用户管理\n- 预估影响范围：3 个接口\n\n如需进一步操作，请使用对应的快捷指令。`,
        skillInvoked: 'intent-analyzer',
        toolCalls: [
          {
            id: `tc-mock-${Date.now()}`,
            type: 'LLM',
            function: {
              name: 'intent-analyzer',
              arguments: { query: userContent },
              result: { intent: 'test_generation', confidence: 0.92, modules: ['user-management'] },
            },
            status: 'success',
            durationMs: Math.floor(Math.random() * 2000) + 500,
          },
        ],
        tokenUsage: {
          promptTokens: Math.floor(Math.random() * 500) + 200,
          completionTokens: Math.floor(Math.random() * 800) + 300,
          totalTokens: 0,
        },
        createdAt: now,
      },
    }
  } else {
    const totalTokens = Math.floor(Math.random() * 1000) + 500
    return {
      conversationId: convId,
      userMessage: {
        id: Date.now(),
        conversationId: convId,
        role: 'user',
        content: userContent,
        createdAt: now,
      },
      assistantMessage: {
        id: Date.now() + 1,
        conversationId: convId,
        role: 'assistant',
        content: `收到你的问题。关于「${userContent.slice(0, 30)}${userContent.length > 30 ? '...' : ''}」，我的建议如下：

### 分析要点
- 首先需要明确测试范围和目标
- 建议采用分层测试策略
- 重点关注边界条件和异常场景

### 推荐做法
1. 编写单元测试覆盖核心逻辑
2. 集成测试验证模块间交互
3. 端到端测试覆盖关键流程

如果需要更详细的方案，可以告诉我具体的模块名称。`,
        tokenUsage: {
          promptTokens: Math.floor(totalTokens * 0.4),
          completionTokens: Math.floor(totalTokens * 0.6),
          totalTokens,
        },
        createdAt: now,
      },
    }
  }
}

// ==================== API Operations ====================

async function loadConversations(): Promise<void> {
  try {
    const res = await getConversations()
    const data = res?.data || res
    if (Array.isArray(data) && data.length > 0) {
      conversations.value = data
    } else {
      generateMockData()
    }
  } catch {
    // API unavailable, use mock data
    generateMockData()
  }
}

async function loadMessages(conversationId: number): Promise<void> {
  // In a real implementation, this would call getConversationMessages API
  // For now, mock data is pre-loaded in generateMockData
  if (conversationId === 1 && messages.value.length > 0) {
    return // already loaded from mock
  }

  // Default messages for other conversations
  const now = dayjs()
  messages.value = [
    {
      id: 200 + conversationId,
      conversationId,
      role: 'system',
      content: '会话开始',
      createdAt: now.toISOString(),
    },
  ]
}

async function handleSend(): Promise<void> {
  const content = inputText.value.trim()
  if (!content || isLoading.value) return

  // If no conversation selected, create one
  if (!currentConversationId.value) {
    await handleNewConversation(content)
    return
  }

  const convId = currentConversationId.value

  // Add user message immediately
  const userMsg: LlmMessage = {
    id: Date.now(),
    conversationId: convId,
    role: 'user',
    content,
    createdAt: dayjs().toISOString(),
  }
  messages.value.push(userMsg)
  inputText.value = ''
  isLoading.value = true

  scrollToBottom()

  try {
    const res = await sendMessageApi({ conversationId: convId, content })
    const data = res?.data || res
    if (data) {
      const chatRes = data as ChatResponse
      messages.value.push(chatRes.assistantMessage)
      // Update conversation
      const conv = conversations.value.find(c => c.id === convId)
      if (conv) {
        conv.lastMessage = chatRes.assistantMessage.content.slice(0, 50)
        conv.updatedAt = dayjs().toISOString()
        conv.messageCount = (conv.messageCount || 0) + 2
      }
    }
  } catch {
    // Use mock response
    const mockRes = generateMockResponse(content)
    // Simulate network delay
    await new Promise(resolve => setTimeout(resolve, 1000 + Math.random() * 1500))

    mockRes.assistantMessage.createdAt = dayjs().toISOString()
    if (mockRes.assistantMessage.tokenUsage) {
      mockRes.assistantMessage.tokenUsage.totalTokens =
        mockRes.assistantMessage.tokenUsage.promptTokens +
        mockRes.assistantMessage.tokenUsage.completionTokens
    }
    messages.value.push(mockRes.assistantMessage)

    // Update conversation
    const conv = conversations.value.find(c => c.id === convId)
    if (conv) {
      conv.lastMessage = mockRes.assistantMessage.content.slice(0, 50)
      conv.updatedAt = dayjs().toISOString()
      conv.messageCount = (conv.messageCount || 0) + 2
    }
  } finally {
    isLoading.value = false
    scrollToBottom()
  }
}

async function handleNewConversation(firstMessage?: string): Promise<void> {
  const title = firstMessage
    ? firstMessage.slice(0, 20) + (firstMessage.length > 20 ? '...' : '')
    : '新会话'

  try {
    const res = await createConversation({ title })
    const data = res?.data || res
    const newConv: LlmConversation = {
      id: data?.id || Date.now(),
      title,
      lastMessage: '',
      messageCount: 0,
      createdAt: dayjs().toISOString(),
      updatedAt: dayjs().toISOString(),
    }
    conversations.value.unshift(newConv)
    currentConversationId.value = newConv.id!
    messages.value = [
      {
        id: Date.now(),
        conversationId: newConv.id!,
        role: 'system',
        content: '会话开始',
        createdAt: dayjs().toISOString(),
      },
    ]

    if (firstMessage) {
      await nextTick()
      await handleSend()
    }
  } catch {
    // Mock new conversation
    const newConv: LlmConversation = {
      id: Date.now(),
      title,
      lastMessage: '',
      messageCount: 0,
      createdAt: dayjs().toISOString(),
      updatedAt: dayjs().toISOString(),
    }
    conversations.value.unshift(newConv)
    currentConversationId.value = newConv.id!
    messages.value = [
      {
        id: Date.now(),
        conversationId: newConv.id!,
        role: 'system',
        content: '会话开始',
        createdAt: dayjs().toISOString(),
      },
    ]

    if (firstMessage) {
      await nextTick()
      await handleSend()
    }
  }
}

async function handleDeleteConversation(conv: LlmConversation): Promise<void> {
  if (!conv.id) return

  try {
    await ElMessageBox.confirm(
      `确定删除会话「${conv.title}」吗？删除后不可恢复。`,
      '删除确认',
      { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return // cancelled
  }

  try {
    await deleteConversationApi(conv.id)
  } catch {
    // mock: just remove locally
  }

  conversations.value = conversations.value.filter(c => c.id !== conv.id)

  if (currentConversationId.value === conv.id) {
    currentConversationId.value = null
    messages.value = []
  }

  ElMessage.success('会话已删除')
}

async function switchConversation(conv: LlmConversation): Promise<void> {
  if (!conv.id || currentConversationId.value === conv.id) return

  // Finish any title editing
  if (isEditingTitle.value) {
    await finishEditTitle()
  }

  currentConversationId.value = conv.id
  messages.value = []
  await loadMessages(conv.id)
  await nextTick()
  scrollToBottom()
}

// ==================== Title Editing ====================

function startEditTitle(): void {
  if (!currentConversation.value) return
  editingTitle.value = currentConversation.value.title
  isEditingTitle.value = true
  nextTick(() => {
    titleInputRef.value?.focus()
  })
}

async function finishEditTitle(): Promise<void> {
  if (!isEditingTitle.value || !currentConversation.value) return

  const newTitle = editingTitle.value.trim()
  if (!newTitle || newTitle === currentConversation.value.title) {
    isEditingTitle.value = false
    return
  }

  try {
    await updateConversationTitle(currentConversation.value.id!, newTitle)
  } catch {
    // mock: just update locally
  }

  currentConversation.value.title = newTitle
  isEditingTitle.value = false
  ElMessage.success('标题已更新')
}

// ==================== Feedback & Copy ====================

function submitFeedback(msg: LlmMessage, type: 'up' | 'down'): void {
  const msgId = msg.id || 0
  if (msgFeedback[msgId] === type) {
    delete msgFeedback[msgId]
    return
  }
  msgFeedback[msgId] = type
  ElMessage.success(type === 'up' ? '感谢反馈 👍' : '感谢反馈，我们会改进 👎')
}

function copyMessage(content: string): void {
  navigator.clipboard.writeText(content).then(() => {
    ElMessage.success('已复制到剪贴板')
  }).catch(() => {
    // Fallback
    const textarea = document.createElement('textarea')
    textarea.value = content
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
    ElMessage.success('已复制到剪贴板')
  })
}

// ==================== Input Handling ====================

function handleInputKeydown(e: KeyboardEvent): void {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

function fillQuickAction(text: string): void {
  inputText.value = text
}

// ==================== Scroll ====================

function scrollToBottom(): void {
  nextTick(() => {
    if (messagesContainerRef.value) {
      messagesContainerRef.value.scrollTop = messagesContainerRef.value.scrollHeight
    }
  })
}

// Watch messages for auto-scroll
watch(
  () => messages.value.length,
  () => scrollToBottom()
)

// ==================== Lifecycle ====================

onMounted(() => {
  loadConversations()
})
</script>

<style scoped lang="scss">
/* ==================== Layout ==================== */
.chat-page {
  display: flex;
  height: calc(100vh - 60px);
  background: var(--el-bg-color);
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid var(--el-border-color-lighter);
}

/* ==================== Sidebar ==================== */
.chat-sidebar {
  width: 260px;
  min-width: 260px;
  border-right: 1px solid var(--el-border-color-lighter);
  display: flex;
  flex-direction: column;
  background: var(--el-bg-color-page);
}

.sidebar-header {
  padding: 12px;
  border-bottom: 1px solid var(--el-border-color-lighter);

  .new-chat-btn {
    width: 100%;
  }
}

.sidebar-search {
  padding: 8px 12px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.conversation-list {
  flex: 1;
  overflow-y: auto;
  padding: 4px 0;
}

.date-group-label {
  padding: 8px 16px 4px;
  font-size: 11px;
  font-weight: 600;
  color: var(--el-text-color-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.conversation-item {
  display: flex;
  align-items: flex-start;
  padding: 10px 12px;
  margin: 2px 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s;

  &:hover {
    background: var(--el-fill-color-light);

    .conv-delete {
      opacity: 1;
    }
  }

  &.active {
    background: var(--el-color-primary-light-9);
    border-left: 3px solid var(--el-color-primary);
  }
}

.conv-main {
  flex: 1;
  min-width: 0;
}

.conv-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  margin-bottom: 2px;
}

.conv-preview {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.4;
}

.conv-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  margin-left: 8px;
  flex-shrink: 0;
}

.conv-time {
  font-size: 11px;
  color: var(--el-text-color-placeholder);
  white-space: nowrap;
}

.conv-delete {
  opacity: 0;
  font-size: 14px;
  color: var(--el-text-color-placeholder);
  cursor: pointer;
  transition: opacity 0.2s, color 0.2s;

  &:hover {
    color: var(--el-color-danger);
  }
}

.empty-conversations {
  padding: 24px 0;
}

/* ==================== Main Chat Area ==================== */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  background: var(--el-bg-color);
  min-height: 52px;
}

.chat-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.chat-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  cursor: text;
  padding: 2px 6px;
  border-radius: 4px;
  transition: background-color 0.2s;

  &:hover {
    background: var(--el-fill-color-light);
  }
}

.title-edit-input {
  width: 260px;
}

.chat-title-placeholder {
  font-size: 14px;
  color: var(--el-text-color-placeholder);
}

/* ==================== Messages ==================== */
.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  scroll-behavior: smooth;
}

.scroll-anchor {
  height: 1px;
}

/* Welcome Screen */
.welcome-screen {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
  color: var(--el-text-color-secondary);

  h2 {
    margin: 16px 0 8px;
    font-size: 20px;
    color: var(--el-text-color-primary);
  }

  p {
    margin-bottom: 8px;
    font-size: 14px;
  }

  ul {
    text-align: left;
    margin: 8px 0 16px;

    li {
      padding: 4px 0;
      font-size: 14px;
    }
  }

  .welcome-hint {
    color: var(--el-text-color-placeholder);
    font-size: 13px;
    margin-top: 16px;
  }
}

/* Date Separator */
.date-separator {
  text-align: center;
  margin: 16px 0;

  span {
    display: inline-block;
    padding: 2px 12px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
    background: var(--el-fill-color-light);
    border-radius: 10px;
  }
}

/* Message Rows */
.message-row {
  display: flex;
  margin-bottom: 16px;
  align-items: flex-start;
}

.system-message {
  justify-content: center;

  .system-bubble {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 4px 12px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
    background: var(--el-fill-color-lighter);
    border-radius: 12px;
  }
}

.user-message {
  justify-content: flex-end;

  .user-bubble {
    max-width: 70%;
    padding: 12px 16px;
    background: var(--el-color-primary);
    color: #fff;
    border-radius: 16px 16px 4px 16px;
    word-break: break-word;

    .message-time {
      text-align: right;
      font-size: 11px;
      opacity: 0.7;
      margin-top: 4px;
    }
  }
}

.user-avatar {
  margin-left: 8px;
  background: var(--el-color-primary-light-5);
  color: #fff;
}

.assistant-message {
  .assistant-bubble-wrapper {
    max-width: 75%;
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .assistant-bubble {
    padding: 12px 16px;
    background: var(--el-fill-color-light);
    border-radius: 16px 16px 16px 4px;
    word-break: break-word;
  }
}

.assistant-avatar {
  margin-right: 8px;
  background: var(--el-color-success-light-5);
  color: var(--el-color-success);
}

.avatar {
  width: 32px;
  height: 32px;
  min-width: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

/* Message Content (markdown rendered) */
.message-content {
  font-size: 14px;
  line-height: 1.6;

  :deep(.md-h2) {
    font-size: 18px;
    margin: 12px 0 8px;
    font-weight: 600;
  }

  :deep(.md-h3) {
    font-size: 16px;
    margin: 10px 0 6px;
    font-weight: 600;
  }

  :deep(.md-h4) {
    font-size: 14px;
    margin: 8px 0 4px;
    font-weight: 600;
  }

  :deep(.md-code-block) {
    background: var(--el-fill-color-darker);
    border-radius: 6px;
    padding: 12px;
    margin: 8px 0;
    overflow-x: auto;
    font-family: 'Fira Code', 'Cascadia Code', Consolas, monospace;
    font-size: 13px;
    line-height: 1.5;

    code {
      font-family: inherit;
    }
  }

  :deep(.md-inline-code) {
    background: var(--el-fill-color-darker);
    padding: 2px 6px;
    border-radius: 4px;
    font-family: 'Fira Code', Consolas, monospace;
    font-size: 13px;
  }

  :deep(.md-table) {
    width: 100%;
    border-collapse: collapse;
    margin: 8px 0;
    font-size: 13px;

    th, td {
      border: 1px solid var(--el-border-color-lighter);
      padding: 6px 10px;
      text-align: left;
    }

    th {
      background: var(--el-fill-color);
      font-weight: 600;
    }

    tr:nth-child(even) td {
      background: var(--el-fill-color-lighter);
    }
  }

  :deep(.md-list) {
    padding-left: 20px;
    margin: 6px 0;

    li {
      list-style: disc;
      padding: 2px 0;
    }
  }

  :deep(strong) {
    font-weight: 600;
  }
}

/* Message Footer */
.message-footer {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 6px;
  padding-top: 6px;
  border-top: 1px solid var(--el-border-color-extra-light);
}

.message-time {
  font-size: 11px;
  color: var(--el-text-color-placeholder);
}

.token-info {
  font-size: 11px;
  color: var(--el-text-color-placeholder);
  margin-left: auto;
}

.feedback-buttons {
  display: flex;
  gap: 0;

  .el-button {
    padding: 2px 4px;
    font-size: 14px;
  }
}

/* Skill Badge */
.skill-badge {
  .el-tag {
    .el-icon {
      margin-right: 4px;
    }
  }
}

/* ==================== Tool Call Card ==================== */
:deep(.tool-call-card) {
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  overflow: hidden;
  background: var(--el-bg-color);
  font-size: 13px;

  .tool-call-header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 12px;
    cursor: pointer;
    background: var(--el-fill-color-lighter);
    transition: background-color 0.2s;

    &:hover {
      background: var(--el-fill-color);
    }
  }

  .tool-call-icon {
    font-size: 16px;
  }

  .tool-call-name {
    font-weight: 500;
    font-family: 'Fira Code', Consolas, monospace;
  }

  .tool-call-duration {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    margin-left: auto;
  }

  .tool-call-expand-icon {
    transition: transform 0.2s;

    &.rotated {
      transform: rotate(90deg);
    }
  }

  .tool-call-body {
    padding: 12px;
    border-top: 1px solid var(--el-border-color-lighter);
  }

  .tool-call-section {
    margin-bottom: 10px;

    &:last-child {
      margin-bottom: 0;
    }

    .section-label {
      font-size: 12px;
      font-weight: 600;
      color: var(--el-text-color-secondary);
      margin-bottom: 4px;
    }
  }

  .code-block {
    background: var(--el-fill-color-darker);
    border-radius: 6px;
    padding: 8px 12px;
    font-family: 'Fira Code', Consolas, monospace;
    font-size: 12px;
    line-height: 1.5;
    overflow-x: auto;
    white-space: pre-wrap;
    word-break: break-all;
    margin: 0;
  }
}

/* ==================== Typing Indicator ==================== */
.typing-indicator {
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 12px 16px !important;

  .typing-text {
    font-size: 14px;
    color: var(--el-text-color-secondary);
  }

  .typing-dots {
    display: inline-flex;
    gap: 2px;

    .dot {
      font-size: 20px;
      color: var(--el-text-color-secondary);
      animation: typingBounce 1.4s infinite ease-in-out;
      line-height: 1;

      &:nth-child(1) { animation-delay: 0s; }
      &:nth-child(2) { animation-delay: 0.2s; }
      &:nth-child(3) { animation-delay: 0.4s; }
    }
  }
}

@keyframes typingBounce {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-4px);
    opacity: 1;
  }
}

/* ==================== Input Area ==================== */
.chat-input-area {
  padding: 12px 20px 16px;
  border-top: 1px solid var(--el-border-color-lighter);
  background: var(--el-bg-color);
}

.quick-actions {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
  flex-wrap: wrap;

  .quick-action-chip {
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      color: var(--el-color-primary);
      border-color: var(--el-color-primary);
      background: var(--el-color-primary-light-9);
    }
  }
}

.input-row {
  display: flex;
  gap: 8px;
  align-items: flex-end;

  .el-textarea {
    flex: 1;

    :deep(.el-textarea__inner) {
      border-radius: 12px;
      padding: 10px 16px;
      font-size: 14px;
      resize: none;
      box-shadow: none;
    }
  }

  .send-btn {
    height: 40px;
    border-radius: 10px;
    padding: 0 16px;
    flex-shrink: 0;

    .el-icon {
      margin-right: 4px;
    }
  }
}

/* ==================== Dark Theme Support ==================== */
html.dark {
  .chat-page {
    border-color: var(--el-border-color);
  }

  .user-message .user-bubble {
    color: #fff;
  }

  .assistant-message .assistant-bubble {
    background: var(--el-fill-color);
  }
}
</style>
