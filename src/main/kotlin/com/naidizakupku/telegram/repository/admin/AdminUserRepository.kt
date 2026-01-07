package com.naidizakupku.telegram.repository.admin

import com.naidizakupku.telegram.domain.admin.AdminUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Репозиторий для работы с пользователями админки
 */
@Repository
interface AdminUserRepository : JpaRepository<AdminUser, Long> {
    
    /**
     * Найти пользователя по username
     */
    fun findByUsername(username: String): AdminUser?
    
    /**
     * Проверить существование пользователя по username
     */
    fun existsByUsername(username: String): Boolean
    
    /**
     * Найти пользователя по email
     */
    fun findByEmail(email: String): AdminUser?
    
    /**
     * Проверить существование пользователя по email
     */
    fun existsByEmail(email: String): Boolean
    
    /**
     * Найти всех активных пользователей
     */
    fun findByActiveTrue(): List<AdminUser>
    
    /**
     * Найти пользователей по роли
     */
    fun findByRole(role: com.naidizakupku.telegram.domain.admin.AdminRole): List<AdminUser>
}

