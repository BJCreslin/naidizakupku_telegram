package com.naidizakupku.telegram.service

import com.naidizakupku.telegram.config.TelegramConfig
import com.naidizakupku.telegram.handler.TelegramCodeHandler
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

/**
 * Сервис Telegram бота с эхо-функцией
 */
@Service
@ConditionalOnProperty(name = ["telegram.bot.token"])
class TelegramBotService(
    private val telegramConfig: TelegramConfig,
    private val userService: UserServiceInterface,
    private val telegramCodeHandler: TelegramCodeHandler,
    private val userCodeService: UserCodeService,
    @Autowired(required = false) private val coroutineScope: CoroutineScope? = null
) : TelegramLongPollingBot(telegramConfig.botToken), TelegramBotExecutor {
    
    override fun execute(message: SendMessage): org.telegram.telegrambots.meta.api.objects.Message {
        return super.execute(message)
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
                            /help - Показать эту справку
                            
                            <i>Для получения кода используйте команду /code</i>
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
                    val success = userCodeService.confirmAuth(java.util.UUID.fromString(traceId))
                    
                    if (success) {
                        answerCallbackQuery(callbackQuery.id, "✅ Вход подтвержден")
                    } else {
                        answerCallbackQuery(callbackQuery.id, "❌ Ошибка подтверждения входа")
                    }
                }
                callbackData.startsWith("auth_revoke_") -> {
                    val traceId = callbackData.removePrefix("auth_revoke_")
                    val success = userCodeService.revokeAuth(java.util.UUID.fromString(traceId))
                    
                    if (success) {
                        answerCallbackQuery(callbackQuery.id, "❌ Вход отозван")
                    } else {
                        answerCallbackQuery(callbackQuery.id, "❌ Ошибка отзыва входа")
                    }
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
