package com.naidizakupku.telegram.controller

import com.naidizakupku.telegram.service.KafkaVerificationService
import com.naidizakupku.telegram.service.UserCodeService
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.messaging.handler.annotation.Header
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/code")
class CodeController(
    private val kafkaVerificationService: KafkaVerificationService,
    private val userCodeService: UserCodeService
) {

    private val logger = LoggerFactory.getLogger(CodeController::class.java)

    /**
     * Endpoint для проверки кода
     */
    @PostMapping("/verify")
    fun verifyCode(
        @RequestBody @Valid request: VerificationRequest,
    ): ResponseEntity<Boolean> {
        return ResponseEntity.ok().body(userCodeService.verifyCode(request.code))
    }

    /**
     * Endpoint для отправки кода для аутентификации
     */
    @PostMapping("/auth")
    fun sendCode(
        @RequestBody @Valid request: VerificationRequest,
        @Header("X-Trace-Id") traceId: UUID
    ): ResponseEntity<Map<String, Any>> {
        val result = userCodeService.verifyCodeForAuth(request, traceId)
        
        return when (result) {
            is UserCodeService.AuthVerificationResult.Success -> {
                ResponseEntity.ok(mapOf(
                    "success" to true,
                    "message" to "Код верифицирован, запрос отправлен пользователю"
                ))
            }
            is UserCodeService.AuthVerificationResult.CodeNotFound -> {
                ResponseEntity.badRequest().body(mapOf(
                    "success" to false,
                    "error" to "Код не найден или просрочен"
                ))
            }
            is UserCodeService.AuthVerificationResult.CodeExpired -> {
                ResponseEntity.badRequest().body(mapOf(
                    "success" to false,
                    "error" to "Код просрочен"
                ))
            }
            is UserCodeService.AuthVerificationResult.Error -> {
                ResponseEntity.status(500).body(mapOf(
                    "success" to false,
                    "error" to result.message
                ))
            }
        }
    }

    /**
     * Получение статуса сессии верификации
     */
    @GetMapping("/status/{correlationId}")
    fun getVerificationStatus(
        @PathVariable correlationId: String
    ): ResponseEntity<Map<String, Any>> {
        try {
            val uuid = UUID.fromString(correlationId)
            // TODO: Реализовать получение статуса из сервиса
            return ResponseEntity.ok(
                mapOf(
                    "correlationId" to correlationId,
                    "status" to "PENDING",
                    "message" to "Status retrieval not implemented yet"
                )
            )
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.badRequest().body(
                mapOf(
                    "error" to "Invalid correlation ID format"
                )
            )
        }
    }

    data class VerificationRequest(
        /** Код для проверки */
        @field:NotBlank(message = "Код обязателен")
        @field:Pattern(regexp = "^[1-9]\\d{6}$", message = "Код должен быть 7-значным числом, начинающимся не с 0")
        val code: String,
        
        /** IP адрес запрашиваемого */
        @field:Pattern(regexp = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$", message = "Некорректный IP адрес")
        val ip: String? = null,
        
        /** User Agent запрашиваемого */
        @field:Size(max = 500, message = "User-Agent не должен превышать 500 символов")
        val userAgent: String? = null,
        
        /** Локация запрашиваемого */
        @field:Size(max = 200, message = "Локация не должна превышать 200 символов")
        val location: String? = null
    )
}
