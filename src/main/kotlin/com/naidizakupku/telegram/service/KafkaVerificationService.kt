package com.naidizakupku.telegram.service

import com.naidizakupku.telegram.domain.dto.*
import com.naidizakupku.telegram.domain.entity.VerificationStatus
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class KafkaVerificationService(
    private val verificationSessionService: VerificationSessionService,
    private val telegramNotificationService: TelegramNotificationService,
    private val kafkaProducerService: KafkaProducerService,
    private val telegramBotExecutor: TelegramBotExecutor
) {
    
    private val logger = LoggerFactory.getLogger(KafkaVerificationService::class.java)
    
    fun processVerificationRequest(request: CodeVerificationRequestDto) {
        logger.info("Обработка запроса верификации: correlationId=${request.correlationId}, code=${request.code}")
        
        try {
            // Создаем сессию верификации
            val session = verificationSessionService.createVerificationSession(
                correlationId = request.correlationId,
                code = request.code,
                userBrowserInfo = request.userBrowserInfo
            )
            
            if (session != null) {
                // Отправляем сообщение в Telegram
                val messageId = telegramNotificationService.sendVerificationRequest(telegramBotExecutor, session, request.userBrowserInfo)
                
                if (messageId != null) {
                    // Отправляем успешный ответ в Kafka
                    kafkaProducerService.sendVerificationResponse(
                        correlationId = request.correlationId,
                        success = true,
                        telegramUserId = session.telegramUserId,
                        message = "Verification request sent to user"
                    )
                    logger.info("Запрос верификации обработан успешно: correlationId=${request.correlationId}")
                } else {
                    // Ошибка отправки в Telegram
                    kafkaProducerService.sendVerificationResponse(
                        correlationId = request.correlationId,
                        success = false,
                        telegramUserId = null,
                        message = "Failed to send Telegram notification"
                    )
                    logger.error("Ошибка отправки уведомления в Telegram: correlationId=${request.correlationId}")
                }
            } else {
                // Код не найден или просрочен
                kafkaProducerService.sendVerificationResponse(
                    correlationId = request.correlationId,
                    success = false,
                    telegramUserId = null,
                    message = "Code not found or expired"
                )
                logger.warn("Код не найден или просрочен: correlationId=${request.correlationId}, code=${request.code}")
            }
            
        } catch (e: Exception) {
            logger.error("Ошибка обработки запроса верификации: ${e.message}", e)
            
            // Отправляем ответ с ошибкой
            kafkaProducerService.sendVerificationResponse(
                correlationId = request.correlationId,
                success = false,
                telegramUserId = null,
                message = "Internal error: ${e.message}"
            )
        }
    }
    
    fun processVerificationConfirmation(correlationId: UUID): Boolean {
        logger.info("Подтверждение верификации: correlationId=$correlationId")
        
        return try {
            val session = verificationSessionService.findByCorrelationId(correlationId)
                ?: return false
                
            // Обновляем статус сессии
            val updated = verificationSessionService.updateSessionStatus(correlationId, VerificationStatus.CONFIRMED)
            
            if (updated) {
                // Обновляем сообщение в Telegram
                telegramNotificationService.updateMessageToConfirmed(telegramBotExecutor, session.telegramUserId, 0) // TODO: сохранять messageId
                logger.info("Верификация подтверждена: correlationId=$correlationId")
                true
            } else {
                logger.error("Не удалось обновить статус сессии: correlationId=$correlationId")
                false
            }
            
        } catch (e: Exception) {
            logger.error("Ошибка подтверждения верификации: ${e.message}", e)
            false
        }
    }
    
    fun processVerificationRevocation(correlationId: UUID): Boolean {
        logger.info("Отзыв верификации: correlationId=$correlationId")
        
        return try {
            val session = verificationSessionService.findByCorrelationId(correlationId)
                ?: return false
                
            // Обновляем статус сессии
            val updated = verificationSessionService.updateSessionStatus(correlationId, VerificationStatus.REVOKED)
            
            if (updated) {
                // Обновляем сообщение в Telegram
                telegramNotificationService.updateMessageToRevoking(telegramBotExecutor, session.telegramUserId, 0) // TODO: сохранять messageId
                
                // Отправляем запрос на отзыв авторизации
                val revokeCorrelationId = UUID.randomUUID()
                kafkaProducerService.sendRevokeRequest(
                    correlationId = revokeCorrelationId,
                    telegramUserId = session.telegramUserId,
                    originalVerificationCorrelationId = correlationId,
                    reason = "User requested revocation"
                )
                
                logger.info("Запрос отзыва отправлен: correlationId=$correlationId, revokeCorrelationId=$revokeCorrelationId")
                true
            } else {
                logger.error("Не удалось обновить статус сессии: correlationId=$correlationId")
                false
            }
            
        } catch (e: Exception) {
            logger.error("Ошибка отзыва верификации: ${e.message}", e)
            false
        }
    }
    
    fun processRevokeResponse(response: AuthorizationRevokeResponseDto) {
        logger.info("Обработка ответа отзыва: correlationId=${response.correlationId}")
        
        try {
            if (response.success) {
                // Отправляем подтверждение отзыва в Telegram
                telegramNotificationService.sendRevocationConfirmed(telegramBotExecutor, response.telegramUserId)
                logger.info("Отзыв авторизации подтвержден: correlationId=${response.correlationId}")
            } else {
                logger.warn("Отзыв авторизации не удался: correlationId=${response.correlationId}, message=${response.message}")
            }
            
        } catch (e: Exception) {
            logger.error("Ошибка обработки ответа отзыва: ${e.message}", e)
        }
    }
}
