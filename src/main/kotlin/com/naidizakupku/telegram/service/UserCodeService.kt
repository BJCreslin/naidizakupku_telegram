package com.naidizakupku.telegram.service

import com.naidizakupku.telegram.repository.UserCodeRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class UserCodeService(
    private val userCodeRepository: UserCodeRepository,
    private val codeGenerationService: CodeGenerationService
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
    fun verifyCodeForAuth(code: String, traceId: UUID): Boolean? {
        val existingCode = userCodeRepository.existsByCodeAndNotExpired(code)
        if (!existingCode) {
            logger.info("Код $code не найден или просрочен. $traceId")
            return false
        }
        //todo: тут написать сохранение в БД в таблицу запросов выданных кодов
        userCodeRepository.deleteByCode(code)
        logger.info("Код $code найден и удален. $traceId")
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
}


    data class UserCodeResponse(
        val code: String,
        val expiresAt: LocalDateTime,
        val isNew: Boolean,
        val timezone: String
    )
