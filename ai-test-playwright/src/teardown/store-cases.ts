/**
 * Playwright globalTeardown - stores all E2E test cases into the case management system via API.
 * Runs after all tests complete.
 */
import { storeTestCase, getOrCreateE2ESystem } from '../utils/api';

// Define all E2E test cases
const e2eTestCases = [
  {
    caseCode: 'E2E-LOGIN-001',
    caseName: 'E2E测试 - 登录模块 - 正确账号登录成功',
    preconditions: '1. 前后端服务已启动\n2. 存在账号 admin/admin123',
    expectedResult: '登录成功，跳转到工作台页面',
    steps: [
      { stepOrder: 1, stepAction: 'navigate', stepTarget: '/login', stepDescription: '打开登录页面' },
      { stepOrder: 2, stepAction: 'input', stepTarget: '请输入用户名', stepValue: 'admin', stepDescription: '输入用户名 admin' },
      { stepOrder: 3, stepAction: 'input', stepTarget: '请输入密码', stepValue: 'admin123', stepDescription: '输入密码 admin123' },
      { stepOrder: 4, stepAction: 'click', stepTarget: '登录按钮', stepDescription: '点击登录按钮', expectedResult: '登录成功，跳转到 /dashboard' },
    ],
  },
  {
    caseCode: 'E2E-LOGIN-002',
    caseName: 'E2E测试 - 登录模块 - 错误密码登录失败',
    preconditions: '1. 前后端服务已启动\n2. 存在账号 admin',
    expectedResult: '登录失败，显示错误提示',
    steps: [
      { stepOrder: 1, stepAction: 'navigate', stepTarget: '/login', stepDescription: '打开登录页面' },
      { stepOrder: 2, stepAction: 'input', stepTarget: '请输入用户名', stepValue: 'admin', stepDescription: '输入用户名 admin' },
      { stepOrder: 3, stepAction: 'input', stepTarget: '请输入密码', stepValue: 'wrongpassword', stepDescription: '输入错误密码' },
      { stepOrder: 4, stepAction: 'click', stepTarget: '登录按钮', stepDescription: '点击登录按钮', expectedResult: '显示错误提示信息' },
    ],
  },
  {
    caseCode: 'E2E-LOGIN-003',
    caseName: 'E2E测试 - 登录模块 - 空用户名验证',
    preconditions: '前后端服务已启动',
    expectedResult: '表单验证提示，无法提交',
    steps: [
      { stepOrder: 1, stepAction: 'navigate', stepTarget: '/login', stepDescription: '打开登录页面' },
      { stepOrder: 2, stepAction: 'input', stepTarget: '请输入密码', stepValue: 'admin123', stepDescription: '只输入密码' },
      { stepOrder: 3, stepAction: 'click', stepTarget: '登录按钮', stepDescription: '点击登录按钮', expectedResult: '停留在登录页，表单验证提示' },
    ],
  },
  {
    caseCode: 'E2E-LOGIN-004',
    caseName: 'E2E测试 - 登录模块 - 未登录访问重定向',
    preconditions: '前后端服务已启动，本地存储已清空',
    expectedResult: '自动重定向到登录页',
    steps: [
      { stepOrder: 1, stepAction: 'click', stepTarget: '清除localStorage', stepDescription: '清除本地存储的token' },
      { stepOrder: 2, stepAction: 'navigate', stepTarget: '/dashboard', stepDescription: '直接访问工作台', expectedResult: '重定向到 /login 页面' },
    ],
  },
  {
    caseCode: 'E2E-DASH-001',
    caseName: 'E2E测试 - 工作台 - 页面正常加载',
    preconditions: '已使用 admin/admin123 登录',
    expectedResult: '工作台页面正常显示，包含统计卡片、侧边栏、欢迎信息',
    steps: [
      { stepOrder: 1, stepAction: 'navigate', stepTarget: '/dashboard', stepDescription: '进入工作台' },
      { stepOrder: 2, stepAction: 'assert', stepTarget: '.main-layout', stepDescription: '验证主布局可见' },
      { stepOrder: 3, stepAction: 'assert', stepTarget: '.el-card', stepDescription: '验证统计卡片可见', expectedResult: '至少显示5个统计卡片' },
      { stepOrder: 4, stepAction: 'assert', stepTarget: '.sidebar', stepDescription: '验证侧边栏可见' },
    ],
  },
  {
    caseCode: 'E2E-DASH-002',
    caseName: 'E2E测试 - 工作台 - 侧边栏导航完整',
    preconditions: '已登录',
    expectedResult: '侧边栏包含所有功能模块导航',
    steps: [
      { stepOrder: 1, stepAction: 'assert', stepTarget: '工作台', stepDescription: '验证工作台菜单项' },
      { stepOrder: 2, stepAction: 'assert', stepTarget: '测试用例', stepDescription: '验证测试用例菜单项' },
      { stepOrder: 3, stepAction: 'assert', stepTarget: '用例套件', stepDescription: '验证用例套件菜单项' },
      { stepOrder: 4, stepAction: 'assert', stepTarget: '知识库', stepDescription: '验证知识库菜单项' },
      { stepOrder: 5, stepAction: 'assert', stepTarget: '缺陷管理', stepDescription: '验证缺陷管理菜单项' },
      { stepOrder: 6, stepAction: 'assert', stepTarget: '系统管理', stepDescription: '验证系统管理菜单项' },
    ],
  },
  {
    caseCode: 'E2E-SYS-001',
    caseName: 'E2E测试 - 系统管理 - 查看系统列表',
    preconditions: '已登录，H2数据库有预置数据',
    expectedResult: '系统列表显示至少1条记录',
    steps: [
      { stepOrder: 1, stepAction: 'navigate', stepTarget: '/systems', stepDescription: '进入系统管理页面' },
      { stepOrder: 2, stepAction: 'assert', stepTarget: '.el-table', stepDescription: '验证表格可见' },
      { stepOrder: 3, stepAction: 'assert', stepTarget: '表格行数>0', stepDescription: '验证列表有数据', expectedResult: '表格至少显示1行数据' },
    ],
  },
  {
    caseCode: 'E2E-SYS-002',
    caseName: 'E2E测试 - 系统管理 - 新增E2E测试系统',
    preconditions: '已登录',
    expectedResult: '成功新增系统 "E2E测试系统"，列表中可见',
    steps: [
      { stepOrder: 1, stepAction: 'navigate', stepTarget: '/systems', stepDescription: '进入系统管理页面' },
      { stepOrder: 2, stepAction: 'click', stepTarget: '新增系统按钮', stepDescription: '点击新增系统按钮' },
      { stepOrder: 3, stepAction: 'input', stepTarget: '系统名称', stepValue: 'E2E测试系统', stepDescription: '输入系统名称' },
      { stepOrder: 4, stepAction: 'input', stepTarget: '系统编码', stepValue: 'E2E-SYS', stepDescription: '输入系统编码' },
      { stepOrder: 5, stepAction: 'input', stepTarget: '基础URL', stepValue: 'http://e2e-test.example.com', stepDescription: '输入基础URL' },
      { stepOrder: 6, stepAction: 'input', stepTarget: '描述', stepValue: 'Playwright E2E 自动化测试专用系统', stepDescription: '输入描述' },
      { stepOrder: 7, stepAction: 'click', stepTarget: '确定按钮', stepDescription: '点击确定', expectedResult: '弹窗关闭，列表中出现 "E2E测试系统"' },
    ],
  },
  {
    caseCode: 'E2E-SYS-003',
    caseName: 'E2E测试 - 系统管理 - 搜索系统',
    preconditions: '已登录，已创建E2E测试系统',
    expectedResult: '搜索结果显示匹配的系统',
    steps: [
      { stepOrder: 1, stepAction: 'navigate', stepTarget: '/systems', stepDescription: '进入系统管理页面' },
      { stepOrder: 2, stepAction: 'input', stepTarget: '搜索框', stepValue: 'E2E', stepDescription: '输入搜索关键词' },
      { stepOrder: 3, stepAction: 'click', stepTarget: '搜索按钮', stepDescription: '点击搜索', expectedResult: '列表中显示 "E2E测试系统"' },
    ],
  },
  {
    caseCode: 'E2E-CASE-001',
    caseName: 'E2E测试 - 案例创建 - 选择PC类型创建案例',
    preconditions: '已登录',
    expectedResult: '成功创建PC类型测试案例',
    steps: [
      { stepOrder: 1, stepAction: 'navigate', stepTarget: '/cases/create', stepDescription: '进入案例创建页' },
      { stepOrder: 2, stepAction: 'click', stepTarget: 'PC Web 测试卡片', stepDescription: '选择PC类型' },
      { stepOrder: 3, stepAction: 'click', stepTarget: '下一步按钮', stepDescription: '进入表单' },
      { stepOrder: 4, stepAction: 'input', stepTarget: '用例名称', stepValue: 'E2E-PC-Test', stepDescription: '填写用例名称' },
      { stepOrder: 5, stepAction: 'click', stepTarget: '保存按钮', stepDescription: '保存案例', expectedResult: '案例创建成功' },
    ],
  },
  {
    caseCode: 'E2E-CASE-002',
    caseName: 'E2E测试 - 案例管理 - 列表查看与搜索',
    preconditions: '已登录，存在测试案例',
    expectedResult: '案例列表正常显示，搜索功能正常',
    steps: [
      { stepOrder: 1, stepAction: 'navigate', stepTarget: '/cases', stepDescription: '进入案例列表页' },
      { stepOrder: 2, stepAction: 'assert', stepTarget: '.el-table', stepDescription: '验证表格可见' },
      { stepOrder: 3, stepAction: 'input', stepTarget: '搜索框', stepValue: 'E2E', stepDescription: '搜索E2E案例' },
      { stepOrder: 4, stepAction: 'click', stepTarget: '搜索按钮', stepDescription: '执行搜索', expectedResult: '搜索结果过滤显示' },
      { stepOrder: 5, stepAction: 'click', stepTarget: '重置按钮', stepDescription: '重置搜索', expectedResult: '恢复完整列表' },
    ],
  },
  {
    caseCode: 'E2E-SUITE-001',
    caseName: 'E2E测试 - 套件管理 - 页面加载与弹窗',
    preconditions: '已登录',
    expectedResult: '套件列表正常加载，新建弹窗正常弹出',
    steps: [
      { stepOrder: 1, stepAction: 'navigate', stepTarget: '/suites', stepDescription: '进入套件列表页' },
      { stepOrder: 2, stepAction: 'assert', stepTarget: '.el-table', stepDescription: '验证表格可见' },
      { stepOrder: 3, stepAction: 'click', stepTarget: '新建套件按钮', stepDescription: '点击新建' },
      { stepOrder: 4, stepAction: 'assert', stepTarget: '.el-dialog', stepDescription: '验证弹窗出现', expectedResult: '弹窗正常显示' },
    ],
  },
  {
    caseCode: 'E2E-EXEC-001',
    caseName: 'E2E测试 - 执行记录 - 页面加载',
    preconditions: '已登录',
    expectedResult: '执行记录页面正常加载',
    steps: [
      { stepOrder: 1, stepAction: 'navigate', stepTarget: '/executions', stepDescription: '进入执行记录页' },
      { stepOrder: 2, stepAction: 'assert', stepTarget: '.el-table', stepDescription: '验证表格可见' },
      { stepOrder: 3, stepAction: 'assert', stepTarget: '.el-tabs__nav', stepDescription: '验证状态Tab可见' },
    ],
  },
  {
    caseCode: 'E2E-KB-001',
    caseName: 'E2E测试 - 知识库 - 分类树与Tab切换',
    preconditions: '已登录',
    expectedResult: '分类树正常显示，Tab切换正常',
    steps: [
      { stepOrder: 1, stepAction: 'navigate', stepTarget: '/knowledge', stepDescription: '进入知识库页面' },
      { stepOrder: 2, stepAction: 'assert', stepTarget: '.el-tree', stepDescription: '验证分类树可见' },
      { stepOrder: 3, stepAction: 'assert', stepTarget: '案例库', stepDescription: '验证预置分类存在' },
      { stepOrder: 4, stepAction: 'click', stepTarget: '文档管理Tab', stepDescription: '切换到文档Tab' },
      { stepOrder: 5, stepAction: 'click', stepTarget: '视频库Tab', stepDescription: '切换到视频Tab' },
      { stepOrder: 6, stepAction: 'click', stepTarget: 'AI搜索Tab', stepDescription: '切换到搜索Tab' },
    ],
  },
  {
    caseCode: 'E2E-DEFECT-001',
    caseName: 'E2E测试 - 缺陷管理 - 创建缺陷弹窗',
    preconditions: '已登录',
    expectedResult: '缺陷列表正常显示，创建弹窗正常弹出',
    steps: [
      { stepOrder: 1, stepAction: 'navigate', stepTarget: '/defects', stepDescription: '进入缺陷管理页面' },
      { stepOrder: 2, stepAction: 'assert', stepTarget: '.el-table', stepDescription: '验证表格可见' },
      { stepOrder: 3, stepAction: 'assert', stepTarget: 'S1|致命', stepDescription: '验证统计卡片可见' },
      { stepOrder: 4, stepAction: 'click', stepTarget: '新建缺陷按钮', stepDescription: '点击新建' },
      { stepOrder: 5, stepAction: 'assert', stepTarget: '.el-dialog', stepDescription: '验证弹窗出现', expectedResult: '创建缺陷弹窗正常显示' },
    ],
  },
  {
    caseCode: 'E2E-SKILL-001',
    caseName: 'E2E测试 - AI技能 - 技能列表与Tab切换',
    preconditions: '已登录',
    expectedResult: '技能列表正常显示，内置技能可见',
    steps: [
      { stepOrder: 1, stepAction: 'navigate', stepTarget: '/skills', stepDescription: '进入AI技能页面' },
      { stepOrder: 2, stepAction: 'assert', stepTarget: '智能案例生成', stepDescription: '验证内置技能可见' },
      { stepOrder: 3, stepAction: 'assert', stepTarget: '.el-tabs', stepDescription: '验证类型Tab可见' },
    ],
  },
  {
    caseCode: 'E2E-CHAT-001',
    caseName: 'E2E测试 - AI对话 - 新建会话与发送消息',
    preconditions: '已登录',
    expectedResult: '可以新建会话并发送消息',
    steps: [
      { stepOrder: 1, stepAction: 'navigate', stepTarget: '/chat', stepDescription: '进入AI对话页面' },
      { stepOrder: 2, stepAction: 'click', stepTarget: '新建会话按钮', stepDescription: '新建会话' },
      { stepOrder: 3, stepAction: 'input', stepTarget: '消息输入框', stepValue: '你好', stepDescription: '输入消息' },
      { stepOrder: 4, stepAction: 'click', stepTarget: '发送按钮', stepDescription: '发送消息', expectedResult: '收到AI回复' },
    ],
  },
  {
    caseCode: 'E2E-RECORD-001',
    caseName: 'E2E测试 - 录制回放 - 页面加载',
    preconditions: '已登录',
    expectedResult: '录制列表页面正常显示',
    steps: [
      { stepOrder: 1, stepAction: 'navigate', stepTarget: '/record', stepDescription: '进入录制回放页面' },
      { stepOrder: 2, stepAction: 'assert', stepTarget: '.el-table', stepDescription: '验证录制列表可见' },
      { stepOrder: 3, stepAction: 'assert', stepTarget: '开始录制按钮', stepDescription: '验证开始录制按钮可见' },
    ],
  },
  {
    caseCode: 'E2E-ANALYSIS-001',
    caseName: 'E2E测试 - 需求分析 - 页面加载',
    preconditions: '已登录',
    expectedResult: '需求分析页面正常显示',
    steps: [
      { stepOrder: 1, stepAction: 'navigate', stepTarget: '/analysis', stepDescription: '进入需求分析页面' },
      { stepOrder: 2, stepAction: 'assert', stepTarget: '开始分析按钮', stepDescription: '验证按钮可见' },
      { stepOrder: 3, stepAction: 'assert', stepTarget: '开始分析按钮(disabled)', stepDescription: '验证未选择文档时按钮禁用', expectedResult: '按钮为禁用状态' },
    ],
  },
  {
    caseCode: 'E2E-USER-001',
    caseName: 'E2E测试 - 用户管理 - 列表查看',
    preconditions: '已登录',
    expectedResult: '用户列表正常显示，包含admin用户',
    steps: [
      { stepOrder: 1, stepAction: 'navigate', stepTarget: '/users', stepDescription: '进入用户管理页面' },
      { stepOrder: 2, stepAction: 'assert', stepTarget: '.el-table', stepDescription: '验证表格可见' },
      { stepOrder: 3, stepAction: 'assert', stepTarget: 'admin', stepDescription: '验证admin用户可见', expectedResult: '列表中显示admin用户' },
    ],
  },
  {
    caseCode: 'E2E-SETTINGS-001',
    caseName: 'E2E测试 - 系统设置 - Tab切换',
    preconditions: '已登录',
    expectedResult: '设置页面正常显示，Tab切换正常',
    steps: [
      { stepOrder: 1, stepAction: 'navigate', stepTarget: '/settings', stepDescription: '进入系统设置页面' },
      { stepOrder: 2, stepAction: 'assert', stepTarget: '.el-tabs__nav', stepDescription: '验证Tab导航可见' },
      { stepOrder: 3, stepAction: 'click', stepTarget: 'LLM配置Tab', stepDescription: '切换到LLM配置' },
      { stepOrder: 4, stepAction: 'click', stepTarget: 'Playwright配置Tab', stepDescription: '切换到Playwright配置' },
      { stepOrder: 5, stepAction: 'click', stepTarget: '存储配置Tab', stepDescription: '切换到存储配置' },
    ],
  },
];

async function globalTeardown() {
  console.log('\n📦 Storing E2E test cases to case management...');

  try {
    const system = await getOrCreateE2ESystem();
    console.log(`✅ E2E System ID: ${system.id}`);

    let success = 0;
    let failed = 0;

    for (const tc of e2eTestCases) {
      try {
        const res = await storeTestCase(tc);
        if (res.code === 200) {
          success++;
          console.log(`  ✅ ${tc.caseCode}: ${tc.caseName}`);
        } else {
          failed++;
          console.log(`  ❌ ${tc.caseCode}: ${res.message}`);
        }
      } catch (err: any) {
        failed++;
        console.log(`  ❌ ${tc.caseCode}: ${err.message}`);
      }
    }

    console.log(`\n📊 Test case storage complete: ${success} succeeded, ${failed} failed out of ${e2eTestCases.length} total`);
  } catch (err: any) {
    console.error('❌ Failed to store test cases:', err.message);
  }
}

export default globalTeardown;
