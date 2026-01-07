import { apiClient } from './client'

export interface LoginRequest {
  username: string
  password: string
}

export interface RefreshTokenRequest {
  refreshToken: string
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  expiresAt: string
  user: {
    id: number
    username: string
    email: string | null
    role: string
  }
}

export interface CurrentUserResponse {
  id: number
  username: string
  email: string | null
  role: string
}

export const authApi = {
  login: async (data: LoginRequest): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>('/api/admin/auth/login', data)
    return response.data
  },

  refreshToken: async (refreshToken: string): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>('/api/admin/auth/refresh', {
      refreshToken,
    })
    return response.data
  },

  logout: async (): Promise<void> => {
    await apiClient.post('/api/admin/auth/logout')
  },

  getCurrentUser: async (): Promise<CurrentUserResponse> => {
    const response = await apiClient.get<CurrentUserResponse>('/api/admin/auth/me')
    return response.data
  },
}

