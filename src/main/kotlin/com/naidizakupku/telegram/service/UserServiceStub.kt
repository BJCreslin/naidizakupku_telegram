package com.naidizakupku.telegram.service

import com.naidizakupku.telegram.domain.User
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * Заглушка для UserService когда база данных недоступна
 */
@Service
@ConditionalOnMissingBean(UserService::class)
class UserServiceStub : UserServiceInterface {
    
    private val logger = LoggerFactory.getLogger(UserServiceStub::class.java)
    
    override suspend fun saveOrUpdateUser(
        telegramId: Long,
        firstName: String?,
        lastName: String?,
        username: String?
    ): User {
        logger.info("Заглушка: Сохранение пользователя $telegramId ($firstName $lastName @$username)")
        
        return User(
            id = null,
            telegramId = telegramId,
            firstName = firstName,
            lastName = lastName,
            username = username,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            active = true
        )
    }
}
