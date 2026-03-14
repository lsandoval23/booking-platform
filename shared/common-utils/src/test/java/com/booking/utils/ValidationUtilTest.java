package com.booking.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ValidationUtil class.
 */
class ValidationUtilTest {

    private ValidationUtil validationUtil;

    @BeforeEach
    void setUp() {
        validationUtil = new ValidationUtil();
    }

    // Email Validation Tests

    @ParameterizedTest
    @ValueSource(strings = {
        "test@example.com",
        "user.name@example.com",
        "user+tag@example.co.uk",
        "user_name@example.com",
        "user123@test-domain.com",
        "a@b.co"
    })
    @DisplayName("Should validate correct email addresses")
    void testValidEmails(String email) {
        assertTrue(validationUtil.isValidEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "invalid.email",
        "@example.com",
        "user@",
        "user @example.com",
        "user@.com",
        "user@domain",
        "user@@example.com",
        "user@domain..com",
        ""
    })
    @DisplayName("Should reject invalid email addresses")
    void testInvalidEmails(String email) {
        assertFalse(validationUtil.isValidEmail(email));
    }

    @Test
    @DisplayName("Should reject null email")
    void testNullEmail() {
        assertFalse(validationUtil.isValidEmail(null));
    }

    @Test
    @DisplayName("Should reject email exceeding maximum length")
    void testEmailTooLong() {
        String longEmail = "a".repeat(250) + "@example.com";
        assertFalse(validationUtil.isValidEmail(longEmail));
    }

    @Test
    @DisplayName("Should trim whitespace from email")
    void testEmailWithWhitespace() {
        assertTrue(validationUtil.isValidEmail("  test@example.com  "));
    }

    // Password Validation Tests

