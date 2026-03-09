package com.booking.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Auth Service.
 * 
 * This microservice handles:
 * - User authentication (login/register)
 * - JWT token generation and validation
 * - Token refresh mechanism
 * - Token blacklisting (logout)
 * - Authorization checks
 * 
 * @author Booking Platform Team
 * @version 1.0.0
 */
@SpringBootApplication
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
