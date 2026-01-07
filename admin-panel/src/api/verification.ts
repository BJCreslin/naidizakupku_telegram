import { apiClient } from './client'
import { VerificationSession } from '../types/verification'
import { PagedResponse } from '../types/api'

export const verificationApi = {
  getSessions: async (
    page: number = 0,
    size: number = 20,
    status?: string,
    userId?: number
  ): Promise<PagedResponse<VerificationSession>> => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    })
    if (status) params.append('status', status)
    if (userId) params.append('userId', userId.toString())

    const response = await apiClient.get<PagedResponse<VerificationSession>>(
      `/api/admin/verification-sessions?${params.toString()}`
    )
    return response.data
  },

  getSessionByCorrelationId: async (correlationId: string): Promise<VerificationSession> => {
    const response = await apiClient.get<VerificationSession>(
      `/api/admin/verification-sessions/${correlationId}`
    )
    return response.data
  },

  getStats: async () => {
    const response = await apiClient.get('/api/admin/verification-sessions/stats')
    return response.data
  },
}

