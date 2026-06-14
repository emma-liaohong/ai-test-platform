import { test, expect } from '@playwright/test';
import { SystemListPage } from '../src/pages/SystemListPage';
import { login } from '../src/utils/auth';

test.describe('系统管理', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('查看系统列表', async ({ page }) => {
    const systemPage = new SystemListPage(page);
    await systemPage.goto();
    const count = await systemPage.getTableRowCount();
    expect(count).toBeGreaterThan(0);
  });

  test('新增系统 - E2E测试系统', async ({ page }) => {
    const systemPage = new SystemListPage(page);
    await systemPage.goto();
    const ts = Date.now();
    await systemPage.addSystem(
      `E2E测试系统-${ts}`,
      `E2E-SYS-${ts}`,
      'http://e2e-test.example.com',
      'Playwright E2E 自动化测试专用系统'
    );
    await page.waitForTimeout(1000);
    await systemPage.expectSystemInTable(`E2E测试系统-${ts}`);
  });

  test('搜索系统', async ({ page }) => {
    const systemPage = new SystemListPage(page);
    await systemPage.goto();
    await systemPage.searchSystem('E2E');
    await systemPage.expectSystemInTable('E2E测试系统');
  });

  test('编辑系统', async ({ page }) => {
    const systemPage = new SystemListPage(page);
    await systemPage.goto();
    // Find and click edit button for any system row
    const editBtn = page.locator('.el-table__row').first().getByText('编辑');
    if (await editBtn.isVisible()) {
      await editBtn.click();
      await expect(systemPage.dialog).toBeVisible();
      // Close dialog without changes
      await page.locator('.el-dialog').getByRole('button', { name: /取消/ }).click();
      await expect(systemPage.dialog).not.toBeVisible({ timeout: 5000 });
    }
  });
});