    @ParameterizedTest
    @ValueSource(strings = {
        "Password1!",
        "MyP@ssw0rd",
        "Str0ng!Pass",
        "C0mpl3x@Password",
        "Test123!@#"
    })
    @DisplayName("Should validate strong passwords")
    void testStrongPasswords(String password) {
        assertTrue(validationUtil.isStrongPassword(password));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "short1!",           // Too short
        "nouppercase1!",     // No uppercase
        "NOLOWERCASE1!",     // No lowercase
        "NoDigits!",         // No digits
        "NoSpecial1",        // No special character
        "password",          // Too weak
        ""                   // Empty
    })
    @DisplayName("Should reject weak passwords")
    void testWeakPasswords(String password) {
        assertFalse(validationUtil.isStrongPassword(password));
    }

    @Test
    @DisplayName("Should reject null password")
    void testNullPassword() {
        assertFalse(validationUtil.isStrongPassword(null));
    }

    @Test
    @DisplayName("Should reject password exceeding maximum length")
    void testPasswordTooLong() {
        String longPassword = "A1!" + "a".repeat(130);
        assertFalse(validationUtil.isStrongPassword(longPassword));
    }

    @Test
    @DisplayName("Should accept password with exactly 8 characters")
    void testMinimumPasswordLength() {
        assertTrue(validationUtil.isStrongPassword("Pass123!"));
    }

    // Input Sanitization Tests

    @Test
    @DisplayName("Should sanitize input by removing harmful characters")
    void testSanitizeInput() {
        // Arrange
        String input = "Hello<script>alert('xss')</script>World";

        // Act
        String sanitized = validationUtil.sanitizeInput(input);

        // Assert
        assertFalse(sanitized.contains("<"));
        assertFalse(sanitized.contains(">"));
        assertTrue(sanitized.contains("Hello"));
        assertTrue(sanitized.contains("World"));
    }

    @Test
    @DisplayName("Should return empty string for null input")
    void testSanitizeNullInput() {
        assertEquals("", validationUtil.sanitizeInput(null));
    }

    @Test
    @DisplayName("Should return empty string for empty input")
    void testSanitizeEmptyInput() {
        assertEquals("", validationUtil.sanitizeInput(""));
    }

    @Test
    @DisplayName("Should trim whitespace during sanitization")
    void testSanitizeTrimWhitespace() {
        String input = "  Hello World  ";
        String sanitized = validationUtil.sanitizeInput(input);
        assertEquals("Hello World", sanitized);
    }

    @Test
    @DisplayName("Should truncate input exceeding maximum length")
    void testSanitizeLongInput() {
        String input = "a".repeat(1500);
        String sanitized = validationUtil.sanitizeInput(input);
        assertEquals(1000, sanitized.length());
    }

    @Test
    @DisplayName("Should remove multiple harmful characters")
    void testSanitizeMultipleHarmfulChars() {
        String input = "Test<>\"'%;()&+Data";
        String sanitized = validationUtil.sanitizeInput(input);
        assertEquals("TestData", sanitized);
    }

    // isNotBlank Tests

    @Test
    @DisplayName("Should return true for non-blank string")
    void testIsNotBlankWithValidString() {
        assertTrue(validationUtil.isNotBlank("Hello"));
    }

    @Test
    @DisplayName("Should return false for null string")
    void testIsNotBlankWithNull() {
        assertFalse(validationUtil.isNotBlank(null));
    }

    @Test
    @DisplayName("Should return false for empty string")
    void testIsNotBlankWithEmpty() {
        assertFalse(validationUtil.isNotBlank(""));
    }

    @Test
    @DisplayName("Should return false for blank string")
    void testIsNotBlankWithBlank() {
        assertFalse(validationUtil.isNotBlank("   "));
    }

    // matchesPattern Tests

    @Test
    @DisplayName("Should match valid pattern")
    void testMatchesPattern() {
        assertTrue(validationUtil.matchesPattern("123", "\\d+"));
    }

    @Test
    @DisplayName("Should not match invalid pattern")
    void testDoesNotMatchPattern() {
        assertFalse(validationUtil.matchesPattern("abc", "\\d+"));
    }

    @Test
    @DisplayName("Should return false for null value in pattern matching")
    void testMatchesPatternWithNullValue() {
        assertFalse(validationUtil.matchesPattern(null, "\\d+"));
    }

    @Test
    @DisplayName("Should return false for null pattern")
    void testMatchesPatternWithNullPattern() {
        assertFalse(validationUtil.matchesPattern("123", null));
    }

    @Test
    @DisplayName("Should handle complex regex patterns")
    void testMatchesComplexPattern() {
        String pattern = "^[A-Z]{2}\\d{4}$";
        assertTrue(validationUtil.matchesPattern("AB1234", pattern));
        assertFalse(validationUtil.matchesPattern("AB123", pattern));
    }

    // isLengthValid Tests

    @Test
    @DisplayName("Should validate length within range")
    void testIsLengthValid() {
        assertTrue(validationUtil.isLengthValid("Hello", 3, 10));
    }

    @Test
    @DisplayName("Should validate length at minimum boundary")
    void testIsLengthValidAtMinimum() {
        assertTrue(validationUtil.isLengthValid("Hi", 2, 10));
    }

    @Test
    @DisplayName("Should validate length at maximum boundary")
    void testIsLengthValidAtMaximum() {
        assertTrue(validationUtil.isLengthValid("Hello", 3, 5));
    }

    @Test
    @DisplayName("Should reject length below minimum")
    void testIsLengthInvalidBelowMinimum() {
        assertFalse(validationUtil.isLengthValid("Hi", 3, 10));
    }

    @Test
    @DisplayName("Should reject length above maximum")
    void testIsLengthInvalidAboveMaximum() {
        assertFalse(validationUtil.isLengthValid("Hello World", 3, 5));
    }

    @Test
    @DisplayName("Should return false for null value in length validation")
    void testIsLengthValidWithNull() {
        assertFalse(validationUtil.isLengthValid(null, 3, 10));
    }

    @Test
    @DisplayName("Should throw exception for invalid length constraints")
    void testIsLengthValidWithInvalidConstraints() {
        assertThrows(IllegalArgumentException.class, 
            () -> validationUtil.isLengthValid("Hello", -1, 10));
        assertThrows(IllegalArgumentException.class, 
            () -> validationUtil.isLengthValid("Hello", 10, 5));
    }

    @Test
    @DisplayName("Should validate empty string with zero minimum")
    void testIsLengthValidEmptyString() {
        assertTrue(validationUtil.isLengthValid("", 0, 10));
    }

    @Test
    @DisplayName("Should handle exact length match")
    void testIsLengthValidExactMatch() {
        assertTrue(validationUtil.isLengthValid("Hello", 5, 5));
    }
}
