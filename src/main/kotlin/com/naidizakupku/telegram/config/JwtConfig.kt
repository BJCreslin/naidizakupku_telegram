package com.naidizakupku.telegram.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * Конфигурация JWT для админки
 */
@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class JwtConfig

