package com.naidizakupku.telegram.repository

import com.naidizakupku.telegram.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Репозиторий для работы с пользователями
 */
@Repository
interface UserRepository : JpaRepository<User, Long> {
    
    /**
     * Найти пользователя по Telegram ID
     */
    fun findByTelegramId(telegramId: Long): User?
    
    /**
     * Найти пользователя по username
     */
    fun findByUsername(username: String): User?
    
    /**
     * Проверить существование пользователя по Telegram ID
     */
    fun existsByTelegramId(telegramId: Long): Boolean
    
    /**
     * Найти всех активных пользователей
     */
    fun findByActiveTrue(): List<User>
}

