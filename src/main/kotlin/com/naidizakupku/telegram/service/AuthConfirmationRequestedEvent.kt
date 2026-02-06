package com.naidizakupku.telegram.service

import org.springframework.context.ApplicationEvent
import java.util.UUID

/**
 * Событие, публикуемое при запросе подтверждения аутентификации
 */
class AuthConfirmationRequestedEvent(
    val telegramUserId: Long,
    val traceId: UUID,
    val ip: String,
    val userAgent: String,
    val location: String
) : ApplicationEvent(telegramUserId) {
    override fun toString(): String {
        return "AuthConfirmationRequestedEvent[telegramUserId=$telegramUserId, traceId=$traceId]"
    }
}