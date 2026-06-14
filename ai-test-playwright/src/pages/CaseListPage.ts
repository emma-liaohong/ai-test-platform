import { type Page, type Locator, expect } from '@playwright/test';

export class CaseListPage {
  readonly page: Page;
  readonly table: Locator;
  readonly createButton: Locator;
  readonly searchInput: Locator;
  readonly searchButton: Locator;
  readonly resetButton: Locator;

  constructor(page: Page) {
    this.page = page;
    this.table = page.locator('.el-table');
    this.createButton = page.getByRole('button', { name: /新建案例/ });
    this.searchInput = page.locator('.el-input__inner').first();
    this.searchButton = page.getByRole('button', { name: /搜索/ }).first();
    this.resetButton = page.getByRole('button', { name: /重置/ }).first();
  }

  async goto() {
    await this.page.goto('/cases');
    await expect(this.table).toBeVisible({ timeout: 10000 });
  }

  async expectLoaded() {
    await expect(this.table).toBeVisible();
  }

  async getRowCount(): Promise<number> {
    return this.page.locator('.el-table__body-wrapper .el-table__row').count();
  }
}
