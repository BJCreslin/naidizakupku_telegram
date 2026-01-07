package com.naidizakupku.telegram.domain.dto.admin

import com.naidizakupku.telegram.domain.User
import com.naidizakupku.telegram.domain.UserCode
import com.naidizakupku.telegram.domain.admin.AdminUser
import com.naidizakupku.telegram.domain.AuthRequest
import com.naidizakupku.telegram.domain.entity.VerificationSession
import org.springframework.data.domain.Page

/**
 * Extension функции для маппинга сущностей в DTO для админки
 */

/**
 * Маппинг AdminUser в AdminUserDto
 */
fun AdminUser.toDto(): AdminUserDto {
    return AdminUserDto(
        id = this.id ?: throw IllegalStateException("AdminUser id is null"),
        username = this.username,
        email = this.email,
        role = this.role.name,
        active = this.active,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

/**
 * Маппинг User в UserDto
 */
fun User.toDto(): UserDto {
    return UserDto(
        id = this.id ?: throw IllegalStateException("User id is null"),
        telegramId = this.telegramId,
        firstName = this.firstName,
        lastName = this.lastName,
        username = this.username,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        active = this.active
    )
}

/**
 * Маппинг UserCode в AdminCodeDto
 */
fun UserCode.toAdminDto(): AdminCodeDto {
    return AdminCodeDto(
        id = this.id ?: throw IllegalStateException("UserCode id is null"),
        code = this.code,
        telegramUserId = this.telegramUserId,
        expiresAt = this.expiresAt,
        createdAt = this.createdAt,
        isActive = this.isActive(),
        isExpired = this.isExpired()
    )
}

/**
 * Маппинг VerificationSession в AdminVerificationDto
 */
fun VerificationSession.toAdminDto(): AdminVerificationDto {
    return AdminVerificationDto(
        id = this.id ?: throw IllegalStateException("VerificationSession id is null"),
        correlationId = this.correlationId,
        telegramUserId = this.telegramUserId,
        code = this.code,
        browserInfo = this.browserInfo,
        status = this.status.name,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

/**
 * Маппинг AuthRequest в AdminAuthRequestDto
 */
fun AuthRequest.toAdminDto(): AdminAuthRequestDto {
    return AdminAuthRequestDto(
        id = this.id ?: throw IllegalStateException("AuthRequest id is null"),
        traceId = this.traceId,
        telegramUserId = this.telegramUserId,
        requestedAt = this.requestedAt,
        code = this.code
    )
}

/**
 * Маппинг Page в PagedResponse
 */
fun <T, R> Page<T>.toPagedResponse(mapper: (T) -> R): PagedResponse<R> {
    return PagedResponse.of(
        content = this.content.map(mapper),
        page = this.number,
        size = this.size,
        totalElements = this.totalElements
    )
}

/**
 * Маппинг List в PagedResponse (для случаев без Spring Data Page)
 */
fun <T, R> List<T>.toPagedResponse(
    mapper: (T) -> R,
    page: Int,
    size: Int,
    totalElements: Long
): PagedResponse<R> {
    return PagedResponse.of(
        content = this.map(mapper),
        page = page,
        size = size,
        totalElements = totalElements
    )
}

