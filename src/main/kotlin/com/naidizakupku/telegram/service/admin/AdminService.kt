package com.naidizakupku.telegram.service.admin

import com.naidizakupku.telegram.domain.User
import com.naidizakupku.telegram.domain.UserCode
import com.naidizakupku.telegram.domain.dto.admin.*
import com.naidizakupku.telegram.domain.entity.VerificationSession
import com.naidizakupku.telegram.domain.entity.VerificationStatus
import com.naidizakupku.telegram.domain.AuthRequest
import com.naidizakupku.telegram.repository.UserRepository
import com.naidizakupku.telegram.repository.UserCodeRepository
import com.naidizakupku.telegram.repository.VerificationSessionRepository
import com.naidizakupku.telegram.repository.AuthRequestRepository
import com.naidizakupku.telegram.service.UserService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

/**
 * Сервис для работы с данными в админке
 */
@Service
class AdminService(
    private val userRepository: UserRepository,
    private val userCodeRepository: UserCodeRepository,
    private val verificationSessionRepository: VerificationSessionRepository,
    private val authRequestRepository: AuthRequestRepository,
    private val userService: UserService
) {

    // ========== Users ==========

    /**
     * Получить список пользователей с пагинацией и фильтрами
     */
    suspend fun getUsers(
        page: Int,
        size: Int,
        search: String? = null,
        active: Boolean? = null
    ): PagedResponse<UserDto> {
        val pageable: Pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        
        val usersPage: Page<User> = when {
            search != null && active != null -> {
                // Поиск по username или telegramId и фильтр по active
                val searchLong = search.toLongOrNull()
                val allUsers = if (searchLong != null) {
                    // Поиск по telegramId
                    val user = userRepository.findByTelegramId(searchLong)
                    if (user != null && user.active == active) {
                        listOf(user)
                    } else {
                        emptyList()
                    }
                } else {
                    // Поиск по username, firstName, lastName
                    userRepository.findAll().filter { 
                        (it.username?.contains(search, ignoreCase = true) == true || 
                         it.firstName?.contains(search, ignoreCase = true) == true ||
                         it.lastName?.contains(search, ignoreCase = true) == true) &&
                        it.active == active
                    }
                }
                val start = page * size
                val end = minOf(start + size, allUsers.size)
                val pagedUsers = if (start < allUsers.size) {
                    allUsers.subList(start, end)
                } else {
                    emptyList()
                }
                org.springframework.data.domain.PageImpl(pagedUsers, pageable, allUsers.size.toLong())
            }
            search != null -> {
                val searchLong = search.toLongOrNull()
                val allUsers = if (searchLong != null) {
                    val user = userRepository.findByTelegramId(searchLong)
                    if (user != null) listOf(user) else emptyList()
                } else {
                    userRepository.findAll().filter { 
                        it.username?.contains(search, ignoreCase = true) == true ||
                        it.firstName?.contains(search, ignoreCase = true) == true ||
                        it.lastName?.contains(search, ignoreCase = true) == true
                    }
                }
                val start = page * size
                val end = minOf(start + size, allUsers.size)
                val pagedUsers = if (start < allUsers.size) {
                    allUsers.subList(start, end)
                } else {
                    emptyList()
                }
                org.springframework.data.domain.PageImpl(pagedUsers, pageable, allUsers.size.toLong())
            }
            active != null -> {
                val allUsers = if (active) {
                    userRepository.findByActiveTrue()
                } else {
                    userRepository.findAll().filter { !it.active }
                }
                val start = page * size
                val end = minOf(start + size, allUsers.size)
                val pagedUsers = if (start < allUsers.size) {
                    allUsers.subList(start, end)
                } else {
                    emptyList()
                }
                org.springframework.data.domain.PageImpl(pagedUsers, pageable, allUsers.size.toLong())
            }
            else -> userRepository.findAll(pageable)
        }
        
        return usersPage.toPagedResponse { it.toDto() }
    }

    /**
     * Получить пользователя по ID
     */
    suspend fun getUserById(id: Long): UserDto {
        val user = userService.getUserById(id)
        return user.toDto()
    }

    /**
     * Активировать пользователя
     */
    @Transactional
    suspend fun activateUser(id: Long): UserDto {
        val user = userService.getUserById(id)
        val updatedUser = user.copy(active = true, updatedAt = LocalDateTime.now())
        val savedUser = userRepository.save(updatedUser)
        return savedUser.toDto()
    }

    /**
     * Деактивировать пользователя
     */
    @Transactional
    suspend fun deactivateUser(id: Long): UserDto {
        val user = userService.getUserById(id)
        val updatedUser = user.copy(active = false, updatedAt = LocalDateTime.now())
        val savedUser = userRepository.save(updatedUser)
        return savedUser.toDto()
    }

    // ========== Codes ==========

    /**
     * Получить список кодов с пагинацией и фильтрами
     */
    fun getCodes(
        page: Int,
        size: Int,
        userId: Long? = null,
        active: Boolean? = null,
        expired: Boolean? = null
    ): PagedResponse<AdminCodeDto> {
        val pageable: Pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val now = LocalDateTime.now()
        
        val codesPage: Page<UserCode> = when {
            userId != null && active != null -> {
                if (active) {
                    val code = userCodeRepository.findActiveCodeByTelegramUserId(userId, now)
                    if (code != null) {
                        org.springframework.data.domain.PageImpl(listOf(code), pageable, 1)
                    } else {
                        Page.empty(pageable)
                    }
                } else {
                    val filtered = userCodeRepository.findAll(pageable).content.filter { 
                        it.telegramUserId == userId && it.isExpired()
                    }
                    org.springframework.data.domain.PageImpl(filtered, pageable, filtered.size.toLong())
                }
            }
            userId != null -> {
                val filtered = userCodeRepository.findAll(pageable).content.filter { it.telegramUserId == userId }
                org.springframework.data.domain.PageImpl(filtered, pageable, filtered.size.toLong())
            }
            active != null -> {
                if (active) {
                    val filtered = userCodeRepository.findAll(pageable).content.filter { it.isActive() }
                    org.springframework.data.domain.PageImpl(filtered, pageable, filtered.size.toLong())
                } else {
                    val expired = userCodeRepository.findExpiredCodes(now)
                    org.springframework.data.domain.PageImpl(expired, pageable, expired.size.toLong())
                }
            }
            expired != null && expired -> {
                val expired = userCodeRepository.findExpiredCodes(now)
                org.springframework.data.domain.PageImpl(expired, pageable, expired.size.toLong())
            }
            else -> userCodeRepository.findAll(pageable)
        }
        
        return codesPage.toPagedResponse { it.toAdminDto() }
    }

    /**
     * Получить код по ID
     */
    fun getCodeById(id: Long): AdminCodeDto {
        val code = userCodeRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Code with id $id not found") }
        return code.toAdminDto()
    }

    /**
     * Удалить код (инвалидация)
     */
    @Transactional
    fun deleteCode(id: Long) {
        val code = userCodeRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Code with id $id not found") }
        userCodeRepository.delete(code)
    }

    /**
     * Получить статистику по кодам
     */
    fun getCodeStats(): CodeStatsDto {
        val now = LocalDateTime.now()
        val allCodes = userCodeRepository.findAll()
        val activeCodes = allCodes.count { it.isActive() }
        val expiredCodes = allCodes.count { it.isExpired() }
        
        return CodeStatsDto(
            total = allCodes.size.toLong(),
            active = activeCodes.toLong(),
            expired = expiredCodes.toLong()
        )
    }

    data class CodeStatsDto(
        val total: Long,
        val active: Long,
        val expired: Long
    )

    // ========== Verification Sessions ==========

    /**
     * Получить список сессий верификации с пагинацией и фильтрами
     */
    fun getVerificationSessions(
        page: Int,
        size: Int,
        status: VerificationStatus? = null,
        userId: Long? = null
    ): PagedResponse<AdminVerificationDto> {
        val pageable: Pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        
        val sessionsPage: Page<VerificationSession> = when {
            status != null && userId != null -> {
                val sessions = verificationSessionRepository.findByTelegramUserIdAndStatus(userId, status)
                org.springframework.data.domain.PageImpl(sessions, pageable, sessions.size.toLong())
            }
            status != null -> {
                val filtered = verificationSessionRepository.findAll(pageable).content.filter { it.status == status }
                org.springframework.data.domain.PageImpl(filtered, pageable, filtered.size.toLong())
            }
            userId != null -> {
                val filtered = verificationSessionRepository.findAll(pageable).content.filter { it.telegramUserId == userId }
                org.springframework.data.domain.PageImpl(filtered, pageable, filtered.size.toLong())
            }
            else -> verificationSessionRepository.findAll(pageable)
        }
        
        return sessionsPage.toPagedResponse { it.toAdminDto() }
    }

    /**
     * Получить сессию верификации по correlationId
     */
    fun getVerificationSessionByCorrelationId(correlationId: UUID): AdminVerificationDto {
        val session = verificationSessionRepository.findByCorrelationId(correlationId)
            ?: throw IllegalArgumentException("Verification session with correlationId $correlationId not found")
        return session.toAdminDto()
    }

    /**
     * Получить статистику по сессиям верификации
     */
    fun getVerificationStats(): VerificationStatsDto {
        val allSessions = verificationSessionRepository.findAll()
        val pending = allSessions.count { it.status == VerificationStatus.PENDING }
        val confirmed = allSessions.count { it.status == VerificationStatus.CONFIRMED }
        val revoked = allSessions.count { it.status == VerificationStatus.REVOKED }
        
        return VerificationStatsDto(
            total = allSessions.size.toLong(),
            pending = pending.toLong(),
            confirmed = confirmed.toLong(),
            revoked = revoked.toLong()
        )
    }

    data class VerificationStatsDto(
        val total: Long,
        val pending: Long,
        val confirmed: Long,
        val revoked: Long
    )

    // ========== Auth Requests ==========

    /**
     * Получить список запросов аутентификации с пагинацией и фильтрами
     */
    fun getAuthRequests(
        page: Int,
        size: Int,
        userId: Long? = null,
        traceId: UUID? = null
    ): PagedResponse<AdminAuthRequestDto> {
        val pageable: Pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "requestedAt"))
        
        val requestsPage: Page<AuthRequest> = when {
            traceId != null -> {
                val request = authRequestRepository.findByTraceId(traceId)
                if (request != null) {
                    org.springframework.data.domain.PageImpl(listOf(request), pageable, 1)
                } else {
                    Page.empty(pageable)
                }
            }
            userId != null -> {
                val requests = authRequestRepository.findByTelegramUserId(userId)
                org.springframework.data.domain.PageImpl(requests, pageable, requests.size.toLong())
            }
            else -> authRequestRepository.findAll(pageable)
        }
        
        return requestsPage.toPagedResponse { it.toAdminDto() }
    }

    /**
     * Получить запрос аутентификации по traceId
     */
    fun getAuthRequestByTraceId(traceId: UUID): AdminAuthRequestDto {
        val request = authRequestRepository.findByTraceId(traceId)
            ?: throw IllegalArgumentException("Auth request with traceId $traceId not found")
        return request.toAdminDto()
    }

    /**
     * Получить статистику по запросам аутентификации
     */
    fun getAuthRequestStats(): AuthRequestStatsDto {
        val allRequests = authRequestRepository.findAll()
        val withCode = allRequests.count { it.code != null }
        val withoutCode = allRequests.size - withCode
        
        return AuthRequestStatsDto(
            total = allRequests.size.toLong(),
            withCode = withCode.toLong(),
            withoutCode = withoutCode.toLong()
        )
    }

    data class AuthRequestStatsDto(
        val total: Long,
        val withCode: Long,
        val withoutCode: Long
    )
}

