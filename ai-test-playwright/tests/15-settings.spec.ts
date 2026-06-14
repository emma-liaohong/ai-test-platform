import { test, expect } from '@playwright/test';
import { SettingsPage } from '../src/pages/SettingsPage';
import { login } from '../src/utils/auth';

test.describe('系统设置', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('设置页面加载', async ({ page }) => {
    const settingsPage = new SettingsPage(page);
    await settingsPage.goto();
    await settingsPage.expectLoaded();
  });

  test('LLM 配置 Tab', async ({ page }) => {
    const settingsPage = new SettingsPage(page);
    await settingsPage.goto();
    await settingsPage.llmTab.click();
    await page.waitForTimeout(500);
    // Should show LLM configuration fields
    await expect(page.getByText(/LLM|模型|API/).first()).toBeVisible();
  });

  test('Playwright 配置 Tab', async ({ page }) => {
    const settingsPage = new SettingsPage(page);
    await settingsPage.goto();
    await settingsPage.playwrightTab.click();
    await page.waitForTimeout(500);
    await expect(page.getByText(/浏览器|超时|Playwright/).first()).toBeVisible();
  });

  test('存储配置 Tab', async ({ page }) => {
    const settingsPage = new SettingsPage(page);
    await settingsPage.goto();
    await settingsPage.storageTab.click();
    await page.waitForTimeout(500);
    await expect(page.getByText(/存储|Endpoint|Bucket/).first()).toBeVisible();
  });
});
