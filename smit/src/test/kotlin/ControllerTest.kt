//package org.example
//
//import com.example.marketplace.*
//import com.example.marketplace.com.example.marketplace.Main
//import org.junit.jupiter.api.Test
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
//import org.springframework.test.web.servlet.MockMvc
//import org.springframework.test.web.servlet.post
//import org.springframework.http.MediaType
//import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
//import org.springframework.boot.test.mock.mockito.MockBean
//
//@SpringBootTest(classes = [Main::class])
//@AutoConfigureMockMvc
//class ControllerTest {
//
//    @Autowired
//    lateinit var mockMvc: MockMvc
//
//    @MockBean
//    lateinit var userService: UserService
//
//    @Test
//    fun `should sign up a new user and print response`() {
//        // Prepare mock data for the test
//        val signupRequest = SignupRequest(
//            email = "test@example.com",
//            password = "password123",
//            role = Role.ADMIN
//        )
//
//        // Perform the HTTP POST request and print the response
//        val result = mockMvc.post("/api/v1/signup") {
//            contentType = MediaType.APPLICATION_JSON
//            content = jacksonObjectMapper().writeValueAsString(signupRequest)
//        }
//
//        // Print the response body to the console (for debugging purposes)
//        println("Response: ${result.andReturn().response.contentAsString}")
//
//    }
//}
