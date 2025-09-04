package com.naidizakupku.telegram.controller

import com.naidizakupku.telegram.service.KafkaVerificationService
import com.naidizakupku.telegram.service.UserCodeService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
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
        @RequestBody request: VerificationRequest,
    ): ResponseEntity<Boolean> {
        return ResponseEntity.ok().body(userCodeService.verifyCode(request.code))
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
        val code: String,
        /*** IP адрес запрашиваемого */
        val ip: String? = null,
        /*** User Agent запрашиваемого */
        val userAgent: String? = null,
        /*** Локация запрашиваемого */
        val location: String? = null
    )
}
