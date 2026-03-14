package com.booking.auth.repository;

import com.booking.auth.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link RefreshToken} entity.
 * Provides CRUD operations and custom query methods for refresh token management.
 * 
 * <p>This repository handles refresh token lifecycle operations including:</p>
 * <ul>
 *   <li>Token lookup and validation</li>
 *   <li>User-specific token management</li>
 *   <li>Token revocation (logout)</li>
 *   <li>Expired token cleanup</li>
 * </ul>
 * 
 * <p>All modifying operations are marked with {@code @Modifying} and {@code @Transactional}
 * to ensure proper transaction management and cache invalidation.</p>
 * 
 * @see RefreshToken
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * Finds a refresh token by its token string.
     * 
     * <p>This method is used during token refresh operations to validate and
     * retrieve the token details. The token field is indexed for optimal query
     * performance.</p>
     * 
     * @param token the token string to search for
     * @return an Optional containing the refresh token if found, or empty if not found
     * @throws IllegalArgumentException if token is null
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Finds all refresh tokens associated with a specific user.
     * 
     * <p>This method is useful for administrative purposes or when implementing
     * features like "view active sessions" or "logout from all devices". The
     * userId field is indexed for optimal query performance.</p>
     * 
     * @param userId the UUID of the user whose tokens to retrieve
     * @return a list of refresh tokens for the user (never null, empty if no tokens)
     * @throws IllegalArgumentException if userId is null
     */
    List<RefreshToken> findByUserId(UUID userId);

    /**
     * Deletes all refresh tokens associated with a specific user.
     *
     * <p>This method is used for "logout from all devices" functionality or when
     * a user account is deleted/suspended. The operation is transactional to
     * ensure atomicity.</p>
     *
     * <p>Note: This is a modifying query that requires a transaction. Spring Data
     * JPA will automatically clear the persistence context after execution.</p>
     *
     * @param userId the UUID of the user whose tokens should be deleted
     * @return the number of tokens deleted
     * @throws IllegalArgumentException if userId is null
     */
    @Modifying
    @Transactional
    int deleteByUserId(UUID userId);

    /**
     * Deletes a specific refresh token by its token string.
     * 
     * <p>This method is used for single device logout functionality. When a user
     * logs out from a specific device, only that device's refresh token is
     * revoked.</p>
     * 
     * <p>Note: This is a modifying query that requires a transaction. Spring Data
     * JPA will automatically clear the persistence context after execution.</p>
     * 
     * @param token the token string to delete
     * @throws IllegalArgumentException if token is null
     */
    @Modifying
    @Transactional
    void deleteByToken(String token);

    /**
     * Deletes all expired refresh tokens from the database.
     *
     * <p>This method is intended to be called by a scheduled cleanup job to
     * remove expired tokens and maintain database hygiene. It uses a custom
     * JPQL query to efficiently delete expired tokens in bulk.</p>
     *
     * <p>Usage example in a scheduled task:</p>
     * <pre>
     * {@code
     * @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
     * public void cleanupExpiredTokens() {
     *     int deletedCount = refreshTokenRepository.deleteExpiredTokens(Instant.now());
     *     logger.info("Deleted {} expired tokens", deletedCount);
     * }
     * }
     * </pre>
     *
     * @param now the current timestamp to compare against token expiration times
     * @return the number of tokens deleted
     * @throws IllegalArgumentException if now is null
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") Instant now);
}
