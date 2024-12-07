package com.example.marketplace.utils

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.Claims
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.stereotype.Component
import java.util.*

@Component
class JwtUtil {

    private val secretKey = System.getenv("JWT_SECRET_KEY") ?: "secret" // Load secret key from env

    private fun extractClaims(token: String): Claims {
        return try {
            Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .body
        } catch (e: Exception) {
            throw RuntimeException("Invalid token")
        }
    }

    fun generateToken(username: String, role: String, id: UUID): String {
        val now = Date()
        val expiryDate = Date(now.time + 1000 * 60 * 60 * 1) // Token valid for 1 hour

        return Jwts.builder()
            .setSubject(username)
            .claim("role", role)
            .claim("id", id)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(SignatureAlgorithm.HS256, secretKey.toByteArray())
            .compact()
    }

    fun getIdFromToken(token: String): UUID? {
        try {
            val claims = extractClaims(token)
            val idString = claims["id"] as? String ?: return null
            return UUID.fromString(idString)
        } catch (e: Exception) {
            println("Error extracting ID from token: $e")
            return null
        }
    }

    fun getRoleFromToken(token: String): String? {
        val claims = extractClaims(token)
        return claims["role"] as? String
    }

    // Get username from token
    fun getUsernameFromToken(token: String): String {
        return extractClaims(token).subject
    }

    // Validate token
    fun validateToken(token: String): Boolean {
        return try {
            !isTokenExpired(token)
        } catch (e: Exception) {
            false
        }
    }

    // Check if the token is expired
    private fun isTokenExpired(token: String): Boolean {
        return extractClaims(token).expiration.before(Date())
    }

}
