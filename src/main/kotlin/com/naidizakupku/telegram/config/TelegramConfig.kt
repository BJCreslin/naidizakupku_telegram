package com.naidizakupku.telegram.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration

/**
 * Конфигурация Telegram бота
 */
@Configuration
@ConditionalOnProperty(name = ["telegram.bot.token"])
class TelegramConfig {
    
    @Value("\${telegram.bot.token}")
    lateinit var botToken: String
    
    @Value("\${telegram.bot.name}")
    lateinit var botName: String
    
    @Value("\${telegram.bot.username}")
    lateinit var botUsername: String
}
