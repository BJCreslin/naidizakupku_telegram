package com.naidizakupku.telegram.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

/**
 * Сервис для работы с Kafka
 */
@Service
@ConditionalOnProperty(name = ["spring.kafka.bootstrap-servers"])
class KafkaService(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {
    
    private val logger = LoggerFactory.getLogger(KafkaService::class.java)
    
    /**
     * Отправляет сообщение в топик
     */
    suspend fun sendMessage(topic: String, key: String, message: Any): CompletableFuture<SendResult<String, String>> {
        val jsonMessage = objectMapper.writeValueAsString(message)
        logger.info("Отправляем сообщение в топик $topic с ключом $key: $jsonMessage")
        
        return kafkaTemplate.send(topic, key, jsonMessage)
            .whenComplete { result, throwable ->
                if (throwable != null) {
                    logger.error("Ошибка отправки сообщения в топик $topic", throwable)
                } else {
                    logger.info("Сообщение успешно отправлено в топик $topic, partition: ${result.recordMetadata.partition()}, offset: ${result.recordMetadata.offset()}")
                }
            }
    }
    
    /**
     * Отправляет событие пользователя
     */
    suspend fun sendUserEvent(userId: Long, eventType: String, data: Map<String, Any>) {
        val event = mapOf(
            "userId" to userId,
            "eventType" to eventType,
            "timestamp" to System.currentTimeMillis(),
            "data" to data
        )
        
        sendMessage("user-events", userId.toString(), event)
    }
    
    /**
     * Отправляет уведомление
     */
    suspend fun sendNotification(userId: Long, message: String, type: String = "info") {
        val notification = mapOf(
            "userId" to userId,
            "message" to message,
            "type" to type,
            "timestamp" to System.currentTimeMillis()
        )
        
        sendMessage("notifications", userId.toString(), notification)
    }
}
