package com.naidizakupku.telegram.service

import com.naidizakupku.telegram.domain.User

/**
 * Интерфейс для работы с пользователями
 */
interface UserServiceInterface {
    
    /**
     * Сохранить или обновить пользователя по Telegram данным
     */
    suspend fun saveOrUpdateUser(
        telegramId: Long,
        firstName: String?,
        lastName: String?,
        username: String?
    ): User
}
