package com.naidizakupku.telegram.controller

import com.naidizakupku.telegram.domain.User
import com.naidizakupku.telegram.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST контроллер для работы с пользователями
 */
@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {
    
    /**
     * Создать нового пользователя
     */
    @PostMapping
    suspend fun createUser(@RequestBody request: CreateUserRequest): ResponseEntity<User> {
        val user = userService.createUser(
            telegramId = request.telegramId,
            username = request.username,
            firstName = request.firstName,
            lastName = request.lastName
        )
        return ResponseEntity.ok(user)
    }
    
    /**
     * Получить пользователя по Telegram ID
     */
    @GetMapping("/{telegramId}")
    suspend fun getUserByTelegramId(@PathVariable telegramId: Long): ResponseEntity<User> {
        val user = userService.findByTelegramId(telegramId)
        return if (user != null) {
            ResponseEntity.ok(user)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    /**
     * Получить всех активных пользователей
     */
    @GetMapping
    suspend fun getAllActiveUsers(): ResponseEntity<List<User>> {
        val users = userService.getAllActiveUsers()
        return ResponseEntity.ok(users)
    }
    
    /**
     * Деактивировать пользователя
     */
    @DeleteMapping("/{telegramId}")
    suspend fun deactivateUser(@PathVariable telegramId: Long): ResponseEntity<User> {
        val user = userService.deactivateUser(telegramId)
        return if (user != null) {
            ResponseEntity.ok(user)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    /**
     * Обновить пользователя
     */
    @PutMapping("/{telegramId}")
    suspend fun updateUser(
        @PathVariable telegramId: Long,
        @RequestBody request: UpdateUserRequest
    ): ResponseEntity<User> {
        val existingUser = userService.findByTelegramId(telegramId) ?: return ResponseEntity.notFound().build()
        
        val updatedUser = existingUser.copy(
            username = request.username ?: existingUser.username,
            firstName = request.firstName,
            lastName = request.lastName
        )
        
        val savedUser = userService.updateUser(updatedUser)
        return ResponseEntity.ok(savedUser)
    }
}

/**
 * DTO для создания пользователя
 */
data class CreateUserRequest(
    val telegramId: Long,
    val username: String,
    val firstName: String? = null,
    val lastName: String? = null
)

/**
 * DTO для обновления пользователя
 */
data class UpdateUserRequest(
    val username: String? = null,
    val firstName: String? = null,
    val lastName: String? = null
)

