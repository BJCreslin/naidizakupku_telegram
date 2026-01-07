package com.naidizakupku.telegram.domain.dto.admin

import com.fasterxml.jackson.annotation.JsonRawValue
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.UUID

/**
 * DTO для сессии верификации
 */
@Schema(description = "Сессия верификации")
data class AdminVerificationDto(
    @Schema(description = "ID сессии", example = "1", required = true)
    val id: Long,
    
    @Schema(description = "Correlation ID (UUID)", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
    val correlationId: UUID,
    
    @Schema(description = "ID пользователя Telegram", example = "123456789", required = true)
    val telegramUserId: Long,
    
    @Schema(description = "Код верификации", example = "1234567", required = true)
    val code: String,
    
    @Schema(description = "Информация о браузере (JSON)", example = "{\"ip\":\"192.168.1.1\",\"userAgent\":\"Chrome\"}", required = true)
    @JsonRawValue
    val browserInfo: String,
    
    @Schema(description = "Статус верификации", example = "PENDING", required = true)
    val status: String,
    
    @Schema(description = "Дата создания", example = "2024-01-01T15:30:00Z", required = true)
    val createdAt: Instant,
    
    @Schema(description = "Дата последнего обновления", example = "2024-01-01T15:35:00Z", required = true)
    val updatedAt: Instant
)

