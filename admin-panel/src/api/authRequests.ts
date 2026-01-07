import { apiClient } from './client'
import { PagedResponse } from '../types/api'

export interface AuthRequest {
  id: number
  traceId: string
  telegramUserId: number
  requestedAt: string
  code: string | null
}

export const authRequestsApi = {
  getAuthRequests: async (
    page: number = 0,
    size: number = 20,
    userId?: number,
    traceId?: string
  ): Promise<PagedResponse<AuthRequest>> => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    })
    if (userId) params.append('userId', userId.toString())
    if (traceId) params.append('traceId', traceId)

    const response = await apiClient.get<PagedResponse<AuthRequest>>(
      `/api/admin/auth-requests?${params.toString()}`
    )
    return response.data
  },

  getAuthRequestByTraceId: async (traceId: string): Promise<AuthRequest> => {
    const response = await apiClient.get<AuthRequest>(`/api/admin/auth-requests/${traceId}`)
    return response.data
  },

  getStats: async () => {
    const response = await apiClient.get('/api/admin/auth-requests/stats')
    return response.data
  },
}

