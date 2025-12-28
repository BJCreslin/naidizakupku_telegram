package com.naidizakupku.telegram.service

import com.naidizakupku.telegram.domain.UserCode
import com.naidizakupku.telegram.repository.AuthRequestRepository
import com.naidizakupku.telegram.repository.UserCodeRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class UserCodeServiceTest {

    private lateinit var userCodeRepository: UserCodeRepository
    private lateinit var authRequestRepository: AuthRequestRepository
    private lateinit var codeGenerationService: CodeGenerationService
    private lateinit var telegramNotificationService: TelegramNotificationService
    private lateinit var kafkaProducerService: KafkaProducerService
    private lateinit var telegramBotExecutor: TelegramBotExecutor
    private lateinit var metricsService: MetricsService
    private lateinit var userCodeService: UserCodeService

    @BeforeEach
    fun setUp() {
        userCodeRepository = mockk()
        authRequestRepository = mockk()
        codeGenerationService = mockk()
        telegramNotificationService = mockk()
        kafkaProducerService = mockk()
        telegramBotExecutor = mockk()
        metricsService = mockk(relaxed = true)
        
        userCodeService = UserCodeService(
            userCodeRepository,
            authRequestRepository,
            codeGenerationService,
            telegramNotificationService,
            kafkaProducerService,
            telegramBotExecutor,
            metricsService
        )
    }

    @Test
    fun `verifyCode should return true when code exists and not expired`() {
        // Given
        val code = "1234567"
        val now = LocalDateTime.now()
        every { userCodeRepository.existsByCodeAndNotExpired(code, now) } returns true

        // When
        val result = userCodeService.verifyCode(code)

        // Then
        assertTrue(result)
        verify { metricsService.incrementCodeVerified(true) }
    }

    @Test
    fun `verifyCode should return false when code does not exist or expired`() {
        // Given
        val code = "1234567"
        val now = LocalDateTime.now()
        every { userCodeRepository.existsByCodeAndNotExpired(code, now) } returns false

        // When
        val result = userCodeService.verifyCode(code)

        // Then
        assertFalse(result)
        verify { metricsService.incrementCodeVerified(false) }
    }

    @Test
    fun `getOrCreateUserCode should return existing code when available`() {
        // Given
        val telegramUserId = 123L
        val existingCode = UserCode(
            id = 1,
            code = "1234567",
            telegramUserId = telegramUserId,
            expiresAt = LocalDateTime.now().plusMinutes(5)
        )
        val now = LocalDateTime.now()
        
        every { userCodeRepository.findActiveCodeByTelegramUserId(telegramUserId, now) } returns existingCode

        // When
        val result = userCodeService.getOrCreateUserCode(telegramUserId)

        // Then
        assertEquals("1234567", result.code)
        assertFalse(result.isNew)
        verify(exactly = 0) { codeGenerationService.createUserCode(any()) }
    }

    @Test
    fun `getOrCreateUserCode should create new code when none exists`() {
        // Given
        val telegramUserId = 123L
        val newCode = UserCode(
            id = 1,
            code = "7654321",
            telegramUserId = telegramUserId,
            expiresAt = LocalDateTime.now().plusMinutes(5)
        )
        val now = LocalDateTime.now()
        
        every { userCodeRepository.findActiveCodeByTelegramUserId(telegramUserId, now) } returns null
        every { codeGenerationService.createUserCode(telegramUserId) } returns newCode

        // When
        val result = userCodeService.getOrCreateUserCode(telegramUserId)

        // Then
        assertEquals("7654321", result.code)
        assertTrue(result.isNew)
        verify { codeGenerationService.createUserCode(telegramUserId) }
    }
}

