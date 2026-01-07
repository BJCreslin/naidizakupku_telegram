package com.naidizakupku.telegram.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Свойства конфигурации Telegram бота
 */
@ConfigurationProperties(prefix = "telegram.bot")
data class TelegramBotProperties(
    val token: String = "",
    val name: String = ""
)

