import { test, expect } from '@playwright/test';
import { ExecutionPage } from '../src/pages/ExecutionPage';
import { login } from '../src/utils/auth';

test.describe('执行记录', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('执行列表页面加载', async ({ page }) => {
    const execPage = new ExecutionPage(page);
    await execPage.goto();
    await execPage.expectLoaded();
  });

  test('状态Tab可见', async ({ page }) => {
    const execPage = new ExecutionPage(page);
    await execPage.goto();
    await expect(execPage.tabs).toBeVisible();
  });

  test('切换状态Tab', async ({ page }) => {
    const execPage = new ExecutionPage(page);
    await execPage.goto();
    // Click on different tabs
    const successTab = page.locator('.el-tabs__item').filter({ hasText: /成功/ });
    if (await successTab.isVisible()) {
      await successTab.click();
      await page.waitForTimeout(500);
    }
    const failedTab = page.locator('.el-tabs__item').filter({ hasText: /失败/ });
    if (await failedTab.isVisible()) {
      await failedTab.click();
      await page.waitForTimeout(500);
    }
  });
});
