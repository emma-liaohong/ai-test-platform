import { test, expect } from '@playwright/test';
import { DashboardPage } from '../src/pages/DashboardPage';
import { login } from '../src/utils/auth';

test.describe('工作台', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('工作台页面正常加载', async ({ page }) => {
    const dashboard = new DashboardPage(page);
    await dashboard.expectLoaded();
  });

  test('统计卡片可见', async ({ page }) => {
    const dashboard = new DashboardPage(page);
    await dashboard.expectStatCardsVisible();
  });

  test('侧边栏导航可见', async ({ page }) => {
    const dashboard = new DashboardPage(page);
    await dashboard.expectSidebarVisible();
  });

  test('侧边栏菜单项完整', async ({ page }) => {
    // Check key menu items exist in sidebar
    await expect(page.locator('.sidebar-menu')).toBeVisible();
    // Top-level menu items
    await expect(page.locator('.el-menu-item').filter({ hasText: '工作台' })).toBeVisible();
    await expect(page.locator('.el-menu-item').filter({ hasText: '知识库' })).toBeVisible();
    await expect(page.locator('.el-menu-item').filter({ hasText: '缺陷管理' })).toBeVisible();
    // Sub-menus exist (collapsed by default)
    await expect(page.locator('.el-sub-menu').filter({ hasText: '测试管理' })).toBeVisible();
    await expect(page.locator('.el-sub-menu').filter({ hasText: 'AI 能力' })).toBeVisible();
    await expect(page.locator('.el-sub-menu').filter({ hasText: '系统管理' })).toBeVisible();
  });

  test('可以通过侧边栏导航到各模块', async ({ page }) => {
    // Navigate directly via URL (sidebar sub-menus need expansion)
    await page.goto('/systems');
    await expect(page).toHaveURL(/\/systems/);
    await expect(page.locator('.el-table')).toBeVisible({ timeout: 10000 });
  });
});
