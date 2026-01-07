package com.naidizakupku.telegram.domain.dto.admin

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * DTO для пользователя Telegram бота (для админки)
 */
@Schema(description = "Пользователь Telegram бота")
data class UserDto(
    @Schema(description = "ID пользователя", example = "1", required = true)
    val id: Long,
    
    @Schema(description = "ID пользователя в Telegram", example = "123456789", required = true)
    val telegramId: Long,
    
    @Schema(description = "Имя пользователя", example = "Иван", required = false)
    val firstName: String?,
    
    @Schema(description = "Фамилия пользователя", example = "Петров", required = false)
    val lastName: String?,
    
    @Schema(description = "Username пользователя в Telegram", example = "ivan_petrov", required = false)
    val username: String?,
    
    @Schema(description = "Дата создания", example = "2024-01-01T15:30:00", required = true)
    val createdAt: LocalDateTime,
    
    @Schema(description = "Дата последнего обновления", example = "2024-01-01T15:30:00", required = true)
    val updatedAt: LocalDateTime,
    
    @Schema(description = "Активен ли пользователь", example = "true", required = true)
    val active: Boolean
)

