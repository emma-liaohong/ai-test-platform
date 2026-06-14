import { type Page, type Locator, expect } from '@playwright/test';

export class SettingsPage {
  readonly page: Page;
  readonly tabs: Locator;
  readonly llmTab: Locator;
  readonly playwrightTab: Locator;
  readonly storageTab: Locator;

  constructor(page: Page) {
    this.page = page;
    this.tabs = page.locator('.el-tabs__nav');
    this.llmTab = page.locator('.el-tabs__item').filter({ hasText: /LLM/ });
    this.playwrightTab = page.locator('.el-tabs__item').filter({ hasText: /Playwright/ });
    this.storageTab = page.locator('.el-tabs__item').filter({ hasText: /存储/ });
  }

  async goto() {
    await this.page.goto('/settings');
    await expect(this.tabs).toBeVisible({ timeout: 10000 });
  }

  async expectLoaded() {
    await expect(this.tabs).toBeVisible();
  }

  async switchToTab(tabLocator: Locator) {
    await tabLocator.click();
  }
}
