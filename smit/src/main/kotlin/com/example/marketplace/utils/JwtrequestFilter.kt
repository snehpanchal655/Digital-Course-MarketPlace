package com.example.marketplace.utils

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Component
class JwtRequestFilter(private val jwtUtil: JwtUtil) : OncePerRequestFilter() {

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = request.getHeader("Authorization")?.let {
            if (it.startsWith("Bearer ")) it.substring(7) else null
        }

        if (token != null && jwtUtil.validateToken(token)) {
            val userDetails = jwtUtil.getUsernameFromToken(token)
            val role = jwtUtil.getRoleFromToken(token)

            // You can add more claims here if needed
            val authorities = listOf(SimpleGrantedAuthority("ROLE_$role"))

            // Create an authentication token with the user's details and authorities (roles)
            val authentication = UsernamePasswordAuthenticationToken(userDetails, null, authorities)

            // Set the authentication in the SecurityContext
            SecurityContextHolder.getContext().authentication = authentication
        }

        // Continue the filter chain
        filterChain.doFilter(request, response)
    }
}





