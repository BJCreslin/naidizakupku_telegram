package com.naidizakupku.telegram.service

import com.naidizakupku.telegram.domain.dto.*
import com.naidizakupku.telegram.domain.entity.VerificationSession
import com.naidizakupku.telegram.domain.entity.VerificationStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CompletableFuture

class KafkaVerificationServiceTest : FunSpec({
    
    val verificationSessionService = mockk<VerificationSessionService>()
    val telegramNotificationService = mockk<TelegramNotificationService>()
    val kafkaProducerService = mockk<KafkaProducerService>()
    val service = KafkaVerificationService(verificationSessionService, telegramNotificationService, kafkaProducerService)
    
    val testCorrelationId = UUID.randomUUID()
    val testTelegramUserId = 123456789L
    val testCode = "1234567"
    val testBrowserInfo = UserBrowserInfoDto("192.168.1.1", "Chrome/120.0.0.0", "Moscow, Russia")
    
    beforeEach {
        clearAllMocks()
    }
    
    test("processVerificationRequest should process valid request successfully") {
        // Given
        val request = CodeVerificationRequestDto(
            correlationId = testCorrelationId,
            code = testCode,
            userBrowserInfo = testBrowserInfo,
            timestamp = Instant.now()
        )
        
        val session = VerificationSession(
            id = 1L,
            correlationId = testCorrelationId,
            telegramUserId = testTelegramUserId,
            code = testCode,
            browserInfo = "{}",
            status = VerificationStatus.PENDING
        )
        
        every { verificationSessionService.createVerificationSession(testCorrelationId, testCode, testBrowserInfo) } returns session
        every { telegramNotificationService.sendVerificationRequest(session, testBrowserInfo) } returns 123L
        every { kafkaProducerService.sendVerificationResponse(any(), any(), any(), any()) } returns CompletableFuture.completedFuture(null)
        
        // When
        service.processVerificationRequest(request)
        
        // Then
        verify { 
            verificationSessionService.createVerificationSession(testCorrelationId, testCode, testBrowserInfo)
            telegramNotificationService.sendVerificationRequest(session, testBrowserInfo)
            kafkaProducerService.sendVerificationResponse(testCorrelationId, true, testTelegramUserId, "Verification request sent to user")
        }
    }
    
    test("processVerificationRequest should handle code not found") {
        // Given
        val request = CodeVerificationRequestDto(
            correlationId = testCorrelationId,
            code = testCode,
            userBrowserInfo = testBrowserInfo,
            timestamp = Instant.now()
        )
        
        every { verificationSessionService.createVerificationSession(testCorrelationId, testCode, testBrowserInfo) } returns null
        every { kafkaProducerService.sendVerificationResponse(any(), any(), any(), any()) } returns CompletableFuture.completedFuture(null)
        
        // When
        service.processVerificationRequest(request)
        
        // Then
        verify { 
            verificationSessionService.createVerificationSession(testCorrelationId, testCode, testBrowserInfo)
            kafkaProducerService.sendVerificationResponse(testCorrelationId, false, null, "Code not found or expired")
        }
        verify(exactly = 0) { telegramNotificationService.sendVerificationRequest(any(), any()) }
    }
    
    test("processVerificationRequest should handle telegram notification failure") {
        // Given
        val request = CodeVerificationRequestDto(
            correlationId = testCorrelationId,
            code = testCode,
            userBrowserInfo = testBrowserInfo,
            timestamp = Instant.now()
        )
        
        val session = VerificationSession(
            id = 1L,
            correlationId = testCorrelationId,
            telegramUserId = testTelegramUserId,
            code = testCode,
            browserInfo = "{}",
            status = VerificationStatus.PENDING
        )
        
        every { verificationSessionService.createVerificationSession(testCorrelationId, testCode, testBrowserInfo) } returns session
        every { telegramNotificationService.sendVerificationRequest(session, testBrowserInfo) } returns null
        every { kafkaProducerService.sendVerificationResponse(any(), any(), any(), any()) } returns CompletableFuture.completedFuture(null)
        
        // When
        service.processVerificationRequest(request)
        
        // Then
        verify { 
            verificationSessionService.createVerificationSession(testCorrelationId, testCode, testBrowserInfo)
            telegramNotificationService.sendVerificationRequest(session, testBrowserInfo)
            kafkaProducerService.sendVerificationResponse(testCorrelationId, false, null, "Failed to send Telegram notification")
        }
    }
    
    test("processVerificationConfirmation should confirm verification successfully") {
        // Given
        val session = VerificationSession(
            id = 1L,
            correlationId = testCorrelationId,
            telegramUserId = testTelegramUserId,
            code = testCode,
            browserInfo = "{}",
            status = VerificationStatus.PENDING
        )
        
        every { verificationSessionService.findByCorrelationId(testCorrelationId) } returns session
        every { verificationSessionService.updateSessionStatus(testCorrelationId, VerificationStatus.CONFIRMED) } returns true
        every { telegramNotificationService.updateMessageToConfirmed(testTelegramUserId, 0) } returns true
        
        // When
        val result = service.processVerificationConfirmation(testCorrelationId)
        
        // Then
        result shouldBe true
        
        verify { 
            verificationSessionService.findByCorrelationId(testCorrelationId)
            verificationSessionService.updateSessionStatus(testCorrelationId, VerificationStatus.CONFIRMED)
            telegramNotificationService.updateMessageToConfirmed(testTelegramUserId, 0)
        }
    }
    
    test("processVerificationRevocation should revoke verification successfully") {
        // Given
        val session = VerificationSession(
            id = 1L,
            correlationId = testCorrelationId,
            telegramUserId = testTelegramUserId,
            code = testCode,
            browserInfo = "{}",
            status = VerificationStatus.PENDING
        )
        
        every { verificationSessionService.findByCorrelationId(testCorrelationId) } returns session
        every { verificationSessionService.updateSessionStatus(testCorrelationId, VerificationStatus.REVOKED) } returns true
        every { telegramNotificationService.updateMessageToRevoking(testTelegramUserId, 0) } returns true
        every { kafkaProducerService.sendRevokeRequest(any(), any(), any(), any()) } returns CompletableFuture.completedFuture(null)
        
        // When
        val result = service.processVerificationRevocation(testCorrelationId)
        
        // Then
        result shouldBe true
        
        verify { 
            verificationSessionService.findByCorrelationId(testCorrelationId)
            verificationSessionService.updateSessionStatus(testCorrelationId, VerificationStatus.REVOKED)
            telegramNotificationService.updateMessageToRevoking(testTelegramUserId, 0)
            kafkaProducerService.sendRevokeRequest(any(), testTelegramUserId, testCorrelationId, "User requested revocation")
        }
    }
    
    test("processRevokeResponse should handle successful revocation") {
        // Given
        val response = AuthorizationRevokeResponseDto(
            correlationId = UUID.randomUUID(),
            originalVerificationCorrelationId = testCorrelationId,
            telegramUserId = testTelegramUserId,
            success = true,
            message = "Authorization revoked successfully",
            timestamp = Instant.now()
        )
        
        every { telegramNotificationService.sendRevocationConfirmed(testTelegramUserId) } returns true
        
        // When
        service.processRevokeResponse(response)
        
        // Then
        verify { telegramNotificationService.sendRevocationConfirmed(testTelegramUserId) }
    }
})
