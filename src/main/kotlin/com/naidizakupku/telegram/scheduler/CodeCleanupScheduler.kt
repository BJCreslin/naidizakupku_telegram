package com.naidizakupku.telegram.scheduler

import com.naidizakupku.telegram.service.UserCodeService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class CodeCleanupScheduler(
    private val userCodeService: UserCodeService
) {
    
    companion object {
        private val logger = LoggerFactory.getLogger(CodeCleanupScheduler::class.java)
    }
    
    @Scheduled(fixedRate = 300000) // каждые 5 минут (300000 мс)
    fun cleanupExpiredCodes() {
        logger.debug("Запуск очистки просроченных кодов")
        userCodeService.cleanupExpiredCodes()
    }
}
