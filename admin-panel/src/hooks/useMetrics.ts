import { useQuery } from '@tanstack/react-query'
import { metricsApi } from '../api/metrics'
import { Metrics } from '../types/metrics'

export const useDashboardMetrics = () => {
  return useQuery<Metrics>({
    queryKey: ['metrics', 'dashboard'],
    queryFn: () => metricsApi.getDashboardMetrics(),
    refetchInterval: 30000, // Обновление каждые 30 секунд
  })
}

export const useCodeMetrics = (period: string = '24h') => {
  return useQuery({
    queryKey: ['metrics', 'codes', period],
    queryFn: () => metricsApi.getCodeMetrics(period),
  })
}

export const useVerificationMetrics = (period: string = '24h') => {
  return useQuery({
    queryKey: ['metrics', 'verification', period],
    queryFn: () => metricsApi.getVerificationMetrics(period),
  })
}

export const useTelegramMetrics = (period: string = '24h') => {
  return useQuery({
    queryKey: ['metrics', 'telegram', period],
    queryFn: () => metricsApi.getTelegramMetrics(period),
  })
}

export const useKafkaMetrics = (period: string = '24h') => {
  return useQuery({
    queryKey: ['metrics', 'kafka', period],
    queryFn: () => metricsApi.getKafkaMetrics(period),
  })
}
