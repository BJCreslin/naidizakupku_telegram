package com.naidizakupku.telegram.domain

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

/**
 * Entity для хранения запросов аутентификации
 * Используется для отслеживания уникальных запросов
 */
@Entity
@Table(name = "auth_requests")
data class AuthRequest(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    /**
     * Уникальный идентификатор запроса (UUID из header X-Trace-Id)
     */
    @Column(name = "trace_id", nullable = false, unique = true)
    val traceId: UUID,
    
    /**
     * ID пользователя Telegram
     */
    @Column(name = "telegram_user_id", nullable = false)
    val telegramUserId: Long,
    
    /**
     * Время создания запроса
     */
    @Column(name = "requested_at", nullable = false)
    val requestedAt: LocalDateTime = LocalDateTime.now()
)

