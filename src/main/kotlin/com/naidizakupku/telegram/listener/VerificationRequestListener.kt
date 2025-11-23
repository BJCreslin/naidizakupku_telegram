package com.naidizakupku.telegram.listener

import com.fasterxml.jackson.databind.ObjectMapper
import com.naidizakupku.telegram.domain.dto.CodeVerificationRequestDto
import com.naidizakupku.telegram.service.KafkaVerificationService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class VerificationRequestListener(
    private val kafkaVerificationService: KafkaVerificationService,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {
    
    private val logger = LoggerFactory.getLogger(VerificationRequestListener::class.java)
    
    companion object {
        private const val MAX_RETRIES = 3
    }
    
    // Thread-safe map для отслеживания количества попыток для каждого сообщения
    private val retryCountMap = ConcurrentHashMap<String, Int>()
    
    @KafkaListener(
        topics = ["\${kafka.topics.verification-request:code-verification-request}"],
        containerFactory = "verificationKafkaListenerContainerFactory"
    )
    fun handleVerificationRequest(
        @Payload request: CodeVerificationRequestDto,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        acknowledgment: Acknowledgment
    ) {
        logger.info("Получен запрос верификации из Kafka: topic=$topic, partition=$partition, offset=$offset, correlationId=${request.correlationId}")
        
        try {
            // Обрабатываем запрос верификации
            kafkaVerificationService.processVerificationRequest(request)
            
            // Подтверждаем обработку сообщения
            acknowledgment.acknowledge()
            
            // Очищаем счетчик попыток при успешной обработке
            retryCountMap.remove(request.correlationId.toString())
            
            logger.info("Запрос верификации обработан успешно: correlationId=${request.correlationId}")
            
        } catch (e: Exception) {
            logger.error("Ошибка обработки запроса верификации: correlationId=${request.correlationId}, error=${e.message}", e)
            
            val correlationId = request.correlationId.toString()
            val currentRetryCount = retryCountMap.compute(correlationId) { _, value -> (value ?: 0) + 1 } ?: 1
            
            if (currentRetryCount >= MAX_RETRIES) {
                // Отправляем в DLQ после исчерпания попыток
                sendToDLQ(request, e)
                acknowledgment.acknowledge()
                retryCountMap.remove(correlationId)
                logger.warn("Сообщение отправлено в DLQ после $MAX_RETRIES попыток: correlationId=$correlationId")
            } else {
                // Не подтверждаем для повторной попытки
                logger.info("Повторная попытка обработки ($currentRetryCount/$MAX_RETRIES): correlationId=$correlationId")
                throw e
            }
        }
    }
    
    /**
     * Отправляет сообщение в Dead Letter Queue (DLQ)
     */
    private fun sendToDLQ(request: CodeVerificationRequestDto, error: Exception) {
        try {
            val dlqMessage = mapOf(
                "originalRequest" to request,
                "error" to (error.message ?: "Unknown error"),
                "errorType" to error.javaClass.simpleName,
                "timestamp" to System.currentTimeMillis()
            )
            val jsonMessage = objectMapper.writeValueAsString(dlqMessage)
            kafkaTemplate.send("verification-request-dlq", request.correlationId.toString(), jsonMessage)
            logger.warn("Сообщение отправлено в DLQ: correlationId=${request.correlationId}")
        } catch (e: Exception) {
            logger.error("Ошибка отправки в DLQ для correlationId=${request.correlationId}", e)
        }
    }
}
