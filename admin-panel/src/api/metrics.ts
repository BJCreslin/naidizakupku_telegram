import { apiClient } from './client'
import { Metrics, CodeMetrics, VerificationMetrics, TelegramMetrics, KafkaMetrics } from '../types/metrics'

export const metricsApi = {
  getDashboardMetrics: async (): Promise<Metrics> => {
    const response = await apiClient.get<Metrics>('/api/admin/metrics/dashboard')
    return response.data
  },

  getCodeMetrics: async (period: string = '24h'): Promise<CodeMetrics> => {
    const response = await apiClient.get<CodeMetrics>(
      `/api/admin/metrics/codes?period=${period}`
    )
    return response.data
  },

  getVerificationMetrics: async (period: string = '24h'): Promise<VerificationMetrics> => {
    const response = await apiClient.get<VerificationMetrics>(
      `/api/admin/metrics/verification?period=${period}`
    )
    return response.data
  },

  getTelegramMetrics: async (period: string = '24h'): Promise<TelegramMetrics> => {
    const response = await apiClient.get<TelegramMetrics>(
      `/api/admin/metrics/telegram?period=${period}`
    )
    return response.data
  },

  getKafkaMetrics: async (period: string = '24h'): Promise<KafkaMetrics> => {
    const response = await apiClient.get<KafkaMetrics>(
      `/api/admin/metrics/kafka?period=${period}`
    )
    return response.data
  },
}

