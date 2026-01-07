package com.naidizakupku.telegram.service.admin

import com.naidizakupku.telegram.domain.dto.admin.*
import com.naidizakupku.telegram.domain.entity.VerificationStatus
import com.naidizakupku.telegram.repository.VerificationSessionRepository
import com.naidizakupku.telegram.service.MetricsService
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Service
import java.time.Instant

/**
 * Сервис для агрегации метрик для админки
 */
@Service
class AdminMetricsService(
    private val meterRegistry: MeterRegistry,
    private val metricsService: MetricsService,
    private val verificationSessionRepository: VerificationSessionRepository
) {

    /**
     * Получить метрики для dashboard
     */
    fun getDashboardMetrics(): MetricsDto {
        val codesMetrics = getCodeMetrics()
        val verificationMetrics = getVerificationMetrics()
        val telegramMetrics = getTelegramMetrics()
        val kafkaMetrics = getKafkaMetrics()

        return MetricsDto(
            codes = codesMetrics,
            verification = verificationMetrics,
            telegram = telegramMetrics,
            kafka = kafkaMetrics,
            timestamp = Instant.now()
        )
    }

    /**
     * Получить метрики кодов
     */
    fun getCodeMetrics(): CodeMetrics {
        val generated = getCounterValue("telegram.codes.generated")
        val verified = getCounterValue("telegram.codes.verified", "result", "success")
        val verificationFailed = getCounterValue("telegram.codes.verified", "result", "failed")
        val expired = getCounterValue("telegram.codes.expired")
        
        val avgGenerationTime = getTimerMean("telegram.codes.generation.time")
        val avgVerificationTime = getTimerMean("telegram.codes.verification.time")

        return CodeMetrics(
            generated = generated,
            verified = verified,
            verificationFailed = verificationFailed,
            expired = expired,
            avgGenerationTime = avgGenerationTime,
            avgVerificationTime = avgVerificationTime
        )
    }

    /**
     * Получить метрики верификации
     */
    fun getVerificationMetrics(): VerificationMetrics {
        val requests = getCounterValue("telegram.verification.requests")
        val confirmed = getCounterValue("telegram.verification.confirmed")
        val revoked = getCounterValue("telegram.verification.revoked")
        
        // Количество активных сессий из БД
        val activeSessions = verificationSessionRepository.findAll()
            .count { it.status == VerificationStatus.PENDING }
            .toLong()

        return VerificationMetrics(
            requests = requests,
            confirmed = confirmed,
            revoked = revoked,
            activeSessions = activeSessions
        )
    }

    /**
     * Получить метрики Telegram
     */
    fun getTelegramMetrics(): TelegramMetrics {
        val messagesSent = getCounterValue("telegram.messages.sent")
        val messagesFailed = getCounterValue("telegram.messages.failed")
        val avgSendTime = getTimerMean("telegram.messages.send.time")
        
        val total = messagesSent + messagesFailed
        val successRate = if (total > 0) {
            (messagesSent.toDouble() / total.toDouble()) * 100.0
        } else {
            0.0
        }

        return TelegramMetrics(
            messagesSent = messagesSent,
            messagesFailed = messagesFailed,
            avgSendTime = avgSendTime,
            successRate = successRate
        )
    }

    /**
     * Получить метрики Kafka
     */
    fun getKafkaMetrics(): KafkaMetrics {
        val messagesSent = getCounterValue("kafka.messages.sent")
        val messagesReceived = getCounterValue("kafka.messages.received")

        return KafkaMetrics(
            messagesSent = messagesSent,
            messagesReceived = messagesReceived
        )
    }

    /**
     * Получить значение счетчика по имени
     */
    private fun getCounterValue(name: String, tagKey: String? = null, tagValue: String? = null): Long {
        return try {
            val counter = if (tagKey != null && tagValue != null) {
                meterRegistry.counter(name, tagKey, tagValue)
            } else {
                meterRegistry.counter(name)
            }
            counter.count().toLong()
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Получить среднее значение таймера
     */
    private fun getTimerMean(name: String): Double {
        return try {
            val timer = meterRegistry.find(name).timer()
            timer?.mean(java.util.concurrent.TimeUnit.MILLISECONDS) ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }
}

