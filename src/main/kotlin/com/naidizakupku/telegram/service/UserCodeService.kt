package com.naidizakupku.telegram.service

import com.naidizakupku.telegram.controller.CodeController
import com.naidizakupku.telegram.domain.AuthRequest
import com.naidizakupku.telegram.repository.AuthRequestRepository
import com.naidizakupku.telegram.repository.UserCodeRepository
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
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
    @Lazy private val telegramBotService: TelegramBotService
) {

    companion object {
        private val logger = LoggerFactory.getLogger(UserCodeService::class.java)
        private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }

    /**
     * Проверяет существование кода и не просрочен ли он
     */
    fun verifyCode(code: String): Boolean {

        val existingCode = userCodeRepository.existsByCodeAndNotExpired(code)

        val existText = if (existingCode) " - код существует и не просрочен" else " - код не найден или просрочен"
        logger.info("Проверка кода: $code. $existText")

        return existingCode
    }

    /**
     * Проверяет существование кода и не просрочен ли он для аутентификации.
     */
    @Transactional
    fun verifyCodeForAuth(request: CodeController.VerificationRequest, traceId: UUID): Boolean? {
        val code = request.code
        val existingCode = userCodeRepository.findByCodeAndNotExpired(code)
        if (existingCode == null) {
            logger.info("Код $code не найден или просрочен. $traceId")
            return false
        }
        
        // Сохраняем запрос аутентификации в БД
        val authRequest = AuthRequest(
            traceId = traceId,
            telegramUserId = existingCode.telegramUserId,
            requestedAt = LocalDateTime.now(),
            code = code,
        )
        authRequestRepository.save(authRequest)
        
        // Отправляем уведомление в Telegram с кнопками подтверждения
        try {
            telegramNotificationService.sendAuthConfirmationRequest(
                telegramBot = telegramBotService,
                telegramUserId = existingCode.telegramUserId,
                traceId = traceId,
                ip = request.ip,
                userAgent = request.userAgent,
                location = request.location
            )
            logger.info("Отправлено уведомление о входе пользователю ${existingCode.telegramUserId} для traceId $traceId")
        } catch (e: Exception) {
            logger.error("Ошибка отправки уведомления в Telegram для traceId $traceId", e)
        }
        
        // Удаляем использованный код
        userCodeRepository.deleteByCode(code)
        logger.info("Код $code найден, запрос сохранен и код удален. $traceId")
        return true
    }

    fun getOrCreateUserCode(telegramUserId: Long, userTimezone: String? = null): UserCodeResponse {
        try {
            // Проверяем существующий активный код
            val existingCode = userCodeRepository.findActiveCodeByTelegramUserId(telegramUserId)

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

            return UserCodeResponse(
                code = newCode.code,
                expiresAt = newCode.expiresAt,
                isNew = true,
                timezone = userTimezone ?: "UTC+3"
            )

        } catch (e: Exception) {
            logger.error("Ошибка при получении/создании кода для пользователя $telegramUserId", e)
            throw RuntimeException("Не удалось получить код", e)
        }
    }

    fun cleanupExpiredCodes() {
        try {
            val now = LocalDateTime.now()
            val expiredCodes = userCodeRepository.findExpiredCodes(now)

            if (expiredCodes.isNotEmpty()) {
                userCodeRepository.deleteExpiredCodes(now)
                logger.info("Удалено ${expiredCodes.size} просроченных кодов")
            }
        } catch (e: Exception) {
            logger.error("Ошибка при очистке просроченных кодов", e)
        }
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
            telegramNotificationService.removeAuthConfirmationButtons(telegramBotService, authRequest.telegramUserId, traceId)
            
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
            telegramNotificationService.sendAuthRevokedMessage(telegramBotService, authRequest.telegramUserId)
            
            logger.info("Вход отозван для traceId $traceId")
            true
        } catch (e: Exception) {
            logger.error("Ошибка при отзыве входа для traceId $traceId", e)
            false
        }
    }
}


    data class UserCodeResponse(
        val code: String,
        val expiresAt: LocalDateTime,
        val isNew: Boolean,
        val timezone: String
    )
