import { test, expect } from '@playwright/test';
import { AnalysisPage } from '../src/pages/AnalysisPage';
import { login } from '../src/utils/auth';

test.describe('需求分析', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('需求分析页面加载', async ({ page }) => {
    const analysisPage = new AnalysisPage(page);
    await analysisPage.goto();
    await analysisPage.expectLoaded();
  });

  test('分析类型选择可见', async ({ page }) => {
    const analysisPage = new AnalysisPage(page);
    await analysisPage.goto();
    // Radio group for analysis type
    await expect(page.getByText(/全量分析|功能点|影响/).first()).toBeVisible();
  });

  test('开始分析按钮在未选择文档时禁用', async ({ page }) => {
    const analysisPage = new AnalysisPage(page);
    await analysisPage.goto();
    // Button should be disabled since no document is selected
    const startBtn = page.getByRole('button', { name: /开始分析/ });
    await expect(startBtn).toBeDisabled();
  });
});
