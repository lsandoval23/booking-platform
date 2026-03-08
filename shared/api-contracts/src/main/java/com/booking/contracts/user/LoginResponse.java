package com.booking.contracts.user;

import java.time.Instant;

/**
 * Response DTO for successful user login.
 * Contains authentication tokens and user information.
 * 
 * @param accessToken JWT access token for authenticating API requests
 * @param refreshToken JWT refresh token for obtaining new access tokens
 * @param expiresAt Timestamp when the access token expires
 * @param user User information
 */
public record LoginResponse(
    String accessToken,
    String refreshToken,
    Instant expiresAt,
    UserDTO user
) {}
