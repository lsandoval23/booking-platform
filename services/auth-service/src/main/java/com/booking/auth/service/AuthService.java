package com.booking.auth.service;

import com.booking.auth.domain.RefreshToken;
import com.booking.auth.domain.Role;
import com.booking.auth.domain.User;
import com.booking.auth.exception.InvalidCredentialsException;
import com.booking.auth.exception.InvalidTokenException;
import com.booking.auth.exception.UserAlreadyExistsException;
import com.booking.auth.repository.RefreshTokenRepository;
import com.booking.auth.repository.UserRepository;
import com.booking.contracts.user.*;
import com.booking.utils.JwtUtil;
import com.booking.utils.PasswordUtil;
import com.booking.utils.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Service layer for authentication operations.
 * Handles user registration, login, token management, and validation.
 * 
 * <p>This service implements comprehensive authentication business logic including:
 * <ul>
 *   <li>User registration with validation</li>
 *   <li>Login with credential verification</li>
 *   <li>JWT token generation and validation</li>
 *   <li>Refresh token rotation for enhanced security</li>
 *   <li>Token blacklisting for logout functionality</li>
 *   <li>Scheduled cleanup of expired tokens</li>
 * </ul>
 * 
 * @author Booking Platform Team
 * @version 1.0.0
 */
