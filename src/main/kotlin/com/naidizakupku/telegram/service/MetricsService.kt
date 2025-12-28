package com.naidizakupku.telegram.service

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

/**
 * Сервис для кастомных метрик приложения
 */
@Service
class MetricsService(private val meterRegistry: MeterRegistry) {

    // Метрики для кодов
    private val codeGeneratedCounter: Counter = Counter.builder("telegram.codes.generated")
        .description("Количество сгенерированных кодов")
        .register(meterRegistry)

    private val codeVerifiedCounter: Counter = Counter.builder("telegram.codes.verified")
        .description("Количество проверенных кодов")
        .tag("result", "success")
        .register(meterRegistry)

    private val codeVerificationFailedCounter: Counter = Counter.builder("telegram.codes.verified")
        .description("Количество неудачных проверок кодов")
        .tag("result", "failed")
        .register(meterRegistry)

    private val codeExpiredCounter: Counter = Counter.builder("telegram.codes.expired")
        .description("Количество просроченных кодов")
        .register(meterRegistry)

    // Метрики для верификации
    private val verificationRequestCounter: Counter = Counter.builder("telegram.verification.requests")
        .description("Количество запросов верификации")
        .register(meterRegistry)

    private val verificationConfirmedCounter: Counter = Counter.builder("telegram.verification.confirmed")
        .description("Количество подтвержденных верификаций")
        .register(meterRegistry)

    private val verificationRevokedCounter: Counter = Counter.builder("telegram.verification.revoked")
        .description("Количество отозванных верификаций")
        .register(meterRegistry)

    // Метрики для Telegram
    private val telegramMessageSentCounter: Counter = Counter.builder("telegram.messages.sent")
        .description("Количество отправленных сообщений в Telegram")
        .register(meterRegistry)

    private val telegramMessageFailedCounter: Counter = Counter.builder("telegram.messages.failed")
        .description("Количество неудачных отправок в Telegram")
        .register(meterRegistry)

    // Метрики для Kafka
    private val kafkaMessageSentCounter: Counter = Counter.builder("kafka.messages.sent")
        .description("Количество отправленных сообщений в Kafka")
        .register(meterRegistry)

    private val kafkaMessageReceivedCounter: Counter = Counter.builder("kafka.messages.received")
        .description("Количество полученных сообщений из Kafka")
        .register(meterRegistry)

    // Таймеры
    private val codeGenerationTimer: Timer = Timer.builder("telegram.codes.generation.time")
        .description("Время генерации кода")
        .register(meterRegistry)

    private val codeVerificationTimer: Timer = Timer.builder("telegram.codes.verification.time")
        .description("Время проверки кода")
        .register(meterRegistry)

    private val telegramSendTimer: Timer = Timer.builder("telegram.messages.send.time")
        .description("Время отправки сообщения в Telegram")
        .register(meterRegistry)

    // Методы для кодов
    fun incrementCodeGenerated() {
        codeGeneratedCounter.increment()
    }

    fun incrementCodeVerified(success: Boolean) {
        if (success) {
            codeVerifiedCounter.increment()
        } else {
            codeVerificationFailedCounter.increment()
        }
    }

    fun incrementCodeExpired() {
        codeExpiredCounter.increment()
    }

    fun recordCodeGenerationTime(duration: Long, unit: TimeUnit) {
        codeGenerationTimer.record(duration, unit)
    }

    fun recordCodeVerificationTime(duration: Long, unit: TimeUnit) {
        codeVerificationTimer.record(duration, unit)
    }

    // Методы для верификации
    fun incrementVerificationRequest() {
        verificationRequestCounter.increment()
    }

    fun incrementVerificationConfirmed() {
        verificationConfirmedCounter.increment()
    }

    fun incrementVerificationRevoked() {
        verificationRevokedCounter.increment()
    }

    // Методы для Telegram
    fun incrementTelegramMessageSent() {
        telegramMessageSentCounter.increment()
    }

    fun incrementTelegramMessageFailed() {
        telegramMessageFailedCounter.increment()
    }

    fun recordTelegramSendTime(duration: Long, unit: TimeUnit) {
        telegramSendTimer.record(duration, unit)
    }

    // Методы для Kafka
    fun incrementKafkaMessageSent() {
        kafkaMessageSentCounter.increment()
    }

    fun incrementKafkaMessageReceived() {
        kafkaMessageReceivedCounter.increment()
    }
}

