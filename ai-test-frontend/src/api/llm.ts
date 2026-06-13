import request from '@/utils/request'

// ==================== Types ====================

export interface LlmConversation {
  id?: number
  sessionId?: string
  userId?: number
  title: string
  context?: string
  lastMessage?: string
  messageCount?: number
  createdAt?: string
  updatedAt?: string
}

export interface LlmMessage {
  id?: number
  conversationId: number
  role: 'user' | 'assistant' | 'system' | 'tool'
  content: string
  toolCalls?: ToolCall[]
  skillInvoked?: string
  tokenUsage?: TokenUsage
  createdAt?: string
}

export interface ToolCall {
  id: string
  type: string
  function: {
    name: string
    arguments: Record<string, any>
    result: Record<string, any>
  }
  status: string
  durationMs?: number
}

export interface TokenUsage {
  promptTokens: number
  completionTokens: number
  totalTokens: number
}

export interface ChatResponse {
  conversationId: number
  userMessage: LlmMessage
  assistantMessage: LlmMessage
  toolCalls?: ToolCall[]
  skillInvoked?: string
  tokenUsage?: TokenUsage
}

// ==================== Conversation APIs ====================

export function getConversations(params?: { userId?: number }) {
  return request.get('/llm/conversations', { params })
}

export function createConversation(data: { title?: string }) {
  return request.post('/llm/conversations', data)
}

export function getConversation(id: number) {
  return request.get(`/llm/conversations/${id}`)
}

export function deleteConversation(id: number) {
  return request.delete(`/llm/conversations/${id}`)
}

export function updateConversationTitle(id: number, title: string) {
  return request.put(`/llm/conversations/${id}/title`, { title })
}

// ==================== Chat APIs ====================

export function sendMessage(data: { conversationId?: number; content: string }) {
  return request.post('/llm/chat', data)
}

export function processIntent(data: { query: string; conversationId?: number }) {
  return request.post('/llm/intent', data)
}
