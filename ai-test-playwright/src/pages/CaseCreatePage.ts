import { type Page, type Locator, expect } from '@playwright/test';

export class CaseCreatePage {
  readonly page: Page;
  readonly pcTypeCard: Locator;
  readonly apiTypeCard: Locator;
  readonly nextButton: Locator;
  readonly caseNameInput: Locator;
  readonly systemSelect: Locator;
  readonly prioritySelect: Locator;
  readonly saveButton: Locator;
  readonly cancelButton: Locator;

  constructor(page: Page) {
    this.page = page;
    this.pcTypeCard = page.locator('.type-card, .el-card').filter({ hasText: /PC|Web/i }).first();
    this.apiTypeCard = page.locator('.type-card, .el-card').filter({ hasText: /API|接口/i }).first();
    this.nextButton = page.getByRole('button', { name: /下一步/ });
    this.caseNameInput = page.locator('.el-form-item').filter({ hasText: /用例名称/ }).locator('input');
    this.systemSelect = page.locator('.el-form-item').filter({ hasText: /所属系统/ }).locator('.el-select');
    this.prioritySelect = page.locator('.el-form-item').filter({ hasText: /优先级/ }).locator('.el-select');
    this.saveButton = page.getByRole('button', { name: /保存/ });
    this.cancelButton = page.getByRole('button', { name: /取消/ }).first();
  }

  async goto() {
    await this.page.goto('/cases/create');
  }

  async selectPCType() {
    await this.pcTypeCard.click();
  }

  async selectAPIType() {
    await this.apiTypeCard.click();
  }

  async goToForm() {
    await this.nextButton.click();
    await expect(this.caseNameInput).toBeVisible({ timeout: 5000 });
  }

  async fillBasicInfo(name: string) {
    await this.caseNameInput.fill(name);
  }

  /**
   * Select a system from the dropdown by name.
   */
  async selectSystem(systemName: string) {
    await this.systemSelect.click();
    // Wait for dropdown to appear and select the option
    await this.page.locator('.el-select-dropdown').locator('.el-select-dropdown__item').filter({ hasText: systemName }).click();
  }

  async save() {
    await this.saveButton.click();
  }
}
