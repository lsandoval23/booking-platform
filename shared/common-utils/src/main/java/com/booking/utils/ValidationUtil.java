package com.booking.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Utility class for input validation and sanitization.
 * This class is thread-safe and can be used as a Spring bean.
 * 
 * @author Booking Platform Team
 * @version 1.0.0
 */
@Component
public class ValidationUtil {

    private static final Logger logger = LoggerFactory.getLogger(ValidationUtil.class);

    // RFC 5322 compliant email regex (simplified but robust version)
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    // Password pattern: min 8 chars, at least one uppercase, one lowercase, one digit, one special char
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?#&]{8,}$"
    );

    // Pattern for potentially harmful characters (for basic sanitization)
    private static final Pattern HARMFUL_CHARS_PATTERN = Pattern.compile(
            "[<>\"'%;()&+]"
    );

    /**
     * Validates an email address according to RFC 5322 standards.
     *
     * @param email the email address to validate
     * @return true if the email is valid, false otherwise
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            logger.debug("Email validation failed: email is null or blank");
            return false;
        }

        // Trim whitespace
        email = email.trim();

        // Check length constraints
        if (email.length() > 254) {
            logger.debug("Email validation failed: email exceeds maximum length");
            return false;
        }

        // Validate against pattern
        boolean isValid = EMAIL_PATTERN.matcher(email).matches();
        logger.debug("Email validation result for '{}': {}", 
                email.replaceAll("(?<=.{3}).(?=.*@)", "*"), isValid);
        return isValid;
    }

    /**
     * Validates password strength.
     * Requirements:
     * - Minimum 8 characters
     * - At least one uppercase letter
     * - At least one lowercase letter
     * - At least one digit
     * - At least one special character (@$!%*?&)
     *
     * @param password the password to validate
     * @return true if the password meets strength requirements, false otherwise
     */
    public boolean isStrongPassword(String password) {
        if (password == null || password.isEmpty()) {
            logger.debug("Password validation failed: password is null or empty");
            return false;
        }

        // Check minimum length
        if (password.length() < 8) {
            logger.debug("Password validation failed: password is too short");
            return false;
        }

        // Check maximum length (prevent DoS attacks)
        if (password.length() > 128) {
            logger.debug("Password validation failed: password exceeds maximum length");
            return false;
        }

        // Validate against pattern
        boolean isStrong = PASSWORD_PATTERN.matcher(password).matches();
        logger.debug("Password strength validation result: {}", isStrong);
        return isStrong;
    }

    /**
     * Sanitizes input by removing potentially harmful characters.
     * This is a basic sanitization method that removes common injection characters.
     * For more comprehensive sanitization, consider using OWASP Java Encoder.
     *
     * @param input the input string to sanitize
     * @return the sanitized string, or empty string if input is null
     */
    public String sanitizeInput(String input) {
        if (input == null) {
            logger.debug("Sanitization skipped: input is null");
            return "";
        }

        if (input.isEmpty()) {
            return input;
        }

        // Remove potentially harmful characters
        String sanitized = HARMFUL_CHARS_PATTERN.matcher(input).replaceAll("");

        // Trim whitespace
        sanitized = sanitized.trim();

        // Limit length to prevent DoS
        if (sanitized.length() > 1000) {
            sanitized = sanitized.substring(0, 1000);
            logger.warn("Input truncated to 1000 characters during sanitization");
        }

        logger.debug("Input sanitized: original length={}, sanitized length={}", 
                input.length(), sanitized.length());
        return sanitized;
    }

    /**
     * Validates if a string is not null and not blank.
     *
     * @param value the string to validate
     * @return true if the string is not null and not blank, false otherwise
     */
    public boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    /**
     * Validates if a string matches a specific pattern.
     *
     * @param value the string to validate
     * @param pattern the regex pattern to match against
     * @return true if the string matches the pattern, false otherwise
     */
    public boolean matchesPattern(String value, String pattern) {
        if (value == null || pattern == null) {
            logger.debug("Pattern matching failed: value or pattern is null");
            return false;
        }

        try {
            boolean matches = Pattern.matches(pattern, value);
            logger.debug("Pattern matching result: {}", matches);
            return matches;
        } catch (Exception e) {
            logger.error("Error during pattern matching: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validates if a string is within a specified length range.
     *
     * @param value the string to validate
     * @param minLength the minimum length (inclusive)
     * @param maxLength the maximum length (inclusive)
     * @return true if the string length is within the range, false otherwise
     */
    public boolean isLengthValid(String value, int minLength, int maxLength) {
        if (value == null) {
            logger.debug("Length validation failed: value is null");
            return false;
        }

        if (minLength < 0 || maxLength < minLength) {
            throw new IllegalArgumentException("Invalid length constraints");
        }

        int length = value.length();
        boolean isValid = length >= minLength && length <= maxLength;
        logger.debug("Length validation result: length={}, valid={}", length, isValid);
        return isValid;
    }
}
