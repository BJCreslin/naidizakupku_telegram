import { apiClient } from './client'
import { User, PagedResponse } from '../types/user'

export const usersApi = {
  getUsers: async (
    page: number = 0,
    size: number = 20,
    search?: string,
    active?: boolean
  ): Promise<PagedResponse<User>> => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    })
    if (search) params.append('search', search)
    if (active !== undefined) params.append('active', active.toString())

    const response = await apiClient.get<PagedResponse<User>>(
      `/api/admin/users?${params.toString()}`
    )
    return response.data
  },

  getUserById: async (id: number): Promise<User> => {
    const response = await apiClient.get<User>(`/api/admin/users/${id}`)
    return response.data
  },

  activateUser: async (id: number): Promise<User> => {
    const response = await apiClient.put<User>(`/api/admin/users/${id}/activate`)
    return response.data
  },

  deactivateUser: async (id: number): Promise<User> => {
    const response = await apiClient.put<User>(`/api/admin/users/${id}/deactivate`)
    return response.data
  },
}

