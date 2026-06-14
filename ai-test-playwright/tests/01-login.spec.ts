import { test, expect } from '@playwright/test';
import { LoginPage } from '../src/pages/LoginPage';

test.describe('登录模块', () => {
  test('正确账号登录成功', async ({ page }) => {
    const loginPage = new LoginPage(page);
    await loginPage.goto();
    await loginPage.login('admin', 'admin123');
    await loginPage.expectLoginSuccess();
  });

  test('错误密码登录失败', async ({ page }) => {
    const loginPage = new LoginPage(page);
    await loginPage.goto();
    await loginPage.login('admin', 'wrongpassword');
    // Should remain on login page after failed login
    await page.waitForTimeout(3000);
    await expect(page).toHaveURL(/\/login/);
    // Login button should be visible again (loading state cleared)
    await expect(loginPage.loginButton).toBeVisible();
  });

  test('空用户名提示验证错误', async ({ page }) => {
    const loginPage = new LoginPage(page);
    await loginPage.goto();
    await loginPage.passwordInput.fill('admin123');
    await loginPage.loginButton.click();
    // Form validation should prevent submission or show error
    await page.waitForTimeout(1000);
    // Should still be on login page
    await expect(page).toHaveURL(/\/login/);
  });

  test('空密码提示验证错误', async ({ page }) => {
    const loginPage = new LoginPage(page);
    await loginPage.goto();
    await loginPage.usernameInput.fill('admin');
    await loginPage.loginButton.click();
    await page.waitForTimeout(1000);
    await expect(page).toHaveURL(/\/login/);
  });

  test('未登录访问受保护页面重定向到登录页', async ({ page }) => {
    // Clear any stored token
    await page.goto('/dashboard');
    await page.evaluate(() => localStorage.clear());
    await page.goto('/dashboard');
    await expect(page).toHaveURL(/\/login/, { timeout: 10000 });
  });

  test('登录页面UI元素完整', async ({ page }) => {
    const loginPage = new LoginPage(page);
    await loginPage.goto();
    await expect(loginPage.usernameInput).toBeVisible();
    await expect(loginPage.passwordInput).toBeVisible();
    await expect(loginPage.loginButton).toBeVisible();
    await expect(page.getByText('AI 测试平台')).toBeVisible();
  });
});
