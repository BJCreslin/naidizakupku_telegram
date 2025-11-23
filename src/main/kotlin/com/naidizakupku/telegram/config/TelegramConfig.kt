package com.naidizakupku.telegram.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * Конфигурация Telegram бота
 */
@Configuration
@ConditionalOnProperty(name = ["telegram.bot.token"])
@EnableConfigurationProperties(TelegramBotProperties::class)
class TelegramConfig(
    private val properties: TelegramBotProperties
) {
    val botToken: String get() = properties.token
    val botName: String get() = properties.name
}
