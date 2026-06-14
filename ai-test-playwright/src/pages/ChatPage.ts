import { type Page, type Locator, expect } from '@playwright/test';

export class ChatPage {
  readonly page: Page;
  readonly newConversationButton: Locator;
  readonly conversationList: Locator;
  readonly messageInput: Locator;
  readonly sendButton: Locator;
  readonly messageList: Locator;

  constructor(page: Page) {
    this.page = page;
    this.newConversationButton = page.getByRole('button', { name: /新建会话/ });
    this.conversationList = page.locator('.conversation-list, .conversation-item').first();
    this.messageInput = page.locator('textarea').last();
    this.sendButton = page.getByRole('button', { name: /发送/ });
    this.messageList = page.locator('.message-row');
  }

  async goto() {
    await this.page.goto('/chat');
    await this.page.waitForLoadState('networkidle');
  }

  async expectLoaded() {
    await expect(this.newConversationButton).toBeVisible({ timeout: 10000 });
  }

  async createNewConversation() {
    await this.newConversationButton.click();
  }

  async sendMessage(text: string) {
    await this.messageInput.fill(text);
    await this.sendButton.click();
  }
}
