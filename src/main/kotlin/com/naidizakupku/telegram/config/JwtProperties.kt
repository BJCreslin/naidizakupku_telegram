package com.naidizakupku.telegram.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Свойства конфигурации JWT для админки
 */
@ConfigurationProperties(prefix = "admin.jwt")
data class JwtProperties(
    /**
     * Секретный ключ для подписи JWT токенов
     */
    val secret: String = "default-secret-key-change-in-production",
    
    /**
     * Время жизни access token в минутах
     */
    val accessTokenExpirationMinutes: Long = 60,
    
    /**
     * Время жизни refresh token в днях
     */
    val refreshTokenExpirationDays: Long = 7
)

