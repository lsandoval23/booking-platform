package com.booking.auth.controller;

import com.booking.auth.service.AuthService;
import com.booking.contracts.user.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for authentication and authorization operations.
 * Provides endpoints for user registration, login, token management, and validation.
 * 
 * <p>This controller exposes the following endpoints:
 * <ul>
 *   <li>POST /auth/register - Register a new user account</li>
 *   <li>POST /auth/login - Authenticate user and return JWT tokens</li>
 *   <li>POST /auth/refresh - Generate new access token using refresh token</li>
 *   <li>POST /auth/logout - Logout user and invalidate tokens</li>
 *   <li>GET /auth/verify - Validate JWT token and return user details</li>
 * </ul>
 * 
 * <p>All request validation is handled automatically via {@code @Valid} annotation,
 * and exceptions are caught by the {@link com.booking.auth.exception.GlobalExceptionHandler}.
 * 
 * @author Booking Platform Team
 * @version 1.0.0
 * @see AuthService
 */
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    /**
     * Constructor for dependency injection.
     *
     * @param authService Service layer for authentication operations
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new user account.
     * 
     * <p>Creates a new user with the provided email and password. The password is
     * validated for strength and securely hashed before storage. Email uniqueness
     * is enforced.
     * 
     * <p><b>Validation Rules:</b>
     * <ul>
     *   <li>Email must be valid format</li>
     *   <li>Password must be at least 8 characters with uppercase, lowercase, digit, and special character</li>
     *   <li>Email must not already exist in the system</li>
     * </ul>
     *
     * @param request Registration request containing email, password, and role
     * @return ResponseEntity containing the created user information (without password)
     * @throws IllegalArgumentException if email or password validation fails
     * @throws com.booking.auth.exception.UserAlreadyExistsException if email already exists
     */
    @Operation(
        summary = "Register new user",
        description = "Create a new user account with email and password. Password must meet strength requirements."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or validation error"),
        @ApiResponse(responseCode = "409", description = "Email already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for email: {}", request.email());
        UserDTO user = authService.register(request);
        log.info("User registered successfully with ID: {}", user.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    /**
     * Authenticates a user and returns JWT tokens.
     * 
     * <p>Validates user credentials and generates both an access token (for API requests)
     * and a refresh token (for obtaining new access tokens). The access token should be
     * included in the Authorization header as "Bearer {token}" for authenticated requests.
     * 
     * <p><b>Response includes:</b>
     * <ul>
     *   <li>Access token - Short-lived JWT for API authentication</li>
     *   <li>Refresh token - Long-lived token for obtaining new access tokens</li>
     *   <li>Expiration time - When the access token expires</li>
     *   <li>User information - Details about the authenticated user</li>
     * </ul>
     *
     * @param request Login request containing email and password
     * @return ResponseEntity containing access token, refresh token, expiration, and user info
     * @throws com.booking.auth.exception.InvalidCredentialsException if credentials are invalid
     */
    @Operation(
        summary = "User login",
        description = "Authenticate user and return JWT access token and refresh token"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "400", description = "Invalid input or validation error"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for email: {}", request.email());
        LoginResponse response = authService.login(request);
        log.info("User logged in successfully with ID: {}", response.user().id());
        return ResponseEntity.ok(response);
    }

    /**
     * Refreshes an access token using a valid refresh token.
     * 
     * <p>Implements token rotation for enhanced security. When a refresh token is used,
     * it is invalidated and a new refresh token is issued along with a new access token.
     * This prevents refresh token reuse attacks.
     * 
     * <p><b>Token Rotation:</b>
     * <ul>
     *   <li>Old refresh token is deleted from the database</li>
     *   <li>New access token is generated</li>
     *   <li>New refresh token is generated and stored</li>
     * </ul>
     *
     * @param request Refresh token request containing the refresh token
     * @return ResponseEntity containing new access token, new refresh token, and expiration
     * @throws com.booking.auth.exception.InvalidTokenException if refresh token is invalid or expired
     */
    @Operation(
        summary = "Refresh access token",
        description = "Generate new access token using refresh token. Implements token rotation for security."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or validation error"),
        @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request received");
        RefreshTokenResponse response = authService.refreshToken(request);
        log.info("Token refreshed successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Logs out a user by invalidating their tokens.
     * 
     * <p>Performs the following operations:
     * <ul>
     *   <li>Adds the access token to a Redis blacklist (prevents further use)</li>
     *   <li>Deletes all refresh tokens for the user (logs out from all devices)</li>
     * </ul>
     * 
     * <p>The blacklisted token is stored in Redis with a TTL matching the token's
     * remaining lifetime, ensuring automatic cleanup when the token would have expired anyway.
     * 
     * <p><b>Authorization Header Format:</b> {@code Bearer {access_token}}
     *
     * @param authHeader Authorization header containing the JWT access token
     * @return ResponseEntity with success message
     * @throws com.booking.auth.exception.InvalidTokenException if token is invalid
     */
    @Operation(
        summary = "User logout",
        description = "Logout user by blacklisting access token and deleting all refresh tokens"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout successful"),
        @ApiResponse(responseCode = "401", description = "Invalid or missing token"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader("Authorization") String authHeader) {
        log.info("Logout request received");
        authService.logout(authHeader);
        log.info("User logged out successfully");
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    /**
     * Validates a JWT access token and returns user information.
     * 
     * <p>Performs comprehensive token validation:
     * <ul>
     *   <li>Checks if token is blacklisted (logged out)</li>
     *   <li>Validates token signature</li>
     *   <li>Checks token expiration</li>
     *   <li>Verifies user still exists in database</li>
     * </ul>
     * 
     * <p>This endpoint is useful for other microservices to validate tokens
     * and retrieve user information without direct database access.
     * 
     * <p><b>Authorization Header Format:</b> {@code Bearer {access_token}}
     *
     * @param authHeader Authorization header containing the JWT access token
     * @return ResponseEntity containing validation result and user details if valid
     */
    @Operation(
        summary = "Verify token",
        description = "Validate JWT token and return user details if valid"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token validation completed (check 'valid' field in response)"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/verify")
    public ResponseEntity<TokenValidationResponse> verifyToken(
            @RequestHeader("Authorization") String authHeader) {
        log.debug("Token verification request received");
        TokenValidationResponse response = authService.validateToken(authHeader);
        if (response.valid()) {
            log.debug("Token verified successfully for user ID: {}", response.userId());
        } else {
            log.debug("Token verification failed");
        }
        return ResponseEntity.ok(response);
    }
}
