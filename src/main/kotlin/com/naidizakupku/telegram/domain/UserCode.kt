package com.naidizakupku.telegram.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "user_codes")
data class UserCode(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(name = "code", length = 7, nullable = false, unique = true)
    val code: String,
    
    @Column(name = "telegram_user_id", nullable = false)
    val telegramUserId: Long,
    
    @Column(name = "expires_at", nullable = false)
    val expiresAt: LocalDateTime,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    fun isExpired(): Boolean = LocalDateTime.now().isAfter(expiresAt)
    
    fun isActive(): Boolean = !isExpired()
}
