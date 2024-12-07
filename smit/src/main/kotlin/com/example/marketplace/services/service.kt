package com.example.marketplace.services

import com.example.marketplace.controller.CourseRequest
import com.example.marketplace.controller.LoginRequest
import com.example.marketplace.controller.SignupRequest
import com.example.marketplace.models.Course
import com.example.marketplace.models.Transaction
import com.example.marketplace.models.User
import com.example.marketplace.repository.CourseRepository
import com.example.marketplace.repository.TransactionRepository
import com.example.marketplace.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.time.LocalDateTime
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: BCryptPasswordEncoder
) {
    fun createUser(data: SignupRequest): User {
        if (userRepository.findByEmail(data.email) != null) {
            throw IllegalArgumentException("User already exists with email: $data.email")
        }
        val hashedPassword = passwordEncoder.encode(data.password)
        val user = User(email = data.email, password = hashedPassword, role = data.role)
        return userRepository.createUser(user)
    }

    fun getUserById(id: UUID): User? {
        return userRepository.getUserById(id)
    }

    fun authenticateUser(data: LoginRequest): User {
        val user = userRepository.findByEmail(data.email) ?: throw IllegalArgumentException("User not found with email: $data.email")
        if (!passwordEncoder.matches(data.password, user.password)) {
            throw IllegalArgumentException("Invalid credentials")
        }
        return user
    }

    fun fetchAllUsers(): List<User>{
        return userRepository.fetchAllUsers()
    }
}

class CourseService(
    private val courseRepository: CourseRepository
) {
    fun addCourse(data: CourseRequest, id: UUID): Course {
        val course = Course(title = data.title, description = data.description, price = data.price, creatorId = id)
        return courseRepository.createCourse(course)
    }

    fun getAllCourses(param: String?): List<Course> {
        return courseRepository.findCoursesByTitleOrDescription(param)
    }

    fun getCourseById(id: UUID): Course? {
        return courseRepository.getCourseById(id)
    }

    fun getAllCoursesByCreatorId(id: UUID): List<Course> {
        return courseRepository.findCoursesByCreatorId(id)
    }

}

class TransactionService(
    private val transactionRepository: TransactionRepository
) {
    fun addCourseToLib(course: Course, user: User, amount: Double): Transaction {
        val transaction = Transaction(course = course, customer = user, amount = amount)
        return transactionRepository.saveTransaction(transaction)
    }

    fun fetchTransactionByDateRange(startDate: LocalDateTime?, endDate: LocalDateTime?): List<Transaction> {
        return transactionRepository.fetchTransactionsByDateRange(startDate, endDate)
    }
}