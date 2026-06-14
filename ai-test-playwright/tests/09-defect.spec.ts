import { test, expect } from '@playwright/test';
import { DefectPage } from '../src/pages/DefectPage';
import { login } from '../src/utils/auth';

test.describe('缺陷管理', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('缺陷列表页面加载', async ({ page }) => {
    const defectPage = new DefectPage(page);
    await defectPage.goto();
    await defectPage.expectLoaded();
  });

  test('统计卡片可见', async ({ page }) => {
    const defectPage = new DefectPage(page);
    await defectPage.goto();
    // Should show severity stats (S1, S2, S3, S4)
    await expect(page.getByText(/S1|致命/).first()).toBeVisible();
    await expect(page.getByText(/S2|严重/).first()).toBeVisible();
  });

  test('新建缺陷按钮可见', async ({ page }) => {
    const defectPage = new DefectPage(page);
    await defectPage.goto();
    await expect(defectPage.createButton).toBeVisible();
  });

  test('创建缺陷弹窗', async ({ page }) => {
    const defectPage = new DefectPage(page);
    await defectPage.goto();
    await defectPage.createButton.click();
    await expect(defectPage.dialog).toBeVisible();
    await expect(defectPage.titleInput).toBeVisible();
    // Close dialog
    await page.locator('.el-dialog').getByRole('button', { name: /取消/ }).click();
    await expect(defectPage.dialog).not.toBeVisible({ timeout: 5000 });
  });
});
