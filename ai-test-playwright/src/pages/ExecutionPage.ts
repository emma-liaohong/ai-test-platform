import { type Page, type Locator, expect } from '@playwright/test';

export class ExecutionPage {
  readonly page: Page;
  readonly table: Locator;
  readonly tabs: Locator;

  constructor(page: Page) {
    this.page = page;
    this.table = page.locator('.el-table');
    this.tabs = page.locator('.el-tabs__nav');
  }

  async goto() {
    await this.page.goto('/executions');
    await expect(this.table).toBeVisible({ timeout: 10000 });
  }

  async expectLoaded() {
    await expect(this.table).toBeVisible();
  }

  async switchTab(tabName: string) {
    await this.page.locator('.el-tabs__item').filter({ hasText: tabName }).click();
  }
}
