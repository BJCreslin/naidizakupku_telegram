import { apiClient } from './client'
import { PagedResponse } from '../types/api'

export interface LogEntry {
  id?: string | number
  level: string
  timestamp: string
  logger: string
  message: string
  traceId?: string
  correlationId?: string
  exception?: string
}

export const logsApi = {
  getLogs: async (
    page: number = 0,
    size: number = 50,
    level?: string,
    traceId?: string,
    correlationId?: string
  ): Promise<PagedResponse<LogEntry>> => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    })
    if (level) params.append('level', level)
    if (traceId) params.append('traceId', traceId)
    if (correlationId) params.append('correlationId', correlationId)

    const response = await apiClient.get<PagedResponse<LogEntry>>(
      `/api/admin/logs?${params.toString()}`
    )
    return response.data
  },
}

