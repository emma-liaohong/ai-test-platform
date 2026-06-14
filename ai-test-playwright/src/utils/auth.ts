import { type Page, expect } from '@playwright/test';

const STORAGE_STATE_KEY = 'auth-storage';

/**
 * Perform login via the UI and return the page ready for use.
 */
export async function login(page: Page, username = 'admin', password = 'admin123') {
  await page.goto('/login');
  await page.getByPlaceholder('请输入用户名').fill(username);
  await page.getByPlaceholder('请输入密码').fill(password);
  await page.getByRole('button', { name: /登\s*录/ }).click();
  await expect(page).toHaveURL(/\/(dashboard)?$/, { timeout: 15_000 });
}

/**
 * Login and wait for dashboard to be visible.
 */
export async function loginAndWaitForDashboard(page: Page) {
  await login(page);
  await expect(page.locator('.main-layout')).toBeVisible({ timeout: 15_000 });
}
