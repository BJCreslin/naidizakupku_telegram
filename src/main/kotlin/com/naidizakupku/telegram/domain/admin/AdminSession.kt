package com.naidizakupku.telegram.domain.admin

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * Доменная модель сессии админа
 */
@Entity
@Table(name = "admin_sessions")
data class AdminSession(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(name = "admin_user_id", nullable = false)
    val adminUserId: Long,
    
    @Column(name = "access_token", nullable = false, columnDefinition = "TEXT")
    val accessToken: String,
    
    @Column(name = "refresh_token", nullable = false, unique = true, columnDefinition = "TEXT")
    val refreshToken: String,
    
    @Column(name = "expires_at", nullable = false)
    val expiresAt: LocalDateTime,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Проверяет, истекла ли сессия
     */
    fun isExpired(): Boolean = LocalDateTime.now().isAfter(expiresAt)
    
    /**
     * Проверяет, активна ли сессия
     */
    fun isActive(): Boolean = !isExpired()
}

