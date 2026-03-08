package com.booking.contracts.user;

import java.time.Instant;
import java.util.UUID;

/**
 * Data Transfer Object representing a user in the system.
 * Used for inter-service communication to share user information.
 * 
 * @param id The unique identifier of the user
 * @param email The user's email address
 * @param role The user's role (e.g., CUSTOMER, PROVIDER, ADMIN)
 * @param createdAt Timestamp when the user was created
 * @param updatedAt Timestamp when the user was last updated
 */
public record UserDTO(
    UUID id,
    String email,
    String role,
    Instant createdAt,
    Instant updatedAt
) {}
