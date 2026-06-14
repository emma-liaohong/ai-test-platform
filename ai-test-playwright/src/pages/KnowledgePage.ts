import { type Page, type Locator, expect } from '@playwright/test';

export class KnowledgePage {
  readonly page: Page;
  readonly categoryTree: Locator;
  readonly documentTab: Locator;
  readonly videoTab: Locator;
  readonly searchTab: Locator;
  readonly table: Locator;

  constructor(page: Page) {
    this.page = page;
    this.categoryTree = page.locator('.el-tree');
    this.documentTab = page.locator('.el-tabs__item').filter({ hasText: /文档/ });
    this.videoTab = page.locator('.el-tabs__item').filter({ hasText: /视频/ });
    this.searchTab = page.locator('.el-tabs__item').filter({ hasText: /搜索/ });
    this.table = page.locator('.el-table');
  }

  async goto() {
    await this.page.goto('/knowledge');
    await expect(this.categoryTree).toBeVisible({ timeout: 10000 });
  }

  async expectLoaded() {
    await expect(this.categoryTree).toBeVisible();
  }
}
