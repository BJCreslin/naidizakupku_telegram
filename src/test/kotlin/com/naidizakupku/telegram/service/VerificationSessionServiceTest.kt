package com.naidizakupku.telegram.service

import com.naidizakupku.telegram.domain.dto.UserBrowserInfoDto
import com.naidizakupku.telegram.domain.entity.UserCode
import com.naidizakupku.telegram.domain.entity.VerificationSession
import com.naidizakupku.telegram.domain.entity.VerificationStatus
import com.naidizakupku.telegram.repository.UserCodeRepository
import com.naidizakupku.telegram.repository.VerificationSessionRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import java.time.Instant
import java.util.UUID

class VerificationSessionServiceTest : FunSpec({
    
    val userCodeRepository = mockk<UserCodeRepository>()
    val verificationSessionRepository = mockk<VerificationSessionRepository>()
    val service = VerificationSessionService(userCodeRepository, verificationSessionRepository)
    
    val testCorrelationId = UUID.randomUUID()
    val testTelegramUserId = 123456789L
    val testCode = "1234567"
    val testBrowserInfo = UserBrowserInfoDto("192.168.1.1", "Chrome/120.0.0.0", "Moscow, Russia")
    
    beforeEach {
        clearAllMocks()
    }
    
    test("createVerificationSession should create session when code is valid") {
        // Given
        val userCode = UserCode(
            id = 1L,
            telegramUserId = testTelegramUserId,
            code = testCode,
            expiresAt = Instant.now().plusSeconds(300)
        )
        
        every { userCodeRepository.findByCodeAndExpiresAtAfter(testCode, any()) } returns userCode
        every { verificationSessionRepository.save(any()) } answers { firstArg() }
        
        // When
        val result = service.createVerificationSession(testCorrelationId, testCode, testBrowserInfo)
        
        // Then
        result shouldNotBe null
        result!!.correlationId shouldBe testCorrelationId
        result.telegramUserId shouldBe testTelegramUserId
        result.code shouldBe testCode
        result.status shouldBe VerificationStatus.PENDING
        
        verify { 
            userCodeRepository.findByCodeAndExpiresAtAfter(testCode, any())
            verificationSessionRepository.save(any())
        }
    }
    
    test("createVerificationSession should return null when code is not found") {
        // Given
        every { userCodeRepository.findByCodeAndExpiresAtAfter(testCode, any()) } returns null
        
        // When
        val result = service.createVerificationSession(testCorrelationId, testCode, testBrowserInfo)
        
        // Then
        result shouldBe null
        
        verify { 
            userCodeRepository.findByCodeAndExpiresAtAfter(testCode, any())
        }
        verify(exactly = 0) { verificationSessionRepository.save(any()) }
    }
    
    test("updateSessionStatus should update status successfully") {
        // Given
        val session = VerificationSession(
            id = 1L,
            correlationId = testCorrelationId,
            telegramUserId = testTelegramUserId,
            code = testCode,
            browserInfo = "{}",
            status = VerificationStatus.PENDING
        )
        
        every { verificationSessionRepository.findByCorrelationId(testCorrelationId) } returns session
        every { verificationSessionRepository.updateStatus(1L, VerificationStatus.CONFIRMED, any()) } just Runs
        
        // When
        val result = service.updateSessionStatus(testCorrelationId, VerificationStatus.CONFIRMED)
        
        // Then
        result shouldBe true
        
        verify { 
            verificationSessionRepository.findByCorrelationId(testCorrelationId)
            verificationSessionRepository.updateStatus(1L, VerificationStatus.CONFIRMED, any())
        }
    }
    
    test("updateSessionStatus should return false when session not found") {
        // Given
        every { verificationSessionRepository.findByCorrelationId(testCorrelationId) } returns null
        
        // When
        val result = service.updateSessionStatus(testCorrelationId, VerificationStatus.CONFIRMED)
        
        // Then
        result shouldBe false
        
        verify { verificationSessionRepository.findByCorrelationId(testCorrelationId) }
        verify(exactly = 0) { verificationSessionRepository.updateStatus(any(), any(), any()) }
    }
    
    test("cleanupExpiredSessions should delete expired sessions") {
        // Given
        val cutoffTime = Instant.now().minusSeconds(1800) // 30 minutes ago
        val expiredSessions = listOf(
            VerificationSession(id = 1L, correlationId = UUID.randomUUID(), telegramUserId = 1L, code = "1111111", browserInfo = "{}"),
            VerificationSession(id = 2L, correlationId = UUID.randomUUID(), telegramUserId = 2L, code = "2222222", browserInfo = "{}")
        )
        
        every { verificationSessionRepository.findExpiredSessions(cutoffTime) } returns expiredSessions
        every { verificationSessionRepository.deleteByIds(listOf(1L, 2L)) } just Runs
        
        // When
        val result = service.cleanupExpiredSessions(cutoffTime)
        
        // Then
        result shouldBe 2
        
        verify { 
            verificationSessionRepository.findExpiredSessions(cutoffTime)
            verificationSessionRepository.deleteByIds(listOf(1L, 2L))
        }
    }
})
