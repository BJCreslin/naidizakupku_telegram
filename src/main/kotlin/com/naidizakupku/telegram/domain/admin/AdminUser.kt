package com.naidizakupku.telegram.domain.admin

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * Доменная модель пользователя админки
 */
@Entity
@Table(name = "admin_users")
data class AdminUser(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(name = "username", nullable = false, unique = true, length = 100)
    val username: String,
    
    @Column(name = "password_hash", nullable = false)
    val passwordHash: String,
    
    @Column(name = "email", unique = true)
    val email: String? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    val role: AdminRole = AdminRole.VIEWER,
    
    @Column(name = "active", nullable = false)
    val active: Boolean = true,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

