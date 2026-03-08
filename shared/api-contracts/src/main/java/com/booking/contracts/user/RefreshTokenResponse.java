package com.booking.contracts.user;

import java.time.Instant;

/**
 * Response DTO for token refresh operation.
 * Contains a new access token and its expiration time.
 * 
 * @param accessToken New JWT access token for authenticating API requests
 * @param expiresAt Timestamp when the new access token expires
 */
public record RefreshTokenResponse(
    String accessToken,
    Instant expiresAt
) {}
