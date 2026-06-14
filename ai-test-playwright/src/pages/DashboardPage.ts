import { type Page, type Locator, expect } from '@playwright/test';

export class DashboardPage {
  readonly page: Page;
  readonly statCards: Locator;
  readonly sidebar: Locator;
  readonly username: Locator;
  readonly welcomeSection: Locator;
  readonly statValues: Locator;

  constructor(page: Page) {
    this.page = page;
    this.statCards = page.locator('.stat-card');
    this.sidebar = page.locator('.sidebar');
    this.username = page.locator('.username');
    this.welcomeSection = page.locator('.welcome-section');
    this.statValues = page.locator('.stat-value');
  }

  async goto() {
    await this.page.goto('/dashboard');
  }

  async expectLoaded() {
    await expect(this.page.locator('.main-layout')).toBeVisible();
  }

  async expectStatCardsVisible() {
    await expect(this.statCards.first()).toBeVisible();
    // Should have 5 stat cards
    const count = await this.statCards.count();
    expect(count).toBe(5);
  }

  /**
   * Verify stat cards show real numbers (not hardcoded placeholders).
   * Returns the parsed values for further assertions.
   */
  async getStatValues(): Promise<Record<string, number>> {
    // Wait for API data to load
    await this.page.waitForTimeout(2000);
    const values: Record<string, number> = {};
    const titles = ['测试用例', '用例套件', '缺陷总数', 'AI 技能', 'AI 对话'];
    for (let i = 0; i < titles.length; i++) {
      const card = this.statCards.nth(i);
      const title = await card.locator('.stat-title').textContent();
      const valueText = await card.locator('.stat-value').textContent();
      const value = parseInt(valueText?.replace(/,/g, '') || '0', 10);
      values[title || titles[i]] = value;
    }
    return values;
  }

  async expectSidebarVisible() {
    await expect(this.sidebar).toBeVisible();
  }
}
