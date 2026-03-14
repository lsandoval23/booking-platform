# Common Utils Library

A shared utilities library for the Booking Platform microservices architecture. This library provides essential utilities for JWT token management, password hashing, and input validation.

## Overview

The `common-utils` library is a Maven-based Java 21 project that provides reusable utility classes for:
- JWT token generation, validation, and claim extraction
- Password hashing and verification using BCrypt
- Input validation and sanitization

## Features

### JwtUtil
- Generate access and refresh tokens
- Validate JWT tokens
- Extract claims (userId, email, role, expiration)
- Check token expiration
- Thread-safe implementation
- Configurable token expiration times

### PasswordUtil
- Hash passwords using BCrypt with cost factor 12
- Verify passwords against hashed values
- Thread-safe implementation
- Secure password handling

### ValidationUtil
- Email validation (RFC 5322 compliant)
- Password strength validation
- Input sanitization
- Pattern matching
- Length validation
- Thread-safe implementation

## Requirements

- Java 21 or higher
- Maven 3.6 or higher
- Spring Boot 3.2.3

## Installation

### Build and Install to Local Maven Repository

```bash
cd shared/common-utils
mvn clean install
```

This will build the library and install it to your local Maven repository (`~/.m2/repository`).

### Add as Dependency

Add the following dependency to your service's `pom.xml`:

```xml
<dependency>
    <groupId>com.booking</groupId>
    <artifactId>common-utils</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Configuration

Add the following properties to your service's `application.properties` or `application.yml`:

### application.properties
```properties
# JWT Configuration
jwt.secret=your-secret-key-change-in-production-minimum-32-characters-required
jwt.expiration.access=900000
jwt.expiration.refresh=604800000
```

### application.yml
```yaml
jwt:
  secret: your-secret-key-change-in-production-minimum-32-characters-required
  expiration:
    access: 900000      # 15 minutes in milliseconds
    refresh: 604800000  # 7 days in milliseconds
```

**Important:** Change the `jwt.secret` value in production to a secure, randomly generated key of at least 32 characters.

## Usage

### JwtUtil

```java
import com.booking.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.UUID;

@Service
public class AuthService {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    public String authenticateUser(UUID userId, String email, String role) {
        // Generate access token
        String accessToken = jwtUtil.generateAccessToken(userId, email, role);
        
        // Generate refresh token
        String refreshToken = jwtUtil.generateRefreshToken();
        
