export interface Metrics {
  codes: CodeMetrics
  verification: VerificationMetrics
  telegram: TelegramMetrics
  kafka: KafkaMetrics
  timestamp: string
}

export interface CodeMetrics {
  generated: number
  verified: number
  verificationFailed: number
  expired: number
  avgGenerationTime: number
  avgVerificationTime: number
}

export interface VerificationMetrics {
  requests: number
  confirmed: number
  revoked: number
  activeSessions: number
}

export interface TelegramMetrics {
  messagesSent: number
  messagesFailed: number
  avgSendTime: number
  successRate: number
}

export interface KafkaMetrics {
  messagesSent: number
  messagesReceived: number
}