@Service
@Transactional
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String BLACKLIST_KEY_PREFIX = "blacklist:token:";

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final PasswordUtil passwordUtil;
    private final ValidationUtil validationUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    /**
     * Constructor for dependency injection.
     *
     * @param userRepository Repository for user data access
     * @param refreshTokenRepository Repository for refresh token data access
     * @param jwtUtil Utility for JWT operations
     * @param passwordUtil Utility for password hashing and verification
     * @param validationUtil Utility for input validation
     * @param redisTemplate Redis template for token blacklist
     * @param accessTokenExpiration Access token expiration time in milliseconds
     * @param refreshTokenExpiration Refresh token expiration time in milliseconds
     */
    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            JwtUtil jwtUtil,
            PasswordUtil passwordUtil,
            ValidationUtil validationUtil,
            RedisTemplate<String, String> redisTemplate,
            @Value("${jwt.expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-expiration}") long refreshTokenExpiration) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtUtil = jwtUtil;
        this.passwordUtil = passwordUtil;
        this.validationUtil = validationUtil;
        this.redisTemplate = redisTemplate;
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    /**
     * Registers a new user in the system.
     * 
     * <p>This method performs the following operations:
     * <ol>
     *   <li>Validates email format</li>
     *   <li>Validates password strength</li>
     *   <li>Checks for existing user with same email</li>
     *   <li>Hashes the password securely</li>
     *   <li>Creates and persists the user entity</li>
     *   <li>Returns user information without password</li>
     * </ol>
     *
     * @param request Registration request containing email, password, and role
     * @return UserDTO containing the registered user's information (without password)
     * @throws IllegalArgumentException if email or password validation fails
     * @throws UserAlreadyExistsException if a user with the email already exists
     */
    public UserDTO register(RegisterRequest request) {
        logger.info("Attempting to register new user with email: {}", request.email());

        // Validate email format
        if (!validationUtil.isValidEmail(request.email())) {
            logger.warn("Registration failed: Invalid email format for {}", request.email());
            throw new IllegalArgumentException("Invalid email format");
        }

        // Validate password strength
        if (!validationUtil.isStrongPassword(request.password())) {
            logger.warn("Registration failed: Weak password for email {}", request.email());
            throw new IllegalArgumentException(
                "Password must be at least 8 characters long and contain uppercase, lowercase, digit, and special character"
            );
        }

        // Check if user already exists
        if (userRepository.existsByEmail(request.email())) {
            logger.warn("Registration failed: User already exists with email {}", request.email());
            throw new UserAlreadyExistsException("User with email " + request.email() + " already exists");
        }

        // Hash password
        String hashedPassword = passwordUtil.hashPassword(request.password());

        // Create user entity with default role USER
        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(hashedPassword);
        user.setRole(Optional.of(Role.valueOf(request.role()))
                .orElse(Role.USER));
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        // Save user to database
        User savedUser = userRepository.save(user);
        logger.info("Successfully registered user with ID: {}", savedUser.getId());

        return convertToDTO(savedUser);
    }

    /**
     * Authenticates a user and generates access and refresh tokens.
     * 
     * <p>This method performs the following operations:
     * <ol>
     *   <li>Finds user by email</li>
     *   <li>Verifies password</li>
     *   <li>Generates JWT access token</li>
     *   <li>Generates refresh token</li>
     *   <li>Persists refresh token with expiration</li>
     *   <li>Returns tokens and user information</li>
     * </ol>
     *
     * @param request Login request containing email and password
     * @return LoginResponse containing access token, refresh token, expiration, and user info
     * @throws InvalidCredentialsException if email not found or password is incorrect
     */
    public LoginResponse login(LoginRequest request) {
        logger.info("Login attempt for email: {}", request.email());

        // Find user by email
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    logger.warn("Login failed: User not found with email {}", request.email());
                    return new InvalidCredentialsException("Invalid email or password");
                });

        // Verify password
        if (!passwordUtil.verifyPassword(request.password(), user.getPasswordHash())) {
            logger.warn("Login failed: Invalid password for email {}", request.email());
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Generate JWT access token
        String accessToken = jwtUtil.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        // Calculate access token expiration
        Instant accessTokenExpiresAt = Instant.now().plus(Duration.ofMillis(accessTokenExpiration));

        // Generate refresh token
        String refreshTokenValue = jwtUtil.generateRefreshToken();
        Instant refreshTokenExpiresAt = Instant.now().plus(Duration.ofMillis(refreshTokenExpiration));

        // Create and save refresh token entity
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenValue);
        refreshToken.setUserId(user.getId());
        refreshToken.setExpiresAt(refreshTokenExpiresAt);
        refreshToken.setCreatedAt(Instant.now());
        refreshTokenRepository.save(refreshToken);

        logger.info("Successfully logged in user with ID: {}", user.getId());

        return new LoginResponse(
                accessToken,
                refreshTokenValue,
                accessTokenExpiresAt,
                convertToDTO(user)
        );
    }

    /**
     * Refreshes an access token using a valid refresh token.
     * Implements token rotation for enhanced security.
     * 
     * <p>This method performs the following operations:
     * <ol>
     *   <li>Validates refresh token exists</li>
     *   <li>Checks if refresh token is expired</li>
     *   <li>Verifies user still exists</li>
     *   <li>Generates new access token</li>
     *   <li>Deletes old refresh token (token rotation)</li>
     *   <li>Creates and persists new refresh token</li>
     *   <li>Returns new tokens</li>
     * </ol>
     *
     * @param request Refresh token request containing the refresh token
     * @return RefreshTokenResponse containing new access token and new refresh token
     * @throws InvalidTokenException if refresh token is invalid, expired, or user not found
     */
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        logger.info("Attempting to refresh token");

        // Find refresh token
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> {
                    logger.warn("Token refresh failed: Invalid refresh token");
                    return new InvalidTokenException("Invalid refresh token");
                });

        // Check if token is expired
        if (refreshToken.isExpired()) {
            logger.warn("Token refresh failed: Refresh token expired for user ID {}", refreshToken.getUserId());
            refreshTokenRepository.delete(refreshToken);
            throw new InvalidTokenException("Refresh token expired");
        }

        // Find user
        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> {
                    logger.warn("Token refresh failed: User not found with ID {}", refreshToken.getUserId());
                    return new InvalidTokenException("User not found");
                });

        // Generate new access token
        String newAccessToken = jwtUtil.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        // Token Rotation: Delete old refresh token
        refreshTokenRepository.delete(refreshToken);

        // Create new refresh token
        String newRefreshTokenValue = jwtUtil.generateRefreshToken();
        Instant newRefreshTokenExpiresAt = Instant.now().plus(Duration.ofMillis(refreshTokenExpiration));

        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setToken(newRefreshTokenValue);
        newRefreshToken.setUserId(user.getId());
        newRefreshToken.setExpiresAt(newRefreshTokenExpiresAt);
        newRefreshToken.setCreatedAt(Instant.now());
        refreshTokenRepository.save(newRefreshToken);

        // Calculate new access token expiration
        Instant newAccessTokenExpiresAt = Instant.now().plus(Duration.ofMillis(accessTokenExpiration));

        logger.info("Successfully refreshed token for user ID: {}", user.getId());

        return new RefreshTokenResponse(newAccessToken, newRefreshTokenValue, newAccessTokenExpiresAt);
    }

    /**
     * Logs out a user by blacklisting their access token and deleting all refresh tokens.
     * This effectively logs out the user from all devices.
     * 
     * <p>This method performs the following operations:
     * <ol>
     *   <li>Extracts token from Bearer header</li>
     *   <li>Parses token to get user ID</li>
     *   <li>Calculates token TTL</li>
     *   <li>Adds token to Redis blacklist with TTL</li>
     *   <li>Deletes all refresh tokens for the user</li>
     * </ol>
     *
     * @param accessToken The access token to blacklist (with or without "Bearer " prefix)
     * @throws InvalidTokenException if token is invalid or cannot be parsed
     */
    public void logout(String accessToken) {
        logger.info("Attempting to logout user");

        // Extract token from Bearer prefix if present
        String token = extractTokenFromHeader(accessToken);

        try {
            // Parse token to get user ID
            UUID userId = jwtUtil.extractUserId(token);

            // Calculate token TTL (time until expiration)
            Instant expirationTime = jwtUtil.extractExpiration(token);
            long ttlSeconds = Duration.between(Instant.now(), expirationTime).getSeconds();

            // Only blacklist if token hasn't expired yet
            if (ttlSeconds > 0) {
                // Add token to Redis blacklist with TTL
                String blacklistKey = BLACKLIST_KEY_PREFIX + token;
                redisTemplate.opsForValue().set(blacklistKey, "blacklisted", ttlSeconds, TimeUnit.SECONDS);
                logger.debug("Added token to blacklist with TTL: {} seconds", ttlSeconds);
            }

            // Delete all refresh tokens for user (logout from all devices)
            int deletedCount = refreshTokenRepository.deleteByUserId(userId);
            logger.info("Successfully logged out user ID: {}, deleted {} refresh tokens", userId, deletedCount);

        } catch (Exception e) {
            logger.error("Logout failed: {}", e.getMessage());
            throw new InvalidTokenException("Invalid token");
        }
    }

    /**
     * Validates an access token and returns user information if valid.
     * 
     * <p>This method performs the following operations:
     * <ol>
     *   <li>Extracts token from Bearer header</li>
     *   <li>Checks if token is blacklisted in Redis</li>
     *   <li>Validates token signature and expiration</li>
     *   <li>Extracts user details from token</li>
     *   <li>Verifies user still exists in database</li>
     *   <li>Returns validation response with user details</li>
     * </ol>
     *
     * @param accessToken The access token to validate (with or without "Bearer " prefix)
     * @return TokenValidationResponse indicating if token is valid and containing user details
     */
    public TokenValidationResponse validateToken(String accessToken) {
        logger.debug("Validating token");

        // Extract token from Bearer prefix if present
        String token = extractTokenFromHeader(accessToken);

        try {
            // Check if token is blacklisted
            String blacklistKey = BLACKLIST_KEY_PREFIX + token;
            Boolean isBlacklisted = redisTemplate.hasKey(blacklistKey);
            if (Boolean.TRUE.equals(isBlacklisted)) {
                logger.debug("Token validation failed: Token is blacklisted");
                return new TokenValidationResponse(false, null, null, null);
            }

            // Validate token signature and expiration
            if (!jwtUtil.validateToken(token)) {
                logger.debug("Token validation failed: Invalid token signature or expired");
                return new TokenValidationResponse(false, null, null, null);
            }

            // Extract user details from token
            UUID userId = jwtUtil.extractUserId(token);
            String email = jwtUtil.extractEmail(token);
            String role = jwtUtil.extractRole(token);

            // Verify user still exists in database
            if (!userRepository.existsById(userId)) {
                logger.warn("Token validation failed: User not found with ID {}", userId);
                return new TokenValidationResponse(false, null, null, null);
            }

            logger.debug("Token validation successful for user ID: {}", userId);
            return new TokenValidationResponse(true, userId, email, role);

        } catch (Exception e) {
            logger.error("Token validation error: {}", e.getMessage());
            return new TokenValidationResponse(false, null, null, null);
        }
    }

    /**
     * Scheduled task to clean up expired refresh tokens from the database.
     * Runs hourly to maintain database hygiene.
     * 
     * <p>This task is scheduled using Spring's @Scheduled annotation with a cron expression
     * that runs at the start of every hour (0 0 * * * *).
     */
    @Scheduled(cron = "0 0 * * * *")
    public void cleanupExpiredTokens() {
        logger.info("Starting scheduled cleanup of expired refresh tokens");

        try {
            int deletedCount = refreshTokenRepository.deleteExpiredTokens(Instant.now());
            logger.info("Successfully deleted {} expired refresh tokens", deletedCount);
        } catch (Exception e) {
            logger.error("Error during expired token cleanup: {}", e.getMessage(), e);
        }
    }

    /**
     * Converts a User entity to a UserDTO.
     * Excludes sensitive information like password hash.
     *
     * @param user The user entity to convert
     * @return UserDTO containing user information without sensitive data
     */
    private UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    /**
     * Extracts the JWT token from a Bearer authorization header.
     * If the token doesn't have the "Bearer " prefix, returns it as-is.
     *
     * @param bearerToken The token string, potentially with "Bearer " prefix
     * @return The extracted token without the prefix
     */
    private String extractTokenFromHeader(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return bearerToken;
    }

}
