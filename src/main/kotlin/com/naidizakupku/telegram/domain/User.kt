package com.naidizakupku.telegram.domain

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * Доменная модель пользователя
 */
@Entity
@Table(name = "users")
@Schema(description = "Модель пользователя Telegram бота")
data class User(
    @Schema(description = "Уникальный идентификатор пользователя в системе", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Schema(description = "ID пользователя в Telegram", example = "123456789", required = true)
    @Column(unique = true, nullable = false)
    val telegramId: Long,
    
    @Schema(description = "Имя пользователя", example = "Иван", required = false)
    @Column(name = "first_name")
    val firstName: String? = null,
    
    @Schema(description = "Фамилия пользователя", example = "Петров", required = false)
    @Column(name = "last_name")
    val lastName: String? = null,
    
    @Schema(description = "Username пользователя в Telegram", example = "ivan_petrov", required = false)
    @Column(name = "username")
    val username: String? = null,
    
    @Schema(description = "Дата и время создания записи", example = "2024-01-01T15:30:00")
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Schema(description = "Дата и время последнего обновления", example = "2024-01-01T15:30:00")
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Schema(description = "Статус активности пользователя", example = "true", defaultValue = "true")
    @Column(nullable = false)
    val active: Boolean = true
)

