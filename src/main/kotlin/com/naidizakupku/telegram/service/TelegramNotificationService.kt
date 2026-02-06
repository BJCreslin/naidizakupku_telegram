package com.naidizakupku.telegram.service

import com.naidizakupku.telegram.domain.dto.UserBrowserInfoDto
import com.naidizakupku.telegram.domain.entity.VerificationSession
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class TelegramNotificationService(
    private val telegramOperationService: TelegramOperationService
) {
    
    private val logger = LoggerFactory.getLogger(TelegramNotificationService::class.java)
    
    @Value("\${telegram.timezone:Moscow}")
    private lateinit var timezone: String
    
    private val moscowZone = ZoneId.of("Europe/Moscow")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    
    @CircuitBreaker(name = "telegramApi", fallbackMethod = "sendVerificationRequestFallback")
    fun sendVerificationRequest(
        telegramBot: TelegramOperationService,
        session: VerificationSession,
        browserInfo: UserBrowserInfoDto
    ): Long? {
        val message = buildVerificationMessage(session, browserInfo)
        val keyboard = buildVerificationKeyboard(session.correlationId)
        
        return sendMessageWithKeyboard(
            telegramBot = telegramBot,
            message = message,
            keyboard = keyboard,
            successLogMessage = "Сообщение верификации отправлено",
            errorLogMessage = "Попытка отправки сообщения верификации не удалась"
        )
    }
    
    fun sendVerificationRequestFallback(
        e: Exception,
        telegramBot: TelegramOperationService,
        session: VerificationSession,
        browserInfo: UserBrowserInfoDto
    ): Long? {
        logger.error("Circuit Breaker открыт для Telegram API при отправке верификации: correlationId=${session.correlationId}", e)
        return null
    }
    
    fun updateMessageToConfirmed(telegramBot: TelegramOperationService, chatId: Long, messageId: Long): Boolean {
        return sendSimpleMessage(
            telegramBot = telegramBot,
            chatId = chatId,
            text = "✅ Авторизация подтверждена",
            logMessage = "Сообщение обновлено на подтверждено: messageId=$messageId"
        )
    }
    
    fun updateMessageToRevoking(telegramBot: TelegramOperationService, chatId: Long, messageId: Long): Boolean {
        return sendSimpleMessage(
            telegramBot = telegramBot,
            chatId = chatId,
            text = "⏳ Отзываем авторизацию...",
            logMessage = "Сообщение обновлено на отзыв: messageId=$messageId"
        )
    }
    
    fun sendRevocationConfirmed(telegramBot: TelegramOperationService, chatId: Long): Boolean {
        return sendSimpleMessage(
            telegramBot = telegramBot,
            chatId = chatId,
            text = "❌ Авторизация отозвана",
            logMessage = "Сообщение об отзыве отправлено: chatId=$chatId"
        )
    }

    /**
     * Отправляет уведомление о запросе авторизации с кнопками подтверждения
     */
    @CircuitBreaker(name = "telegramApi", fallbackMethod = "sendAuthConfirmationRequestFallback")
    fun sendAuthConfirmationRequest(
        telegramBot: TelegramOperationService,
        telegramUserId: Long,
        traceId: UUID,
        ip: String?,
        userAgent: String?,
        location: String?
    ): Long? {
        val message = buildAuthConfirmationMessage(telegramUserId, traceId, ip, userAgent, location)
        val keyboard = buildAuthConfirmationKeyboard(traceId)
        
        return sendMessageWithKeyboard(
            telegramBot = telegramBot,
            message = message,
            keyboard = keyboard,
            successLogMessage = "Сообщение подтверждения авторизации отправлено",
            errorLogMessage = "Попытка отправки сообщения подтверждения авторизации не удалась"
        )
    }
    
    fun sendAuthConfirmationRequestFallback(
        e: Exception,
        telegramBot: TelegramOperationService,
        telegramUserId: Long,
        traceId: UUID,
        ip: String?,
        userAgent: String?,
        location: String?
    ): Long? {
        logger.error("Circuit Breaker открыт для Telegram API при отправке подтверждения авторизации: traceId=$traceId", e)
        return null
    }

    /**
     * Удаляет кнопки из сообщения подтверждения авторизации
     */
    @CircuitBreaker(name = "telegramApi", fallbackMethod = "removeAuthConfirmationButtonsFallback")
    fun removeAuthConfirmationButtons(telegramBot: TelegramOperationService, telegramUserId: Long, traceId: UUID): Boolean {
        return sendSimpleMessage(
            telegramBot = telegramBot,
            chatId = telegramUserId,
            text = "✅ Авторизация подтверждена",
            logMessage = "Кнопки подтверждения удалены для traceId $traceId",
            errorMessage = "Ошибка удаления кнопок подтверждения"
        )
    }
    
    fun removeAuthConfirmationButtonsFallback(
        e: Exception,
        telegramBot: TelegramOperationService,
        telegramUserId: Long,
        traceId: UUID
    ): Boolean {
        logger.error("Circuit Breaker открыт для Telegram API при удалении кнопок: traceId=$traceId", e)
        return false
    }

    /**
     * Отправляет сообщение об отзыве авторизации
     */
    @CircuitBreaker(name = "telegramApi", fallbackMethod = "sendAuthRevokedMessageFallback")
    fun sendAuthRevokedMessage(telegramBot: TelegramOperationService, telegramUserId: Long): Boolean {
        return sendSimpleMessage(
            telegramBot = telegramBot,
            chatId = telegramUserId,
            text = "❌ Авторизация отозвана",
            logMessage = "Сообщение об отзыве авторизации отправлено: telegramUserId=$telegramUserId",
            errorMessage = "Ошибка отправки сообщения об отзыве авторизации"
        )
    }
    
    fun sendAuthRevokedMessageFallback(
        e: Exception,
        telegramBot: TelegramOperationService,
        telegramUserId: Long
    ): Boolean {
        logger.error("Circuit Breaker открыт для Telegram API при отправке сообщения об отзыве: telegramUserId=$telegramUserId", e)
        return false
    }
    
    private fun buildVerificationMessage(
        session: VerificationSession,
        browserInfo: UserBrowserInfoDto
    ): SendMessage {
        val message = SendMessage()
        message.chatId = session.telegramUserId.toString()
        message.parseMode = "HTML"
        
        val moscowTime = session.createdAt.atZone(moscowZone)
        val timeStr = moscowTime.format(timeFormatter)
        
        message.text = """
            🔐 <b>Запрос авторизации</b>
            
            IP: <code>${browserInfo.ip}</code>
            Браузер: <code>${browserInfo.userAgent}</code>
            Время: <code>$timeStr (МСК)</code>
            
            Подтвердите авторизацию:
        """.trimIndent()
        
        return message
    }
    
    private fun buildVerificationKeyboard(correlationId: UUID): InlineKeyboardMarkup {
        val keyboard = InlineKeyboardMarkup()
        val row = mutableListOf<InlineKeyboardButton>()
        
        val confirmButton = InlineKeyboardButton()
        confirmButton.text = "✅ Подтвердить"
        confirmButton.callbackData = "confirm_${correlationId}"
        
        val revokeButton = InlineKeyboardButton()
        revokeButton.text = "❌ Отозвать авторизацию"
        revokeButton.callbackData = "revoke_${correlationId}"
        
        row.add(confirmButton)
        row.add(revokeButton)
        keyboard.keyboard = listOf(row)
        
        return keyboard
    }

    private fun buildAuthConfirmationMessage(
        telegramUserId: Long,
        @Suppress("UNUSED_PARAMETER") traceId: UUID,
        ip: String?,
        userAgent: String?,
        location: String?
    ): SendMessage {
        val message = SendMessage()
        message.chatId = telegramUserId.toString()
        message.parseMode = "HTML"
        
        val currentTime = java.time.LocalDateTime.now().atZone(moscowZone)
        val timeStr = currentTime.format(timeFormatter)
        
        val ipInfo = ip?.let { "IP: <code>$it</code>\n" } ?: ""
        val userAgentInfo = userAgent?.let { "Браузер: <code>$it</code>\n" } ?: ""
        val locationInfo = location?.let { "Локация: <code>$it</code>\n" } ?: ""
        
        message.text = """
            🔐 <b>Запрос авторизации</b>
            
            $ipInfo$userAgentInfo$locationInfo
            Время: <code>$timeStr (МСК)</code>
            
            Подтвердите вход в систему:
        """.trimIndent()
        
        return message
    }

    private fun buildAuthConfirmationKeyboard(traceId: UUID): InlineKeyboardMarkup {
        val keyboard = InlineKeyboardMarkup()
        val row = mutableListOf<InlineKeyboardButton>()
        
        val confirmButton = InlineKeyboardButton()
        confirmButton.text = "✅ Подтвердить вход"
        confirmButton.callbackData = "auth_confirm_${traceId}"
        
        val revokeButton = InlineKeyboardButton()
        revokeButton.text = "❌ Отозвать вход"
        revokeButton.callbackData = "auth_revoke_${traceId}"
        
        row.add(confirmButton)
        row.add(revokeButton)
        keyboard.keyboard = listOf(row)
        
        return keyboard
    }
    
    /**
     * Общий метод для отправки простого текстового сообщения
     */
    private fun sendSimpleMessage(
        telegramBot: TelegramOperationService,
        chatId: Long,
        text: String,
        logMessage: String,
        errorMessage: String = "Ошибка отправки сообщения"
    ): Boolean {
        return try {
            val message = SendMessage().apply {
                this.chatId = chatId.toString()
                this.text = text
            }
            
            telegramBot.execute(message)
            logger.info(logMessage)
            true
            
        } catch (e: TelegramApiException) {
            logger.error("$errorMessage: ${e.message}", e)
            false
        }
    }
    
    /**
     * Общий метод для отправки сообщения с клавиатурой (с поддержкой retry)
     */
    private fun sendMessageWithKeyboard(
        telegramBot: TelegramOperationService,
        message: SendMessage,
        keyboard: InlineKeyboardMarkup,
        successLogMessage: String,
        errorLogMessage: String
    ): Long? {
        return try {
            message.replyMarkup = keyboard
            val result = telegramBot.execute(message)
            logger.info("$successLogMessage: messageId=${result.messageId}")
            result.messageId.toLong()
            
        } catch (e: TelegramApiException) {
            logger.warn("$errorLogMessage: ${e.message}")
            throw e // Пробрасываем для retry
        }
    }
}
