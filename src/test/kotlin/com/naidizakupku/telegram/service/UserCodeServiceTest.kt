package com.naidizakupku.telegram.service

import com.naidizakupku.telegram.domain.UserCode
import com.naidizakupku.telegram.repository.UserCodeRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import java.time.LocalDateTime

class UserCodeServiceTest : FunSpec({
    
    val mockUserCodeRepository = mockk<UserCodeRepository>()
    val mockCodeGenerationService = mockk<CodeGenerationService>()
    val userCodeService = UserCodeService(mockUserCodeRepository, mockCodeGenerationService)
    
    beforeEach {
        clearAllMocks()
    }
    
    test("getOrCreateUserCode должен вернуть существующий активный код") {
        // Given
        val telegramUserId = 123L
        val existingCode = UserCode(
            id = 1L,
            code = "1234567",
            telegramUserId = telegramUserId,
            expiresAt = LocalDateTime.now().plusMinutes(10)
        )
        
        every { mockUserCodeRepository.findActiveCodeByTelegramUserId(telegramUserId, any()) } returns existingCode
        
        // When
        val result = userCodeService.getOrCreateUserCode(telegramUserId)
        
        // Then
        result.code shouldBe "1234567"
        result.isNew shouldBe false
        verify(exactly = 1) { mockUserCodeRepository.findActiveCodeByTelegramUserId(telegramUserId, any()) }
        verify(exactly = 0) { mockCodeGenerationService.createUserCode(any()) }
    }
    
    test("getOrCreateUserCode должен создать новый код если активного нет") {
        // Given
        val telegramUserId = 123L
        val newCode = UserCode(
            id = 1L,
            code = "7654321",
            telegramUserId = telegramUserId,
            expiresAt = LocalDateTime.now().plusMinutes(5)
        )
        
        every { mockUserCodeRepository.findActiveCodeByTelegramUserId(telegramUserId, any()) } returns null
        every { mockCodeGenerationService.createUserCode(telegramUserId) } returns newCode
        
        // When
        val result = userCodeService.getOrCreateUserCode(telegramUserId)
        
        // Then
        result.code shouldBe "7654321"
        result.isNew shouldBe true
        verify(exactly = 1) { mockCodeGenerationService.createUserCode(telegramUserId) }
    }
    
    test("formatExpirationTime должен содержать правильный формат времени для UTC+3") {
        // Given
        val expiresAt = LocalDateTime.of(2024, 1, 1, 15, 30)
        val timezone = "UTC+3"
        
        // When
        val result = userCodeService.formatExpirationTime(expiresAt, timezone)
        
        // Then
        result.contains("(МСК)") shouldBe true
        result.contains(":") shouldBe true
        result.length shouldBe 11 // "XX:XX (МСК)"
    }
    
    test("cleanupExpiredCodes должен удалять просроченные коды") {
        // Given
        val now = LocalDateTime.now()
        val expiredCodes = listOf(
            UserCode(code = "1111111", telegramUserId = 1L, expiresAt = now.minusMinutes(1)),
            UserCode(code = "2222222", telegramUserId = 2L, expiresAt = now.minusMinutes(2))
        )
        
        every { mockUserCodeRepository.findExpiredCodes(any()) } returns expiredCodes
        every { mockUserCodeRepository.deleteExpiredCodes(any()) } just Runs
        
        // When
        userCodeService.cleanupExpiredCodes()
        
        // Then
        verify(exactly = 1) { mockUserCodeRepository.deleteExpiredCodes(any()) }
    }
})
