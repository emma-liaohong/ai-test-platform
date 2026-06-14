import { test, expect } from '@playwright/test';
import { SkillPage } from '../src/pages/SkillPage';
import { login } from '../src/utils/auth';

test.describe('AI 技能', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('技能列表页面加载', async ({ page }) => {
    const skillPage = new SkillPage(page);
    await skillPage.goto();
    await skillPage.expectLoaded();
  });

  test('内置技能卡片可见', async ({ page }) => {
    const skillPage = new SkillPage(page);
    await skillPage.goto();
    // Skills from h2-data.sql: 智能案例生成, 需求解析, 缺陷分析
    // Or mock fallback: 案例生成器, 需求解析器, 缺陷分析器
    await page.waitForTimeout(2000);
    const skillText = page.getByText(/智能案例生成|案例生成器|需求解析|缺陷分析/).first();
    await expect(skillText).toBeVisible({ timeout: 15000 });
  });

  test('类型Tab切换', async ({ page }) => {
    const skillPage = new SkillPage(page);
    await skillPage.goto();
    // Click on different type tabs
    const customTab = page.locator('.el-tabs__item').filter({ hasText: /CUSTOM/ });
    if (await customTab.isVisible()) {
      await customTab.click();
      await page.waitForTimeout(500);
    }
  });
});
