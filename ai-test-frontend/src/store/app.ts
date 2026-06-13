import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useDark } from '@vueuse/core'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const isDark = useDark()
  const theme = computed<'light' | 'dark'>(() => isDark.value ? 'dark' : 'light')

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function toggleTheme() {
    isDark.value = !isDark.value
  }

  return {
    sidebarCollapsed,
    theme,
    isDark,
    toggleSidebar,
    toggleTheme,
  }
})
