import { type Page, type Locator, expect } from '@playwright/test';

export class SystemListPage {
  readonly page: Page;
  readonly addButton: Locator;
  readonly searchInput: Locator;
  readonly table: Locator;
  readonly dialog: Locator;
  readonly nameInput: Locator;
  readonly codeInput: Locator;
  readonly baseUrlInput: Locator;
  readonly descriptionInput: Locator;
  readonly submitButton: Locator;

  constructor(page: Page) {
    this.page = page;
    this.addButton = page.getByRole('button', { name: /新增系统/ });
    this.searchInput = page.getByPlaceholder('搜索系统名称/编码');
    this.table = page.locator('.el-table');
    this.dialog = page.locator('.el-dialog');
    this.nameInput = page.locator('.el-dialog .el-form-item').filter({ hasText: /系统名称/ }).locator('input');
    this.codeInput = page.locator('.el-dialog .el-form-item').filter({ hasText: /系统编码/ }).locator('input');
    this.baseUrlInput = page.locator('.el-dialog .el-form-item').filter({ hasText: /基础URL/ }).locator('input');
    this.descriptionInput = page.locator('.el-dialog .el-form-item').filter({ hasText: /描述/ }).locator('textarea');
    this.submitButton = page.locator('.el-dialog').getByRole('button', { name: /确定/ });
  }

  async goto() {
    await this.page.goto('/systems');
    await expect(this.table).toBeVisible({ timeout: 10000 });
    // Wait for table data to load
    await this.page.waitForLoadState('networkidle');
  }

  async addSystem(name: string, code: string, baseUrl: string, description?: string) {
    await this.addButton.click();
    await expect(this.dialog).toBeVisible();
    await this.nameInput.fill(name);
    await this.codeInput.fill(code);
    await this.baseUrlInput.fill(baseUrl);
    if (description) {
      await this.descriptionInput.fill(description);
    }
    await this.submitButton.click();
    // Wait for dialog to close (success means dialog closed)
    await expect(this.dialog).not.toBeVisible({ timeout: 15000 });
  }

  async searchSystem(keyword: string) {
    await this.searchInput.fill(keyword);
    await this.searchInput.press('Enter');
    await this.page.waitForLoadState('networkidle');
  }

  async expectSystemInTable(name: string) {
    await expect(this.table.getByText(name).first()).toBeVisible({ timeout: 10000 });
  }

  async getTableRowCount(): Promise<number> {
    await this.page.waitForTimeout(500);
    return this.page.locator('.el-table__body-wrapper .el-table__row').count();
  }
}
