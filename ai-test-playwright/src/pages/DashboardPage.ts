import { type Page, type Locator, expect } from '@playwright/test';

export class DashboardPage {
  readonly page: Page;
  readonly statCards: Locator;
  readonly sidebar: Locator;
  readonly username: Locator;

  constructor(page: Page) {
    this.page = page;
    this.statCards = page.locator('.el-card');
    this.sidebar = page.locator('.sidebar');
    this.username = page.locator('.username');
  }

  async goto() {
    await this.page.goto('/dashboard');
  }

  async expectLoaded() {
    await expect(this.page.locator('.main-layout')).toBeVisible();
  }

  async expectStatCardsVisible() {
    await expect(this.statCards.first()).toBeVisible();
  }

  async expectSidebarVisible() {
    await expect(this.sidebar).toBeVisible();
  }
}
