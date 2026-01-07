package com.naidizakupku.telegram.domain.dto.admin

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * DTO для записи лога
 */
@Schema(description = "Запись лога")
data class LogEntryDto(
    @Schema(description = "Уровень лога", example = "INFO", required = true)
    val level: String,
    
    @Schema(description = "Время записи", example = "2024-01-01T15:30:00", required = true)
    val timestamp: LocalDateTime,
    
    @Schema(description = "Логгер", example = "com.naidizakupku.telegram.service.UserService", required = true)
    val logger: String,
    
    @Schema(description = "Сообщение", example = "User created successfully", required = true)
    val message: String,
    
    @Schema(description = "Trace ID", example = "550e8400-e29b-41d4-a716-446655440000", required = false)
    val traceId: String?,
    
    @Schema(description = "Correlation ID", example = "550e8400-e29b-41d4-a716-446655440000", required = false)
    val correlationId: String?,
    
    @Schema(description = "Исключение (если есть)", required = false)
    val exception: String?
)

