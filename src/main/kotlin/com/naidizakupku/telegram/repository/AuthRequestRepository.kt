package com.naidizakupku.telegram.repository

import com.naidizakupku.telegram.domain.AuthRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Repository для работы с запросами аутентификации
 */
@Repository
interface AuthRequestRepository : JpaRepository<AuthRequest, Long> {

    /**
     * Находит запрос по trace ID
     */
    fun findByTraceId(traceId: UUID): AuthRequest?

    /**
     * Находит все запросы для пользователя
     */
    @Query("SELECT ar FROM AuthRequest ar WHERE ar.telegramUserId = :telegramUserId ORDER BY ar.requestedAt DESC")
    fun findByTelegramUserId(@Param("telegramUserId") telegramUserId: Long): List<AuthRequest>

    /**
     * Проверяет существование запроса по trace ID
     */
    fun existsByTraceId(traceId: UUID): Boolean

    /**
     * Удаляет запрос по trace ID
     */
    fun deleteByTraceId(traceId: UUID)
}
