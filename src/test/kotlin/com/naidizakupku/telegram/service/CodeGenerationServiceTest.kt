package com.naidizakupku.telegram.service

import com.naidizakupku.telegram.domain.UserCode
import com.naidizakupku.telegram.repository.UserCodeRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import java.time.LocalDateTime

class CodeGenerationServiceTest : FunSpec({
    
    val mockUserCodeRepository = mockk<UserCodeRepository>()
    val codeGenerationService = CodeGenerationService(mockUserCodeRepository)
    
    beforeEach {
        clearAllMocks()
    }
    
    test("generateUniqueCode должен генерировать код длиной 7 символов") {
        // Given
        every { mockUserCodeRepository.existsByCodeAndNotExpired(any(), any()) } returns false
        
        // When
        val code = codeGenerationService.generateUniqueCode()
        
        // Then
        code.length shouldBe 7
        code.toIntOrNull() shouldNotBe null
        code.toInt() shouldBe code.toInt().coerceIn(1000000, 9999999)
    }
    
    test("generateUniqueCode должен генерировать код начинающийся не с 0") {
        // Given
        every { mockUserCodeRepository.existsByCodeAndNotExpired(any(), any()) } returns false
        
        // When
        val code = codeGenerationService.generateUniqueCode()
        
        // Then
        code[0] shouldNotBe '0'
    }
    
    test("generateUniqueCode должен генерировать новый код при конфликте") {
        // Given
        every { mockUserCodeRepository.existsByCodeAndNotExpired(any(), any()) } returnsMany listOf(true, false)
        
        // When
        val code = codeGenerationService.generateUniqueCode()
        
        // Then
        code.length shouldBe 7
        code.toIntOrNull() shouldNotBe null
        verify(exactly = 2) { mockUserCodeRepository.existsByCodeAndNotExpired(any(), any()) }
    }
    
    test("createUserCode должен создать UserCode с правильными данными") {
        // Given
        val telegramUserId = 123L
        val generatedCode = "1234567"
        val savedUserCode = UserCode(
            id = 1L,
            code = generatedCode,
            telegramUserId = telegramUserId,
            expiresAt = LocalDateTime.now().plusMinutes(5)
        )
        
        every { mockUserCodeRepository.existsByCodeAndNotExpired(any(), any()) } returns false
        every { mockUserCodeRepository.save(any()) } returns savedUserCode
        
        // When
        val result = codeGenerationService.createUserCode(telegramUserId)
        
        // Then
        result.telegramUserId shouldBe telegramUserId
        result.code shouldBe generatedCode
        result.expiresAt.isAfter(LocalDateTime.now()) shouldBe true
        verify(exactly = 1) { mockUserCodeRepository.save(any()) }
    }
})
