import { type Page, type Locator, expect } from '@playwright/test';

export class SuiteListPage {
  readonly page: Page;
  readonly createButton: Locator;
  readonly table: Locator;
  readonly dialog: Locator;
  readonly nameInput: Locator;
  readonly submitButton: Locator;

  constructor(page: Page) {
    this.page = page;
    this.createButton = page.getByRole('button', { name: /新建套件/ });
    this.table = page.locator('.el-table');
    this.dialog = page.locator('.el-dialog');
    this.nameInput = page.locator('.el-dialog .el-form-item').filter({ hasText: /套件名称/ }).locator('input');
    this.submitButton = page.locator('.el-dialog').getByRole('button', { name: /确定/ });
  }

  async goto() {
    await this.page.goto('/suites');
    await expect(this.table).toBeVisible({ timeout: 10000 });
  }

  async expectLoaded() {
    await expect(this.table).toBeVisible();
  }
}
