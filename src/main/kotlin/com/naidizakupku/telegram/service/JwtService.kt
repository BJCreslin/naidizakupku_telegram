package com.naidizakupku.telegram.service

import com.naidizakupku.telegram.config.JwtProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.security.Key
import java.util.*

/**
 * Сервис для работы с JWT токенами
 */
@Service
class JwtService(
    private val jwtProperties: JwtProperties
) {
    
    private val logger = LoggerFactory.getLogger(JwtService::class.java)
    
    private val secretKey: Key by lazy {
        val secretBytes = jwtProperties.secret.toByteArray()
        // Для HMAC-SHA256 требуется минимум 256 бит (32 байта)
        if (secretBytes.size < 32) {
            logger.warn("JWT secret key is too short (${secretBytes.size} bytes). Minimum 32 bytes required for HMAC-SHA256. Using padded key.")
            // Дополняем ключ до нужной длины
            val paddedSecret = ByteArray(32)
            System.arraycopy(secretBytes, 0, paddedSecret, 0, minOf(secretBytes.size, 32))
            Keys.hmacShaKeyFor(paddedSecret)
        } else {
            Keys.hmacShaKeyFor(secretBytes)
        }
    }

    /**
     * Генерирует access token для пользователя
     */
    fun generateAccessToken(username: String, userId: Long, role: String): String {
        val now = Date()
        val expiration = Date(now.time + jwtProperties.accessTokenExpirationMinutes * 60 * 1000)

        return Jwts.builder()
            .setSubject(username)
            .claim("userId", userId)
            .claim("role", role)
            .claim("type", "access")
            .setIssuedAt(now)
            .setExpiration(expiration)
            .signWith(secretKey)
            .compact()
    }

    /**
     * Генерирует refresh token
     */
    fun generateRefreshToken(username: String, userId: Long): String {
        val now = Date()
        val expiration = Date(now.time + jwtProperties.refreshTokenExpirationDays * 24 * 60 * 60 * 1000)

        return Jwts.builder()
            .setSubject(username)
            .claim("userId", userId)
            .claim("type", "refresh")
            .setIssuedAt(now)
            .setExpiration(expiration)
            .signWith(secretKey)
            .compact()
    }

    /**
     * Извлекает username из токена
     */
    fun extractUsername(token: String): String? {
        return try {
            extractClaims(token).subject
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Извлекает userId из токена
     */
    fun extractUserId(token: String): Long? {
        return try {
            extractClaims(token)["userId"] as? Long
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Извлекает роль из токена
     */
    fun extractRole(token: String): String? {
        return try {
            extractClaims(token)["role"] as? String
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Проверяет валидность токена
     */
    fun isTokenValid(token: String): Boolean {
        return try {
            val claims = extractClaims(token)
            val expiration = claims.expiration
            expiration.after(Date())
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Проверяет, является ли токен refresh token
     */
    fun isRefreshToken(token: String): Boolean {
        return try {
            extractClaims(token)["type"] == "refresh"
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Извлекает claims из токена
     */
    private fun extractClaims(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body
    }
}

