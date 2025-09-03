package com.naidizakupku.telegram.repository

import com.naidizakupku.telegram.domain.entity.VerificationSession
import com.naidizakupku.telegram.domain.entity.VerificationStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
interface VerificationSessionRepository : JpaRepository<VerificationSession, Long> {
    
    fun findByCorrelationId(correlationId: UUID): VerificationSession?
    
    fun findByTelegramUserIdAndStatus(telegramUserId: Long, status: VerificationStatus): List<VerificationSession>
    
    @Query("SELECT vs FROM VerificationSession vs WHERE vs.createdAt < :cutoffTime")
    fun findExpiredSessions(@Param("cutoffTime") cutoffTime: Instant): List<VerificationSession>
    
    @Modifying
    @Query("UPDATE VerificationSession vs SET vs.status = :status, vs.updatedAt = :updatedAt WHERE vs.id = :id")
    fun updateStatus(@Param("id") id: Long, @Param("status") status: VerificationStatus, @Param("updatedAt") updatedAt: Instant)
    
    @Modifying
    @Query("DELETE FROM VerificationSession vs WHERE vs.id IN :ids")
    fun deleteByIds(@Param("ids") ids: List<Long>)
}
