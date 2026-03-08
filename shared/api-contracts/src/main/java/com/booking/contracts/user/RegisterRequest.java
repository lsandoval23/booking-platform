package com.booking.contracts.user;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for user registration.
 * Contains the necessary information to create a new user account.
 * 
 * @param email The user's email address (must not be blank)
 * @param password The user's password (must not be blank)
 * @param role The user's role (must not be blank, e.g., CUSTOMER, PROVIDER)
 */
public record RegisterRequest(
    @NotBlank(message = "Email is required") String email,
    @NotBlank(message = "Password is required") String password,
    @NotBlank(message = "Role is required") String role
) {}
