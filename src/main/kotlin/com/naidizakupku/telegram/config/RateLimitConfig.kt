package com.naidizakupku.telegram.config

import com.bucket4j.Bandwidth
import com.bucket4j.Bucket
import com.bucket4j.Refill
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

/**
 * Конфигурация rate limiting для API endpoints
 */
@Configuration
class RateLimitConfig {

    /**
     * Общий rate limiter для всех API endpoints
     * 100 запросов в минуту на IP
     */
    @Bean
    fun apiRateLimiter(): Bucket {
        return Bucket.builder()
            .addLimit(
                Bandwidth.classic(
                    100,
                    Refill.intervally(100, Duration.ofMinutes(1))
                )
            )
            .build()
    }

    /**
     * Строгий rate limiter для endpoints верификации кодов
     * 10 запросов в минуту на IP
     */
    @Bean("codeVerificationRateLimiter")
    fun codeVerificationRateLimiter(): Bucket {
        return Bucket.builder()
            .addLimit(
                Bandwidth.classic(
                    10,
                    Refill.intervally(10, Duration.ofMinutes(1))
                )
            )
            .build()
    }

    /**
     * Rate limiter для генерации кодов через Telegram
     * 5 запросов в минуту на пользователя
     */
    @Bean("codeGenerationRateLimiter")
    fun codeGenerationRateLimiter(): Bucket {
        return Bucket.builder()
            .addLimit(
                Bandwidth.classic(
                    5,
                    Refill.intervally(5, Duration.ofMinutes(1))
                )
            )
            .build()
    }
}

