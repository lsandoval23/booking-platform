package com.booking.contracts.user;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for refreshing an access token.
 * Contains the refresh token needed to obtain a new access token.
 * 
 * @param refreshToken The refresh token (must not be blank)
 */
public record RefreshTokenRequest(
    @NotBlank(message = "Refresh token is required") String refreshToken
) {}
