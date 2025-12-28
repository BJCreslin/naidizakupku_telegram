package com.naidizakupku.telegram.service

import com.naidizakupku.telegram.controller.CodeController
import com.naidizakupku.telegram.domain.AuthRequest
import com.naidizakupku.telegram.repository.AuthRequestRepository
import com.naidizakupku.telegram.repository.UserCodeRepository
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service
import org.springframework.transaction.TransactionException
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class UserCodeService(
    private val userCodeRepository: UserCodeRepository,
    private val authRequestRepository: AuthRequestRepository,
    private val codeGenerationService: CodeGenerationService,
    private val telegramNotificationService: TelegramNotificationService,
    private val kafkaProducerService: KafkaProducerService,
    private val telegramBotExecutor: TelegramBotExecutor,
    private val metricsService: MetricsService
) {

    companion object {
        private val logger = LoggerFactory.getLogger(UserCodeService::class.java)
        private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }

    /**
     * Проверяет существование кода и не просрочен ли он
     */
    fun verifyCode(code: String): Boolean {
        val startTime = System.currentTimeMillis()
        val now = LocalDateTime.now()
        val existingCode = userCodeRepository.existsByCodeAndNotExpired(code, now)

        val existText = if (existingCode) " - код существует и не просрочен" else " - код не найден или просрочен"
        logger.info("Проверка кода: $code. $existText")

        // Метрики
        metricsService.incrementCodeVerified(existingCode)
        val duration = System.currentTimeMillis() - startTime
        metricsService.recordCodeVerificationTime(duration, java.util.concurrent.TimeUnit.MILLISECONDS)

        return existingCode
    }

    /**
     * Проверяет существование кода и не просрочен ли он для аутентификации.
     */
    @Transactional
    fun verifyCodeForAuth(request: CodeController.VerificationRequest, traceId: UUID): AuthVerificationResult {
        val code = request.code
        val now = LocalDateTime.now()
        val existingCode = userCodeRepository.findByCodeAndNotExpired(code, now)
        
        return when {
            existingCode == null -> {
                logger.info("Код $code не найден или просрочен. $traceId")
                AuthVerificationResult.CodeNotFound
            }
            existingCode.isExpired() -> {
                logger.info("Код $code просрочен. $traceId")
                AuthVerificationResult.CodeExpired
            }
            else -> {
                try {
                    // Сохраняем запрос аутентификации в БД
                    val authRequest = AuthRequest(
                        traceId = traceId,
                        telegramUserId = existingCode.telegramUserId,
                        requestedAt = LocalDateTime.now(),
                        code = code,
                    )
                    authRequestRepository.save(authRequest)
                    
                    // Отправляем уведомление в Telegram с кнопками подтверждения
                    telegramNotificationService.sendAuthConfirmationRequest(
                        telegramBot = telegramBotExecutor,
                        telegramUserId = existingCode.telegramUserId,
                        traceId = traceId,
                        ip = request.ip,
                        userAgent = request.userAgent,
                        location = request.location
                    )
                    logger.info("Отправлено уведомление о входе пользователю ${existingCode.telegramUserId} для traceId $traceId")
                    
                    // Удаляем использованный код и инвалидируем кэш
                    userCodeRepository.deleteByCode(code)
                    evictUserCodeCache(existingCode.telegramUserId)
                    logger.info("Код $code найден, запрос сохранен и код удален. $traceId")
                    
                    AuthVerificationResult.Success
                } catch (e: Exception) {
                    logger.error("Ошибка при верификации кода для traceId $traceId", e)
                    AuthVerificationResult.Error(e.message ?: "Неизвестная ошибка")
                }
            }
        }
    }

    @Cacheable(value = ["userCodes"], key = "#telegramUserId")
    fun getOrCreateUserCode(telegramUserId: Long, userTimezone: String? = null): UserCodeResponse {
        try {
            // Проверяем существующий активный код
            val now = LocalDateTime.now()
            val existingCode = userCodeRepository.findActiveCodeByTelegramUserId(telegramUserId, now)

            if (existingCode != null) {
                logger.info("Найден существующий код для пользователя $telegramUserId: ${existingCode.code}")
                return UserCodeResponse(
                    code = existingCode.code,
                    expiresAt = existingCode.expiresAt,
                    isNew = false,
                    timezone = userTimezone ?: "UTC+3"
                )
            }

            // Создаем новый код
            val newCode = codeGenerationService.createUserCode(telegramUserId)
            logger.info("Создан новый код для пользователя $telegramUserId: ${newCode.code}")
            
            // Инвалидируем кэш при создании нового кода
            evictUserCodeCache(telegramUserId)

            return UserCodeResponse(
                code = newCode.code,
                expiresAt = newCode.expiresAt,
                isNew = true,
                timezone = userTimezone ?: "UTC+3"
            )

        } catch (e: DataAccessException) {
            logger.error("Ошибка доступа к БД при получении кода для пользователя $telegramUserId", e)
            throw CodeServiceException("Ошибка базы данных", e)
        } catch (e: TransactionException) {
            logger.error("Ошибка транзакции при получении кода для пользователя $telegramUserId", e)
            throw CodeServiceException("Ошибка транзакции", e)
        } catch (e: IllegalArgumentException) {
            logger.error("Некорректные параметры для пользователя $telegramUserId", e)
            throw e
        } catch (e: Exception) {
            logger.error("Неожиданная ошибка при получении кода для пользователя $telegramUserId", e)
            throw CodeServiceException("Внутренняя ошибка сервиса", e)
        }
    }

    @CacheEvict(value = ["userCodes"], allEntries = true)
    fun cleanupExpiredCodes() {
        try {
            val now = LocalDateTime.now()
            val expiredCodes = userCodeRepository.findExpiredCodes(now)

            if (expiredCodes.isNotEmpty()) {
                userCodeRepository.deleteExpiredCodes(now)
                logger.info("Удалено ${expiredCodes.size} просроченных кодов")
                // Метрики
                expiredCodes.forEach { metricsService.incrementCodeExpired() }
            }
        } catch (e: Exception) {
            logger.error("Ошибка при очистке просроченных кодов", e)
        }
    }
    
    /**
     * Инвалидирует кэш для конкретного пользователя
     */
    @CacheEvict(value = ["userCodes"], key = "#telegramUserId")
    private fun evictUserCodeCache(telegramUserId: Long) {
        // Метод используется только для инвалидации кэша через AOP
    }

    fun formatExpirationTime(expiresAt: LocalDateTime, timezone: String): String {
        return try {
            val zoneId = when {
                timezone.startsWith("UTC") -> {
                    val offset = timezone.substring(3)
                    ZoneId.of("GMT$offset")
                }

                else -> ZoneId.of(timezone)
            }

            val zonedDateTime = expiresAt.atZone(ZoneId.systemDefault()).withZoneSameInstant(zoneId)
            val timeStr = zonedDateTime.format(timeFormatter)

            when {
                timezone == "UTC+3" -> "$timeStr (МСК)"
                timezone.startsWith("UTC") -> "$timeStr ($timezone)"
                else -> "$timeStr ($timezone)"
            }
        } catch (e: Exception) {
            logger.warn("Не удалось определить часовой пояс $timezone, используем UTC+3", e)
            val mskTime = expiresAt.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("GMT+3"))
            "${mskTime.format(timeFormatter)} (МСК)"
        }
    }

    /**
     * Подтверждает вход пользователя по traceId
     */
    @Transactional
    fun confirmAuth(traceId: UUID): Boolean {
        return try {
            val authRequest = authRequestRepository.findByTraceId(traceId)
            if (authRequest == null) {
                logger.warn("Запрос аутентификации с traceId $traceId не найден")
                return false
            }

            // Удаляем запрос из БД
            authRequestRepository.deleteByTraceId(traceId)
            
            // Удаляем кнопки из Telegram сообщения
            telegramNotificationService.removeAuthConfirmationButtons(telegramBotExecutor, authRequest.telegramUserId, traceId)
            
            logger.info("Вход подтвержден для traceId $traceId")
            true
        } catch (e: Exception) {
            logger.error("Ошибка при подтверждении входа для traceId $traceId", e)
            false
        }
    }

    /**
     * Отклоняет вход пользователя по traceId
     */
    @Transactional
    fun revokeAuth(traceId: UUID): Boolean {
        return try {
            val authRequest = authRequestRepository.findByTraceId(traceId)
            if (authRequest == null) {
                logger.warn("Запрос аутентификации с traceId $traceId не найден")
                return false
            }

            // Отправляем сообщение об отзыве в Kafka
            try {
                kafkaProducerService.sendRevokeRequest(
                    correlationId = UUID.randomUUID(),
                    telegramUserId = authRequest.telegramUserId,
                    originalVerificationCorrelationId = traceId,
                    reason = "Пользователь отозвал авторизацию через Telegram"
                )
                logger.info("Отправлено сообщение об отзыве в Kafka для traceId $traceId")
            } catch (e: Exception) {
                logger.error("Ошибка отправки сообщения об отзыве в Kafka для traceId $traceId", e)
            }

            // Удаляем запрос из БД
            authRequestRepository.deleteByTraceId(traceId)
            
            // Уведомляем пользователя об отзыве
            telegramNotificationService.sendAuthRevokedMessage(telegramBotExecutor, authRequest.telegramUserId)
            
            logger.info("Вход отозван для traceId $traceId")
            true
        } catch (e: Exception) {
            logger.error("Ошибка при отзыве входа для traceId $traceId", e)
            false
        }
    }
    
    /**
     * Результат верификации кода для аутентификации
     */
    sealed class AuthVerificationResult {
        data object Success : AuthVerificationResult()
        data object CodeNotFound : AuthVerificationResult()
        data object CodeExpired : AuthVerificationResult()
        data class Error(val message: String) : AuthVerificationResult()
    }
}


    data class UserCodeResponse(
        val code: String,
        val expiresAt: LocalDateTime,
        val isNew: Boolean,
        val timezone: String
    )
