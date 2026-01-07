import { create } from 'zustand'

interface UIState {
  sidebarCollapsed: boolean
  theme: 'light' | 'dark'
  setSidebarCollapsed: (collapsed: boolean) => void
  setTheme: (theme: 'light' | 'dark') => void
  toggleSidebar: () => void
}

// Получение темы из localStorage при инициализации
const getInitialTheme = (): 'light' | 'dark' => {
  if (typeof window !== 'undefined') {
    const saved = localStorage.getItem('admin-theme')
    if (saved === 'dark' || saved === 'light') {
      return saved
    }
    // Проверка системной темы
    if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
      return 'dark'
    }
  }
  return 'light'
}

// Получение состояния sidebar из localStorage
const getInitialSidebarState = (): boolean => {
  if (typeof window !== 'undefined') {
    const saved = localStorage.getItem('admin-sidebar-collapsed')
    return saved === 'true'
  }
  return false
}

export const useUIStore = create<UIState>((set) => ({
  sidebarCollapsed: getInitialSidebarState(),
  theme: getInitialTheme(),
  setSidebarCollapsed: (collapsed) => {
    set({ sidebarCollapsed: collapsed })
    if (typeof window !== 'undefined') {
      localStorage.setItem('admin-sidebar-collapsed', String(collapsed))
    }
  },
  setTheme: (theme) => {
    set({ theme })
    if (typeof window !== 'undefined') {
      localStorage.setItem('admin-theme', theme)
    }
  },
  toggleSidebar: () => set((state) => {
    const newState = !state.sidebarCollapsed
    if (typeof window !== 'undefined') {
      localStorage.setItem('admin-sidebar-collapsed', String(newState))
    }
    return { sidebarCollapsed: newState }
  }),
}))

