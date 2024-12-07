package com.example.marketplace

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@SpringBootApplication
class Application {
    @Bean
    public fun passwordEncoder() = BCryptPasswordEncoder()
}

fun main(args: Array<String>) {
//    println("Hello World")
    runApplication<Application>(*args)
}