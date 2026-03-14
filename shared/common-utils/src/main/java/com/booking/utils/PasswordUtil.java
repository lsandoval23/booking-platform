package com.booking.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Utility class for password hashing and verification using BCrypt.
 * This class is thread-safe and can be used as a Spring bean.
 * 
 * @author Booking Platform Team
 * @version 1.0.0
 */
@Component
public class PasswordUtil {

    private static final Logger logger = LoggerFactory.getLogger(PasswordUtil.class);
    private static final int BCRYPT_COST_FACTOR = 12;
    
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * Constructs a PasswordUtil with BCrypt cost factor of 12.
     */
    public PasswordUtil() {
        this.passwordEncoder = new BCryptPasswordEncoder(BCRYPT_COST_FACTOR);
        logger.debug("PasswordUtil initialized with BCrypt cost factor: {}", BCRYPT_COST_FACTOR);
    }

    /**
     * Hashes a plain text password using BCrypt.
     *
     * @param plainPassword the plain text password to hash
     * @return the hashed password
     * @throws IllegalArgumentException if plainPassword is null or empty
     */
    public String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            logger.error("Attempted to hash null or empty password");
            throw new IllegalArgumentException("Password must not be null or empty");
        }

        try {
            String hashedPassword = passwordEncoder.encode(plainPassword);
            logger.debug("Password hashed successfully");
            return hashedPassword;
        } catch (Exception e) {
            logger.error("Error hashing password: {}", e.getMessage());
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    /**
     * Verifies a plain text password against a hashed password.
     *
     * @param plainPassword the plain text password to verify
     * @param hashedPassword the hashed password to compare against
     * @return true if the password matches, false otherwise
     * @throws IllegalArgumentException if either parameter is null or empty
     */
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            logger.error("Attempted to verify with null or empty plain password");
            throw new IllegalArgumentException("Plain password must not be null or empty");
        }

        if (hashedPassword == null || hashedPassword.isEmpty()) {
            logger.error("Attempted to verify against null or empty hashed password");
            throw new IllegalArgumentException("Hashed password must not be null or empty");
        }

        try {
            boolean matches = passwordEncoder.matches(plainPassword, hashedPassword);
            logger.debug("Password verification result: {}", matches);
            return matches;
        } catch (Exception e) {
            logger.error("Error verifying password: {}", e.getMessage());
            return false;
        }
    }
}
