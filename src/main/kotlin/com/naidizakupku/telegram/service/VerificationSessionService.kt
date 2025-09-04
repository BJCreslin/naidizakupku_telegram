package com.naidizakupku.telegram.service

import com.naidizakupku.telegram.domain.dto.UserBrowserInfoDto
import com.naidizakupku.telegram.domain.entity.VerificationSession
import com.naidizakupku.telegram.domain.entity.VerificationStatus
import com.naidizakupku.telegram.repository.UserCodeRepository
import com.naidizakupku.telegram.repository.VerificationSessionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Service
class VerificationSessionService(
    private val verificationSessionRepository: VerificationSessionRepository,
    private val userCodeRepository: UserCodeRepository
) {

    @Transactional
    fun createVerificationSession(
        correlationId: UUID,
        code: String,
        userBrowserInfo: UserBrowserInfoDto
    ): VerificationSession? {
        // Проверяем, что код существует и не просрочен
        val userCode = userCodeRepository.findByCodeAndNotExpired(code)
            ?: return null

        val session = VerificationSession(
            correlationId = correlationId,
            telegramUserId = userCode.telegramUserId,
            code = code,
            browserInfo = userBrowserInfo.toJsonString()
        )

        return verificationSessionRepository.save(session)
    }

    @Transactional
    fun updateSessionStatus(correlationId: UUID, status: VerificationStatus): Boolean {
        val session = verificationSessionRepository.findByCorrelationId(correlationId)
            ?: return false

        verificationSessionRepository.updateStatus(session.id!!, status, Instant.now())
        return true
    }

    fun findByCorrelationId(correlationId: UUID): VerificationSession? {
        return verificationSessionRepository.findByCorrelationId(correlationId)
    }

    fun findExpiredSessions(cutoffTime: Instant): List<VerificationSession> {
        return verificationSessionRepository.findExpiredSessions(cutoffTime)
    }

    @Transactional
    fun cleanupExpiredSessions(cutoffTime: Instant): Int {
        val expiredSessions = findExpiredSessions(cutoffTime)
        if (expiredSessions.isNotEmpty()) {
            val ids = expiredSessions.mapNotNull { it.id }
            verificationSessionRepository.deleteByIds(ids)
        }
        return expiredSessions.size
    }

    private fun UserBrowserInfoDto.toJsonString(): String {
        return """
            {
                "ip": "$ip",
                "userAgent": "$userAgent",
                "location": "$location"
            }
        """.trimIndent()
    }
}
