package com.naidizakupku.telegram.service.admin

import com.naidizakupku.telegram.domain.admin.AdminRole
import com.naidizakupku.telegram.domain.admin.AdminUser
import com.naidizakupku.telegram.repository.admin.AdminUserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * Сервис для создания первого администратора
 */
@Service
class AdminUserCreationService(
    private val adminUserRepository: AdminUserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    
    private val logger = LoggerFactory.getLogger(AdminUserCreationService::class.java)

    /**
     * Создать первого администратора, если его еще нет
     * Используется только если в системе нет ни одного админа
     */
    @Transactional
    fun createFirstAdminIfNotExists(
        username: String,
        password: String,
        email: String? = null
    ): AdminUser {
        // Проверяем, есть ли уже администраторы
        val existingAdmins = adminUserRepository.findByRole(AdminRole.ADMIN)
        if (existingAdmins.isNotEmpty()) {
            throw IllegalStateException("Administrator already exists. Cannot create first admin.")
        }

        // Проверяем, существует ли пользователь с таким username
        if (adminUserRepository.existsByUsername(username)) {
            throw IllegalArgumentException("User with username '$username' already exists")
        }

        // Проверяем email, если указан
        if (email != null && adminUserRepository.existsByEmail(email)) {
            throw IllegalArgumentException("User with email '$email' already exists")
        }

        // Хешируем пароль
        val passwordHash = passwordEncoder.encode(password)

        // Создаем администратора
        val admin = AdminUser(
            username = username,
            passwordHash = passwordHash,
            email = email,
            role = AdminRole.ADMIN,
            active = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val savedAdmin = adminUserRepository.save(admin)
        logger.info("First administrator created: $username")
        
        return savedAdmin
    }

    /**
     * Проверить, есть ли в системе администраторы
     */
    fun hasAdmins(): Boolean {
        return adminUserRepository.findByRole(AdminRole.ADMIN).isNotEmpty()
    }
}

