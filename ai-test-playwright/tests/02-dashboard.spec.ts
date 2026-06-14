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

  test('统计卡片显示真实数据', async ({ page }) => {
    const dashboard = new DashboardPage(page);
    await dashboard.goto();
    await dashboard.expectStatCardsVisible();

    // Wait for API response
    await page.waitForTimeout(2000);

    const values = await dashboard.getStatValues();

    // Verify data is from MySQL (not hardcoded placeholders)
    // caseCount should be >= 1 (we created MYSQL-TEST-001)
    expect(values['测试用例']).toBeGreaterThanOrEqual(1);

    // skillCount should be 3 (pre-seeded in MySQL)
    expect(values['AI 技能']).toBe(3);

    // All values should be numbers, not the old hardcoded values
    // Old values were: 1286, 48, 156, 12, 3842
    expect(values['测试用例']).not.toBe(1286);
    expect(values['用例套件']).not.toBe(48);
    expect(values['缺陷总数']).not.toBe(156);
    expect(values['AI 技能']).not.toBe(12);
    expect(values['AI 对话']).not.toBe(3842);
  });

  test('欢迎信息正确', async ({ page }) => {
    const dashboard = new DashboardPage(page);
    await dashboard.goto();
    // Wait for getInfo() fallback to load user info
    await page.waitForTimeout(2000);
    // Should show the real user name (fetched via getInfo fallback)
    await expect(page.locator('.welcome-section')).toContainText('管理员');
    // Should show today's date
    await expect(page.locator('.welcome-section')).toContainText('2026');
  });

  test('侧边栏导航可见', async ({ page }) => {
    const dashboard = new DashboardPage(page);
    await dashboard.expectSidebarVisible();
  });

  test('侧边栏菜单项完整', async ({ page }) => {
    await expect(page.locator('.sidebar-menu')).toBeVisible();
    await expect(page.locator('.el-menu-item').filter({ hasText: '工作台' })).toBeVisible();
    await expect(page.locator('.el-menu-item').filter({ hasText: '知识库' })).toBeVisible();
    await expect(page.locator('.el-menu-item').filter({ hasText: '缺陷管理' })).toBeVisible();
    await expect(page.locator('.el-sub-menu').filter({ hasText: '测试管理' })).toBeVisible();
    await expect(page.locator('.el-sub-menu').filter({ hasText: 'AI 能力' })).toBeVisible();
    await expect(page.locator('.el-sub-menu').filter({ hasText: '系统管理' })).toBeVisible();
  });

  test('可以通过侧边栏导航到各模块', async ({ page }) => {
    await page.goto('/systems');
    await expect(page).toHaveURL(/\/systems/);
    await expect(page.locator('.el-table')).toBeVisible({ timeout: 10000 });
  });
});
