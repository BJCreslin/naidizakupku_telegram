package com.naidizakupku.telegram.service

import com.naidizakupku.telegram.domain.admin.AdminAuditLog
import com.naidizakupku.telegram.repository.admin.AdminAuditLogRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Сервис для логирования действий админов
 */
@Service
class AdminAuditService(
    private val adminAuditLogRepository: AdminAuditLogRepository
) {

    /**
     * Логирует действие админа
     */
    @Transactional
    fun logAction(
        adminUserId: Long?,
        action: String,
        entityType: String? = null,
        entityId: Long? = null,
        details: String? = null,
        request: HttpServletRequest? = null
    ) {
        val auditLog = AdminAuditLog(
            adminUserId = adminUserId,
            action = action,
            entityType = entityType,
            entityId = entityId,
            details = details,
            ipAddress = request?.remoteAddr,
            userAgent = request?.getHeader("User-Agent")
        )
        
        adminAuditLogRepository.save(auditLog)
    }

    /**
     * Логирует действие админа с дополнительными параметрами
     */
    @Transactional
    fun logAction(
        adminUserId: Long?,
        action: String,
        entityType: String?,
        entityId: Long?,
        details: String?,
        ipAddress: String?,
        userAgent: String?
    ) {
        val auditLog = AdminAuditLog(
            adminUserId = adminUserId,
            action = action,
            entityType = entityType,
            entityId = entityId,
            details = details,
            ipAddress = ipAddress,
            userAgent = userAgent
        )
        
        adminAuditLogRepository.save(auditLog)
    }
}

