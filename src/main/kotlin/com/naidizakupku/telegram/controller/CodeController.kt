package com.naidizakupku.telegram.controller

import com.naidizakupku.telegram.service.KafkaVerificationService
import com.naidizakupku.telegram.service.UserCodeService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.springframework.lang.Nullable
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.messaging.handler.annotation.Header as SpringHeader
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/code")
@Tag(name = "Code Verification", description = "API для работы с кодами верификации и авторизации")
class CodeController(
    private val kafkaVerificationService: KafkaVerificationService,
    private val userCodeService: UserCodeService
) {

    private val logger = LoggerFactory.getLogger(CodeController::class.java)

    /**
     * Endpoint для проверки кода
     */
    @Operation(
        summary = "Проверить код",
        description = "Проверяет существование и валидность временного кода"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Результат проверки кода",
                content = [Content(schema = Schema(implementation = Boolean::class))]
            ),
            ApiResponse(responseCode = "400", description = "Некорректный формат кода")
        ]
    )
    @PostMapping("/verify")
    fun verifyCode(
        @Parameter(description = "Данные для проверки кода", required = true)
        @RequestBody @Valid request: VerificationRequest
    ): ResponseEntity<Boolean> {
        return ResponseEntity.ok().body(userCodeService.verifyCode(request.code))
    }

    /**
     * Endpoint для отправки кода для аутентификации
     */
    @Operation(
        summary = "Проверить код для аутентификации",
        description = """
            Проверяет код и отправляет запрос на подтверждение авторизации пользователю в Telegram.
            Требуется заголовок X-Trace-Id для отслеживания запроса.
        """
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Код верифицирован, запрос отправлен пользователю",
                content = [Content(
                    schema = Schema(implementation = Map::class),
                    examples = [ExampleObject(value = "{\"success\": true, \"message\": \"Код верифицирован, запрос отправлен пользователю\"}")]
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Код не найден или просрочен",
                content = [Content(
                    schema = Schema(implementation = Map::class),
                    examples = [ExampleObject(value = "{\"success\": false, \"error\": \"Код не найден или просрочен\"}")]
                )]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Внутренняя ошибка сервера",
                content = [Content(schema = Schema(implementation = Map::class))]
            )
        ]
    )
    @PostMapping("/auth")
    fun sendCode(
        @Parameter(description = "Данные для проверки кода", required = true)
        @RequestBody @Valid request: VerificationRequest,
        @Parameter(
            description = "Уникальный идентификатор запроса (UUID)",
            required = true,
            example = "550e8400-e29b-41d4-a716-446655440000"
        )
        @SpringHeader("X-Trace-Id") traceId: UUID
    ): ResponseEntity<Map<String, Any>> {
        val result = userCodeService.verifyCodeForAuth(request, traceId)
        
        return when (result) {
            is UserCodeService.AuthVerificationResult.Success -> {
                ResponseEntity.ok(mapOf<String, Any>(
                    "success" to true,
                    "message" to "Код верифицирован, запрос отправлен пользователю"
                ))
            }
            is UserCodeService.AuthVerificationResult.CodeNotFound -> {
                ResponseEntity.badRequest().body(mapOf<String, Any>(
                    "success" to false,
                    "error" to "Код не найден или просрочен"
                ))
            }
            is UserCodeService.AuthVerificationResult.CodeExpired -> {
                ResponseEntity.badRequest().body(mapOf<String, Any>(
                    "success" to false,
                    "error" to "Код просрочен"
                ))
            }
            is UserCodeService.AuthVerificationResult.Error -> {
                ResponseEntity.status(500).body(mapOf<String, Any>(
                    "success" to false,
                    "error" to (result.message ?: "Неизвестная ошибка")
                ))
            }
        }
    }

    /**
     * Получение статуса сессии верификации
     */
    @Operation(
        summary = "Получить статус сессии верификации",
        description = "Возвращает статус сессии верификации по correlation ID (UUID)"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Статус сессии",
                content = [Content(
                    schema = Schema(implementation = Map::class),
                    examples = [ExampleObject(value = "{\"correlationId\": \"uuid\", \"status\": \"PENDING\", \"message\": \"Status retrieval not implemented yet\"}")]
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Некорректный формат correlation ID",
                content = [Content(
                    schema = Schema(implementation = Map::class),
                    examples = [ExampleObject(value = "{\"error\": \"Invalid correlation ID format\"}")]
                )]
            )
        ]
    )
    @GetMapping("/status/{correlationId}")
    fun getVerificationStatus(
        @Parameter(
            description = "Correlation ID сессии верификации (UUID)",
            required = true,
            example = "550e8400-e29b-41d4-a716-446655440000"
        )
        @PathVariable correlationId: String
    ): ResponseEntity<Map<String, Any>> {
        val uuid = try {
            UUID.fromString(correlationId)
        } catch (e: IllegalArgumentException) {
            logger.warn("Некорректный формат correlationId: $correlationId")
            return ResponseEntity.badRequest().body(
                mapOf(
                    "error" to "Invalid correlation ID format",
                    "message" to "Correlation ID должен быть в формате UUID (например: 550e8400-e29b-41d4-a716-446655440000)"
                )
            )
        }
        
        val session = kafkaVerificationService.getVerificationSessionStatus(uuid)
        
        return if (session != null) {
            ResponseEntity.ok(
                mapOf<String, Any>(
                    "correlationId" to correlationId,
                    "status" to session.status.name,
                    "telegramUserId" to session.telegramUserId,
                    "createdAt" to session.createdAt.toString(),
                    "updatedAt" to (session.updatedAt?.toString() ?: "")
                )
            )
        } else {
            ResponseEntity.status(404).body(
                mapOf<String, Any>(
                    "correlationId" to correlationId,
                    "error" to "Session not found"
                )
            )
        }
    }

    @Schema(description = "Запрос на верификацию кода")
    data class VerificationRequest(
        /** Код для проверки */
        @field:Schema(
            description = "7-значный код для проверки (от 1000000 до 9999999)",
            example = "1234567",
            required = true
        )
        @field:NotBlank(message = "Код обязателен")
        @field:Pattern(regexp = "^[1-9]\\d{6}$", message = "Код должен быть 7-значным числом, начинающимся не с 0")
        val code: String,
        
        /** IP адрес запрашиваемого */
        @field:Schema(
            description = "IP адрес запрашивающего устройства",
            example = "192.168.1.1",
            required = false
        )
        @Nullable
        @field:Pattern(regexp = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$|^$", message = "Некорректный IP адрес")
        val ip: String? = null,
        
        /** User Agent запрашиваемого */
        @field:Schema(
            description = "User-Agent браузера или клиента",
            example = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
            required = false,
            maxLength = 500
        )
        @Nullable
        @field:Size(max = 500, message = "User-Agent не должен превышать 500 символов")
        val userAgent: String? = null,
        
        /** Локация запрашиваемого */
        @field:Schema(
            description = "Географическая локация запрашивающего",
            example = "Moscow, Russia",
            required = false,
            maxLength = 200
        )
        @Nullable
        @field:Size(max = 200, message = "Локация не должна превышать 200 символов")
        val location: String? = null
    )
}
