package com.naidizakupku.telegram.service

import org.telegram.telegrambots.meta.api.methods.send.SendMessage

/**
 * Интерфейс для выполнения операций с Telegram Bot API
 * Используется для устранения циклических зависимостей
 */
interface TelegramBotExecutor {
    /**
     * Выполняет отправку сообщения в Telegram
     */
    fun execute(message: SendMessage): org.telegram.telegrambots.meta.api.objects.Message
}

