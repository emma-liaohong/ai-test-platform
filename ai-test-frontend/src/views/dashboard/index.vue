<template>
  <div class="dashboard-container">
    <!-- Welcome -->
    <div class="welcome-section">
      <h2>👋 欢迎回来，{{ userStore.userInfo?.realName || 'Admin' }}</h2>
      <p class="welcome-desc">今天是 {{ currentDate }}，祝你工作愉快！</p>
    </div>

    <!-- Stat Cards -->
    <el-row :gutter="16" class="stat-cards">
      <el-col :xs="12" :sm="8" :md="4" :lg="4" v-for="stat in statCards" :key="stat.title">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <p class="stat-title">{{ stat.title }}</p>
              <p class="stat-value">{{ stat.value }}</p>
            </div>
            <el-icon :size="40" :style="{ color: stat.color }">
              <component :is="stat.icon" />
            </el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Charts & Activity -->
    <el-row :gutter="16" class="dashboard-grid">
      <el-col :xs="24" :lg="16">
        <el-card shadow="hover">
          <template #header>
            <span>执行趋势（近7天）</span>
          </template>
          <v-chart :option="trendChartOption" autoresize style="height: 280px" />
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>用例分布</span>
              <el-radio-group v-model="pieDimension" size="small">
                <el-radio-button label="type">类型</el-radio-button>
                <el-radio-button label="priority">优先级</el-radio-button>
                <el-radio-button label="system">系统</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <v-chart :option="pieChartOption" autoresize style="height: 280px" />
        </el-card>
      </el-col>

      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>最近活动</span>
              <el-button text type="primary">查看全部</el-button>
            </div>
          </template>
          <el-table :data="recentActivities" stripe>
            <el-table-column prop="time" label="时间" width="180" />
            <el-table-column prop="user" label="操作人" width="120" />
            <el-table-column prop="action" label="操作" />
            <el-table-column prop="target" label="对象" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.statusType" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import dayjs from 'dayjs'
import { useUserStore } from '@/store/user'
import { getDashboardStats, type DashboardStats } from '@/api/dashboard'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
} from 'echarts/components'

use([
  CanvasRenderer,
  LineChart,
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
])

const userStore = useUserStore()

const currentDate = computed(() => dayjs().format('YYYY年MM月DD日 dddd'))

const stats = ref<DashboardStats>({
  caseCount: 0,
  suiteCount: 0,
  defectCount: 0,
  skillCount: 0,
  conversationCount: 0,
})

const statCards = computed(() => [
  { title: '测试用例', value: stats.value.caseCount.toLocaleString(), icon: 'Document', color: '#409EFF' },
  { title: '用例套件', value: stats.value.suiteCount.toLocaleString(), icon: 'FolderOpened', color: '#67C23A' },
  { title: '缺陷总数', value: stats.value.defectCount.toLocaleString(), icon: 'WarningFilled', color: '#E6A23C' },
  { title: 'AI 技能', value: stats.value.skillCount.toLocaleString(), icon: 'MagicStick', color: '#F56C6C' },
  { title: 'AI 对话', value: stats.value.conversationCount.toLocaleString(), icon: 'ChatDotRound', color: '#909399' },
])

// --- Chart data ---
const trendData = ref<{ dates: string[]; values: number[] }>({ dates: [], values: [] })
const distributionData = ref<{
  byType: { name: string; value: number }[]
  byPriority: { name: string; value: number }[]
  bySystem: { name: string; value: number }[]
}>({ byType: [], byPriority: [], bySystem: [] })

const pieDimension = ref<'type' | 'priority' | 'system'>('type')

const trendChartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 40, right: 20, top: 20, bottom: 30 },
  xAxis: {
    type: 'category',
    data: trendData.value.dates,
    axisLabel: { fontSize: 12 },
  },
  yAxis: {
    type: 'value',
    minInterval: 1,
    axisLabel: { fontSize: 12 },
  },
  series: [
    {
      name: '新增用例',
      type: 'line',
      data: trendData.value.values,
      smooth: true,
      areaStyle: { color: 'rgba(64,158,255,0.15)' },
      lineStyle: { color: '#409EFF', width: 2 },
      itemStyle: { color: '#409EFF' },
    },
  ],
}))

const pieColors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#b37feb', '#36cfc9', '#ff85c0']

const pieChartOption = computed(() => {
  const dimKey = pieDimension.value === 'type' ? 'byType' : pieDimension.value === 'priority' ? 'byPriority' : 'bySystem'
  const data = distributionData.value[dimKey] || []
  return {
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, left: 'center', textStyle: { fontSize: 12 } },
    color: pieColors,
    series: [
      {
        type: 'pie',
        radius: ['40%', '65%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: true,
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        label: { show: true, formatter: '{b}\n{c}' },
        data: data.length > 0 ? data : [{ name: '暂无数据', value: 0 }],
      },
    ],
  }
})

onMounted(async () => {
  try {
    const res = await getDashboardStats()
    stats.value = res.data || res
  } catch {
    // stats remain at 0
  }
  // Fetch chart data
  try {
    const [trendRes, distRes]: any[] = await Promise.all([
      (await import('@/utils/request')).default.get('/dashboard/chart/execution-trend'),
      (await import('@/utils/request')).default.get('/dashboard/chart/case-distribution'),
    ])
    trendData.value = trendRes.data || trendRes
    distributionData.value = distRes.data || distRes
  } catch {
    // chart data not critical
  }
})

const recentActivities = ref([
  {
    time: '2026-06-12 14:30',
    user: '张三',
    action: '执行测试套件',
    target: '回归测试套件 v2.1',
    status: '通过',
    statusType: 'success' as const,
  },
  {
    time: '2026-06-12 13:15',
    user: '李四',
    action: '创建测试用例',
    target: '登录模块 - 异常场景',
    status: '待审核',
    statusType: 'warning' as const,
  },
  {
    time: '2026-06-12 11:42',
    user: '王五',
    action: '提交缺陷',
    target: '订单支付超时问题',
    status: '已确认',
    statusType: 'danger' as const,
  },
  {
    time: '2026-06-12 10:20',
    user: 'AI 助手',
    action: '自动生成用例',
    target: '用户管理模块',
    status: '完成',
    statusType: 'success' as const,
  },
  {
    time: '2026-06-12 09:05',
    user: '赵六',
    action: '录制操作回放',
    target: '购物车流程',
    status: '失败',
    statusType: 'danger' as const,
  },
])
</script>

<style scoped lang="scss">
.dashboard-container {
  max-width: 1400px;
}

.welcome-section {
  margin-bottom: 24px;

  h2 {
    font-size: 22px;
    color: var(--el-text-color-primary);
    margin-bottom: 4px;
  }

  .welcome-desc {
    color: var(--el-text-color-secondary);
    font-size: 14px;
  }
}

.stat-cards {
  margin-bottom: 16px;

  .stat-card {
    margin-bottom: 16px;

    .stat-content {
      display: flex;
      align-items: center;
      justify-content: space-between;
    }

    .stat-title {
      font-size: 13px;
      color: var(--el-text-color-secondary);
      margin-bottom: 8px;
    }

    .stat-value {
      font-size: 24px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }
  }
}

.dashboard-grid {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>
