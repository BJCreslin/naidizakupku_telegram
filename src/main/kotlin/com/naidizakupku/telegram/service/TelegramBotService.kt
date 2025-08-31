package com.naidizakupku.telegram.service

import com.naidizakupku.telegram.config.TelegramConfig
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

/**
 * Сервис Telegram бота с эхо-функцией
 */
@Service
@Deprecated("Используется deprecated конструктор TelegramLongPollingBot")
class TelegramBotService(
    private val telegramConfig: TelegramConfig,
    private val userService: UserService
) : TelegramLongPollingBot() {
    
    private val logger = LoggerFactory.getLogger(TelegramBotService::class.java)
    
    override fun getBotToken(): String = telegramConfig.botToken
    
    override fun getBotUsername(): String = telegramConfig.botUsername
    
    override fun onUpdateReceived(update: Update) {
        try {
            if (update.hasMessage() && update.message.hasText() && update.message.text != null && update.message.text.isNotBlank()) {
                val message = update.message
                val chatId = message.chatId
                val text = message.text
                val userId = message.from.id
                
                logger.info("Получено сообщение от пользователя $userId: $text")
                
                // Сохраняем/обновляем пользователя
                runBlocking {
                    userService.saveOrUpdateUser(userId, message.from.firstName, message.from.lastName, message.from.userName)
                }
                
                // Эхо-функция
                val response = "Эхо: $text"
                sendMessage(chatId, response)
                
                logger.info("Отправлен ответ пользователю $userId: $response")
            }
        } catch (e: Exception) {
            logger.error("Ошибка при обработке сообщения", e)
        }
    }
    
    /**
     * Отправляет сообщение пользователю
     */
    fun sendMessage(chatId: Long, text: String) {
        try {
            val message = SendMessage()
            message.chatId = chatId.toString()
            message.text = text
            
            execute(message)
        } catch (e: TelegramApiException) {
            logger.error("Ошибка при отправке сообщения в чат $chatId", e)
        }
    }
}
