package com.naidizakupku.telegram.controller

import com.naidizakupku.telegram.domain.User
import com.naidizakupku.telegram.service.KafkaService
import com.naidizakupku.telegram.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Контроллер для работы с пользователями
 */
@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
    private val kafkaService: KafkaService
) {
    
    /**
     * Создание нового пользователя
     */
    @PostMapping
    suspend fun createUser(@RequestBody user: User): ResponseEntity<User> {
        val createdUser = userService.createUser(user)
        
        // Отправляем событие в Kafka
        kafkaService.sendUserEvent(
            userId = createdUser.id!!,
            eventType = "user_registered",
            data = mapOf(
                "telegramId" to createdUser.telegramId.toString()
            )
        )
        
        return ResponseEntity.ok(createdUser)
    }
    
    /**
     * Получение пользователя по ID
     */
    @GetMapping("/{id}")
    suspend fun getUserById(@PathVariable id: Long): ResponseEntity<User> {
        val user = userService.getUserById(id)
        return ResponseEntity.ok(user)
    }
    
    /**
     * Получение всех пользователей
     */
    @GetMapping
    suspend fun getAllUsers(): ResponseEntity<List<User>> {
        val users = userService.getAllUsers()
        return ResponseEntity.ok(users)
    }
    
    /**
     * Обновление пользователя
     */
    @PutMapping("/{id}")
    suspend fun updateUser(@PathVariable id: Long, @RequestBody user: User): ResponseEntity<User> {
        val updatedUser = userService.updateUser(id, user)
        
        // Отправляем событие в Kafka
        kafkaService.sendUserEvent(
            userId = id,
            eventType = "user_updated",
            data = mapOf(
                "telegramId" to updatedUser.telegramId.toString()
            )
        )
        
        return ResponseEntity.ok(updatedUser)
    }
    
    /**
     * Удаление пользователя
     */
    @DeleteMapping("/{id}")
    suspend fun deleteUser(@PathVariable id: Long): ResponseEntity<Unit> {
        userService.deleteUser(id)
        
        // Отправляем событие в Kafka
        kafkaService.sendUserEvent(
            userId = id,
            eventType = "user_deleted",
            data = mapOf("deletedAt" to System.currentTimeMillis())
        )
        
        return ResponseEntity.ok().build()
    }
    
    /**
     * Отправка уведомления
     */
    @PostMapping("/{id}/notify")
    suspend fun sendNotification(
        @PathVariable id: Long,
        @RequestBody notification: Map<String, String>
    ): ResponseEntity<Map<String, String>> {
        val message = notification["message"] ?: "Уведомление"
        val type = notification["type"] ?: "info"
        
        kafkaService.sendNotification(id, message, type)
        
        return ResponseEntity.ok(mapOf(
            "status" to "success",
            "message" to "Уведомление отправлено в очередь"
        ))
    }
}


