export interface User {
  id: number
  telegramId: number
  firstName: string | null
  lastName: string | null
  username: string | null
  createdAt: string
  updatedAt: string
  active: boolean
}

export interface PagedResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  hasNext: boolean
  hasPrevious: boolean
}

