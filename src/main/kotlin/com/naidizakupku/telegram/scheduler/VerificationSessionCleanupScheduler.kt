package com.naidizakupku.telegram.scheduler

import com.naidizakupku.telegram.service.VerificationSessionService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class VerificationSessionCleanupScheduler(
    private val verificationSessionService: VerificationSessionService
) {
    
    private val logger = LoggerFactory.getLogger(VerificationSessionCleanupScheduler::class.java)
    
    @Value("\${verification.session.cleanup.minutes:30}")
    private var cleanupMinutes: Long = 30
    
    /**
     * Очистка просроченных сессий верификации каждые 5 минут
     */
    @Scheduled(fixedRate = 300000) // 5 минут
    fun cleanupExpiredSessions() {
        try {
            val cutoffTime = Instant.now().minusSeconds(cleanupMinutes * 60)
            logger.info("Запуск очистки просроченных сессий верификации, старше $cleanupMinutes минут")
            
            val cleanedCount = verificationSessionService.cleanupExpiredSessions(cutoffTime)
            
            if (cleanedCount > 0) {
                logger.info("Очищено $cleanedCount просроченных сессий верификации")
            } else {
                logger.debug("Просроченных сессий для очистки не найдено")
            }
            
        } catch (e: Exception) {
            logger.error("Ошибка при очистке просроченных сессий: ${e.message}", e)
        }
    }
}
