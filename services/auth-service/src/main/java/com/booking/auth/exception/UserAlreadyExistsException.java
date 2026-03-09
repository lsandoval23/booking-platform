package com.booking.auth.exception;

/**
 * Exception thrown when attempting to register a user that already exists.
 * 
 * This exception is thrown when:
 * - Email is already registered
 * - Username is already taken (if applicable)
 * 
 * @author Booking Platform Team
 * @version 1.0.0
 */
public class UserAlreadyExistsException extends RuntimeException {

    /**
     * Constructs a new UserAlreadyExistsException with a default message.
     */
    public UserAlreadyExistsException() {
        super("User already exists");
    }

    /**
     * Constructs a new UserAlreadyExistsException with a custom message.
     * 
     * @param message the detail message
     */
    public UserAlreadyExistsException(String message) {
        super(message);
    }

    /**
     * Constructs a new UserAlreadyExistsException with a custom message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
