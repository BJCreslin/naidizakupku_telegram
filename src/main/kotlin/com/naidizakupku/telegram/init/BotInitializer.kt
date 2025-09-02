package com.naidizakupku.telegram.init

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException
import org.telegram.telegrambots.meta.generics.LongPollingBot
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

@Component
class BotInitializer(
    private val telegramBot: LongPollingBot
) {
    val logger: Logger = LoggerFactory.getLogger(BotInitializer::class.java)

    @EventListener(ContextRefreshedEvent::class)
    @Throws(TelegramApiException::class)
    fun init() {
        // Проверяем конфигурацию бота через сам бот
        val token = telegramBot.botToken
        val name = telegramBot.botUsername

        logger.info("Telegram bot configuration: Token: ${if (token.isNotBlank()) "SET" else "MISSING"}, Name: $name")

        if (token.isBlank() || name.isBlank()) {
            logger.warn("Telegram bot configuration is incomplete. Token: ${if (token.isBlank()) "MISSING" else "SET"}, Name: ${if (name.isBlank()) "MISSING" else "SET"}")
            logger.warn("Telegram bot will not be initialized. Set environment variables NAIDI_ZAKUPKU_TELEGRAM_BOT_TOKEN and NAIDI_ZAKUPKU_TELEGRAM_BOT_NAME or configure telergram.token and telergram.name in application.properties")
            return
        }

        val telegramBotsApi = TelegramBotsApi(DefaultBotSession::class.java)
        try {
            telegramBotsApi.registerBot(telegramBot)
            logger.info("Registered telegram Bot: $name")
        } catch (e: TelegramApiRequestException) {
            logger.error("Failed to register telegram bot: ${e.message}")
            throw e
        } catch (e: Exception) {
            logger.error("Unexpected error during telegram bot registration", e)
            throw e
        }
    }
}