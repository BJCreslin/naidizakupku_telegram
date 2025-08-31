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
    
    @Column(name = "first_name")
    val firstName: String? = null,
    
    @Column(name = "last_name")
    val lastName: String? = null,
    
    @Column(name = "username")
    val username: String? = null,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    val active: Boolean = true
)

