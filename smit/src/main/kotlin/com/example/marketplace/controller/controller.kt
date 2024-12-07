package com.example.marketplace.controller

import com.example.marketplace.models.Role
import com.example.marketplace.models.Transaction
import com.example.marketplace.services.CourseService
import com.example.marketplace.services.TransactionService
import com.example.marketplace.services.UserService
import com.example.marketplace.utils.JwtUtil
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.util.*


@RestController
@RequestMapping("/api/v1")
class Controller(
    private val courseService: CourseService,
    private val jwtUtil: JwtUtil,
    private val transactionService: TransactionService,
    private val userService: UserService
) {

    @PostMapping("/signup")
    fun signup(@RequestBody request: SignupRequest): ResponseEntity<UserResponse> {
        val user = userService.createUser(request)
        val userResponse = UserResponse(user.id, user.email, user.role)
        return ResponseEntity.ok(userResponse)
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        val user = userService.authenticateUser(request)
        val token = jwtUtil.generateToken(user.email, user.role.value, user.id)
        return ResponseEntity.ok(LoginResponse(token))
    }

    @PreAuthorize("hasRole('creator')")
    @PostMapping("/course")  //create a course
    fun addCourse(@RequestBody courseRequest: CourseRequest, request: HttpServletRequest): ResponseEntity<CourseResponse> {
        val token = request.getHeader("Authorization")?.substring(7)
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401).build()
        }
        val userId = jwtUtil.getIdFromToken(token)!! //get user from userid if its a creator pass otherwise fail
        val course = courseService.addCourse(courseRequest, userId)
        val courseResponse = CourseResponse(course.title, course.description, course.price)
        return ResponseEntity.ok(courseResponse)
    }

    @PreAuthorize("hasRole('creator')")
    @GetMapping("/course") //Fetch all courses created by the logged-in Creator.
    fun getCoursesByCreator(request: HttpServletRequest): ResponseEntity<List<CourseResponse>> {
        val token = request.getHeader("Authorization")?.substring(7)
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401).build()
        }
        val creatorId = jwtUtil.getIdFromToken(token)!!
        val courses = courseService.getAllCoursesByCreatorId(creatorId)
        val courseResponses = courses.map { course -> CourseResponse(course.title, course.description, course.price) }
        return ResponseEntity.ok(courseResponses)
    }

    @PreAuthorize("hasRole('customer')")
    @GetMapping("/course") //fetch all available course
    fun getAllCourses(@RequestParam(required = false) params: String?, request: HttpServletRequest): ResponseEntity<List<CourseResponse>>{
        val token = request.getHeader("Authorization")?.substring(7)
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401).build()
        }
        val courses = courseService.getAllCourses(params)
        val courseResponse =  courses.map { course -> CourseResponse(course.title, course.description, course.price) }
        return ResponseEntity.ok(courseResponse)
    }

    @PreAuthorize("hasRole('Customer')")
    @GetMapping("/buy/course/{id}")  //buy a course
    fun buyCourse(@PathVariable id: UUID, request: HttpServletRequest): ResponseEntity<Transaction> {
        val token = request.getHeader("Authorization")?.substring(7)
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401).build()
        }
        val userId = jwtUtil.getIdFromToken(token)!!
        val user = userService.getUserById(userId)!!
        val course = courseService.getCourseById(id) ?: throw RuntimeException("Course with ID $id does not exist.")
        val userCourse = transactionService.addCourseToLib(course, user, 100.0)
        return ResponseEntity.ok(userCourse)
    }

    @PreAuthorize("hasRole('admin')")
    @GetMapping("/user") //fetch all users
    fun fetchAllUser(request: HttpServletRequest): ResponseEntity<List<UserResponse>>{
        val token = request.getHeader("Authorization")?.substring(7)
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401).build()
        }
        val users = userService.fetchAllUsers()
        val userResponses = users.map { UserResponse(it.id, it.email, it.role) }
        return ResponseEntity.ok(userResponses)
    }

    @GetMapping("/stats") // Fetch a list of all bought courses and the total amount paid.
    fun fetchAllBoughtCourses(
        @RequestParam(required = false) startDate: String?,
        @RequestParam(required = false) endDate: String?,
        request: HttpServletRequest): ResponseEntity<Map<String, Any>> {
        val token = request.getHeader("Authorization")?.substring(7)
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401).build()
        }

        val start = startDate?.let { LocalDateTime.parse(it) }
        val end = endDate?.let { LocalDateTime.parse(it) }
        val transactions = transactionService.fetchTransactionByDateRange(start, end)
        val totalAmount = transactions.sumOf { it.amount }

        return ResponseEntity.ok(mapOf("transactions" to transactions, "totalAmount" to totalAmount))
    }

//    @GetMapping("/course/:id")
//    fun getAllCoursesByCreatorId(): ResponseEntity<List<CourseResponse>>{
//        val creatorId: UUID = getLoggedInCreatorId()
//        val courses = courseService.getAllCoursesByCreatorId()
//        val courseResponses = courses.map { course ->
//            CourseResponse(course.title, course.description, course.price, course.creatorId)
//        }
//        return ResponseEntity.ok(courseResponses)
//    }
}


data class CourseRequest(val title: String, val description: String, val price: Double)
data class CourseResponse(val title: String, val description: String, val price: Double)

data class SignupRequest(val email: String, val password: String, val role: Role)
data class LoginRequest(val email: String, val password: String)
data class UserResponse(val id: UUID, val email: String, val role: Role)
data class LoginResponse(val token: String)