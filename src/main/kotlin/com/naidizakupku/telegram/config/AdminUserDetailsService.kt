package com.naidizakupku.telegram.config

import com.naidizakupku.telegram.domain.admin.AdminUser
import com.naidizakupku.telegram.repository.admin.AdminUserRepository
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

/**
 * Сервис для загрузки данных пользователя админки для Spring Security
 */
@Service
class AdminUserDetailsService(
    private val adminUserRepository: AdminUserRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val adminUser = adminUserRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("Admin user not found: $username")

        if (!adminUser.active) {
            throw UsernameNotFoundException("Admin user is inactive: $username")
        }

        val authorities: Collection<GrantedAuthority> = listOf(
            SimpleGrantedAuthority("ROLE_${adminUser.role.name}")
        )

        return User(
            adminUser.username,
            adminUser.passwordHash,
            adminUser.active,
            true, // accountNonExpired
            true, // credentialsNonExpired
            true, // accountNonLocked
            authorities
        )
    }

    /**
     * Загрузить AdminUser по username
     */
    fun loadAdminUserByUsername(username: String): AdminUser {
        return adminUserRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("Admin user not found: $username")
    }
}

