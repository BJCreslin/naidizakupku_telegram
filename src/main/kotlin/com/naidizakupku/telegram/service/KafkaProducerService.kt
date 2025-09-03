package com.naidizakupku.telegram.service

import com.naidizakupku.telegram.domain.dto.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CompletableFuture

@Service
class KafkaProducerService(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {
    
    private val logger = LoggerFactory.getLogger(KafkaProducerService::class.java)
    
    @Value("\${kafka.topics.verification-response:code-verification-response}")
    private lateinit var verificationResponseTopic: String
    
    @Value("\${kafka.topics.revoke-request:authorization-revoke-request}")
    private lateinit var revokeRequestTopic: String
    
    fun sendVerificationResponse(response: CodeVerificationResponseDto): CompletableFuture<SendResult<String, Any>> {
        logger.info("Отправка ответа верификации: correlationId=${response.correlationId}, success=${response.success}")
        
        return kafkaTemplate.send(verificationResponseTopic, response.correlationId.toString(), response)
            .completable()
            .whenComplete { result, throwable ->
                if (throwable != null) {
                    logger.error("Ошибка отправки ответа верификации: ${throwable.message}", throwable)
                } else {
                    logger.info("Ответ верификации отправлен успешно: ${result?.producerRecord?.key()}")
                }
            }
    }
    
    fun sendRevokeRequest(request: AuthorizationRevokeRequestDto): CompletableFuture<SendResult<String, Any>> {
        logger.info("Отправка запроса отзыва авторизации: correlationId=${request.correlationId}")
        
        return kafkaTemplate.send(revokeRequestTopic, request.correlationId.toString(), request)
            .completable()
            .whenComplete { result, throwable ->
                if (throwable != null) {
                    logger.error("Ошибка отправки запроса отзыва: ${throwable.message}", throwable)
                } else {
                    logger.info("Запрос отзыва отправлен успешно: ${result?.producerRecord?.key()}")
                }
            }
    }
    
    fun sendVerificationResponse(
        correlationId: UUID,
        success: Boolean,
        telegramUserId: Long?,
        message: String
    ): CompletableFuture<SendResult<String, Any>> {
        val response = CodeVerificationResponseDto(
            correlationId = correlationId,
            success = success,
            telegramUserId = telegramUserId,
            message = message,
            timestamp = Instant.now()
        )
        return sendVerificationResponse(response)
    }
    
    fun sendRevokeRequest(
        correlationId: UUID,
        telegramUserId: Long,
        originalVerificationCorrelationId: UUID,
        reason: String
    ): CompletableFuture<SendResult<String, Any>> {
        val request = AuthorizationRevokeRequestDto(
            correlationId = correlationId,
            telegramUserId = telegramUserId,
            originalVerificationCorrelationId = originalVerificationCorrelationId,
            reason = reason,
            timestamp = Instant.now()
        )
        return sendRevokeRequest(request)
    }
}