        return accessToken;
    }
    
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }
    
    public UUID getUserIdFromToken(String token) {
        return jwtUtil.extractUserId(token);
    }
    
    public String getEmailFromToken(String token) {
        return jwtUtil.extractEmail(token);
    }
    
    public String getRoleFromToken(String token) {
        return jwtUtil.extractRole(token);
    }
}
```

### PasswordUtil

```java
import com.booking.utils.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class UserService {
    
    @Autowired
    private PasswordUtil passwordUtil;
    
    public String registerUser(String plainPassword) {
        // Hash password before storing
        return passwordUtil.hashPassword(plainPassword);
    }
    
    public boolean authenticateUser(String plainPassword, String hashedPassword) {
        // Verify password during login
        return passwordUtil.verifyPassword(plainPassword, hashedPassword);
    }
}
```

### ValidationUtil

```java
import com.booking.utils.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class RegistrationService {
    
    @Autowired
    private ValidationUtil validationUtil;
    
    public void validateRegistration(String email, String password, String input) {
        // Validate email
        if (!validationUtil.isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }
        
        // Validate password strength
        if (!validationUtil.isStrongPassword(password)) {
            throw new IllegalArgumentException(
                "Password must be at least 8 characters with uppercase, lowercase, digit, and special character"
            );
        }
        
        // Sanitize user input
        String sanitizedInput = validationUtil.sanitizeInput(input);
        
        // Additional validations
        if (!validationUtil.isNotBlank(email)) {
            throw new IllegalArgumentException("Email is required");
        }
        
        if (!validationUtil.isLengthValid(password, 8, 128)) {
            throw new IllegalArgumentException("Password length must be between 8 and 128 characters");
        }
    }
}
```

## API Reference

### JwtUtil Methods

| Method | Parameters | Returns | Description |
|--------|-----------|---------|-------------|
| `generateAccessToken` | `UUID userId, String email, String role` | `String` | Generates a JWT access token with user claims |
| `generateRefreshToken` | - | `String` | Generates a JWT refresh token |
| `validateToken` | `String token` | `boolean` | Validates token signature and expiration |
| `extractUserId` | `String token` | `UUID` | Extracts user ID from token |
| `extractEmail` | `String token` | `String` | Extracts email from token |
| `extractRole` | `String token` | `String` | Extracts role from token |
| `extractExpiration` | `String token` | `Instant` | Extracts expiration time from token |
| `isTokenExpired` | `String token` | `boolean` | Checks if token is expired |

### PasswordUtil Methods

| Method | Parameters | Returns | Description |
|--------|-----------|---------|-------------|
| `hashPassword` | `String plainPassword` | `String` | Hashes password using BCrypt |
| `verifyPassword` | `String plainPassword, String hashedPassword` | `boolean` | Verifies password against hash |

### ValidationUtil Methods

| Method | Parameters | Returns | Description |
|--------|-----------|---------|-------------|
| `isValidEmail` | `String email` | `boolean` | Validates email format (RFC 5322) |
| `isStrongPassword` | `String password` | `boolean` | Validates password strength |
| `sanitizeInput` | `String input` | `String` | Removes harmful characters |
| `isNotBlank` | `String value` | `boolean` | Checks if string is not null/blank |
| `matchesPattern` | `String value, String pattern` | `boolean` | Validates against regex pattern |
| `isLengthValid` | `String value, int min, int max` | `boolean` | Validates string length |

## Password Requirements

Strong passwords must meet the following criteria:
- Minimum 8 characters
- At least one uppercase letter (A-Z)
- At least one lowercase letter (a-z)
- At least one digit (0-9)
- At least one special character (@$!%*?&)
- Maximum 128 characters

## Security Considerations

1. **JWT Secret**: Always use a strong, randomly generated secret key in production (minimum 32 characters)
2. **Password Hashing**: BCrypt cost factor is set to 12, providing strong security while maintaining reasonable performance
3. **Input Sanitization**: The `sanitizeInput` method provides basic protection but should be combined with other security measures
4. **Token Expiration**: Access tokens expire after 15 minutes, refresh tokens after 7 days (configurable)

## Testing

Run the test suite:

```bash
mvn test
```

Run tests with coverage:

```bash
mvn test jacoco:report
```

The library includes comprehensive unit tests with 80%+ code coverage.

## Building

### Compile
```bash
mvn compile
```

### Package
```bash
mvn package
```

### Clean and Install
```bash
mvn clean install
```

### Skip Tests
```bash
mvn clean install -DskipTests
```

## Project Structure

```
shared/common-utils/
├── pom.xml
├── README.md
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── booking/
│   │   │           └── utils/
│   │   │               ├── JwtUtil.java
│   │   │               ├── PasswordUtil.java
│   │   │               └── ValidationUtil.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/
│           └── com/
│               └── booking/
│                   └── utils/
│                       ├── JwtUtilTest.java
│                       ├── PasswordUtilTest.java
│                       └── ValidationUtilTest.java
```

## Dependencies

- Spring Boot Starter 3.2.3
- Spring Security Crypto
- JJWT (JSON Web Token) 0.12.5
- Jakarta Validation API
- JUnit 5
- Mockito

## Versioning

Current version: `1.0.0-SNAPSHOT`

## License

Copyright © 2026 Booking Platform Team

## Support

For issues, questions, or contributions, please contact the Booking Platform development team.

## Changelog

### Version 1.0.0-SNAPSHOT
- Initial release
- JWT token generation and validation
- Password hashing with BCrypt
- Input validation and sanitization
- Comprehensive unit tests
