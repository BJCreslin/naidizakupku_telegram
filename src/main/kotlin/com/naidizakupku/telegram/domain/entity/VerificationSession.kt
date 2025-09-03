package com.naidizakupku.telegram.domain.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "verification_sessions")
data class VerificationSession(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(name = "correlation_id", unique = true, nullable = false)
    val correlationId: UUID,
    
    @Column(name = "telegram_user_id", nullable = false)
    val telegramUserId: Long,
    
    @Column(name = "code", nullable = false, length = 7)
    val code: String,
    
    @Column(name = "browser_info", columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    val browserInfo: String,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val status: VerificationStatus = VerificationStatus.PENDING,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant = Instant.now()
)

enum class VerificationStatus {
    PENDING,
    CONFIRMED,
    REVOKED
}
