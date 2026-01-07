package com.naidizakupku.telegram.service

import com.naidizakupku.telegram.config.AdminUserDetailsService
import com.naidizakupku.telegram.config.JwtProperties
import com.naidizakupku.telegram.domain.admin.AdminSession
import com.naidizakupku.telegram.domain.admin.AdminUser
import com.naidizakupku.telegram.repository.admin.AdminSessionRepository
import com.naidizakupku.telegram.repository.admin.AdminUserRepository
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * Сервис аутентификации для админки
 */
@Service
class AdminAuthService(
    private val adminUserRepository: AdminUserRepository,
    private val adminSessionRepository: AdminSessionRepository,
    private val adminUserDetailsService: AdminUserDetailsService,
    private val jwtService: JwtService,
    private val jwtProperties: JwtProperties,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val adminAuditService: AdminAuditService
) {

    private val logger = LoggerFactory.getLogger(AdminAuthService::class.java)

    /**
     * Результат аутентификации
     */
    data class AuthResponse(
        val accessToken: String,
        val refreshToken: String,
        val expiresAt: LocalDateTime,
        val user: AdminUserInfo
    )

    /**
     * Информация о пользователе для ответа
     */
    data class AdminUserInfo(
        val id: Long,
        val username: String,
        val email: String?,
        val role: String
    )

    /**
     * Аутентификация пользователя
     */
    @Transactional
    fun login(username: String, password: String, request: HttpServletRequest?): AuthResponse {
        logger.info("Attempting login for user: $username")

        // Аутентификация через Spring Security
        val authentication: Authentication = try {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(username, password)
            )
        } catch (e: BadCredentialsException) {
            logger.warn("Invalid credentials for user: $username")
            adminAuditService.logAction(
                adminUserId = null,
                action = "LOGIN_FAILED",
                details = "Invalid credentials for username: $username",
                request = request
            )
            throw BadCredentialsException("Invalid username or password")
        }

        // Получаем пользователя
        val adminUser = adminUserDetailsService.loadAdminUserByUsername(username)
        
        if (!adminUser.active) {
            logger.warn("Attempted login for inactive user: $username")
            adminAuditService.logAction(
                adminUserId = adminUser.id,
                action = "LOGIN_FAILED",
                details = "User is inactive",
                request = request
            )
            throw BadCredentialsException("User account is inactive")
        }

        // Генерируем токены
        val accessToken = jwtService.generateAccessToken(
            username = adminUser.username,
            userId = adminUser.id!!,
            role = adminUser.role.name
        )
        
        val refreshToken = jwtService.generateRefreshToken(
            username = adminUser.username,
            userId = adminUser.id
        )

        // Сохраняем сессию
        val expiresAt = LocalDateTime.now().plusMinutes(jwtProperties.accessTokenExpirationMinutes)
        
        val session = AdminSession(
            adminUserId = adminUser.id,
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAt = expiresAt
        )
        adminSessionRepository.save(session)

        // Логируем успешный вход
        adminAuditService.logAction(
            adminUserId = adminUser.id,
            action = "LOGIN_SUCCESS",
            request = request
        )

        logger.info("User logged in successfully: $username")

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAt = expiresAt,
            user = AdminUserInfo(
                id = adminUser.id,
                username = adminUser.username,
                email = adminUser.email,
                role = adminUser.role.name
            )
        )
    }

    /**
     * Обновление токена
     */
    @Transactional
    fun refreshToken(refreshToken: String, request: HttpServletRequest?): AuthResponse {
        logger.info("Refreshing token")

        // Проверяем валидность refresh token
        if (!jwtService.isTokenValid(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
            logger.warn("Invalid refresh token")
            throw BadCredentialsException("Invalid refresh token")
        }

        // Находим сессию
        val session = adminSessionRepository.findByRefreshToken(refreshToken)
            ?: throw BadCredentialsException("Session not found")

        if (session.isExpired()) {
            logger.warn("Refresh token expired")
            adminSessionRepository.delete(session)
            throw BadCredentialsException("Refresh token expired")
        }

        // Получаем пользователя
        val adminUser = adminUserRepository.findById(session.adminUserId)
            .orElseThrow { BadCredentialsException("User not found") }

        if (!adminUser.active) {
            logger.warn("User is inactive: ${adminUser.username}")
            adminSessionRepository.delete(session)
            throw BadCredentialsException("User account is inactive")
        }

        // Генерируем новые токены
        val newAccessToken = jwtService.generateAccessToken(
            username = adminUser.username,
            userId = adminUser.id!!,
            role = adminUser.role.name
        )
        
        val newRefreshToken = jwtService.generateRefreshToken(
            username = adminUser.username,
            userId = adminUser.id
        )

        // Обновляем сессию
        val expiresAt = LocalDateTime.now().plusMinutes(jwtProperties.accessTokenExpirationMinutes)
        val updatedSession = session.copy(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
            expiresAt = expiresAt
        )
        adminSessionRepository.save(updatedSession)

        // Логируем обновление токена
        adminAuditService.logAction(
            adminUserId = adminUser.id,
            action = "TOKEN_REFRESHED",
            request = request
        )

        logger.info("Token refreshed for user: ${adminUser.username}")

        return AuthResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
            expiresAt = expiresAt,
            user = AdminUserInfo(
                id = adminUser.id,
                username = adminUser.username,
                email = adminUser.email,
                role = adminUser.role.name
            )
        )
    }

    /**
     * Выход пользователя
     */
    @Transactional
    fun logout(accessToken: String, request: HttpServletRequest?) {
        logger.info("Logging out user")

        val username = jwtService.extractUsername(accessToken)
        if (username == null) {
            logger.warn("Cannot extract username from token")
            return
        }

        val adminUser = adminUserRepository.findByUsername(username)
        if (adminUser != null) {
            // Удаляем все сессии пользователя
            adminSessionRepository.deleteAllByAdminUserId(adminUser.id!!)
            
            // Логируем выход
            adminAuditService.logAction(
                adminUserId = adminUser.id,
                action = "LOGOUT",
                request = request
            )
            
            logger.info("User logged out: $username")
        }
    }

    /**
     * Валидация токена и получение пользователя
     */
    fun validateToken(token: String): AdminUser? {
        if (!jwtService.isTokenValid(token)) {
            return null
        }

        val username = jwtService.extractUsername(token) ?: return null
        return adminUserRepository.findByUsername(username)
    }
}

