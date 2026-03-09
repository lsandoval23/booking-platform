package com.booking.auth.exception;

/**
 * Exception thrown when a JWT token is invalid or expired.
 * 
 * This exception is thrown when:
 * - Token is expired
 * - Token signature is invalid
 * - Token format is malformed
 * - Token is blacklisted
 * - Refresh token is invalid or expired
 * 
 * @author Booking Platform Team
 * @version 1.0.0
 */
public class InvalidTokenException extends RuntimeException {

    /**
     * Constructs a new InvalidTokenException with a default message.
     */
    public InvalidTokenException() {
        super("Invalid or expired token");
    }

    /**
     * Constructs a new InvalidTokenException with a custom message.
     * 
     * @param message the detail message
     */
    public InvalidTokenException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidTokenException with a custom message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
