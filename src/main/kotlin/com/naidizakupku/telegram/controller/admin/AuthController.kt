package com.naidizakupku.telegram.controller.admin

import com.naidizakupku.telegram.service.AdminAuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

/**
 * Контроллер для аутентификации админов
 */
@RestController
@RequestMapping("/api/admin/auth")
@Tag(name = "Admin Auth", description = "API для аутентификации администраторов")
class AuthController(
    private val adminAuthService: AdminAuthService
) {

    /**
     * Запрос на вход
     */
    @Schema(description = "Запрос на аутентификацию")
    data class LoginRequest(
        @field:NotBlank(message = "Username обязателен")
        val username: String,
        
        @field:NotBlank(message = "Password обязателен")
        val password: String
    )

    /**
     * Запрос на обновление токена
     */
    @Schema(description = "Запрос на обновление токена")
    data class RefreshTokenRequest(
        @field:NotBlank(message = "Refresh token обязателен")
        val refreshToken: String
    )

    /**
     * Ответ с информацией о пользователе
     */
    @Schema(description = "Информация о текущем пользователе")
    data class CurrentUserResponse(
        val id: Long,
        val username: String,
        val email: String?,
        val role: String
    )

    /**
     * Вход в систему
     */
    @Operation(
        summary = "Вход в систему",
        description = "Аутентификация администратора и получение JWT токенов"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Успешная аутентификация",
                content = [Content(schema = Schema(implementation = AdminAuthService.AuthResponse::class))]
            ),
            ApiResponse(responseCode = "401", description = "Неверные учетные данные"),
            ApiResponse(responseCode = "400", description = "Некорректные данные запроса")
        ]
    )
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<AdminAuthService.AuthResponse> {
        val response = adminAuthService.login(request.username, request.password, httpRequest)
        return ResponseEntity.ok(response)
    }

    /**
     * Обновление токена
     */
    @Operation(
        summary = "Обновление токена",
        description = "Получение нового access token по refresh token"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Токен успешно обновлен",
                content = [Content(schema = Schema(implementation = AdminAuthService.AuthResponse::class))]
            ),
            ApiResponse(responseCode = "401", description = "Неверный или истекший refresh token")
        ]
    )
    @PostMapping("/refresh")
    fun refreshToken(
        @Valid @RequestBody request: RefreshTokenRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<AdminAuthService.AuthResponse> {
        val response = adminAuthService.refreshToken(request.refreshToken, httpRequest)
        return ResponseEntity.ok(response)
    }

    /**
     * Выход из системы
     */
    @Operation(
        summary = "Выход из системы",
        description = "Выход администратора и удаление сессии"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Успешный выход"),
            ApiResponse(responseCode = "401", description = "Неавторизован")
        ]
    )
    @PostMapping("/logout")
    fun logout(
        @RequestHeader("Authorization") authHeader: String,
        httpRequest: HttpServletRequest
    ): ResponseEntity<Map<String, String>> {
        val token = authHeader.removePrefix("Bearer ")
        adminAuthService.logout(token, httpRequest)
        return ResponseEntity.ok(mapOf("message" to "Successfully logged out"))
    }

    /**
     * Получение информации о текущем пользователе
     */
    @Operation(
        summary = "Текущий пользователь",
        description = "Получение информации о текущем аутентифицированном пользователе"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Информация о пользователе",
                content = [Content(schema = Schema(implementation = CurrentUserResponse::class))]
            ),
            ApiResponse(responseCode = "401", description = "Неавторизован")
        ]
    )
    @GetMapping("/me")
    fun getCurrentUser(
        authentication: Authentication,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<CurrentUserResponse> {
        val token = authHeader.removePrefix("Bearer ")
        val adminUser = adminAuthService.validateToken(token)
            ?: throw IllegalStateException("User not found")

        return ResponseEntity.ok(
            CurrentUserResponse(
                id = adminUser.id!!,
                username = adminUser.username,
                email = adminUser.email,
                role = adminUser.role.name
            )
        )
    }
}

