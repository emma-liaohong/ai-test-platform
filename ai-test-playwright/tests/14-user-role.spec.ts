import { test, expect } from '@playwright/test';
import { UserListPage } from '../src/pages/UserListPage';
import { login } from '../src/utils/auth';

test.describe('用户和角色管理', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('用户列表页面加载', async ({ page }) => {
    const userPage = new UserListPage(page);
    await userPage.goto();
    await userPage.expectLoaded();
  });

  test('用户列表显示管理员', async ({ page }) => {
    const userPage = new UserListPage(page);
    await userPage.goto();
    // Should show the admin user
    await expect(page.getByText('admin').first()).toBeVisible();
  });

  test('新增用户按钮可见', async ({ page }) => {
    const userPage = new UserListPage(page);
    await userPage.goto();
    await expect(userPage.addButton).toBeVisible();
  });

  test('角色列表页面加载', async ({ page }) => {
    await page.goto('/roles');
    await page.waitForLoadState('networkidle');
    // Should show role table or cards
    await expect(page.locator('.el-table, .el-card').first()).toBeVisible({ timeout: 10000 });
  });
});
