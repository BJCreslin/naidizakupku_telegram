import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { usersApi } from '../api/users'
import { User, PagedResponse } from '../types/user'
import { message } from 'antd'

export const useUsers = (page: number = 0, size: number = 20, search?: string, active?: boolean) => {
  return useQuery<PagedResponse<User>>({
    queryKey: ['users', page, size, search, active],
    queryFn: () => usersApi.getUsers(page, size, search, active),
  })
}

export const useUser = (id: number) => {
  return useQuery<User>({
    queryKey: ['user', id],
    queryFn: () => usersApi.getUserById(id),
    enabled: !!id,
  })
}

export const useActivateUser = () => {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: (id: number) => usersApi.activateUser(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] })
      message.success('Пользователь активирован')
    },
    onError: () => {
      message.error('Ошибка при активации пользователя')
    },
  })
}

export const useDeactivateUser = () => {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: (id: number) => usersApi.deactivateUser(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] })
      message.success('Пользователь деактивирован')
    },
    onError: () => {
      message.error('Ошибка при деактивации пользователя')
    },
  })
}
