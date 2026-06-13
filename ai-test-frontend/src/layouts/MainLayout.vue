<template>
  <div class="main-layout">
    <!-- Sidebar -->
    <aside class="sidebar" :class="{ collapsed: appStore.sidebarCollapsed }">
      <div class="logo">
        <el-icon :size="24"><Monitor /></el-icon>
        <span v-show="!appStore.sidebarCollapsed" class="logo-text">AI 测试平台</span>
      </div>
      <el-scrollbar>
        <el-menu
          :default-active="activeMenu"
          :collapse="appStore.sidebarCollapsed"
          :collapse-transition="false"
          router
          class="sidebar-menu"
        >
          <el-menu-item index="/dashboard">
            <el-icon><Odometer /></el-icon>
            <template #title>工作台</template>
          </el-menu-item>

          <el-sub-menu index="test">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>测试管理</span>
            </template>
            <el-menu-item index="/cases">测试用例</el-menu-item>
            <el-menu-item index="/suites">用例套件</el-menu-item>
            <el-menu-item index="/executions">执行记录</el-menu-item>
          </el-sub-menu>

          <el-menu-item index="/knowledge">
            <el-icon><Collection /></el-icon>
            <template #title>知识库</template>
          </el-menu-item>

          <el-menu-item index="/defects">
            <el-icon><WarningFilled /></el-icon>
            <template #title>缺陷管理</template>
          </el-menu-item>

          <el-sub-menu index="ai">
            <template #title>
              <el-icon><MagicStick /></el-icon>
              <span>AI 能力</span>
            </template>
            <el-menu-item index="/skills">AI 技能</el-menu-item>
            <el-menu-item index="/chat">AI 对话</el-menu-item>
            <el-menu-item index="/record">录制回放</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="system">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/systems">系统管理</el-menu-item>
            <el-menu-item index="/users">用户管理</el-menu-item>
            <el-menu-item index="/roles">角色管理</el-menu-item>
            <el-menu-item index="/settings">系统设置</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-scrollbar>
    </aside>

    <!-- Main area -->
    <div class="main-area">
      <!-- Header -->
      <header class="header">
        <div class="header-left">
          <el-icon
            class="collapse-btn"
            :size="20"
            @click="appStore.toggleSidebar"
          >
            <Expand v-if="appStore.sidebarCollapsed" />
            <Fold v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRoute?.meta?.title">
              {{ currentRoute.meta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-tooltip :content="appStore.isDark ? '切换亮色模式' : '切换暗色模式'">
            <el-icon class="header-icon" :size="18" @click="appStore.toggleTheme">
              <Moon v-if="!appStore.isDark" />
              <Sunny v-else />
            </el-icon>
          </el-tooltip>

          <el-badge :value="3" :max="99" class="notification-badge">
            <el-icon class="header-icon" :size="18">
              <Bell />
            </el-icon>
          </el-badge>

          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" icon="UserFilled" />
              <span class="username">{{ userStore.userInfo?.nickname || 'Admin' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="settings">系统设置</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- Content -->
      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/store/app'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const currentRoute = computed(() => route)

function handleCommand(command: string) {
  switch (command) {
    case 'profile':
      break
    case 'settings':
      router.push('/settings')
      break
    case 'logout':
      userStore.logout()
      break
  }
}
</script>

<style scoped lang="scss">
.main-layout {
  display: flex;
  height: 100vh;
  width: 100%;
  overflow: hidden;
}

.sidebar {
  width: 220px;
  height: 100vh;
  background: var(--el-bg-color);
  border-right: 1px solid var(--el-border-color-light);
  display: flex;
  flex-direction: column;
  transition: width 0.3s;
  overflow: hidden;
  flex-shrink: 0;

  &.collapsed {
    width: 64px;
  }

  .logo {
    height: 56px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    border-bottom: 1px solid var(--el-border-color-light);
    padding: 0 16px;
    flex-shrink: 0;

    .logo-text {
      font-size: 16px;
      font-weight: 600;
      white-space: nowrap;
      color: var(--el-text-color-primary);
    }
  }

  .sidebar-menu {
    border-right: none;
    height: calc(100vh - 56px);
  }
}

.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}

.header {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color-light);
  flex-shrink: 0;

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;

    .collapse-btn {
      cursor: pointer;
      color: var(--el-text-color-regular);

      &:hover {
        color: var(--el-color-primary);
      }
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 20px;

    .header-icon {
      cursor: pointer;
      color: var(--el-text-color-regular);

      &:hover {
        color: var(--el-color-primary);
      }
    }

    .notification-badge {
      line-height: 1;
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;

      .username {
        font-size: 14px;
        color: var(--el-text-color-primary);
      }
    }
  }
}

.content {
  flex: 1;
  padding: 20px;
  overflow: auto;
  background: var(--el-fill-color-light);
}
</style>
