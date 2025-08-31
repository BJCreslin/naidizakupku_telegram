package com.naidizakupku.telegram.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service

/**
 * Сервис для потребления сообщений из Kafka
 */
@Service
class KafkaConsumerService(
    private val objectMapper: ObjectMapper
) {
    
    private val logger = LoggerFactory.getLogger(KafkaConsumerService::class.java)
    
    /**
     * Обработчик пользовательских событий
     */
    @KafkaListener(
        topics = ["user-events"],
        groupId = "\${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun handleUserEvent(
        @Payload message: String,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        acknowledgment: Acknowledgment
    ) {
        try {
            logger.info("Получено пользовательское событие из топика $topic, partition: $partition, offset: $offset")
            
            val event = objectMapper.readTree(message)
            val userId = event.get("userId").asLong()
            val eventType = event.get("eventType").asText()
            
            logger.info("Обрабатываем событие пользователя $userId типа '$eventType'")
            
            // Здесь логика обработки события
            when (eventType) {
                "user_registered" -> handleUserRegistered(userId, event)
                "user_login" -> handleUserLogin(userId, event)
                "user_logout" -> handleUserLogout(userId, event)
                else -> logger.warn("Неизвестный тип события: $eventType")
            }
            
            // Подтверждаем обработку сообщения
            acknowledgment.acknowledge()
            logger.info("Событие пользователя $userId успешно обработано")
            
        } catch (e: Exception) {
            logger.error("Ошибка обработки пользовательского события", e)
            // В продакшене здесь можно добавить логику retry или отправки в DLQ
        }
    }
    
    /**
     * Обработчик уведомлений
     */
    @KafkaListener(
        topics = ["notifications"],
        groupId = "\${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun handleNotification(
        @Payload message: String,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        acknowledgment: Acknowledgment
    ) {
        try {
            logger.info("Получено уведомление из топика $topic, partition: $partition, offset: $offset")
            
            val notification = objectMapper.readTree(message)
            val userId = notification.get("userId").asLong()
            val notificationMessage = notification.get("message").asText()
            val type = notification.get("type").asText()
            
            logger.info("Обрабатываем уведомление для пользователя $userId типа '$type': $notificationMessage")
            
            // Здесь логика отправки уведомления (Telegram, email, SMS и т.д.)
            sendNotificationToUser(userId, notificationMessage, type)
            
            // Подтверждаем обработку сообщения
            acknowledgment.acknowledge()
            logger.info("Уведомление для пользователя $userId успешно обработано")
            
        } catch (e: Exception) {
            logger.error("Ошибка обработки уведомления", e)
        }
    }
    
    private fun handleUserRegistered(userId: Long, event: com.fasterxml.jackson.databind.JsonNode) {
        logger.info("Пользователь $userId зарегистрировался")
        // Логика обработки регистрации
    }
    
    private fun handleUserLogin(userId: Long, event: com.fasterxml.jackson.databind.JsonNode) {
        logger.info("Пользователь $userId вошел в систему")
        // Логика обработки входа
    }
    
    private fun handleUserLogout(userId: Long, event: com.fasterxml.jackson.databind.JsonNode) {
        logger.info("Пользователь $userId вышел из системы")
        // Логика обработки выхода
    }
    
    private fun sendNotificationToUser(userId: Long, message: String, type: String) {
        logger.info("Отправляем уведомление пользователю $userId: $message (тип: $type)")
        // Здесь будет интеграция с Telegram Bot API
    }
}
