package com.booking.auth.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * RefreshToken entity for managing JWT refresh tokens.
 * Follows Isolated Model Architecture - stores userId as UUID, not as @ManyToOne relationship.
 * This ensures service autonomy and clear boundaries.
 */
@Entity
@Table(name = "refresh_tokens", indexes = {
    @Index(name = "idx_refresh_token_user_id", columnList = "user_id"),
    @Index(name = "idx_refresh_token_token", columnList = "token")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "token", nullable = false, unique = true, length = 500)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * JPA lifecycle callback to set the createdAt timestamp before persisting a new entity.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    /**
     * Checks if the refresh token has expired.
     *
     * @return true if the current time is after the expiration time, false otherwise
     */
    public boolean isExpired() {
        return Instant.now().isAfter(this.expiresAt);
    }
}
