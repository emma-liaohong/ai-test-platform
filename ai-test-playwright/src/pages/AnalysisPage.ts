import { type Page, type Locator, expect } from '@playwright/test';

export class AnalysisPage {
  readonly page: Page;
  readonly analysisTypeRadios: Locator;
  readonly startButton: Locator;

  constructor(page: Page) {
    this.page = page;
    this.analysisTypeRadios = page.locator('.el-radio-group');
    this.startButton = page.getByRole('button', { name: /开始分析/ });
  }

  async goto() {
    await this.page.goto('/analysis');
    await this.page.waitForLoadState('networkidle');
  }

  async expectLoaded() {
    await expect(this.startButton).toBeVisible({ timeout: 10000 });
  }
}
