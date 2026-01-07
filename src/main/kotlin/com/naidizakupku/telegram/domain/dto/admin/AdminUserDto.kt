package com.naidizakupku.telegram.domain.dto.admin

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * DTO для пользователя админки
 */
@Schema(description = "Пользователь админки")
data class AdminUserDto(
    @Schema(description = "ID пользователя", example = "1", required = true)
    val id: Long,
    
    @Schema(description = "Username", example = "admin", required = true)
    val username: String,
    
    @Schema(description = "Email", example = "admin@example.com", required = false)
    val email: String?,
    
    @Schema(description = "Роль", example = "ADMIN", required = true)
    val role: String,
    
    @Schema(description = "Активен ли пользователь", example = "true", required = true)
    val active: Boolean,
    
    @Schema(description = "Дата создания", example = "2024-01-01T15:30:00", required = true)
    val createdAt: LocalDateTime,
    
    @Schema(description = "Дата последнего обновления", example = "2024-01-01T15:30:00", required = true)
    val updatedAt: LocalDateTime
)

