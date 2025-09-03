package com.naidizakupku.telegram.service

import com.naidizakupku.telegram.domain.UserCode
import com.naidizakupku.telegram.repository.UserCodeRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.ThreadLocalRandom

@Service
class CodeGenerationService(
    private val userCodeRepository: UserCodeRepository
) {
    
    companion object {
        private val logger = LoggerFactory.getLogger(CodeGenerationService::class.java)
        private const val MAX_ATTEMPTS = 100
    }
    
    @Value("\${telegram.code.expiration.minutes:5}")
    private var codeExpirationMinutes: Int = 5
    
    fun generateUniqueCode(): String {
        var attempts = 0
        var code: String
        
        do {
            code = generateRandomCode()
            attempts++
            
            if (attempts > MAX_ATTEMPTS) {
                logger.error("Не удалось сгенерировать уникальный код после $MAX_ATTEMPTS попыток")
                throw RuntimeException("Не удалось сгенерировать уникальный код")
            }
        } while (userCodeRepository.existsByCodeAndNotExpired(code))
        
        logger.info("Сгенерирован уникальный код: $code за $attempts попыток")
        return code
    }
    
    fun createUserCode(telegramUserId: Long): UserCode {
        val code = generateUniqueCode()
        val expiresAt = LocalDateTime.now().plusMinutes(codeExpirationMinutes.toLong())
        
        val userCode = UserCode(
            code = code,
            telegramUserId = telegramUserId,
            expiresAt = expiresAt
        )
        
        val savedCode = userCodeRepository.save(userCode)
        logger.info("Создан код для пользователя $telegramUserId: $code, истекает в $expiresAt")
        
        return savedCode
    }
    
    private fun generateRandomCode(): String {
        // Генерируем код от 1000000 до 9999999 (первая цифра не 0)
        val min = 1000000
        val max = 9999999
        val randomCode = ThreadLocalRandom.current().nextInt(min, max + 1)
        return randomCode.toString()
    }
    
    fun getCodeExpirationMinutes(): Int = codeExpirationMinutes
}
