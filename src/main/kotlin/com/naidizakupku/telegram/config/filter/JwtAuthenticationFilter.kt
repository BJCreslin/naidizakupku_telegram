package com.naidizakupku.telegram.config.filter

import com.naidizakupku.telegram.config.AdminUserDetailsService
import com.naidizakupku.telegram.service.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Фильтр для обработки JWT токенов в запросах
 */
@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val adminUserDetailsService: AdminUserDetailsService
) : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            val token = authHeader.substring(7) // Убираем "Bearer "

            if (jwtService.isTokenValid(token) && !jwtService.isRefreshToken(token)) {
                val username = jwtService.extractUsername(token)

                if (username != null && SecurityContextHolder.getContext().authentication == null) {
                    val userDetails: UserDetails = adminUserDetailsService.loadUserByUsername(username)

                    if (jwtService.isTokenValid(token)) {
                        val authToken = UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.authorities
                        )
                        authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                        SecurityContextHolder.getContext().authentication = authToken
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Cannot set user authentication: ${e.message}", e)
        }

        filterChain.doFilter(request, response)
    }
}

