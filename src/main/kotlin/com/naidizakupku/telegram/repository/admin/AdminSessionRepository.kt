package com.naidizakupku.telegram.repository.admin

import com.naidizakupku.telegram.domain.admin.AdminSession
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * Репозиторий для работы с сессиями админов
 */
@Repository
interface AdminSessionRepository : JpaRepository<AdminSession, Long> {
    
    /**
     * Найти сессию по refresh token
     */
    fun findByRefreshToken(refreshToken: String): AdminSession?
    
    /**
     * Найти все сессии пользователя
     */
    fun findByAdminUserId(adminUserId: Long): List<AdminSession>
    
    /**
     * Найти активные сессии пользователя
     */
    @Query("SELECT s FROM AdminSession s WHERE s.adminUserId = :adminUserId AND s.expiresAt > :now")
    fun findActiveSessionsByAdminUserId(
        @Param("adminUserId") adminUserId: Long,
        @Param("now") now: LocalDateTime
    ): List<AdminSession>
    
    /**
     * Удалить все сессии пользователя
     */
    @Modifying
    @Query("DELETE FROM AdminSession s WHERE s.adminUserId = :adminUserId")
    fun deleteAllByAdminUserId(@Param("adminUserId") adminUserId: Long)
    
    /**
     * Удалить истекшие сессии
     */
    @Modifying
    @Query("DELETE FROM AdminSession s WHERE s.expiresAt < :now")
    fun deleteExpiredSessions(@Param("now") now: LocalDateTime)
    
    /**
     * Удалить сессию по refresh token
     */
    @Modifying
    fun deleteByRefreshToken(refreshToken: String)
}

