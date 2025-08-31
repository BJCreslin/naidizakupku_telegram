package com.naidizakupku.telegram.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration

/**
 * Конфигурация базы данных с условной загрузкой
 */
@Configuration
@ConditionalOnProperty(name = ["spring.datasource.url"])
class DatabaseConfig {
    // Конфигурация будет загружена только если указан URL базы данных
}

