package com.naidizakupku.telegram.repository.admin

import com.naidizakupku.telegram.domain.admin.AdminAuditLog
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * Репозиторий для работы с логами аудита админов
 */
@Repository
interface AdminAuditLogRepository : JpaRepository<AdminAuditLog, Long> {
    
    /**
     * Найти все логи пользователя
     */
    fun findByAdminUserId(adminUserId: Long, pageable: Pageable): Page<AdminAuditLog>
    
    /**
     * Найти логи по действию
     */
    fun findByAction(action: String, pageable: Pageable): Page<AdminAuditLog>
    
    /**
     * Найти логи по типу сущности и ID
     */
    fun findByEntityTypeAndEntityId(entityType: String, entityId: Long, pageable: Pageable): Page<AdminAuditLog>
    
    /**
     * Найти логи в диапазоне дат
     */
    @Query("SELECT l FROM AdminAuditLog l WHERE l.createdAt BETWEEN :startDate AND :endDate")
    fun findByCreatedAtBetween(
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime,
        pageable: Pageable
    ): Page<AdminAuditLog>
    
    /**
     * Найти логи пользователя в диапазоне дат
     */
    @Query("SELECT l FROM AdminAuditLog l WHERE l.adminUserId = :adminUserId AND l.createdAt BETWEEN :startDate AND :endDate")
    fun findByAdminUserIdAndCreatedAtBetween(
        @Param("adminUserId") adminUserId: Long,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime,
        pageable: Pageable
    ): Page<AdminAuditLog>
    
    /**
     * Найти логи по действию и типу сущности
     */
    fun findByActionAndEntityType(action: String, entityType: String, pageable: Pageable): Page<AdminAuditLog>
}

