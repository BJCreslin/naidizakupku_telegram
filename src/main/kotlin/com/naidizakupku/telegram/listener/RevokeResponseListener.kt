package com.naidizakupku.telegram.listener

import com.naidizakupku.telegram.domain.dto.AuthorizationRevokeResponseDto
import com.naidizakupku.telegram.service.KafkaVerificationService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class RevokeResponseListener(
    private val kafkaVerificationService: KafkaVerificationService
) {
    
    private val logger = LoggerFactory.getLogger(RevokeResponseListener::class.java)
    
    @KafkaListener(
        topics = ["\${kafka.topics.revoke-response:authorization-revoke-response}"],
        containerFactory = "verificationKafkaListenerContainerFactory"
    )
    fun handleRevokeResponse(
        @Payload response: AuthorizationRevokeResponseDto,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        acknowledgment: Acknowledgment
    ) {
        logger.info("Получен ответ об отзыве из Kafka: topic=$topic, partition=$partition, offset=$offset, correlationId=${response.correlationId}")
        
        try {
            // Обрабатываем ответ об отзыве
            kafkaVerificationService.processRevokeResponse(response)
            
            // Подтверждаем обработку сообщения
            acknowledgment.acknowledge()
            
            logger.info("Ответ об отзыве обработан успешно: correlationId=${response.correlationId}")
            
        } catch (e: Exception) {
            logger.error("Ошибка обработки ответа об отзыве: correlationId=${response.correlationId}, error=${e.message}", e)
            
            // В случае ошибки также подтверждаем, чтобы не перечитывать сообщение
            acknowledgment.acknowledge()
        }
    }
}
