package com.booking.auth.config;

import com.booking.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * JWT Authentication Filter.
 * 
 * This filter intercepts every HTTP request and:
 * 1. Extracts the JWT token from the Authorization header
 * 2. Validates the token using JwtUtil from common-utils
 * 3. Checks if the token is blacklisted in Redis
 * 4. Sets the authentication in the SecurityContext if valid
 * 
 * The filter runs once per request and is executed before
 * the UsernamePasswordAuthenticationFilter in the security chain.
 * 
 * @author Booking Platform Team
 * @version 1.0.0
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String BLACKLIST_KEY_PREFIX = "blacklist:";

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public JwtAuthenticationFilter(JwtUtil jwtUtil, RedisTemplate<String, String> redisTemplate) {
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Filter method that processes each HTTP request.
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @param filterChain Filter chain to continue processing
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        try {
            // Extract JWT token from Authorization header
            String jwt = extractJwtFromRequest(request);
            
            // If token exists and is valid, set authentication
            if (jwt != null && validateToken(jwt)) {
                setAuthentication(jwt, request);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage());
        }
        
        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from the Authorization header.
     * 
     * Expected format: "Bearer <token>"
     * 
     * @param request HTTP request
     * @return JWT token or null if not present
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        
        return null;
    }

    /**
     * Validate the JWT token.
     * 
     * Checks:
     * 1. Token is not expired (using JwtUtil)
     * 2. Token is not blacklisted in Redis
     * 
     * @param token JWT token
     * @return true if valid, false otherwise
     */
    private boolean validateToken(String token) {
        try {
            // Validate token using JwtUtil from common-utils
            if (!jwtUtil.validateToken(token)) {
                logger.debug("Invalid JWT token");
                return false;
            }
            
            // Check if token is blacklisted in Redis
            if (isTokenBlacklisted(token)) {
                logger.debug("JWT token is blacklisted");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            logger.error("Token validation error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if the token is blacklisted in Redis.
     * 
     * Blacklisted tokens are stored with key: "blacklist:<token>"
     * 
     * @param token JWT token
     * @return true if blacklisted, false otherwise
     */
    private boolean isTokenBlacklisted(String token) {
        try {
            String blacklistKey = BLACKLIST_KEY_PREFIX + token;
            Boolean exists = redisTemplate.hasKey(blacklistKey);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            logger.error("Error checking token blacklist: {}", e.getMessage());
            // In case of Redis error, allow the request (fail open)
            // You might want to fail closed in production for better security
            return false;
        }
    }

    /**
     * Set authentication in the SecurityContext.
     * 
     * Creates an authentication token with:
     * - Principal: user ID from JWT
     * - Credentials: null (not needed after authentication)
     * - Authorities: user role from JWT
     * 
     * @param jwt JWT token
     * @param request HTTP request
     */
    private void setAuthentication(String jwt, HttpServletRequest request) {
        try {
            // Extract user information from JWT
            String userId = jwtUtil.extractUserId(jwt).toString();
            String role = jwtUtil.extractRole(jwt);
            
            // Create authentication token
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
            );
            
            // Set request details
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            
            // Set authentication in SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            logger.debug("Set authentication for user: {}", userId);
        } catch (Exception e) {
            logger.error("Error setting authentication: {}", e.getMessage());
        }
    }

    /**
     * Blacklist a token in Redis.
     * 
     * This method is called when a user logs out.
     * The token is stored with an expiration time equal to its remaining validity.
     * 
     * @param token JWT token to blacklist
     */
    public void blacklistToken(String token) {
        try {
            String blacklistKey = BLACKLIST_KEY_PREFIX + token;
            
            // Get token expiration time
            long expirationTime = jwtUtil.extractExpiration(token).getEpochSecond();
            long currentTime = System.currentTimeMillis();
            long ttl = expirationTime - currentTime;
            
            // Store in Redis with TTL (time to live)
            if (ttl > 0) {
                redisTemplate.opsForValue().set(
                        blacklistKey,
                        "blacklisted",
                        ttl,
                        TimeUnit.MILLISECONDS
                );
                logger.debug("Token blacklisted successfully");
            }
        } catch (Exception e) {
            logger.error("Error blacklisting token: {}", e.getMessage());
            throw new RuntimeException("Failed to blacklist token", e);
        }
    }
}
