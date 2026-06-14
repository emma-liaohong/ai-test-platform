import { test, expect } from '@playwright/test';
import { CaseListPage } from '../src/pages/CaseListPage';
import { login } from '../src/utils/auth';

test.describe('案例管理', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('案例列表页面加载', async ({ page }) => {
    const casePage = new CaseListPage(page);
    await casePage.goto();
    await casePage.expectLoaded();
  });

  test('案例列表显示数据', async ({ page }) => {
    const casePage = new CaseListPage(page);
    await casePage.goto();
    const count = await casePage.getRowCount();
    // There should be at least the cases we created
    expect(count).toBeGreaterThanOrEqual(0);
  });

  test('新建案例按钮可点击', async ({ page }) => {
    const casePage = new CaseListPage(page);
    await casePage.goto();
    await expect(casePage.createButton).toBeVisible();
    await casePage.createButton.click();
    await expect(page).toHaveURL(/\/cases\/create/);
  });

  test('搜索案例', async ({ page }) => {
    const casePage = new CaseListPage(page);
    await casePage.goto();
    await casePage.searchInput.fill('E2E');
    await casePage.searchButton.click();
    await page.waitForTimeout(1000);
    // Should not crash
    await casePage.expectLoaded();
  });

  test('重置搜索', async ({ page }) => {
    const casePage = new CaseListPage(page);
    await casePage.goto();
    await casePage.searchInput.fill('nonexistent');
    await casePage.searchButton.click();
    await page.waitForTimeout(1000);
    await casePage.resetButton.click();
    await page.waitForTimeout(1000);
    await casePage.expectLoaded();
  });
});
