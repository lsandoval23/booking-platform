package com.booking.auth.exception;

/**
 * Exception thrown when user provides invalid credentials during login.
 * 
 * This exception is thrown when:
 * - Email/username does not exist
 * - Password is incorrect
 * - Account is locked or disabled
 * 
 * @author Booking Platform Team
 * @version 1.0.0
 */
public class InvalidCredentialsException extends RuntimeException {

    /**
     * Constructs a new InvalidCredentialsException with a default message.
     */
    public InvalidCredentialsException() {
        super("Invalid email or password");
    }

    /**
     * Constructs a new InvalidCredentialsException with a custom message.
     * 
     * @param message the detail message
     */
    public InvalidCredentialsException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidCredentialsException with a custom message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}
