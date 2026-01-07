import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { codesApi } from '../api/codes'
import { Code } from '../types/code'
import { PagedResponse } from '../types/api'
import { message } from 'antd'

export const useCodes = (
  page: number = 0,
  size: number = 20,
  userId?: number,
  active?: boolean,
  expired?: boolean
) => {
  return useQuery<PagedResponse<Code>>({
    queryKey: ['codes', page, size, userId, active, expired],
    queryFn: () => codesApi.getCodes(page, size, userId, active, expired),
  })
}

export const useCode = (id: number) => {
  return useQuery<Code>({
    queryKey: ['code', id],
    queryFn: () => codesApi.getCodeById(id),
    enabled: !!id,
  })
}

export const useDeleteCode = () => {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: (id: number) => codesApi.deleteCode(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['codes'] })
      message.success('Код удален')
    },
    onError: () => {
      message.error('Ошибка при удалении кода')
    },
  })
}

export const useCodeStats = () => {
  return useQuery({
    queryKey: ['codes', 'stats'],
    queryFn: () => codesApi.getCodeStats(),
  })
}
