package com.naidizakupku.telegram.domain.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.Instant
import java.util.UUID

data class CodeVerificationRequestDto(
    val correlationId: UUID,
    val code: String,
    val userBrowserInfo: UserBrowserInfoDto,
    val timestamp: Instant
)

data class CodeVerificationResponseDto(
    val correlationId: UUID,
    val success: Boolean,
    val telegramUserId: Long?,
    val message: String,
    val timestamp: Instant
)

data class AuthorizationRevokeRequestDto(
    val correlationId: UUID,
    val telegramUserId: Long,
    val originalVerificationCorrelationId: UUID,
    val reason: String,
    val timestamp: Instant
)

data class AuthorizationRevokeResponseDto(
    val correlationId: UUID,
    val originalVerificationCorrelationId: UUID,
    val telegramUserId: Long,
    val success: Boolean,
    val message: String,
    val timestamp: Instant
)

data class UserBrowserInfoDto(
    val ip: String,
    val userAgent: String,
    val location: String
)
