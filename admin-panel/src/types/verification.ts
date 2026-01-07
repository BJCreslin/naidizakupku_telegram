export interface VerificationSession {
  id: number
  correlationId: string
  telegramUserId: number
  code: string
  browserInfo: string
  status: 'PENDING' | 'CONFIRMED' | 'REVOKED'
  createdAt: string
  updatedAt: string
}

