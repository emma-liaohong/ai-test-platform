/**
 * API helper for creating test data and storing E2E cases.
 * Uses the backend directly (bypasses the frontend proxy).
 */

const API_BASE = 'http://localhost:8080/api';

let cachedToken: string | null = null;
let cachedSystemId: number | null = null;

export async function getAuthToken(): Promise<string> {
  if (cachedToken) return cachedToken;

  const res = await fetch(`${API_BASE}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: 'admin', password: 'admin123' }),
  });
  const data = await res.json();
  if (data.code !== 200) throw new Error(`Login failed: ${data.message}`);
  cachedToken = data.data.token;
  return cachedToken!;
}

async function apiRequest(method: string, path: string, body?: unknown) {
  const token = await getAuthToken();
  const res = await fetch(`${API_BASE}${path}`, {
    method,
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
    body: body ? JSON.stringify(body) : undefined,
  });
  return res.json();
}

/**
 * Find or create the E2E test system.
 */
export async function getOrCreateE2ESystem(): Promise<{ id: number; name: string }> {
  if (cachedSystemId) return { id: cachedSystemId, name: 'E2E测试系统' };

  try {
    // Search for existing system
    const listRes = await apiRequest('GET', '/system/systems?current=1&size=100');
    const records = listRes.data?.records || [];
    const existing = records.find((s: any) => s.name?.includes('E2E') || s.code === 'E2E-SYS');

    if (existing) {
      cachedSystemId = existing.id;
      console.log(`  Found existing E2E system: ID=${existing.id}, Name=${existing.name}`);
      return { id: existing.id, name: existing.name };
    }

    // Create it
    console.log('  Creating new E2E system...');
    const createRes = await apiRequest('POST', '/system/systems', {
      name: 'E2E测试系统',
      code: 'E2E-SYS',
      baseUrl: 'http://e2e-test.example.com',
      description: 'Playwright E2E 自动化测试专用系统',
      status: 1,
    });

    const system = createRes.data;
    cachedSystemId = system?.id ?? system;
    console.log(`  Created E2E system: ID=${cachedSystemId}`);
    return { id: cachedSystemId!, name: 'E2E测试系统' };
  } catch (err: any) {
    console.error('  Error in getOrCreateE2ESystem:', err.message);
    throw err;
  }
}

/**
 * Store a test case in the case management system via API.
 */
export async function storeTestCase(params: {
  caseCode: string;
  caseName: string;
  preconditions?: string;
  expectedResult?: string;
  steps: Array<{
    stepOrder: number;
    stepAction: string;
    stepTarget?: string;
    stepValue?: string;
    stepDescription: string;
    expectedResult?: string;
  }>;
}) {
  const system = await getOrCreateE2ESystem();

  // Format steps as natural language text (backend doesn't handle inline steps in create)
  const stepsText = params.steps
    .map(s => `${s.stepOrder}. ${s.stepDescription}${s.expectedResult ? ' → 预期: ' + s.expectedResult : ''}`)
    .join('\n');

  const caseBody = {
    caseCode: params.caseCode,
    caseName: params.caseName,
    caseType: 'PC',
    systemId: system.id,
    priority: 'P1',
    status: 'ready',
    tags: 'E2E,Playwright,自动化',
    preconditions: params.preconditions || '1. 前后端服务已启动\n2. 使用 admin/admin123 账号登录',
    expectedResult: params.expectedResult || '测试场景全部通过',
    naturalLanguageSteps: stepsText,
  };

  const result = await apiRequest('POST', '/cases', caseBody);
  if (result.code !== 200) {
    console.log(`    API error: code=${result.code}, message=${result.message}`);
  }
  return result;
}
