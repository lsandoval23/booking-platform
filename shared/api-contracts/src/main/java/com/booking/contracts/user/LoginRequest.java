package com.booking.contracts.user;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for user login.
 * Contains the credentials needed to authenticate a user.
 * 
 * @param email The user's email address (must not be blank)
 * @param password The user's password (must not be blank)
 */
public record LoginRequest(
    @NotBlank(message = "Email is required") String email,
    @NotBlank(message = "Password is required") String password
) {}
