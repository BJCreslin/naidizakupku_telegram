import { apiClient } from './client'

export const settingsApi = {
  getSettings: async (): Promise<Record<string, any>> => {
    const response = await apiClient.get('/api/admin/settings')
    return response.data
  },

  updateSettings: async (settings: Record<string, any>): Promise<void> => {
    await apiClient.put('/api/admin/settings', settings)
  },
}

