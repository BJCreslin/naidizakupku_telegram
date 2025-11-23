package com.naidizakupku.telegram.controller

import com.naidizakupku.telegram.domain.User
import com.naidizakupku.telegram.service.KafkaService
import com.naidizakupku.telegram.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Контроллер для работы с пользователями
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "API для управления пользователями")
class UserController(
    private val userService: UserService,
    private val kafkaService: KafkaService
) {
    
    /**
     * Создание нового пользователя
     */
    @Operation(
        summary = "Создать нового пользователя",
        description = "Создает нового пользователя и отправляет событие user_registered в Kafka"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Пользователь успешно создан",
                content = [Content(schema = Schema(implementation = User::class))]
            ),
            ApiResponse(responseCode = "400", description = "Некорректные данные пользователя"),
            ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
        ]
    )
    @PostMapping
    suspend fun createUser(
        @Parameter(description = "Данные пользователя", required = true)
        @RequestBody user: User
    ): ResponseEntity<User> {
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
    @Operation(
        summary = "Получить пользователя по ID",
        description = "Возвращает информацию о пользователе по его идентификатору"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Пользователь найден",
                content = [Content(schema = Schema(implementation = User::class))]
            ),
            ApiResponse(responseCode = "404", description = "Пользователь не найден")
        ]
    )
    @GetMapping("/{id}")
    suspend fun getUserById(
        @Parameter(description = "ID пользователя", required = true, example = "1")
        @PathVariable id: Long
    ): ResponseEntity<User> {
        val user = userService.getUserById(id)
        return ResponseEntity.ok(user)
    }
    
    /**
     * Получение всех пользователей
     */
    @Operation(
        summary = "Получить всех пользователей",
        description = "Возвращает список всех пользователей в системе"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Список пользователей",
        content = [Content(schema = Schema(implementation = Array<User>::class))]
    )
    @GetMapping
    suspend fun getAllUsers(): ResponseEntity<List<User>> {
        val users = userService.getAllUsers()
        return ResponseEntity.ok(users)
    }
    
    /**
     * Обновление пользователя
     */
    @Operation(
        summary = "Обновить пользователя",
        description = "Обновляет информацию о пользователе и отправляет событие user_updated в Kafka"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Пользователь успешно обновлен",
                content = [Content(schema = Schema(implementation = User::class))]
            ),
            ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            ApiResponse(responseCode = "400", description = "Некорректные данные")
        ]
    )
    @PutMapping("/{id}")
    suspend fun updateUser(
        @Parameter(description = "ID пользователя", required = true, example = "1")
        @PathVariable id: Long,
        @Parameter(description = "Обновленные данные пользователя", required = true)
        @RequestBody user: User
    ): ResponseEntity<User> {
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
    @Operation(
        summary = "Удалить пользователя",
        description = "Удаляет пользователя и отправляет событие user_deleted в Kafka"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Пользователь успешно удален"),
            ApiResponse(responseCode = "404", description = "Пользователь не найден")
        ]
    )
    @DeleteMapping("/{id}")
    suspend fun deleteUser(
        @Parameter(description = "ID пользователя", required = true, example = "1")
        @PathVariable id: Long
    ): ResponseEntity<Unit> {
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
    @Operation(
        summary = "Отправить уведомление пользователю",
        description = "Отправляет уведомление пользователю через Kafka топик notifications"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Уведомление отправлено в очередь",
                content = [Content(schema = Schema(implementation = Map::class))]
            ),
            ApiResponse(responseCode = "404", description = "Пользователь не найден")
        ]
    )
    @PostMapping("/{id}/notify")
    suspend fun sendNotification(
        @Parameter(description = "ID пользователя", required = true, example = "1")
        @PathVariable id: Long,
        @Parameter(
            description = "Данные уведомления (message, type)",
            required = true,
            example = "{\"message\": \"Добро пожаловать!\", \"type\": \"info\"}"
        )
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


