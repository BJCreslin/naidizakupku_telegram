package com.naidizakupku.telegram.service

import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import java.util.*

/**
 * Интерфейс для выполнения операций с Telegram Bot API
 * Используется для устранения циклических зависимостей
 */
interface TelegramOperationService {
    /**
     * Выполняет отправку сообщения в Telegram
     */
    fun execute(message: SendMessage): org.telegram.telegrambots.meta.api.objects.Message
    
    /**
     * Отправляет сообщение пользователю с кнопками подтверждения авторизации
     */
    fun sendAuthConfirmationRequest(
        telegramUserId: Long,
        traceId: UUID,
        ip: String?,
        userAgent: String?,
        location: String?
    ): Long?
    
    /**
     * Удаляет кнопки из сообщения подтверждения авторизации
     */
    fun removeAuthConfirmationButtons(telegramUserId: Long, traceId: UUID): Boolean
    
    /**
     * Отправляет сообщение об отзыве авторизации
     */
    fun sendAuthRevokedMessage(telegramUserId: Long): Boolean
}