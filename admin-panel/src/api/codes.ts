import { apiClient } from './client'
import { Code } from '../types/code'
import { PagedResponse } from '../types/api'

export const codesApi = {
  getCodes: async (
    page: number = 0,
    size: number = 20,
    userId?: number,
    active?: boolean,
    expired?: boolean
  ): Promise<PagedResponse<Code>> => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    })
    if (userId) params.append('userId', userId.toString())
    if (active !== undefined) params.append('active', active.toString())
    if (expired !== undefined) params.append('expired', expired.toString())

    const response = await apiClient.get<PagedResponse<Code>>(
      `/api/admin/codes?${params.toString()}`
    )
    return response.data
  },

  getCodeById: async (id: number): Promise<Code> => {
    const response = await apiClient.get<Code>(`/api/admin/codes/${id}`)
    return response.data
  },

  deleteCode: async (id: number): Promise<void> => {
    await apiClient.delete(`/api/admin/codes/${id}`)
  },

  getCodeStats: async () => {
    const response = await apiClient.get('/api/admin/codes/stats')
    return response.data
  },
}

