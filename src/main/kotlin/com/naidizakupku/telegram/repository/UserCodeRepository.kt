package com.naidizakupku.telegram.repository

import com.naidizakupku.telegram.domain.UserCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface UserCodeRepository : JpaRepository<UserCode, Long> {
    
    @Query("SELECT uc FROM UserCode uc WHERE uc.telegramUserId = :telegramUserId AND uc.expiresAt > :now ORDER BY uc.createdAt DESC")
    fun findActiveCodeByTelegramUserId(
        @Param("telegramUserId") telegramUserId: Long,
        @Param("now") now: LocalDateTime = LocalDateTime.now()
    ): UserCode?
    
    @Query("SELECT uc FROM UserCode uc WHERE uc.code = :code AND uc.expiresAt > :now")
    fun findByCodeAndNotExpired(
        @Param("code") code: String,
        @Param("now") now: LocalDateTime = LocalDateTime.now()
    ): UserCode?
    
    @Query("SELECT uc FROM UserCode uc WHERE uc.expiresAt <= :now")
    fun findExpiredCodes(@Param("now") now: LocalDateTime = LocalDateTime.now()): List<UserCode>
    
    @Modifying
    @Query("DELETE FROM UserCode uc WHERE uc.expiresAt <= :now")
    fun deleteExpiredCodes(@Param("now") now: LocalDateTime = LocalDateTime.now())
    
    @Query("SELECT COUNT(uc) > 0 FROM UserCode uc WHERE uc.code = :code AND uc.expiresAt > :now")
    fun existsByCodeAndNotExpired(
        @Param("code") code: String,
        @Param("now") now: LocalDateTime = LocalDateTime.now()
    ): Boolean
}
