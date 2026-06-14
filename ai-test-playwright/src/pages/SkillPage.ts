import { type Page, type Locator, expect } from '@playwright/test';

export class SkillPage {
  readonly page: Page;
  readonly skillCards: Locator;
  readonly searchInput: Locator;
  readonly playgroundButton: Locator;

  constructor(page: Page) {
    this.page = page;
    this.skillCards = page.locator('.skill-card, .el-card');
    this.searchInput = page.locator('.el-input__inner').first();
    this.playgroundButton = page.getByRole('button', { name: /测试/ }).first();
  }

  async goto() {
    await this.page.goto('/skills');
    // Wait for the page to load
    await this.page.waitForLoadState('networkidle');
  }

  async expectLoaded() {
    // Skills page uses card layout, not table
    await expect(this.page.locator('.el-tabs')).toBeVisible({ timeout: 10000 });
  }

  async getSkillCardCount(): Promise<number> {
    return this.skillCards.count();
  }
}
