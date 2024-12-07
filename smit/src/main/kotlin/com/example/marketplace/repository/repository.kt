package com.example.marketplace.repository

import com.example.marketplace.models.Course
import com.example.marketplace.models.Role
import com.example.marketplace.models.Transaction
import com.example.marketplace.models.User
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.time.LocalDateTime
import java.util.*

interface UserRepository {
    fun createUser(user: User): User
    fun findByEmail(email: String): User?
    fun checkUserIdExists(id: UUID): Boolean
    fun getUserById(id: UUID): User?
    fun fetchAllUsers(): List<User>
}

interface CourseRepository {
    fun createCourse(course: Course): Course
    fun findAllCourses(): List<Course>
    fun findCoursesByCreatorId(creatorId: UUID): List<Course>
    fun findCoursesByTitleOrDescription(search: String?): List<Course>
    fun getCourseById(id: UUID): Course?
}

interface TransactionRepository {
    fun saveTransaction(transaction: Transaction): Transaction
    fun fetchTransactionsByDateRange(startDate: LocalDateTime?, endDate: LocalDateTime?): List<Transaction>
}


class UserRepositoryImpl : UserRepository {
    // Initialize the H2 in-memory database connection
    private val connection: Connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "")

    init {
        initializeDatabase()
    }

    private fun initializeDatabase() {
        val sql = """
            CREATE TABLE IF NOT EXISTS users (
                id UUID PRIMARY KEY,
                email VARCHAR(255) NOT NULL UNIQUE,
                password VARCHAR(255) NOT NULL,
                role VARCHAR(50) NOT NULL,
                created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """.trimIndent()
        connection.createStatement().use { it.execute(sql) }
    }

    override fun createUser(user: User): User {
        val sql = "INSERT INTO users (id, email, password, role, created_on) VALUES (?, ?, ?, ?, ?)"
        connection.prepareStatement(sql).use { statement ->
            statement.setObject(1, user.id)
            statement.setString(2, user.email)
            statement.setString(3, user.password)
            statement.setString(4, user.role.value)
            statement.setTimestamp(5, java.sql.Timestamp.valueOf(user.createdOn))
            statement.executeUpdate()
        }
        return user
    }

    override fun findByEmail(email: String): User? {
        val sql = "SELECT * FROM users WHERE email = ?"
        connection.prepareStatement(sql).use { statement ->
            statement.setString(1, email)
            val resultSet = statement.executeQuery()
            return if (resultSet.next()) mapToUser(resultSet) else null
        }
    }

    override fun checkUserIdExists(id: UUID): Boolean {
        val sql = "select count(*) from users where id = ?"
        connection.prepareStatement(sql).use { statement ->
            statement.setObject(1, id)
            statement.executeQuery().use { resultSet ->
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0
                }
            }
        }
        return false
    }

    override fun getUserById(id: UUID): User? {
        val sql = "SELECT * FROM users WHERE id = ?"
        connection.prepareStatement(sql).use { statement ->
            statement.setObject(1, id)
            statement.executeQuery().use { resultSet ->
                if (resultSet.next()) {
                    return mapToUser(resultSet)
                }
            }
        }
        return null
    }

    override fun fetchAllUsers(): List<User> {
        val sql = "Select * from users where role = 'creator' or role = 'customer'"
        connection.prepareStatement(sql).use { statement ->
            val resultSet = statement.executeQuery()
            val users = mutableListOf<User>()
            while (resultSet.next()) {
                users.add(mapToUser(resultSet))
            }
            return users
        }

    }

}


class CourseRepositoryImpl : CourseRepository {

