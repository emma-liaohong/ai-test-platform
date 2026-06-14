import { test, expect } from '@playwright/test';
import { ChatPage } from '../src/pages/ChatPage';
import { login } from '../src/utils/auth';

test.describe('AI 对话', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('对话页面加载', async ({ page }) => {
    const chatPage = new ChatPage(page);
    await chatPage.goto();
    await chatPage.expectLoaded();
  });

  test('新建会话按钮可见', async ({ page }) => {
    const chatPage = new ChatPage(page);
    await chatPage.goto();
    await expect(chatPage.newConversationButton).toBeVisible();
  });

  test('新建会话', async ({ page }) => {
    const chatPage = new ChatPage(page);
    await chatPage.goto();
    await chatPage.createNewConversation();
    await page.waitForTimeout(1000);
  });

  test('发送消息', async ({ page }) => {
    const chatPage = new ChatPage(page);
    await chatPage.goto();
    // Create a new conversation first
    await chatPage.createNewConversation();
    await page.waitForTimeout(1000);
    // Send a message
    await chatPage.sendMessage('你好，请介绍一下你自己');
    // Wait for response - the chat uses .message-row class for messages
    await expect(page.locator('.message-row').last()).toBeVisible({ timeout: 15000 });
  });

  test('快捷操作可见', async ({ page }) => {
    const chatPage = new ChatPage(page);
    await chatPage.goto();
    // Quick action chips
    await expect(page.getByText(/生成案例|分析需求/).first()).toBeVisible();
  });
});
