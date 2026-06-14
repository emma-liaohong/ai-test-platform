import { type Page, type Locator, expect } from '@playwright/test';

export class RecordPage {
  readonly page: Page;
  readonly startButton: Locator;
  readonly table: Locator;
  readonly sessionNameInput: Locator;
  readonly targetUrlInput: Locator;

  constructor(page: Page) {
    this.page = page;
    this.startButton = page.getByRole('button', { name: /开始录制/ }).first();
    this.table = page.locator('.el-table');
    this.sessionNameInput = page.locator('input[placeholder*="会话"]');
    this.targetUrlInput = page.locator('input[placeholder*="URL"], input[placeholder*="url"]');
  }

  async goto() {
    await this.page.goto('/record');
    await expect(this.table).toBeVisible({ timeout: 10000 });
  }

  async expectLoaded() {
    await expect(this.table).toBeVisible();
  }
}