    private val connection: Connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "")
    @PersistenceContext
    private lateinit var entityManager: EntityManager

    init {
        initializeDatabase()
    }

    private fun initializeDatabase() {
        val sql = """
            CREATE TABLE IF NOT EXISTS courses (
                id UUID PRIMARY KEY,
                title VARCHAR(255) NOT NULL,
                description TEXT,
                price DOUBLE NOT NULL,
                creator_id UUID NOT NULL,
                created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """.trimIndent()
        connection.createStatement().use { it.execute(sql) }
    }

    override fun createCourse(course: Course): Course {
        val sql = "INSERT INTO courses (id, title, description, price, created_on) VALUES (?, ?, ?, ?, ?)"
        connection.prepareStatement(sql).use { statement ->
            statement.setObject(1, course.id)
            statement.setString(2, course.title)
            statement.setString(3, course.description)
            statement.setDouble(4, course.price)
            statement.setTimestamp(5, java.sql.Timestamp.valueOf(course.createdOn))
            statement.executeUpdate()
        }
        return course
    }

    override fun findAllCourses(): List<Course> {
        val sql = "SELECT * FROM courses"
        connection.prepareStatement(sql).use { statement ->
            val resultSet = statement.executeQuery()
            val courses = mutableListOf<Course>()
            while (resultSet.next()) {
                courses.add(mapToCourse(resultSet))
            }
            return courses
        }
    }

    override fun findCoursesByCreatorId(creatorId: UUID): List<Course> {
        val sql = "SELECT * FROM courses WHERE creator_id = ?"
        connection.prepareStatement(sql).use { statement ->
            statement.setObject(1, creatorId)
            val resultSet = statement.executeQuery()
            val courses = mutableListOf<Course>()
            while (resultSet.next()) {
                courses.add(mapToCourse(resultSet))
            }
            return courses
        }
    }

    override fun findCoursesByTitleOrDescription(search: String?): List<Course> {
        val sql = StringBuilder("SELECT * FROM courses WHERE 1=1")
        sql.append(" AND (LOWER(title) LIKE ? OR LOWER(description) LIKE ?)")

        connection.prepareStatement(sql.toString()).use { statement ->
            val searchParam = "%${search?.lowercase()}%"

            statement.setString(1, searchParam)
            statement.setString(2, searchParam)

            statement.executeQuery().use { resultSet ->
                val courses = mutableListOf<Course>()
                while (resultSet.next()) {
                    courses.add(mapToCourse(resultSet))
                }
                return courses
            }
        }
    }


    override fun getCourseById(id: UUID): Course? {
        val sql = "SELECT id, title, description, price, creator_id, created_on FROM courses WHERE id = ?"
        connection.prepareStatement(sql).use { statement ->
            statement.setObject(1, id)
            statement.executeQuery().use { resultSet ->
                if (resultSet.next()) {
                    return mapToCourse(resultSet) // Return the course if found
                }
            }
        }
        return null // Return null if the course is not found
    }

}

class TransactionRepositoryImpl : TransactionRepository {
    private val connection: Connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "")

    init {
        initializeDatabase()
    }

    private fun initializeDatabase() {
        val sql = """
        CREATE TABLE IF NOT EXISTS transactions (
            id UUID PRIMARY KEY,
            course_id UUID NOT NULL,
            customer_id UUID NOT NULL,
            amount DOUBLE NOT NULL,
            purchased_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
            FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE
        )
    """.trimIndent()

        connection.createStatement().use { it.execute(sql) }
    }

    override fun saveTransaction(transaction: Transaction): Transaction {
        val sql = """
        INSERT INTO transactions (id, course_id, customer_id, amount, purchased_at) 
        VALUES (?, ?, ?, ?, ?)
    """.trimIndent()

        connection.prepareStatement(sql).use { statement ->
            statement.setObject(1, transaction.id)
            statement.setObject(2, transaction.course.id)
            statement.setObject(3, transaction.customer.id)
            statement.setDouble(4, transaction.amount)
            statement.setTimestamp(5, java.sql.Timestamp.valueOf(transaction.purchasedAt))
            statement.executeUpdate()
        }
        return transaction
    }

    override fun fetchTransactionsByDateRange(startDate: LocalDateTime?, endDate: LocalDateTime?): List<Transaction> {
        val sql = StringBuilder("SELECT * FROM transactions WHERE 1=1")

        startDate?.let { sql.append(" AND purchased_at >= ?") }
        endDate?.let { sql.append(" AND purchased_at <= ?") }

        connection.prepareStatement(sql.toString()).use { statement ->
            var index = 1
            startDate?.let {
                statement.setObject(index++, startDate)
            }
            endDate?.let {
                statement.setObject(index, endDate)
            }
            statement.executeQuery().use { resultSet ->
                val transactions = mutableListOf<Transaction>()
                while (resultSet.next()) {
                    transactions.add(mapToTransaction(resultSet))
                }
                return transactions
            }
        }
    }

    private fun mapToTransaction(resultSet: ResultSet): Transaction {
        return Transaction(
            id = UUID.fromString(resultSet.getString("id")),
            course = mapToCourse(resultSet),
            customer = mapToUser(resultSet),
            amount = resultSet.getDouble("amount"),
            purchasedAt = resultSet.getTimestamp("purchased_at").toLocalDateTime()
        )
    }
}

private fun mapToCourse(resultSet: ResultSet): Course {
    return Course(
        id = UUID.fromString(resultSet.getString("id")),
        title = resultSet.getString("title"),
        description = resultSet.getString("description"),
        price = resultSet.getDouble("price"),
        creatorId = UUID.fromString(resultSet.getString("creator_id")),
        createdOn = resultSet.getTimestamp("created_on").toLocalDateTime()
    )
}

private fun mapToUser(resultSet: ResultSet): User {
    return User(
        id = UUID.fromString(resultSet.getString("id")),
        email = resultSet.getString("email"),
        password = resultSet.getString("password"),
        role = Role.valueOf(resultSet.getString("role").uppercase()),
        createdOn = resultSet.getTimestamp("created_on").toLocalDateTime()
    )
}