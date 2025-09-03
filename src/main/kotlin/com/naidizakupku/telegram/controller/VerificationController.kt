package com.naidizakupku.telegram.controller

import com.naidizakupku.telegram.domain.dto.CodeVerificationRequestDto
import com.naidizakupku.telegram.domain.dto.UserBrowserInfoDto
import com.naidizakupku.telegram.service.KafkaVerificationService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("/api/verification")
class VerificationController(
    private val kafkaVerificationService: KafkaVerificationService
) {
    
    private val logger = LoggerFactory.getLogger(VerificationController::class.java)
    
    /**
     * Тестовый endpoint для отправки запроса верификации
     */
    @PostMapping("/test")
    fun testVerification(
        @RequestBody request: TestVerificationRequest
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Тестовый запрос верификации: code=${request.code}")
        
        try {
            val verificationRequest = CodeVerificationRequestDto(
                correlationId = UUID.randomUUID(),
                code = request.code,
                userBrowserInfo = UserBrowserInfoDto(
                    ip = request.ip ?: "127.0.0.1",
                    userAgent = request.userAgent ?: "TestBot/1.0",
                    location = request.location ?: "Test Location"
                ),
                timestamp = Instant.now()
            )
            
            // Обрабатываем запрос напрямую (для тестирования)
            kafkaVerificationService.processVerificationRequest(verificationRequest)
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "correlationId" to verificationRequest.correlationId.toString(),
                "message" to "Verification request processed"
            ))
            
        } catch (e: Exception) {
            logger.error("Ошибка тестового запроса верификации: ${e.message}", e)
            return ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "error" to e.message
            ))
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
            return ResponseEntity.ok(mapOf(
                "correlationId" to correlationId,
                "status" to "PENDING",
                "message" to "Status retrieval not implemented yet"
            ))
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.badRequest().body(mapOf(
                "error" to "Invalid correlation ID format"
            ))
        }
    }
    
    data class TestVerificationRequest(
        val code: String,
        val ip: String? = null,
        val userAgent: String? = null,
        val location: String? = null
    )
}
