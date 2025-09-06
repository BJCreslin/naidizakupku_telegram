package com.naidizakupku.telegram.service

import com.naidizakupku.telegram.domain.dto.UserBrowserInfoDto
import com.naidizakupku.telegram.domain.entity.VerificationSession
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class TelegramNotificationService(
    private val telegramBot: TelegramLongPollingBot
) {
    
    private val logger = LoggerFactory.getLogger(TelegramNotificationService::class.java)
    
    @Value("\${telegram.timezone:Moscow}")
    private lateinit var timezone: String
    
    private val moscowZone = ZoneId.of("Europe/Moscow")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    
    fun sendVerificationRequest(
        session: VerificationSession,
        browserInfo: UserBrowserInfoDto
    ): Long? {
        try {
            val message = buildVerificationMessage(session, browserInfo)
            val keyboard = buildVerificationKeyboard(session.correlationId)
            
            message.replyMarkup = keyboard
            
            val result = telegramBot.execute(message)
            logger.info("Сообщение верификации отправлено: messageId=${result.messageId}")
            return result.messageId.toLong()
            
        } catch (e: TelegramApiException) {
            logger.error("Ошибка отправки сообщения верификации: ${e.message}", e)
            return null
        }
    }
    
    fun updateMessageToConfirmed(chatId: Long, messageId: Long): Boolean {
        try {
            val message = SendMessage()
            message.chatId = chatId.toString()
            message.text = "✅ Авторизация подтверждена"
            
            telegramBot.execute(message)
            logger.info("Сообщение обновлено на подтверждено: messageId=$messageId")
            return true
            
        } catch (e: TelegramApiException) {
            logger.error("Ошибка обновления сообщения: ${e.message}", e)
            return false
        }
    }
    
    fun updateMessageToRevoking(chatId: Long, messageId: Long): Boolean {
        try {
            val message = SendMessage()
            message.chatId = chatId.toString()
            message.text = "⏳ Отзываем авторизацию..."
            
            telegramBot.execute(message)
            logger.info("Сообщение обновлено на отзыв: messageId=$messageId")
            return true
            
        } catch (e: TelegramApiException) {
            logger.error("Ошибка обновления сообщения: ${e.message}", e)
            return false
        }
    }
    
    fun sendRevocationConfirmed(chatId: Long): Boolean {
        try {
            val message = SendMessage()
            message.chatId = chatId.toString()
            message.text = "❌ Авторизация отозвана"
            
            telegramBot.execute(message)
            logger.info("Сообщение об отзыве отправлено: chatId=$chatId")
            return true
            
        } catch (e: TelegramApiException) {
            logger.error("Ошибка отправки сообщения об отзыве: ${e.message}", e)
            return false
        }
    }

    /**
     * Отправляет уведомление о запросе авторизации с кнопками подтверждения
     */
    fun sendAuthConfirmationRequest(
        telegramUserId: Long,
        traceId: UUID,
        ip: String?,
        userAgent: String?,
        location: String?
    ): Long? {
        try {
            val message = buildAuthConfirmationMessage(telegramUserId, traceId, ip, userAgent, location)
            val keyboard = buildAuthConfirmationKeyboard(traceId)
            
            message.replyMarkup = keyboard
            
            val result = telegramBot.execute(message)
            logger.info("Сообщение подтверждения авторизации отправлено: messageId=${result.messageId}")
            return result.messageId.toLong()
            
        } catch (e: TelegramApiException) {
            logger.error("Ошибка отправки сообщения подтверждения авторизации: ${e.message}", e)
            return null
        }
    }

    /**
     * Удаляет кнопки из сообщения подтверждения авторизации
     */
    fun removeAuthConfirmationButtons(telegramUserId: Long, traceId: UUID): Boolean {
        try {
            val message = SendMessage()
            message.chatId = telegramUserId.toString()
            message.text = "✅ Авторизация подтверждена"
            
            telegramBot.execute(message)
            logger.info("Кнопки подтверждения удалены для traceId $traceId")
            return true
            
        } catch (e: TelegramApiException) {
            logger.error("Ошибка удаления кнопок подтверждения: ${e.message}", e)
            return false
        }
    }

    /**
     * Отправляет сообщение об отзыве авторизации
     */
    fun sendAuthRevokedMessage(telegramUserId: Long): Boolean {
        try {
            val message = SendMessage()
            message.chatId = telegramUserId.toString()
            message.text = "❌ Авторизация отозвана"
            
            telegramBot.execute(message)
            logger.info("Сообщение об отзыве авторизации отправлено: telegramUserId=$telegramUserId")
            return true
            
        } catch (e: TelegramApiException) {
            logger.error("Ошибка отправки сообщения об отзыве авторизации: ${e.message}", e)
            return false
        }
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
}
