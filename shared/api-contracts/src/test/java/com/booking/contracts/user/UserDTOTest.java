package com.booking.contracts.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for all user contract DTOs.
 * Tests record creation, field access, JSON serialization/deserialization, and validation.
 */
class UserDTOTest {

    private static ObjectMapper objectMapper;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // UserDTO Tests
    @Test
    void testUserDTOCreation() {
        UUID id = UUID.randomUUID();
        String email = "test@example.com";
        String role = "CUSTOMER";
        Instant now = Instant.now();
        
        UserDTO user = new UserDTO(id, email, role, now, now);
        
        assertEquals(id, user.id());
        assertEquals(email, user.email());
        assertEquals(role, user.role());
        assertEquals(now, user.createdAt());
        assertEquals(now, user.updatedAt());
    }

    @Test
    void testUserDTOJsonSerialization() throws Exception {
        UUID id = UUID.randomUUID();
        Instant now = Instant.parse("2024-01-01T00:00:00Z");
        UserDTO user = new UserDTO(id, "test@example.com", "CUSTOMER", now, now);
        
        String json = objectMapper.writeValueAsString(user);
        
        assertTrue(json.contains(id.toString()));
        assertTrue(json.contains("test@example.com"));
        assertTrue(json.contains("CUSTOMER"));
    }

    @Test
    void testUserDTOJsonDeserialization() throws Exception {
        UUID id = UUID.randomUUID();
        String json = """
        {
          "id": "%s",
          "email": "test@example.com",
          "role": "CUSTOMER",
          "createdAt": "2024-01-01T00:00:00Z",
          "updatedAt": "2024-01-01T00:00:00Z"
        }
        """.formatted(id);
        
        UserDTO user = objectMapper.readValue(json, UserDTO.class);
        
        assertEquals(id, user.id());
        assertEquals("test@example.com", user.email());
        assertEquals("CUSTOMER", user.role());
    }

