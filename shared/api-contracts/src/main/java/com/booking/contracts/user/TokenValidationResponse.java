package com.booking.contracts.user;

import java.util.UUID;

/**
 * Response DTO for token validation.
 * Contains information about whether a token is valid and the associated user details.
 * 
 * @param valid Whether the token is valid
 * @param userId The ID of the user associated with the token (null if invalid)
 * @param email The email of the user associated with the token (null if invalid)
 * @param role The role of the user associated with the token (null if invalid)
 */
public record TokenValidationResponse(
    boolean valid,
    UUID userId,
    String email,
    String role
) {}
