import { test, expect } from '@playwright/test';
import { SuiteListPage } from '../src/pages/SuiteListPage';
import { login } from '../src/utils/auth';

test.describe('套件管理', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('套件列表页面加载', async ({ page }) => {
    const suitePage = new SuiteListPage(page);
    await suitePage.goto();
    await suitePage.expectLoaded();
  });

  test('新建套件按钮可见', async ({ page }) => {
    const suitePage = new SuiteListPage(page);
    await suitePage.goto();
    await expect(suitePage.createButton).toBeVisible();
  });

  test('创建套件弹窗', async ({ page }) => {
    const suitePage = new SuiteListPage(page);
    await suitePage.goto();
    await suitePage.createButton.click();
    await expect(suitePage.dialog).toBeVisible();
    await expect(suitePage.nameInput).toBeVisible();
    // Close dialog
    await page.locator('.el-dialog').getByRole('button', { name: /取消/ }).click();
    await expect(suitePage.dialog).not.toBeVisible({ timeout: 5000 });
  });
});