    // RegisterRequest Tests
    @Test
    void testRegisterRequestCreation() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "CUSTOMER");
        
        assertEquals("test@example.com", request.email());
        assertEquals("password123", request.password());
        assertEquals("CUSTOMER", request.role());
    }

    @Test
    void testRegisterRequestValidation() {
        RegisterRequest validRequest = new RegisterRequest("test@example.com", "password123", "CUSTOMER");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(validRequest);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testRegisterRequestValidationFailsWithBlankEmail() {
        RegisterRequest invalidRequest = new RegisterRequest("", "password123", "CUSTOMER");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(invalidRequest);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().equals("Email is required")));
    }

    @Test
    void testRegisterRequestValidationFailsWithBlankPassword() {
        RegisterRequest invalidRequest = new RegisterRequest("test@example.com", "", "CUSTOMER");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(invalidRequest);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().equals("Password is required")));
    }

    @Test
    void testRegisterRequestValidationFailsWithBlankRole() {
        RegisterRequest invalidRequest = new RegisterRequest("test@example.com", "password123", "");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(invalidRequest);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().equals("Role is required")));
    }

    @Test
    void testRegisterRequestJsonSerialization() throws Exception {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "CUSTOMER");
        
        String json = objectMapper.writeValueAsString(request);
        
        assertTrue(json.contains("test@example.com"));
        assertTrue(json.contains("password123"));
        assertTrue(json.contains("CUSTOMER"));
    }

    // LoginRequest Tests
    @Test
    void testLoginRequestCreation() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        
        assertEquals("test@example.com", request.email());
        assertEquals("password123", request.password());
    }

    @Test
    void testLoginRequestValidation() {
        LoginRequest validRequest = new LoginRequest("test@example.com", "password123");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(validRequest);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testLoginRequestValidationFailsWithBlankEmail() {
        LoginRequest invalidRequest = new LoginRequest("", "password123");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(invalidRequest);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().equals("Email is required")));
    }

    @Test
    void testLoginRequestValidationFailsWithBlankPassword() {
        LoginRequest invalidRequest = new LoginRequest("test@example.com", "");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(invalidRequest);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().equals("Password is required")));
    }

    // LoginResponse Tests
    @Test
    void testLoginResponseCreation() {
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();
        UserDTO user = new UserDTO(userId, "test@example.com", "CUSTOMER", now, now);
        
        LoginResponse response = new LoginResponse(
            "access-token",
            "refresh-token",
            now.plusSeconds(3600),
            user
        );
        
        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        assertEquals(now.plusSeconds(3600), response.expiresAt());
        assertEquals(user, response.user());
    }

    @Test
    void testLoginResponseJsonSerialization() throws Exception {
        UUID userId = UUID.randomUUID();
        Instant now = Instant.parse("2024-01-01T00:00:00Z");
        UserDTO user = new UserDTO(userId, "test@example.com", "CUSTOMER", now, now);
        LoginResponse response = new LoginResponse("access-token", "refresh-token", now.plusSeconds(3600), user);
        
        String json = objectMapper.writeValueAsString(response);
        
        assertTrue(json.contains("access-token"));
        assertTrue(json.contains("refresh-token"));
        assertTrue(json.contains("test@example.com"));
    }

    // RefreshTokenRequest Tests
    @Test
    void testRefreshTokenRequestCreation() {
        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token");
        
        assertEquals("refresh-token", request.refreshToken());
    }

    @Test
    void testRefreshTokenRequestValidation() {
        RefreshTokenRequest validRequest = new RefreshTokenRequest("refresh-token");
        Set<ConstraintViolation<RefreshTokenRequest>> violations = validator.validate(validRequest);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testRefreshTokenRequestValidationFailsWithBlankToken() {
        RefreshTokenRequest invalidRequest = new RefreshTokenRequest("");
        Set<ConstraintViolation<RefreshTokenRequest>> violations = validator.validate(invalidRequest);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().equals("Refresh token is required")));
    }

    // RefreshTokenResponse Tests
    @Test
    void testRefreshTokenResponseCreation() {
        Instant expiresAt = Instant.now().plusSeconds(3600);
        RefreshTokenResponse response = new RefreshTokenResponse("new-access-token", expiresAt);
        
        assertEquals("new-access-token", response.accessToken());
        assertEquals(expiresAt, response.expiresAt());
    }

    @Test
    void testRefreshTokenResponseJsonSerialization() throws Exception {
        Instant expiresAt = Instant.parse("2024-01-01T01:00:00Z");
        RefreshTokenResponse response = new RefreshTokenResponse("new-access-token", expiresAt);
        
        String json = objectMapper.writeValueAsString(response);
        
        assertTrue(json.contains("new-access-token"));
        assertTrue(json.contains("2024-01-01T01:00:00Z"));
    }

    // TokenValidationResponse Tests
    @Test
    void testTokenValidationResponseCreationValid() {
        UUID userId = UUID.randomUUID();
        TokenValidationResponse response = new TokenValidationResponse(
            true,
            userId,
            "test@example.com",
            "CUSTOMER"
        );
        
        assertTrue(response.valid());
        assertEquals(userId, response.userId());
        assertEquals("test@example.com", response.email());
        assertEquals("CUSTOMER", response.role());
    }

    @Test
    void testTokenValidationResponseCreationInvalid() {
        TokenValidationResponse response = new TokenValidationResponse(false, null, null, null);
        
        assertFalse(response.valid());
        assertNull(response.userId());
        assertNull(response.email());
        assertNull(response.role());
    }

    @Test
    void testTokenValidationResponseJsonSerialization() throws Exception {
        UUID userId = UUID.randomUUID();
        TokenValidationResponse response = new TokenValidationResponse(
            true,
            userId,
            "test@example.com",
            "CUSTOMER"
        );
        
        String json = objectMapper.writeValueAsString(response);
        
        assertTrue(json.contains("true"));
        assertTrue(json.contains(userId.toString()));
        assertTrue(json.contains("test@example.com"));
        assertTrue(json.contains("CUSTOMER"));
    }

    @Test
    void testTokenValidationResponseJsonDeserialization() throws Exception {
        UUID userId = UUID.randomUUID();
        String json = """
        {
          "valid": true,
          "userId": "%s",
          "email": "test@example.com",
          "role": "CUSTOMER"
        }
        """.formatted(userId);
        
        TokenValidationResponse response = objectMapper.readValue(json, TokenValidationResponse.class);
        
        assertTrue(response.valid());
        assertEquals(userId, response.userId());
        assertEquals("test@example.com", response.email());
        assertEquals("CUSTOMER", response.role());
    }
}
