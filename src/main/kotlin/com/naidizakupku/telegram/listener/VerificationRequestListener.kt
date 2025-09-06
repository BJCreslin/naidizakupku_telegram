package com.naidizakupku.telegram.listener

import com.naidizakupku.telegram.domain.dto.CodeVerificationRequestDto
import com.naidizakupku.telegram.service.KafkaVerificationService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class VerificationRequestListener(
    private val kafkaVerificationService: KafkaVerificationService
) {
    
    private val logger = LoggerFactory.getLogger(VerificationRequestListener::class.java)
    
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
            
            logger.info("Запрос верификации обработан успешно: correlationId=${request.correlationId}")
            
        } catch (e: Exception) {
            logger.error("Ошибка обработки запроса верификации: correlationId=${request.correlationId}, error=${e.message}", e)
            
            // В случае ошибки также подтверждаем, чтобы не перечитывать сообщение
            // В продакшене здесь можно реализовать retry логику или отправку в DLQ
            acknowledgment.acknowledge()
        }
    }
}
