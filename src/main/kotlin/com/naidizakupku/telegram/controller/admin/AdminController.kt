package com.naidizakupku.telegram.controller.admin

import com.naidizakupku.telegram.domain.dto.admin.*
import com.naidizakupku.telegram.domain.entity.VerificationStatus
import com.naidizakupku.telegram.service.admin.AdminService
import com.naidizakupku.telegram.service.admin.AdminMetricsService
import com.naidizakupku.telegram.service.admin.AdminLogService
import com.naidizakupku.telegram.service.admin.KafkaAdminService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * Контроллер для админки
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "API для административной панели")
class AdminController(
    private val adminService: AdminService,
    private val adminMetricsService: AdminMetricsService,
    private val adminLogService: AdminLogService,
    private val kafkaAdminService: KafkaAdminService
) {

    // ========== Users ==========

    @Operation(
        summary = "Получить список пользователей",
        description = "Возвращает список пользователей с пагинацией, поиском и фильтрами"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Список пользователей",
                content = [Content(schema = Schema(implementation = PagedResponse::class))]
            )
        ]
    )
    @GetMapping("/users")
    suspend fun getUsers(
        @Parameter(description = "Номер страницы (начиная с 0)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        
        @Parameter(description = "Размер страницы", example = "20")
        @RequestParam(defaultValue = "20") size: Int,
        
        @Parameter(description = "Поиск по username, firstName, lastName или telegramId")
        @RequestParam(required = false) search: String?,
        
        @Parameter(description = "Фильтр по активности")
        @RequestParam(required = false) active: Boolean?
    ): ResponseEntity<PagedResponse<UserDto>> {
        val result = adminService.getUsers(page, size, search, active)
        return ResponseEntity.ok(result)
    }

    @Operation(
        summary = "Получить пользователя по ID",
        description = "Возвращает информацию о пользователе по его идентификатору"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Пользователь найден",
                content = [Content(schema = Schema(implementation = UserDto::class))]
            ),
            ApiResponse(responseCode = "404", description = "Пользователь не найден")
        ]
    )
    @GetMapping("/users/{id}")
    suspend fun getUserById(
        @Parameter(description = "ID пользователя", required = true, example = "1")
        @PathVariable id: Long
    ): ResponseEntity<UserDto> {
        val user = adminService.getUserById(id)
        return ResponseEntity.ok(user)
    }

    @Operation(
        summary = "Активировать пользователя",
        description = "Активирует пользователя"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Пользователь активирован",
                content = [Content(schema = Schema(implementation = UserDto::class))]
            ),
            ApiResponse(responseCode = "404", description = "Пользователь не найден")
        ]
    )
    @PutMapping("/users/{id}/activate")
    suspend fun activateUser(
        @Parameter(description = "ID пользователя", required = true, example = "1")
        @PathVariable id: Long
    ): ResponseEntity<UserDto> {
        val user = adminService.activateUser(id)
        return ResponseEntity.ok(user)
    }

    @Operation(
        summary = "Деактивировать пользователя",
        description = "Деактивирует пользователя"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Пользователь деактивирован",
                content = [Content(schema = Schema(implementation = UserDto::class))]
            ),
            ApiResponse(responseCode = "404", description = "Пользователь не найден")
        ]
    )
    @PutMapping("/users/{id}/deactivate")
    suspend fun deactivateUser(
        @Parameter(description = "ID пользователя", required = true, example = "1")
        @PathVariable id: Long
    ): ResponseEntity<UserDto> {
        val user = adminService.deactivateUser(id)
        return ResponseEntity.ok(user)
    }

    // ========== Codes ==========

    @Operation(
        summary = "Получить список кодов",
        description = "Возвращает список кодов с пагинацией и фильтрами"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Список кодов",
                content = [Content(schema = Schema(implementation = PagedResponse::class))]
            )
        ]
    )
    @GetMapping("/codes")
    fun getCodes(
        @Parameter(description = "Номер страницы (начиная с 0)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        
        @Parameter(description = "Размер страницы", example = "20")
        @RequestParam(defaultValue = "20") size: Int,
        
        @Parameter(description = "Фильтр по ID пользователя Telegram")
        @RequestParam(required = false) userId: Long?,
        
        @Parameter(description = "Фильтр по активности")
        @RequestParam(required = false) active: Boolean?,
        
        @Parameter(description = "Фильтр по истечению")
        @RequestParam(required = false) expired: Boolean?
    ): ResponseEntity<PagedResponse<AdminCodeDto>> {
        val result = adminService.getCodes(page, size, userId, active, expired)
        return ResponseEntity.ok(result)
    }

    @Operation(
        summary = "Получить код по ID",
        description = "Возвращает информацию о коде по его идентификатору"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Код найден",
                content = [Content(schema = Schema(implementation = AdminCodeDto::class))]
            ),
            ApiResponse(responseCode = "404", description = "Код не найден")
        ]
    )
    @GetMapping("/codes/{id}")
    fun getCodeById(
        @Parameter(description = "ID кода", required = true, example = "1")
        @PathVariable id: Long
    ): ResponseEntity<AdminCodeDto> {
        val code = adminService.getCodeById(id)
        return ResponseEntity.ok(code)
    }

    @Operation(
        summary = "Удалить код",
        description = "Инвалидирует код (удаляет его)"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Код удален"),
            ApiResponse(responseCode = "404", description = "Код не найден")
        ]
    )
    @DeleteMapping("/codes/{id}")
    fun deleteCode(
        @Parameter(description = "ID кода", required = true, example = "1")
        @PathVariable id: Long
    ): ResponseEntity<Map<String, String>> {
        adminService.deleteCode(id)
        return ResponseEntity.ok(mapOf("message" to "Code deleted successfully"))
    }

    @Operation(
        summary = "Получить статистику по кодам",
        description = "Возвращает статистику по кодам (total, active, expired)"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Статистика по кодам"
            )
        ]
    )
    @GetMapping("/codes/stats")
    fun getCodeStats(): ResponseEntity<AdminService.CodeStatsDto> {
        val stats = adminService.getCodeStats()
        return ResponseEntity.ok(stats)
    }

    // ========== Verification Sessions ==========

    @Operation(
        summary = "Получить список сессий верификации",
        description = "Возвращает список сессий верификации с пагинацией и фильтрами"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Список сессий верификации",
                content = [Content(schema = Schema(implementation = PagedResponse::class))]
            )
        ]
    )
    @GetMapping("/verification-sessions")
    fun getVerificationSessions(
        @Parameter(description = "Номер страницы (начиная с 0)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        
        @Parameter(description = "Размер страницы", example = "20")
        @RequestParam(defaultValue = "20") size: Int,
        
        @Parameter(description = "Фильтр по статусу (PENDING, CONFIRMED, REVOKED)")
        @RequestParam(required = false) status: String?,
        
        @Parameter(description = "Фильтр по ID пользователя Telegram")
        @RequestParam(required = false) userId: Long?
    ): ResponseEntity<PagedResponse<AdminVerificationDto>> {
        val verificationStatus = status?.let { VerificationStatus.valueOf(it) }
        val result = adminService.getVerificationSessions(page, size, verificationStatus, userId)
        return ResponseEntity.ok(result)
    }

    @Operation(
        summary = "Получить сессию верификации по correlationId",
        description = "Возвращает информацию о сессии верификации по correlation ID"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Сессия найдена",
                content = [Content(schema = Schema(implementation = AdminVerificationDto::class))]
            ),
            ApiResponse(responseCode = "404", description = "Сессия не найдена")
        ]
    )
    @GetMapping("/verification-sessions/{correlationId}")
    fun getVerificationSessionByCorrelationId(
        @Parameter(description = "Correlation ID (UUID)", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable correlationId: UUID
    ): ResponseEntity<AdminVerificationDto> {
        val session = adminService.getVerificationSessionByCorrelationId(correlationId)
        return ResponseEntity.ok(session)
    }

    @Operation(
        summary = "Получить статистику по сессиям верификации",
        description = "Возвращает статистику по сессиям верификации"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Статистика по сессиям верификации"
            )
        ]
    )
    @GetMapping("/verification-sessions/stats")
    fun getVerificationStats(): ResponseEntity<AdminService.VerificationStatsDto> {
        val stats = adminService.getVerificationStats()
        return ResponseEntity.ok(stats)
    }

    // ========== Auth Requests ==========

    @Operation(
        summary = "Получить список запросов аутентификации",
        description = "Возвращает список запросов аутентификации с пагинацией и фильтрами"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Список запросов аутентификации",
                content = [Content(schema = Schema(implementation = PagedResponse::class))]
            )
        ]
    )
    @GetMapping("/auth-requests")
    fun getAuthRequests(
        @Parameter(description = "Номер страницы (начиная с 0)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        
        @Parameter(description = "Размер страницы", example = "20")
        @RequestParam(defaultValue = "20") size: Int,
        
        @Parameter(description = "Фильтр по ID пользователя Telegram")
        @RequestParam(required = false) userId: Long?,
        
        @Parameter(description = "Фильтр по trace ID (UUID)")
        @RequestParam(required = false) traceId: UUID?
    ): ResponseEntity<PagedResponse<AdminAuthRequestDto>> {
        val result = adminService.getAuthRequests(page, size, userId, traceId)
        return ResponseEntity.ok(result)
    }

    @Operation(
        summary = "Получить запрос аутентификации по traceId",
        description = "Возвращает информацию о запросе аутентификации по trace ID"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Запрос найден",
                content = [Content(schema = Schema(implementation = AdminAuthRequestDto::class))]
            ),
            ApiResponse(responseCode = "404", description = "Запрос не найден")
        ]
    )
    @GetMapping("/auth-requests/{traceId}")
    fun getAuthRequestByTraceId(
        @Parameter(description = "Trace ID (UUID)", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable traceId: UUID
    ): ResponseEntity<AdminAuthRequestDto> {
        val request = adminService.getAuthRequestByTraceId(traceId)
        return ResponseEntity.ok(request)
    }

    @Operation(
        summary = "Получить статистику по запросам аутентификации",
        description = "Возвращает статистику по запросам аутентификации"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Статистика по запросам аутентификации"
            )
        ]
    )
    @GetMapping("/auth-requests/stats")
    fun getAuthRequestStats(): ResponseEntity<AdminService.AuthRequestStatsDto> {
        val stats = adminService.getAuthRequestStats()
        return ResponseEntity.ok(stats)
    }

    // ========== Metrics ==========

    @Operation(
        summary = "Получить метрики для dashboard",
        description = "Возвращает агрегированные метрики для dashboard"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Метрики для dashboard",
                content = [Content(schema = Schema(implementation = MetricsDto::class))]
            )
        ]
    )
    @GetMapping("/metrics/dashboard")
    fun getDashboardMetrics(): ResponseEntity<MetricsDto> {
        val metrics = adminMetricsService.getDashboardMetrics()
        return ResponseEntity.ok(metrics)
    }

    @Operation(
        summary = "Получить метрики кодов",
        description = "Возвращает метрики по кодам за указанный период"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Метрики кодов",
                content = [Content(schema = Schema(implementation = CodeMetrics::class))]
            )
        ]
    )
    @GetMapping("/metrics/codes")
    fun getCodeMetrics(
        @Parameter(description = "Период (24h, 7d, 30d)", example = "24h")
        @RequestParam(defaultValue = "24h") period: String
    ): ResponseEntity<CodeMetrics> {
        val metrics = adminMetricsService.getCodeMetrics()
        return ResponseEntity.ok(metrics)
    }

    @Operation(
        summary = "Получить метрики верификации",
        description = "Возвращает метрики по верификации за указанный период"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Метрики верификации",
                content = [Content(schema = Schema(implementation = VerificationMetrics::class))]
            )
        ]
    )
    @GetMapping("/metrics/verification")
    fun getVerificationMetrics(
        @Parameter(description = "Период (24h, 7d, 30d)", example = "24h")
        @RequestParam(defaultValue = "24h") period: String
    ): ResponseEntity<VerificationMetrics> {
        val metrics = adminMetricsService.getVerificationMetrics()
        return ResponseEntity.ok(metrics)
    }

    @Operation(
        summary = "Получить метрики Telegram",
        description = "Возвращает метрики по Telegram за указанный период"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Метрики Telegram",
                content = [Content(schema = Schema(implementation = TelegramMetrics::class))]
            )
        ]
    )
    @GetMapping("/metrics/telegram")
    fun getTelegramMetrics(
        @Parameter(description = "Период (24h, 7d, 30d)", example = "24h")
        @RequestParam(defaultValue = "24h") period: String
    ): ResponseEntity<TelegramMetrics> {
        val metrics = adminMetricsService.getTelegramMetrics()
        return ResponseEntity.ok(metrics)
    }

    @Operation(
        summary = "Получить метрики Kafka",
        description = "Возвращает метрики по Kafka за указанный период"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Метрики Kafka",
                content = [Content(schema = Schema(implementation = KafkaMetrics::class))]
            )
        ]
    )
    @GetMapping("/metrics/kafka")
    fun getKafkaMetrics(
        @Parameter(description = "Период (24h, 7d, 30d)", example = "24h")
        @RequestParam(defaultValue = "24h") period: String
    ): ResponseEntity<KafkaMetrics> {
        val metrics = adminMetricsService.getKafkaMetrics()
        return ResponseEntity.ok(metrics)
    }

    // ========== Kafka ==========

    @Operation(
        summary = "Получить статус Kafka",
        description = "Возвращает информацию о топиках и consumer groups Kafka"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Статус Kafka",
                content = [Content(schema = Schema(implementation = KafkaStatusDto::class))]
            )
        ]
    )
    @GetMapping("/kafka/topics")
    fun getKafkaTopics(): ResponseEntity<KafkaStatusDto> {
        val status = kafkaAdminService.getKafkaStatus()
        return ResponseEntity.ok(status)
    }

    @Operation(
        summary = "Получить список consumer groups",
        description = "Возвращает список consumer groups Kafka"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Список consumer groups"
            )
        ]
    )
    @GetMapping("/kafka/consumer-groups")
    fun getConsumerGroups(): ResponseEntity<List<ConsumerGroupInfo>> {
        val status = kafkaAdminService.getKafkaStatus()
        return ResponseEntity.ok(status.consumerGroups)
    }

    // ========== Logs ==========

    @Operation(
        summary = "Получить логи",
        description = "Возвращает логи с пагинацией и фильтрами"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Список логов",
                content = [Content(schema = Schema(implementation = PagedResponse::class))]
            )
        ]
    )
    @GetMapping("/logs")
    fun getLogs(
        @Parameter(description = "Номер страницы (начиная с 0)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        
        @Parameter(description = "Размер страницы", example = "50")
        @RequestParam(defaultValue = "50") size: Int,
        
        @Parameter(description = "Фильтр по уровню (INFO, WARN, ERROR, DEBUG)")
        @RequestParam(required = false) level: String?,
        
        @Parameter(description = "Фильтр по trace ID")
        @RequestParam(required = false) traceId: String?,
        
        @Parameter(description = "Фильтр по correlation ID")
        @RequestParam(required = false) correlationId: String?
    ): ResponseEntity<PagedResponse<LogEntryDto>> {
        val result = adminLogService.getLogs(page, size, level, traceId, correlationId)
        return ResponseEntity.ok(result)
    }

    // ========== Settings ==========

    @Operation(
        summary = "Получить настройки",
        description = "Возвращает текущие настройки приложения"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Настройки приложения"
            )
        ]
    )
    @GetMapping("/settings")
    fun getSettings(): ResponseEntity<Map<String, Any>> {
        // TODO: Реализовать получение настроек из конфигурации
        val settings = mapOf<String, Any>(
            "codeExpirationMinutes" to 5,
            "verificationSessionCleanupMinutes" to 30
        )
        return ResponseEntity.ok(settings)
    }

    @Operation(
        summary = "Обновить настройки",
        description = "Обновляет настройки приложения"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Настройки обновлены"),
            ApiResponse(responseCode = "400", description = "Некорректные данные")
        ]
    )
    @PutMapping("/settings")
    fun updateSettings(
        @Parameter(description = "Настройки для обновления", required = true)
        @RequestBody settings: Map<String, Any>
    ): ResponseEntity<Map<String, String>> {
        // TODO: Реализовать обновление настроек
        return ResponseEntity.ok(mapOf("message" to "Settings update not implemented yet"))
    }
}

