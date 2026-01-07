package com.naidizakupku.telegram.domain.admin

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * Доменная модель лога аудита действий админа
 */
@Entity
@Table(name = "admin_audit_log")
data class AdminAuditLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(name = "admin_user_id")
    val adminUserId: Long? = null,
    
    @Column(name = "action", nullable = false, length = 100)
    val action: String,
    
    @Column(name = "entity_type", length = 50)
    val entityType: String? = null,
    
    @Column(name = "entity_id")
    val entityId: Long? = null,
    
    @Column(name = "details", columnDefinition = "TEXT")
    val details: String? = null,
    
    @Column(name = "ip_address", length = 45)
    val ipAddress: String? = null,
    
    @Column(name = "user_agent", length = 500)
    val userAgent: String? = null,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

