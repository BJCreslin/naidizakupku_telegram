package com.naidizakupku.telegram.domain.dto.admin

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * DTO для кода пользователя (расширенная версия для админки)
 */
@Schema(description = "Код пользователя")
data class AdminCodeDto(
    @Schema(description = "ID кода", example = "1", required = true)
    val id: Long,
    
    @Schema(description = "7-значный код", example = "1234567", required = true)
    val code: String,
    
    @Schema(description = "ID пользователя Telegram", example = "123456789", required = true)
    val telegramUserId: Long,
    
    @Schema(description = "Время истечения кода", example = "2024-01-01T15:30:00", required = true)
    val expiresAt: LocalDateTime,
    
    @Schema(description = "Дата создания кода", example = "2024-01-01T15:25:00", required = true)
    val createdAt: LocalDateTime,
    
    @Schema(description = "Активен ли код", example = "true", required = true)
    val isActive: Boolean,
    
    @Schema(description = "Истек ли код", example = "false", required = true)
    val isExpired: Boolean
)

