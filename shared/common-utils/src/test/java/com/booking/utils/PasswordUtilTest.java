package com.booking.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PasswordUtil class.
 */
class PasswordUtilTest {

    private PasswordUtil passwordUtil;

    @BeforeEach
    void setUp() {
        passwordUtil = new PasswordUtil();
    }

    @Test
    @DisplayName("Should hash password successfully")
    void testHashPassword() {
        // Arrange
        String plainPassword = "MySecurePassword123!";

        // Act
        String hashedPassword = passwordUtil.hashPassword(plainPassword);

        // Assert
        assertNotNull(hashedPassword);
        assertFalse(hashedPassword.isEmpty());
        assertNotEquals(plainPassword, hashedPassword);
        assertTrue(hashedPassword.startsWith("$2a$")); // BCrypt hash prefix
    }

    @Test
    @DisplayName("Should generate different hashes for same password")
    void testHashPasswordGeneratesDifferentHashes() {
        // Arrange
        String plainPassword = "MySecurePassword123!";

        // Act
        String hash1 = passwordUtil.hashPassword(plainPassword);
        String hash2 = passwordUtil.hashPassword(plainPassword);

        // Assert
        assertNotEquals(hash1, hash2); // BCrypt uses salt, so hashes differ
    }

    @Test
    @DisplayName("Should throw exception when hashing null password")
    void testHashNullPassword() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> passwordUtil.hashPassword(null));
    }

    @Test
    @DisplayName("Should throw exception when hashing empty password")
    void testHashEmptyPassword() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> passwordUtil.hashPassword(""));
    }

    @Test
    @DisplayName("Should verify correct password")
    void testVerifyCorrectPassword() {
        // Arrange
        String plainPassword = "MySecurePassword123!";
        String hashedPassword = passwordUtil.hashPassword(plainPassword);

        // Act
        boolean isValid = passwordUtil.verifyPassword(plainPassword, hashedPassword);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should not verify incorrect password")
    void testVerifyIncorrectPassword() {
        // Arrange
        String plainPassword = "MySecurePassword123!";
        String wrongPassword = "WrongPassword456!";
        String hashedPassword = passwordUtil.hashPassword(plainPassword);

        // Act
        boolean isValid = passwordUtil.verifyPassword(wrongPassword, hashedPassword);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should throw exception when verifying with null plain password")
    void testVerifyWithNullPlainPassword() {
        // Arrange
        String hashedPassword = passwordUtil.hashPassword("password");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> passwordUtil.verifyPassword(null, hashedPassword));
    }

    @Test
    @DisplayName("Should throw exception when verifying with empty plain password")
    void testVerifyWithEmptyPlainPassword() {
        // Arrange
        String hashedPassword = passwordUtil.hashPassword("password");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> passwordUtil.verifyPassword("", hashedPassword));
    }

    @Test
    @DisplayName("Should throw exception when verifying with null hashed password")
    void testVerifyWithNullHashedPassword() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> passwordUtil.verifyPassword("password", null));
    }

    @Test
    @DisplayName("Should throw exception when verifying with empty hashed password")
    void testVerifyWithEmptyHashedPassword() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> passwordUtil.verifyPassword("password", ""));
    }

    @Test
    @DisplayName("Should return false when verifying with invalid hash format")
    void testVerifyWithInvalidHashFormat() {
        // Arrange
        String plainPassword = "password";
        String invalidHash = "not-a-valid-bcrypt-hash";

        // Act
        boolean isValid = passwordUtil.verifyPassword(plainPassword, invalidHash);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should handle special characters in password")
    void testHashPasswordWithSpecialCharacters() {
        // Arrange
        String plainPassword = "P@ssw0rd!#$%^&*()";

        // Act
        String hashedPassword = passwordUtil.hashPassword(plainPassword);
        boolean isValid = passwordUtil.verifyPassword(plainPassword, hashedPassword);

        // Assert
        assertNotNull(hashedPassword);
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should handle unicode characters in password")
    void testHashPasswordWithUnicodeCharacters() {
        // Arrange
        String plainPassword = "Pässwörd123!";

        // Act
        String hashedPassword = passwordUtil.hashPassword(plainPassword);
        boolean isValid = passwordUtil.verifyPassword(plainPassword, hashedPassword);

        // Assert
        assertNotNull(hashedPassword);
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should handle long passwords")
    void testHashLongPassword() {
        // Arrange
        String plainPassword = "a".repeat(100) + "A1!";

        // Act
        String hashedPassword = passwordUtil.hashPassword(plainPassword);
        boolean isValid = passwordUtil.verifyPassword(plainPassword, hashedPassword);

        // Assert
        assertNotNull(hashedPassword);
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should be case sensitive")
    void testPasswordCaseSensitivity() {
        // Arrange
        String plainPassword = "MyPassword123!";
        String hashedPassword = passwordUtil.hashPassword(plainPassword);

        // Act
        boolean isValidLower = passwordUtil.verifyPassword("mypassword123!", hashedPassword);
        boolean isValidUpper = passwordUtil.verifyPassword("MYPASSWORD123!", hashedPassword);

        // Assert
        assertFalse(isValidLower);
        assertFalse(isValidUpper);
    }
}
