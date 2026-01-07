import { useQuery } from '@tanstack/react-query'
import { verificationApi } from '../api/verification'
import { VerificationSession } from '../types/verification'
import { PagedResponse } from '../types/api'

export const useVerificationSessions = (
  page: number = 0,
  size: number = 20,
  status?: string,
  userId?: number
) => {
  return useQuery<PagedResponse<VerificationSession>>({
    queryKey: ['verification-sessions', page, size, status, userId],
    queryFn: () => verificationApi.getSessions(page, size, status, userId),
  })
}

export const useVerificationSession = (correlationId: string) => {
  return useQuery<VerificationSession>({
    queryKey: ['verification-session', correlationId],
    queryFn: () => verificationApi.getSessionByCorrelationId(correlationId),
    enabled: !!correlationId,
  })
}

export const useVerificationStats = () => {
  return useQuery({
    queryKey: ['verification', 'stats'],
    queryFn: () => verificationApi.getStats(),
  })
}

