package com.naidizakupku.telegram.service

import com.naidizakupku.telegram.domain.UserCode
import com.naidizakupku.telegram.repository.UserCodeRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class CodeGenerationServiceTest {

    private lateinit var userCodeRepository: UserCodeRepository
    private lateinit var metricsService: MetricsService
    private lateinit var codeGenerationService: CodeGenerationService

    @BeforeEach
    fun setUp() {
        userCodeRepository = mockk()
        metricsService = mockk(relaxed = true)
        
        codeGenerationService = CodeGenerationService(
            userCodeRepository,
            metricsService
        )
    }

    @Test
    fun `generateUniqueCode should generate valid 7-digit code`() {
        // Given
        val now = LocalDateTime.now()
        every { userCodeRepository.existsByCodeAndNotExpired(any(), now) } returns false
        every { userCodeRepository.save(any()) } answers { firstArg() }

        // When
        val code = codeGenerationService.generateUniqueCode()

        // Then
        assertTrue(code.matches(Regex("^[1-9]\\d{6}$")))
        assertEquals(7, code.length)
    }

    @Test
    fun `createUserCode should create UserCode with correct expiration`() {
        // Given
        val telegramUserId = 123L
        val now = LocalDateTime.now()
        every { userCodeRepository.existsByCodeAndNotExpired(any(), now) } returns false
        every { userCodeRepository.save(any<UserCode>()) } answers { 
            val code = firstArg<UserCode>()
            code.copy(id = 1)
        }

        // When
        val result = codeGenerationService.createUserCode(telegramUserId)

        // Then
        assertEquals(telegramUserId, result.telegramUserId)
        assertNotNull(result.expiresAt)
        assertTrue(result.expiresAt.isAfter(LocalDateTime.now()))
        verify { metricsService.incrementCodeGenerated() }
        verify { metricsService.recordCodeGenerationTime(any(), any()) }
    }

    @Test
    fun `generateUniqueCode should retry when code already exists`() {
        // Given
        val now = LocalDateTime.now()
        every { userCodeRepository.existsByCodeAndNotExpired(any(), now) } returnsMany listOf(true, true, false)
        every { userCodeRepository.save(any()) } answers { firstArg() }

        // When
        val code = codeGenerationService.generateUniqueCode()

        // Then
        assertTrue(code.matches(Regex("^[1-9]\\d{6}$")))
        verify(atLeast = 3) { userCodeRepository.existsByCodeAndNotExpired(any(), now) }
    }
}

