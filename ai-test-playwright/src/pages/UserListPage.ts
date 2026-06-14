import { type Page, type Locator, expect } from '@playwright/test';

export class UserListPage {
  readonly page: Page;
  readonly table: Locator;
  readonly addButton: Locator;

  constructor(page: Page) {
    this.page = page;
    this.table = page.locator('.el-table');
    this.addButton = page.getByRole('button', { name: /新增用户/ });
  }

  async goto() {
    await this.page.goto('/users');
    await expect(this.table).toBeVisible({ timeout: 10000 });
  }

  async expectLoaded() {
    await expect(this.table).toBeVisible();
  }

  async getRowCount(): Promise<number> {
    return this.page.locator('.el-table__body-wrapper .el-table__row').count();
  }
}
