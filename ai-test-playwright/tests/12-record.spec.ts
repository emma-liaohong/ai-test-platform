import { test, expect } from '@playwright/test';
import { RecordPage } from '../src/pages/RecordPage';
import { login } from '../src/utils/auth';

test.describe('录制回放', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('录制列表页面加载', async ({ page }) => {
    const recordPage = new RecordPage(page);
    await recordPage.goto();
    await recordPage.expectLoaded();
  });

  test('开始录制按钮可见', async ({ page }) => {
    const recordPage = new RecordPage(page);
    await recordPage.goto();
    await expect(recordPage.startButton).toBeVisible();
  });

  test('录制表单可以展开', async ({ page }) => {
    const recordPage = new RecordPage(page);
    await recordPage.goto();
    // Click start recording - should show form or dialog
    await recordPage.startButton.click();
    await page.waitForTimeout(1000);
    // Should see input fields for session name and URL
    const inputs = page.locator('input:visible');
    expect(await inputs.count()).toBeGreaterThan(0);
  });
});
