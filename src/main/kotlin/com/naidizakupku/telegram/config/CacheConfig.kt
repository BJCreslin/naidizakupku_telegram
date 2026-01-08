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
     * Дефолтный кэш-менеджер для общих кэшей приложения
     * TTL: 5 минут (соответствует настройке в application.yml)
     * Максимальный размер: 1000 записей
     */
    @Bean
    fun cacheManager(): CacheManager {
        val cacheManager = CaffeineCacheManager("userCodes")
        cacheManager.setCaffeine(
            Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats()
        )
        return cacheManager
    }

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

