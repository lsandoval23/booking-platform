# API Contracts Library

A shared Java library containing Data Transfer Objects (DTOs) and contracts for inter-service communication in the booking platform microservices architecture.

## Purpose

This library provides a centralized location for all DTOs used for communication between microservices. By sharing these contracts, we ensure:

- **Type Safety**: Compile-time checking of data structures across services
- **Consistency**: All services use the same data models for communication
- **Maintainability**: Changes to contracts are made in one place
- **Documentation**: Clear contracts serve as API documentation

## Technology Stack

- **Java 21**: Using modern Java records for immutable DTOs
- **Jakarta Validation API**: For declarative validation constraints
- **Jackson**: For JSON serialization/deserialization
- **JUnit 5**: For comprehensive unit testing

## DTOs Overview

### User Contracts (`com.booking.contracts.user`)

#### 1. UserDTO
**Purpose**: Shared representation of a user for inter-service communication

**Fields**:
- `id` (UUID): Unique identifier of the user
- `email` (String): User's email address
- `role` (String): User's role (e.g., CUSTOMER, PROVIDER, ADMIN)
- `createdAt` (Instant): Timestamp when the user was created
- `updatedAt` (Instant): Timestamp when the user was last updated

**Usage**:
```java
UserDTO user = new UserDTO(
    UUID.randomUUID(),
    "user@example.com",
    "CUSTOMER",
    Instant.now(),
    Instant.now()
);
```

---

#### 2. RegisterRequest
**Purpose**: Request DTO for user registration

**Fields**:
- `email` (String, @NotBlank): User's email address
- `password` (String, @NotBlank): User's password
- `role` (String, @NotBlank): User's role

**Validation**: All fields are required (not blank)

**Usage**:
```java
RegisterRequest request = new RegisterRequest(
    "newuser@example.com",
    "securePassword123",
    "CUSTOMER"
);
```

---

#### 3. LoginRequest
**Purpose**: Request DTO for user authentication

**Fields**:
- `email` (String, @NotBlank): User's email address
- `password` (String, @NotBlank): User's password

**Validation**: All fields are required (not blank)

**Usage**:
```java
LoginRequest request = new LoginRequest(
    "user@example.com",
    "password123"
);
```

---

#### 4. LoginResponse
**Purpose**: Response DTO for successful user login

**Fields**:
- `accessToken` (String): JWT access token for API authentication
- `refreshToken` (String): JWT refresh token for obtaining new access tokens
- `expiresAt` (Instant): Timestamp when the access token expires
- `user` (UserDTO): User information

**Usage**:
```java
LoginResponse response = new LoginResponse(
    "eyJhbGciOiJIUzI1NiIs...",
    "eyJhbGciOiJIUzI1NiIs...",
    Instant.now().plusSeconds(3600),
    userDTO
);
```

---

#### 5. RefreshTokenRequest
**Purpose**: Request DTO for refreshing an access token

**Fields**:
- `refreshToken` (String, @NotBlank): The refresh token

**Validation**: Refresh token is required (not blank)

**Usage**:
```java
RefreshTokenRequest request = new RefreshTokenRequest(
    "eyJhbGciOiJIUzI1NiIs..."
);
```

---

#### 6. RefreshTokenResponse
**Purpose**: Response DTO for token refresh operation

**Fields**:
- `accessToken` (String): New JWT access token
- `expiresAt` (Instant): Timestamp when the new access token expires

**Usage**:
```java
RefreshTokenResponse response = new RefreshTokenResponse(
    "eyJhbGciOiJIUzI1NiIs...",
    Instant.now().plusSeconds(3600)
);
```

---

#### 7. TokenValidationResponse
**Purpose**: Response DTO for token validation

**Fields**:
- `valid` (boolean): Whether the token is valid
- `userId` (UUID): ID of the user (null if invalid)
- `email` (String): Email of the user (null if invalid)
- `role` (String): Role of the user (null if invalid)

**Usage**:
```java
// Valid token
TokenValidationResponse response = new TokenValidationResponse(
    true,
    UUID.randomUUID(),
    "user@example.com",
    "CUSTOMER"
);

// Invalid token
TokenValidationResponse response = new TokenValidationResponse(
    false,
    null,
    null,
    null
);
```

---

## Build and Installation

### Prerequisites
- Java 21 or higher
- Maven 3.8 or higher

### Build the Library

```bash
cd shared/api-contracts
mvn clean install
```

This will:
1. Compile the source code
2. Run all unit tests
3. Package the library as a JAR
4. Install it to your local Maven repository (~/.m2/repository)

### Run Tests Only

```bash
mvn test
```

### Skip Tests (not recommended)

```bash
mvn clean install -DskipTests
```

## Using the Library in Other Services

Add the following dependency to your service's `pom.xml`:

```xml
<dependency>
    <groupId>com.booking</groupId>
    <artifactId>api-contracts</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

Then import and use the DTOs:

```java
import com.booking.contracts.user.LoginRequest;
import com.booking.contracts.user.LoginResponse;
import com.booking.contracts.user.UserDTO;

// Use in your service
@PostMapping("/login")
public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    // Your login logic here
    return ResponseEntity.ok(loginResponse);
}
```

## Validation

All request DTOs include Jakarta Validation annotations. To enable validation in your Spring Boot service:

1. Add the validation starter dependency:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

2. Use `@Valid` annotation on controller method parameters:
```java
@PostMapping("/register")
public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterRequest request) {
    // Validation happens automatically before this method is called
}
```

## JSON Serialization

All DTOs are compatible with Jackson for JSON serialization/deserialization. The library includes:
- `jackson-databind`: Core Jackson functionality
- `jackson-datatype-jsr310`: Support for Java 8 date/time types (Instant, LocalDateTime, etc.)

Example configuration in Spring Boot:
```java
@Configuration
public class JacksonConfig {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
```

## Design Principles

1. **Immutability**: All DTOs are Java records, making them immutable by default
2. **No Business Logic**: DTOs contain only data, no business logic
3. **Validation at Boundaries**: Use Jakarta Validation for input validation
4. **Clear Documentation**: All DTOs include JavaDoc comments
5. **Comprehensive Testing**: All DTOs have unit tests covering creation, validation, and serialization

## Version History

- **1.0.0-SNAPSHOT**: Initial release with user authentication contracts

## Contributing

When adding new DTOs to this library:

1. Create the DTO as a Java record in the appropriate package
2. Add Jakarta Validation annotations where appropriate
3. Include comprehensive JavaDoc comments
4. Write unit tests covering all scenarios
5. Update this README with the new DTO documentation
6. Increment the version number appropriately

## Support

For questions or issues related to this library, please contact the platform architecture team.
