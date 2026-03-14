package com.booking.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Utility class for JWT token operations including generation, validation, and extraction.
 * This class is thread-safe and can be used as a Spring bean.
 * 
 * @author Booking Platform Team
 * @version 1.0.0
 */

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    private final SecretKey signingKey;

    /**
     * Constructs a JwtUtil with configuration from application properties.
     *
     * @param secretKey the secret key for signing tokens
     * @param accessTokenExpiration access token expiration time in milliseconds
     * @param refreshTokenExpiration refresh token expiration time in milliseconds
     */
    public JwtUtil(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration.access}") long accessTokenExpiration,
            @Value("${jwt.expiration.refresh}") long refreshTokenExpiration) {
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.signingKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates an access token with user information.
     *
     * @param userId the user's unique identifier
     * @param email the user's email address
     * @param role the user's role
     * @return the generated JWT access token
     * @throws IllegalArgumentException if any parameter is null
     */
    public String generateAccessToken(UUID userId, String email, String role) {
        if (userId == null || email == null || role == null) {
            throw new IllegalArgumentException("userId, email, and role must not be null");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toString());
        claims.put("email", email);
        claims.put("role", role);
        claims.put("type", "access");

        Instant now = Instant.now();
        Instant expiration = now.plus(accessTokenExpiration, ChronoUnit.MILLIS);

        String token = Jwts.builder()
                .claims(claims)
                .subject(userId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(signingKey)
                .compact();

        logger.debug("Generated access token for user: {}", userId);
        return token;
    }

    /**
     * Generates a refresh token with minimal claims.
     *
     * @return the generated JWT refresh token
     */
    public String generateRefreshToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        claims.put("tokenId", UUID.randomUUID().toString());

        Instant now = Instant.now();
        Instant expiration = now.plus(refreshTokenExpiration, ChronoUnit.MILLIS);

        String token = Jwts.builder()
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(signingKey)
                .compact();

        logger.debug("Generated refresh token");
        return token;
    }

    /**
     * Validates a JWT token.
     *
     * @param token the JWT token to validate
     * @return true if the token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            logger.warn("Token validation failed: token is null or empty");
            return false;
        }

        try {
            Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token);
            logger.debug("Token validation successful");
            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Extracts the user ID from a JWT token.
     *
     * @param token the JWT token
     * @return the user ID
     * @throws IllegalArgumentException if the token is invalid or doesn't contain a userId
     */
    public UUID extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        String userIdStr = claims.get("userId", String.class);
        if (userIdStr == null) {
            throw new IllegalArgumentException("Token does not contain userId claim");
        }
        return UUID.fromString(userIdStr);
    }

    /**
     * Extracts the email from a JWT token.
     *
     * @param token the JWT token
     * @return the email address
     * @throws IllegalArgumentException if the token is invalid or doesn't contain an email
     */
    public String extractEmail(String token) {
        Claims claims = extractAllClaims(token);
        String email = claims.get("email", String.class);
        if (email == null) {
            throw new IllegalArgumentException("Token does not contain email claim");
        }
        return email;
    }

    /**
     * Extracts the role from a JWT token.
     *
     * @param token the JWT token
     * @return the user role
     * @throws IllegalArgumentException if the token is invalid or doesn't contain a role
     */
    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        String role = claims.get("role", String.class);
        if (role == null) {
            throw new IllegalArgumentException("Token does not contain role claim");
        }
        return role;
    }

    /**
     * Extracts the expiration time from a JWT token.
     *
     * @param token the JWT token
     * @return the expiration time as an Instant
     * @throws IllegalArgumentException if the token is invalid
     */
    public Instant extractExpiration(String token) {
        Claims claims = extractAllClaims(token);
        Date expiration = claims.getExpiration();
        if (expiration == null) {
            throw new IllegalArgumentException("Token does not contain expiration claim");
        }
        return expiration.toInstant();
    }

    /**
     * Checks if a JWT token is expired.
     *
     * @param token the JWT token
     * @return true if the token is expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            Instant expiration = extractExpiration(token);
            return expiration.isBefore(Instant.now());
        } catch (Exception e) {
            logger.error("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Extracts all claims from a JWT token.
     *
     * @param token the JWT token
     * @return the claims
     * @throws IllegalArgumentException if the token is invalid
     */
    private Claims extractAllClaims(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token must not be null or empty");
        }

        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            logger.error("Failed to extract claims from token: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid token: " + e.getMessage(), e);
        }
    }
}
