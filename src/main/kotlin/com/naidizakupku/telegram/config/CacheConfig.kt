package com.naidizakupku.telegram.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

/**
 * Конфигурация кэширования для приложения
 */
@Configuration
@EnableCaching
class CacheConfig {

    /**
     * Кэш для метрик админки
     * TTL: 2 минуты (метрики должны обновляться достаточно часто)
     * Максимальный размер: 100 записей
     */
    @Bean("metricsCacheManager")
    fun metricsCacheManager(): CacheManager {
        val cacheManager = CaffeineCacheManager("dashboardMetrics", "codeMetrics", "verificationMetrics", "telegramMetrics", "kafkaMetrics")
        cacheManager.setCaffeine(
            Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(2, TimeUnit.MINUTES)
                .recordStats()
        )
        return cacheManager
    }
}

