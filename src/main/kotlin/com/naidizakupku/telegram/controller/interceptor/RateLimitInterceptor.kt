package com.naidizakupku.telegram.controller.interceptor

import com.bucket4j.Bucket
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

/**
 * Перехватчик для rate limiting API endpoints
 */
@Component
class RateLimitInterceptor(
    private val apiRateLimiter: Bucket,
    @Qualifier("codeVerificationRateLimiter") private val codeVerificationRateLimiter: Bucket
) : HandlerInterceptor {

    private val logger = LoggerFactory.getLogger(RateLimitInterceptor::class.java)

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        val clientIp = getClientIp(request)
        val path = request.requestURI

        // Выбираем rate limiter в зависимости от endpoint
        val bucket = when {
            path.startsWith("/api/code/") -> codeVerificationRateLimiter
            else -> apiRateLimiter
        }

        // Проверяем лимит
        if (!bucket.tryConsume(1)) {
            logger.warn("Rate limit exceeded: ip=$clientIp, path=$path")
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.contentType = "application/json"
            response.writer.write(
                """
                {
                    "error": "Too Many Requests",
                    "message": "Превышен лимит запросов. Попробуйте позже.",
                    "retryAfter": 60
                }
                """.trimIndent()
            )
            return false
        }

        return true
    }

    private fun getClientIp(request: HttpServletRequest): String {
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        if (xForwardedFor != null && xForwardedFor.isNotEmpty()) {
            return xForwardedFor.split(",")[0].trim()
        }
        
        val xRealIp = request.getHeader("X-Real-IP")
        if (xRealIp != null && xRealIp.isNotEmpty()) {
            return xRealIp
        }
        
        return request.remoteAddr ?: "unknown"
    }
}

