import { test, expect } from '@playwright/test';
import { KnowledgePage } from '../src/pages/KnowledgePage';
import { login } from '../src/utils/auth';

test.describe('知识库', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('知识库页面加载', async ({ page }) => {
    const knowledgePage = new KnowledgePage(page);
    await knowledgePage.goto();
    await knowledgePage.expectLoaded();
  });

  test('分类树可见', async ({ page }) => {
    const knowledgePage = new KnowledgePage(page);
    await knowledgePage.goto();
    await expect(knowledgePage.categoryTree).toBeVisible();
    // Should have default categories
    await expect(page.getByText('案例库')).toBeVisible();
    await expect(page.getByText('需求文档库')).toBeVisible();
  });

  test('Tab切换 - 文档管理', async ({ page }) => {
    const knowledgePage = new KnowledgePage(page);
    await knowledgePage.goto();
    await knowledgePage.documentTab.click();
    await page.waitForTimeout(500);
  });

  test('Tab切换 - 视频库', async ({ page }) => {
    const knowledgePage = new KnowledgePage(page);
    await knowledgePage.goto();
    await knowledgePage.videoTab.click();
    await page.waitForTimeout(500);
  });

  test('Tab切换 - AI搜索', async ({ page }) => {
    const knowledgePage = new KnowledgePage(page);
    await knowledgePage.goto();
    await knowledgePage.searchTab.click();
    await page.waitForTimeout(500);
  });
});
