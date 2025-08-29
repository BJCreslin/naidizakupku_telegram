package com.naidizakupku.telegram.service

import com.naidizakupku.telegram.domain.User
import com.naidizakupku.telegram.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * Сервис для работы с пользователями
 */
@Service
@Transactional
class UserService(
    private val userRepository: UserRepository
) {
    
    /**
     * Создать нового пользователя
     */
    suspend fun createUser(
        telegramId: Long,
        username: String,
        firstName: String? = null,
        lastName: String? = null
    ): User {
        if (userRepository.existsByTelegramId(telegramId)) {
            throw IllegalArgumentException("Пользователь с Telegram ID $telegramId уже существует")
        }
        
        val user = User(
            telegramId = telegramId,
            username = username,
            firstName = firstName,
            lastName = lastName
        )
        
        return userRepository.save(user)
    }
    
    /**
     * Найти пользователя по Telegram ID
     */
    suspend fun findByTelegramId(telegramId: Long): User? {
        return userRepository.findByTelegramId(telegramId)
    }
    
    /**
     * Обновить пользователя
     */
    suspend fun updateUser(user: User): User {
        val updatedUser = user.copy(updatedAt = LocalDateTime.now())
        return userRepository.save(updatedUser)
    }
    
    /**
     * Деактивировать пользователя
     */
    suspend fun deactivateUser(telegramId: Long): User? {
        val user = userRepository.findByTelegramId(telegramId) ?: return null
        val deactivatedUser = user.copy(active = false, updatedAt = LocalDateTime.now())
        return userRepository.save(deactivatedUser)
    }
    
    /**
     * Получить всех активных пользователей
     */
    suspend fun getAllActiveUsers(): List<User> {
        return userRepository.findByActiveTrue()
    }
}

