package com.booking.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtUtil class.
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String TEST_SECRET = "test-secret-key-minimum-32-characters-required-for-security";
    private static final long ACCESS_TOKEN_EXPIRATION = 900000L; // 15 minutes
    private static final long REFRESH_TOKEN_EXPIRATION = 604800000L; // 7 days

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(TEST_SECRET, ACCESS_TOKEN_EXPIRATION, REFRESH_TOKEN_EXPIRATION);
    }

    @Test
    @DisplayName("Should generate valid access token with user information")
    void testGenerateAccessToken() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        String role = "USER";

        // Act
        String token = jwtUtil.generateAccessToken(userId, email, role);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }

    @Test
    @DisplayName("Should throw exception when generating access token with null userId")
    void testGenerateAccessTokenWithNullUserId() {
        // Arrange
        String email = "test@example.com";
        String role = "USER";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> jwtUtil.generateAccessToken(null, email, role));
    }

    @Test
    @DisplayName("Should throw exception when generating access token with null email")
    void testGenerateAccessTokenWithNullEmail() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String role = "USER";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> jwtUtil.generateAccessToken(userId, null, role));
    }

    @Test
    @DisplayName("Should throw exception when generating access token with null role")
    void testGenerateAccessTokenWithNullRole() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> jwtUtil.generateAccessToken(userId, email, null));
    }

    @Test
    @DisplayName("Should generate valid refresh token")
    void testGenerateRefreshToken() {
        // Act
        String token = jwtUtil.generateRefreshToken();

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    @DisplayName("Should validate valid token")
    void testValidateToken() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String token = jwtUtil.generateAccessToken(userId, "test@example.com", "USER");

        // Act
        boolean isValid = jwtUtil.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should not validate null token")
    void testValidateNullToken() {
        // Act
        boolean isValid = jwtUtil.validateToken(null);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should not validate empty token")
    void testValidateEmptyToken() {
        // Act
        boolean isValid = jwtUtil.validateToken("");

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should not validate malformed token")
    void testValidateMalformedToken() {
        // Act
        boolean isValid = jwtUtil.validateToken("invalid.token.here");

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should extract userId from token")
    void testExtractUserId() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String token = jwtUtil.generateAccessToken(userId, "test@example.com", "USER");

        // Act
        UUID extractedUserId = jwtUtil.extractUserId(token);

        // Assert
        assertEquals(userId, extractedUserId);
    }

    @Test
    @DisplayName("Should extract email from token")
    void testExtractEmail() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        String token = jwtUtil.generateAccessToken(userId, email, "USER");

        // Act
        String extractedEmail = jwtUtil.extractEmail(token);

        // Assert
        assertEquals(email, extractedEmail);
    }

    @Test
    @DisplayName("Should extract role from token")
    void testExtractRole() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String role = "ADMIN";
        String token = jwtUtil.generateAccessToken(userId, "test@example.com", role);

        // Act
        String extractedRole = jwtUtil.extractRole(token);

        // Assert
        assertEquals(role, extractedRole);
    }

    @Test
    @DisplayName("Should extract expiration from token")
    void testExtractExpiration() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String token = jwtUtil.generateAccessToken(userId, "test@example.com", "USER");

        // Act
        Instant expiration = jwtUtil.extractExpiration(token);

        // Assert
        assertNotNull(expiration);
        assertTrue(expiration.isAfter(Instant.now()));
    }

    @Test
    @DisplayName("Should correctly identify non-expired token")
    void testIsTokenNotExpired() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String token = jwtUtil.generateAccessToken(userId, "test@example.com", "USER");

        // Act
        boolean isExpired = jwtUtil.isTokenExpired(token);

        // Assert
        assertFalse(isExpired);
    }

    @Test
    @DisplayName("Should throw exception when extracting userId from invalid token")
    void testExtractUserIdFromInvalidToken() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> jwtUtil.extractUserId("invalid.token"));
    }

    @Test
    @DisplayName("Should throw exception when extracting email from invalid token")
    void testExtractEmailFromInvalidToken() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> jwtUtil.extractEmail("invalid.token"));
    }

    @Test
    @DisplayName("Should throw exception when extracting role from invalid token")
    void testExtractRoleFromInvalidToken() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> jwtUtil.extractRole("invalid.token"));
    }

    @Test
    @DisplayName("Should handle null token when extracting claims")
    void testExtractClaimsFromNullToken() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> jwtUtil.extractUserId(null));
    }

    @Test
    @DisplayName("Should generate different refresh tokens")
    void testGenerateDifferentRefreshTokens() {
        // Act
        String token1 = jwtUtil.generateRefreshToken();
        String token2 = jwtUtil.generateRefreshToken();

        // Assert
        assertNotEquals(token1, token2);
    }
}
