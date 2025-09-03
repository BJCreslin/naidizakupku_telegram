package com.naidizakupku.telegram.handler

import com.naidizakupku.telegram.service.KafkaVerificationService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.Update
import java.util.UUID

@Component
class VerificationCallbackHandler(
    private val kafkaVerificationService: KafkaVerificationService
) {
    
    private val logger = LoggerFactory.getLogger(VerificationCallbackHandler::class.java)
    
    fun handleCallback(update: Update): Boolean {
        val callbackQuery = update.callbackQuery ?: return false
        
        val callbackData = callbackQuery.data
        if (callbackData == null || !callbackData.startsWith("confirm_") && !callbackData.startsWith("revoke_")) {
            return false
        }
        
        try {
            val parts = callbackData.split("_", limit = 2)
            if (parts.size != 2) {
                logger.warn("Неверный формат callback данных: $callbackData")
                return false
            }
            
            val action = parts[0]
            val correlationIdStr = parts[1]
            
            val correlationId = try {
                UUID.fromString(correlationIdStr)
            } catch (e: IllegalArgumentException) {
                logger.error("Неверный UUID в callback: $correlationIdStr", e)
                return false
            }
            
            when (action) {
                "confirm" -> {
                    logger.info("Обработка подтверждения верификации: correlationId=$correlationId")
                    val success = kafkaVerificationService.processVerificationConfirmation(correlationId)
                    
                    if (success) {
                        // Отвечаем на callback
                        answerCallback(callbackQuery, "✅ Авторизация подтверждена")
                    } else {
                        answerCallback(callbackQuery, "❌ Ошибка подтверждения")
                    }
                }
                
                "revoke" -> {
                    logger.info("Обработка отзыва верификации: correlationId=$correlationId")
                    val success = kafkaVerificationService.processVerificationRevocation(correlationId)
                    
                    if (success) {
                        answerCallback(callbackQuery, "⏳ Отзываем авторизацию...")
                    } else {
                        answerCallback(callbackQuery, "❌ Ошибка отзыва")
                    }
                }
                
                else -> {
                    logger.warn("Неизвестное действие в callback: $action")
                    return false
                }
            }
            
            return true
            
        } catch (e: Exception) {
            logger.error("Ошибка обработки callback: ${e.message}", e)
            answerCallback(callbackQuery, "❌ Произошла ошибка")
            return false
        }
    }
    
    private fun answerCallback(callbackQuery: CallbackQuery, text: String) {
        try {
            // Отвечаем на callback query
            val answer = org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery()
            answer.callbackQueryId = callbackQuery.id
            answer.text = text
            answer.showAlert = false
            
            // TODO: Выполнить через telegramBot
            logger.info("Callback ответ: $text")
            
        } catch (e: Exception) {
            logger.error("Ошибка ответа на callback: ${e.message}", e)
        }
    }
}
