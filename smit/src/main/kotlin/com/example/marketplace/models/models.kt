package com.example.marketplace.models

import java.time.LocalDateTime
import java.util.*

data class User (
    val id: UUID = UUID.randomUUID(),
    val email: String,
    val password: String,
    val role: Role,
    val createdOn: LocalDateTime = LocalDateTime.now(),
)

data class Course (
    val id: UUID = UUID.randomUUID(),
    val title: String,
    val description: String,
    val price: Double,
    val creatorId: UUID,
    val createdOn: LocalDateTime = LocalDateTime.now(),
)

data class Transaction (
    val id: UUID = UUID.randomUUID(),
    val course: Course,
    val customer: User,
    val amount: Double,
    val purchasedAt: LocalDateTime = LocalDateTime.now(),
)

enum class Role(val value: String) {
    ADMIN("admin"),
    CREATOR("creator"),
    CUSTOMER("customer")
}