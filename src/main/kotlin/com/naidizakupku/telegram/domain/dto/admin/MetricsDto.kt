package com.naidizakupku.telegram.domain.dto.admin

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

/**
 * DTO для метрик
 */
@Schema(description = "Метрики приложения")
data class MetricsDto(
    @Schema(description = "Метрики кодов", required = true)
    val codes: CodeMetrics,
    
    @Schema(description = "Метрики верификации", required = true)
    val verification: VerificationMetrics,
    
    @Schema(description = "Метрики Telegram", required = true)
    val telegram: TelegramMetrics,
    
    @Schema(description = "Метрики Kafka", required = true)
    val kafka: KafkaMetrics,
    
    @Schema(description = "Время получения метрик", required = true)
    val timestamp: Instant
)

/**
 * Метрики кодов
 */
@Schema(description = "Метрики кодов")
data class CodeMetrics(
    @Schema(description = "Количество сгенерированных кодов", example = "100", required = true)
    val generated: Long,
    
    @Schema(description = "Количество проверенных кодов", example = "80", required = true)
    val verified: Long,
    
    @Schema(description = "Количество неудачных проверок", example = "5", required = true)
    val verificationFailed: Long,
    
    @Schema(description = "Количество просроченных кодов", example = "15", required = true)
    val expired: Long,
    
    @Schema(description = "Среднее время генерации кода (мс)", example = "10.5", required = true)
    val avgGenerationTime: Double,
    
    @Schema(description = "Среднее время проверки кода (мс)", example = "5.2", required = true)
    val avgVerificationTime: Double
)

/**
 * Метрики верификации
 */
@Schema(description = "Метрики верификации")
data class VerificationMetrics(
    @Schema(description = "Количество запросов верификации", example = "50", required = true)
    val requests: Long,
    
    @Schema(description = "Количество подтвержденных верификаций", example = "40", required = true)
    val confirmed: Long,
    
    @Schema(description = "Количество отозванных верификаций", example = "5", required = true)
    val revoked: Long,
    
    @Schema(description = "Количество активных сессий", example = "5", required = true)
    val activeSessions: Long
)

/**
 * Метрики Telegram
 */
@Schema(description = "Метрики Telegram")
data class TelegramMetrics(
    @Schema(description = "Количество отправленных сообщений", example = "200", required = true)
    val messagesSent: Long,
    
    @Schema(description = "Количество неудачных отправок", example = "2", required = true)
    val messagesFailed: Long,
    
    @Schema(description = "Среднее время отправки сообщения (мс)", example = "150.5", required = true)
    val avgSendTime: Double,
    
    @Schema(description = "Процент успешных отправок", example = "99.0", required = true)
    val successRate: Double
)

/**
 * Метрики Kafka
 */
@Schema(description = "Метрики Kafka")
data class KafkaMetrics(
    @Schema(description = "Количество отправленных сообщений", example = "500", required = true)
    val messagesSent: Long,
    
    @Schema(description = "Количество полученных сообщений", example = "480", required = true)
    val messagesReceived: Long
)

