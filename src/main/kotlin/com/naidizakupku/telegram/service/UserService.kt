package com.naidizakupku.telegram.service

import com.naidizakupku.telegram.domain.User
import com.naidizakupku.telegram.repository.UserRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * Сервис для работы с пользователями
 */
@Service
@ConditionalOnProperty(name = ["spring.datasource.url"])
@Transactional
class UserService(
    private val userRepository: UserRepository
) : UserServiceInterface {
    
    /**
     * Создать нового пользователя
     */
    suspend fun createUser(user: User): User {
        if (userRepository.existsByTelegramId(user.telegramId)) {
            throw IllegalArgumentException("Пользователь с Telegram ID ${user.telegramId} уже существует")
        }
        
        val newUser = user.copy(
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            active = true
        )
        
        return userRepository.save(newUser)
    }
    
    /**
     * Создать пользователя по Telegram ID
     */
    suspend fun createUser(telegramId: Long): User {
        if (userRepository.existsByTelegramId(telegramId)) {
            throw IllegalArgumentException("Пользователь с Telegram ID $telegramId уже существует")
        }
        
        val user = User(
            telegramId = telegramId,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            active = true
        )
        
        return userRepository.save(user)
    }
    
    /**
     * Получить пользователя по ID
     */
    suspend fun getUserById(id: Long): User {
        return userRepository.findById(id).orElse(null)
            ?: throw IllegalArgumentException("Пользователь с ID $id не найден")
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
    suspend fun updateUser(id: Long, user: User): User {
        val existingUser = getUserById(id)
        
        val updatedUser = existingUser.copy(
            telegramId = user.telegramId,
            active = user.active,
            updatedAt = LocalDateTime.now()
        )
        
        return userRepository.save(updatedUser)
    }
    
    /**
     * Обновить пользователя
     */
    suspend fun updateUser(user: User): User {
        val updatedUser = user.copy(updatedAt = LocalDateTime.now())
        return userRepository.save(updatedUser)
    }
    
    /**
     * Удалить пользователя
     */
    suspend fun deleteUser(id: Long) {
        val user = getUserById(id)
        userRepository.delete(user)
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
     * Получить всех пользователей
     */
    suspend fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }
    
    /**
     * Получить всех активных пользователей
     */
    suspend fun getAllActiveUsers(): List<User> {
        return userRepository.findByActiveTrue()
    }
    
    /**
     * Сохранить или обновить пользователя по Telegram данным
     */
    override suspend fun saveOrUpdateUser(
        telegramId: Long,
        firstName: String?,
        lastName: String?,
        username: String?
    ): User {
        val existingUser = userRepository.findByTelegramId(telegramId)
        
        return if (existingUser != null) {
            // Обновляем существующего пользователя
            val updatedUser = existingUser.copy(
                firstName = firstName,
                lastName = lastName,
                username = username,
                updatedAt = LocalDateTime.now()
            )
            userRepository.save(updatedUser)
        } else {
            // Создаем нового пользователя
            val newUser = User(
                telegramId = telegramId,
                firstName = firstName,
                lastName = lastName,
                username = username,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                active = true
            )
            userRepository.save(newUser)
        }
    }
}

