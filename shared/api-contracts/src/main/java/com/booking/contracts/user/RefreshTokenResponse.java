package com.booking.contracts.user;

import java.time.Instant;

/**
 * Response DTO for token refresh operation.
 * Contains a new access token, new refresh token (for token rotation), and expiration time.
 *
 * <p>Token rotation is a security best practice where the refresh token is replaced
 * with a new one after each use, preventing token reuse attacks.</p>
 *
 * @param accessToken New JWT access token for authenticating API requests
 * @param refreshToken New refresh token (replaces the old one for security)
 * @param expiresAt Timestamp when the new access token expires
 */
public record RefreshTokenResponse(
    String accessToken,
    String refreshToken,
    Instant expiresAt
) {}
