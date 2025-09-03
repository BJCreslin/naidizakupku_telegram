package com.naidizakupku.telegram.handler

import com.naidizakupku.telegram.service.UserCodeService
import com.naidizakupku.telegram.service.UserCodeResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User

@Component
class TelegramCodeHandler(
    private val userCodeService: UserCodeService
) {
    
    companion object {
        private val logger = LoggerFactory.getLogger(TelegramCodeHandler::class.java)
    }
    
    fun handleCodeCommand(update: Update): SendMessage {
        val chatId = update.message.chatId.toString()
        val user = update.message.from
        
        try {
            logger.info("Обработка команды /code для пользователя ${user.id}")
            
            val userCodeResponse = userCodeService.getOrCreateUserCode(
                telegramUserId = user.id,
                userTimezone = extractUserTimezone(user)
            )
            
            val message = buildCodeMessage(userCodeResponse)
            
            return SendMessage().apply {
                this.chatId = chatId
                this.text = message
                this.parseMode = "HTML"
            }
            
        } catch (e: Exception) {
            logger.error("Ошибка при обработке команды /code для пользователя ${user.id}", e)
            
            return SendMessage().apply {
                this.chatId = chatId
                this.text = "❌ Произошла ошибка при генерации кода. Попробуйте позже."
            }
        }
    }
    
    private fun buildCodeMessage(response: UserCodeResponse): String {
        val statusIcon = if (response.isNew) "🆕" else "🔑"
        val statusText = if (response.isNew) "Сгенерирован новый код" else "Ваш код"
        
        val expirationTime = userCodeService.formatExpirationTime(response.expiresAt, response.timezone)
        
        return """
            $statusIcon <b>$statusText</b>: <code>${response.code}</code>
            ⏰ <b>Действителен до</b>: $expirationTime
        """.trimIndent()
    }
    
    private fun extractUserTimezone(user: User): String? {
        // В реальном Telegram Bot API часовой пояс пользователя недоступен
        // Возвращаем null для использования UTC+3 по умолчанию
        return null
    }
}
