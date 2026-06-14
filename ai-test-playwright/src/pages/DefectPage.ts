import { type Page, type Locator, expect } from '@playwright/test';

export class DefectPage {
  readonly page: Page;
  readonly createButton: Locator;
  readonly table: Locator;
  readonly dialog: Locator;
  readonly titleInput: Locator;
  readonly submitButton: Locator;
  readonly statsCards: Locator;

  constructor(page: Page) {
    this.page = page;
    this.createButton = page.getByRole('button', { name: /新建缺陷/ });
    this.table = page.locator('.el-table');
    this.dialog = page.locator('.el-dialog');
    this.titleInput = page.locator('.el-dialog .el-form-item').filter({ hasText: /缺陷标题/ }).locator('input');
    this.submitButton = page.locator('.el-dialog').getByRole('button', { name: /确定/ });
    this.statsCards = page.locator('.stat-card, .el-card').first();
  }

  async goto() {
    await this.page.goto('/defects');
    await expect(this.table).toBeVisible({ timeout: 10000 });
  }

  async expectLoaded() {
    await expect(this.table).toBeVisible();
  }
}
