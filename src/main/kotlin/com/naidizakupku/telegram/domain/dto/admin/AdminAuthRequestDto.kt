package com.naidizakupku.telegram.domain.dto.admin

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

/**
 * DTO для запроса аутентификации
 */
@Schema(description = "Запрос аутентификации")
data class AdminAuthRequestDto(
    @Schema(description = "ID запроса", example = "1", required = true)
    val id: Long,
    
    @Schema(description = "Trace ID (UUID)", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
    val traceId: UUID,
    
    @Schema(description = "ID пользователя Telegram", example = "123456789", required = true)
    val telegramUserId: Long,
    
    @Schema(description = "Время создания запроса", example = "2024-01-01T15:30:00", required = true)
    val requestedAt: LocalDateTime,
    
    @Schema(description = "Код аутентификации", example = "1234567", required = false)
    val code: String?
)

