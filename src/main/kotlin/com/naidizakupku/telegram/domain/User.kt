package com.naidizakupku.telegram.domain

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * Доменная модель пользователя
 */
@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(unique = true, nullable = false)
    val telegramId: Long,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    val active: Boolean = true
)

