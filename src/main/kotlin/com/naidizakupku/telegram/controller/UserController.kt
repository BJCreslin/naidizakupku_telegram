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
            telegramId = request.telegramId
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
 * DTO для создания пользователя
 */
data class CreateUserRequest(
    val telegramId: Long
)}


