import { test, expect } from '@playwright/test';
import { CaseCreatePage } from '../src/pages/CaseCreatePage';
import { login } from '../src/utils/auth';

test.describe('案例创建', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('创建案例页面加载', async ({ page }) => {
    const createPage = new CaseCreatePage(page);
    await createPage.goto();
    // Should show type selection cards
    await expect(page).toHaveURL(/\/cases\/create/);
  });

  test('选择PC类型并进入表单', async ({ page }) => {
    const createPage = new CaseCreatePage(page);
    await createPage.goto();
    await createPage.selectPCType();
    await createPage.goToForm();
    await expect(createPage.caseNameInput).toBeVisible();
    await expect(createPage.saveButton).toBeVisible();
  });

  test('填写案例基本信息', async ({ page }) => {
    const createPage = new CaseCreatePage(page);
    await createPage.goto();
    await createPage.selectPCType();
    await createPage.goToForm();
    await createPage.fillBasicInfo('E2E测试用例-PC-' + Date.now());
    // Select a system (pre-seeded in H2)
    await createPage.selectSystem('订单系统');
    // Verify the name was filled
    await expect(createPage.caseNameInput).toHaveValue(/E2E测试用例-PC-/);
  });

  test('表单必填项验证', async ({ page }) => {
    const createPage = new CaseCreatePage(page);
    await createPage.goto();
    await createPage.selectPCType();
    await createPage.goToForm();
    // Only fill case name, don't select system
    await createPage.fillBasicInfo('测试无系统');
    // Try to save without selecting a system
    await createPage.save();
    // Should show validation error for missing system
    await expect(page.locator('.el-form-item__error').filter({ hasText: /所属系统/ })).toBeVisible({ timeout: 3000 });
    // Should still be on create page
    await expect(page).toHaveURL(/\/cases\/create/);
  });
});
