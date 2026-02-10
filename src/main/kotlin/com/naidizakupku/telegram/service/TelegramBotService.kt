package com.naidizakupku.telegram.service

import com.naidizakupku.telegram.config.TelegramConfig
import com.naidizakupku.telegram.handler.TelegramCodeHandler
import com.naidizakupku.telegram.handler.TelegramLogHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.util.UUID

/**
 * Сервис Telegram бота с эхо-функцией
 */
@Service
@ConditionalOnProperty(name = ["telegram.bot.token"])
class TelegramBotService(
    private val telegramConfig: TelegramConfig,
    private val userService: UserServiceInterface,
    private val telegramCodeHandler: TelegramCodeHandler,
    private val telegramLogHandler: TelegramLogHandler,
    @Autowired(required = false) private val coroutineScope: CoroutineScope? = null
) : TelegramLongPollingBot(telegramConfig.botToken), TelegramBotExecutor, TelegramOperationService {
    
    override fun execute(message: SendMessage): org.telegram.telegrambots.meta.api.objects.Message {
        return super.execute(message)
    }
    
    override fun sendAuthConfirmationRequest(
        telegramUserId: Long,
        traceId: UUID,
        ip: String?,
        userAgent: String?,
        location: String?
    ): Long? {
        // Реализация метода через Telegram API
        // В данном случае мы просто возвращаем null, так как метод не должен вызываться напрямую
        // Реальная реализация будет в TelegramNotificationService
        return null
    }
    
    override fun removeAuthConfirmationButtons(telegramUserId: Long, traceId: UUID): Boolean {
        // Реализация метода через Telegram API
        // В данном случае мы просто возвращаем false, так как метод не должен вызываться напрямую
        // Реальная реализация будет в TelegramNotificationService
        return false
    }
    
    override fun sendAuthRevokedMessage(telegramUserId: Long): Boolean {
        // Реализация метода через Telegram API
        // В данном случае мы просто возвращаем false, так как метод не должен вызываться напрямую
        // Реальная реализация будет в TelegramNotificationService
        return false
    }
    
    // Используем переданный scope или создаем новый с Dispatchers.IO
    private val scope: CoroutineScope = coroutineScope ?: CoroutineScope(Dispatchers.IO)

    private val logger = LoggerFactory.getLogger(TelegramBotService::class.java)

    override fun getBotUsername(): String {
        val username = System.getenv("TELEGRAM_BOT_NAME") ?: telegramConfig.botName
        if (username.isBlank()) {
            logger.warn("Telegram bot username is not configured")
        }
        return username
    }

    override fun onUpdateReceived(update: Update) {
        try {
            // Обработка callback'ов от inline кнопок
            if (update.hasCallbackQuery()) {
                handleCallbackQuery(update)
                return
            }

            if (update.hasMessage() && update.message.hasText() && update.message.text != null && update.message.text.isNotBlank()) {
                val message = update.message
                val chatId = message.chatId
                val text = message.text
                val userId = message.from.id

                logger.info("Получено сообщение от пользователя $userId: $text")

                // Сохраняем/обновляем пользователя асинхронно
                scope.launch {
                    try {
                        userService.saveOrUpdateUser(
                            userId,
                            message.from.firstName,
                            message.from.lastName,
                            message.from.userName
                        )
                    } catch (e: Exception) {
                        logger.error("Ошибка при сохранении/обновлении пользователя $userId", e)
                    }
                }

                // Обработка команд
                when {
                    text.startsWith("/help") || text.startsWith("/start") -> {
                        val helpMessage = """
                            🤖 <b>Доступные команды:</b>
                            
                            /code - Получить код для входа в систему
                            /log [количество_строк] - Получить лог-файл приложения (по умолчанию 1000 строк)
                            /loginfo - Получить информацию о лог-файле
                            /help - Показать эту справку
                            
                            <i>Примеры:</i>
                            <i>/log - получить последние 1000 строк</i>
                            <i>/log 500 - получить последние 500 строк</i>
                        """.trimIndent()
                        val message = SendMessage()
                        message.chatId = chatId.toString()
                        message.text = helpMessage
                        message.parseMode = "HTML"
                        execute(message)
                        logger.info("Отправлена справка пользователю $userId")
                    }
                    text.startsWith("/code") -> {
                        val responseMessage = telegramCodeHandler.handleCodeCommand(update)
                        execute(responseMessage)
                        logger.info("Отправлен код пользователю $userId")
                    }
                    text.startsWith("/loginfo") -> {
                        val responseMessage = telegramLogHandler.handleLogInfoCommand(update)
                        execute(responseMessage as SendMessage)
                        logger.info("Отправлена информация о лог-файле пользователю $userId")
                    }
                    text.startsWith("/log") -> {
                        val response = telegramLogHandler.handleLogCommand(update)
                        when (response) {
                            is SendMessage -> {
                                execute(response as SendMessage)
                                logger.info("Отправлено сообщение о лог-файле пользователю $userId")
                            }
                            is org.telegram.telegrambots.meta.api.methods.send.SendDocument -> {
                                execute(response as org.telegram.telegrambots.meta.api.methods.send.SendDocument)
                                logger.info("Отправлен лог-файл пользователю $userId")
                            }
                            else -> {
                                logger.warn("Неизвестный тип ответа от TelegramLogHandler")
                            }
                        }
                    }
                    else -> {
                        // Эхо-функция для остальных сообщений
                        val response = "Эхо: $text"
                        sendMessage(chatId, response)
                        logger.info("Отправлен ответ пользователю $userId: $response")
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Ошибка при обработке сообщения", e)
        }
    }

    /**
     * Обрабатывает callback'и от inline кнопок
     */
    private fun handleCallbackQuery(update: Update) {
        try {
            val callbackQuery = update.callbackQuery
            val callbackData = callbackQuery.data
            val userId = callbackQuery.from.id

            logger.info("Получен callback от пользователя $userId: $callbackData")

            when {
                callbackData.startsWith("auth_confirm_") -> {
                    val traceId = callbackData.removePrefix("auth_confirm_")
                    // Используем метод из TelegramNotificationService для обработки
                    // В реальности здесь должна быть логика обработки через Kafka или другой механизм
                    logger.warn("Обработка подтверждения через TelegramBotService не реализована")
                    answerCallbackQuery(callbackQuery.id, "❌ Обработка не реализована")
                }
                callbackData.startsWith("auth_revoke_") -> {
                    val traceId = callbackData.removePrefix("auth_revoke_")
                    // Используем метод из TelegramNotificationService для обработки
                    // В реальности здесь должна быть логика обработки через Kafka или другой механизм
                    logger.warn("Обработка отзыва через TelegramBotService не реализована")
                    answerCallbackQuery(callbackQuery.id, "❌ Обработка не реализована")
                }
                else -> {
                    logger.warn("Неизвестный callback: $callbackData")
                    answerCallbackQuery(callbackQuery.id, "❌ Неизвестная команда")
                }
            }
        } catch (e: Exception) {
            logger.error("Ошибка при обработке callback", e)
            try {
                answerCallbackQuery(update.callbackQuery.id, "❌ Произошла ошибка")
            } catch (ex: Exception) {
                logger.error("Ошибка при отправке ответа на callback", ex)
            }
        }
    }

    /**
     * Отправляет ответ на callback query
     */
    private fun answerCallbackQuery(callbackQueryId: String, text: String) {
        try {
            val answerCallbackQuery = org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery()
            answerCallbackQuery.callbackQueryId = callbackQueryId
            answerCallbackQuery.text = text
            answerCallbackQuery.showAlert = false
            
            execute(answerCallbackQuery)
        } catch (e: TelegramApiException) {
            logger.error("Ошибка при отправке ответа на callback query", e)
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
