# Auth Service

> Authentication and Authorization microservice for the Booking Platform

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## Overview

The **Auth Service** is a secure, production-ready authentication and authorization microservice built with Spring Boot 3.x and Java 21. It provides comprehensive user management, JWT-based authentication, token rotation, and Redis-backed token blacklisting for the Booking Platform ecosystem.

### Key Features

- 🔐 **Secure User Registration** - Email validation and BCrypt password hashing (cost factor 12)
- 🎫 **JWT Authentication** - Stateless authentication with access and refresh tokens
- 🔄 **Token Rotation** - Enhanced security through automatic refresh token rotation
- 🚫 **Token Blacklisting** - Redis-based blacklist for immediate token invalidation
- ✅ **Token Validation** - Comprehensive token verification for microservices
- 🧹 **Scheduled Cleanup** - Automatic removal of expired tokens from database
- 📝 **Role-Based Access** - Support for USER, ADMIN, and PROVIDER roles
- 📚 **API Documentation** - Interactive Swagger UI for testing and exploration

---

## Table of Contents

- [Architecture](#architecture)
- [Features](#features)
- [API Endpoints](#api-endpoints)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Database Schema](#database-schema)
- [Security](#security)
- [Testing](#testing)
- [Development](#development)
- [Troubleshooting](#troubleshooting)
- [API Examples](#api-examples)
- [License](#license)

---

## Architecture

### Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| **Language** | Java | 21 |
| **Framework** | Spring Boot | 3.2.3 |
| **Database** | PostgreSQL | 15+ |
| **Cache** | Redis | 7+ |
| **Migration** | Flyway | 10.10.0 |
| **Security** | Spring Security | 6.x |
| **API Docs** | SpringDoc OpenAPI | 2.3.0 |
| **Build Tool** | Maven | 3.8+ |

### Dependencies

The auth-service depends on two shared modules:

1. **[`api-contracts`](../../shared/api-contracts/README.md)** - Shared DTOs and request/response models
   - [`LoginRequest`](../../shared/api-contracts/src/main/java/com/booking/contracts/user/LoginRequest.java)
   - [`LoginResponse`](../../shared/api-contracts/src/main/java/com/booking/contracts/user/LoginResponse.java)
   - [`RegisterRequest`](../../shared/api-contracts/src/main/java/com/booking/contracts/user/RegisterRequest.java)
   - [`RefreshTokenRequest`](../../shared/api-contracts/src/main/java/com/booking/contracts/user/RefreshTokenRequest.java)
   - [`RefreshTokenResponse`](../../shared/api-contracts/src/main/java/com/booking/contracts/user/RefreshTokenResponse.java)
   - [`TokenValidationResponse`](../../shared/api-contracts/src/main/java/com/booking/contracts/user/TokenValidationResponse.java)
   - [`UserDTO`](../../shared/api-contracts/src/main/java/com/booking/contracts/user/UserDTO.java)

2. **[`common-utils`](../../shared/common-utils/README.md)** - Shared utility classes
   - [`JwtUtil`](../../shared/common-utils/src/main/java/com/booking/utils/JwtUtil.java) - JWT token generation and validation
   - [`PasswordUtil`](../../shared/common-utils/src/main/java/com/booking/utils/PasswordUtil.java) - Password hashing and verification
   - [`ValidationUtil`](../../shared/common-utils/src/main/java/com/booking/utils/ValidationUtil.java) - Email and password validation

### Isolated Model Architecture

The auth-service follows an **Isolated Model Architecture** pattern:

- **Domain Models** ([`User`](src/main/java/com/booking/auth/domain/User.java), [`RefreshToken`](src/main/java/com/booking/auth/domain/RefreshToken.java)) - Internal JPA entities, never exposed externally
- **DTOs** (from `api-contracts`) - External API contracts for communication
- **Separation of Concerns** - Domain models can evolve independently without breaking API contracts
- **Security** - Sensitive fields (password hashes) never leave the service boundary

This architecture ensures loose coupling between services and allows independent evolution of internal data models.

---

## Features

### 1. User Registration
- Email format validation
- Password strength validation (min 8 chars, uppercase, lowercase, digit, special character)
- Duplicate email detection
- BCrypt password hashing with cost factor 12
- Automatic timestamp management

### 2. User Login
- Credential validation
- JWT access token generation (1 hour expiration)
- JWT refresh token generation (7 days expiration)
- Refresh token persistence in database
- User information in response

### 3. Token Refresh
- Refresh token validation
- Token rotation for enhanced security
- Old refresh token invalidation
- New access and refresh token generation
- Automatic cleanup of expired tokens

### 4. User Logout
- Access token blacklisting in Redis
- Deletion of all user refresh tokens
- Multi-device logout support
- Automatic blacklist expiration (TTL matches token expiration)

### 5. Token Validation
- Blacklist checking
- Signature verification
- Expiration validation
- User existence verification
- User information retrieval

### 6. Scheduled Token Cleanup
- Automatic removal of expired refresh tokens
- Runs daily at midnight
- Prevents database bloat
- Configurable schedule

---

## API Endpoints

All endpoints are prefixed with `/auth` and return JSON responses.

### 1. Register User

**Endpoint:** `POST /auth/register`

**Description:** Create a new user account with email and password.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "role": "USER"
}
```

**Response:** `201 Created`
```json
{
  "id": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
  "email": "user@example.com",
  "role": "USER",
  "createdAt": "2026-03-09T01:00:00Z",
  "updatedAt": "2026-03-09T01:00:00Z"
}
```

**Status Codes:**
- `201` - User registered successfully
- `400` - Invalid input or validation error
- `409` - Email already exists
- `500` - Internal server error

**Validation Rules:**
- Email must be valid format
- Password must be at least 8 characters
- Password must contain uppercase, lowercase, digit, and special character
- Role must be one of: USER, ADMIN, PROVIDER

---

### 2. User Login

**Endpoint:** `POST /auth/login`

**Description:** Authenticate user and return JWT access token and refresh token.

**Request Body:**
```json
{
  "email": "admin@booking.com",
  "password": "Admin123!"
}
```

**Response:** `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600000,
  "user": {
    "id": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
    "email": "admin@booking.com",
    "role": "ADMIN",
    "createdAt": "2026-03-09T01:00:00Z",
    "updatedAt": "2026-03-09T01:00:00Z"
  }
}
```

**Status Codes:**
- `200` - Login successful
- `400` - Invalid input or validation error
- `401` - Invalid credentials
- `500` - Internal server error

**Token Information:**
- Access token expires in 1 hour (3600000 ms)
- Refresh token expires in 7 days (604800000 ms)
- Include access token in `Authorization: Bearer {token}` header for authenticated requests

---

### 3. Refresh Access Token

**Endpoint:** `POST /auth/refresh`

**Description:** Generate new access token using refresh token. Implements token rotation for security.

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response:** `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600000
}
```

**Status Codes:**
- `200` - Token refreshed successfully
- `400` - Invalid input or validation error
- `401` - Invalid or expired refresh token
- `500` - Internal server error

**Token Rotation:**
- Old refresh token is deleted from database
- New access token is generated
- New refresh token is generated and stored
- Prevents refresh token reuse attacks

---

### 4. User Logout

**Endpoint:** `POST /auth/logout`

**Description:** Logout user by blacklisting access token and deleting all refresh tokens.

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response:** `200 OK`
```json
{
  "message": "Logged out successfully"
}
```

**Status Codes:**
- `200` - Logout successful
- `401` - Invalid or missing token
- `500` - Internal server error

**Logout Behavior:**
- Access token is added to Redis blacklist
- All refresh tokens for the user are deleted
- User is logged out from all devices
- Blacklisted token expires automatically (TTL matches token expiration)

---

### 5. Verify Token

**Endpoint:** `GET /auth/verify`

**Description:** Validate JWT token and return user details if valid.

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response:** `200 OK`
```json
{
  "valid": true,
  "userId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
  "email": "admin@booking.com",
  "role": "ADMIN",
  "message": "Token is valid"
}
```

**Response (Invalid Token):** `200 OK`
```json
{
  "valid": false,
  "userId": null,
  "email": null,
  "role": null,
  "message": "Token is blacklisted"
}
```

**Status Codes:**
- `200` - Token validation completed (check `valid` field)
- `400` - Invalid input
- `500` - Internal server error

**Validation Checks:**
- Token is not blacklisted
- Token signature is valid
- Token is not expired
- User still exists in database

---

## Getting Started

### Prerequisites

- **Java 21** - [Download OpenJDK 21](https://openjdk.java.net/)
- **Maven 3.8+** - [Download Maven](https://maven.apache.org/download.cgi)
- **Docker & Docker Compose** - [Download Docker](https://www.docker.com/products/docker-desktop)

### Running with Docker (Recommended)

The easiest way to run the auth-service is using Docker Compose, which includes PostgreSQL and Redis:

```bash
# Navigate to the Docker directory
cd services/auth-service/auth-service-docker

# Copy the example environment file
cp .env.example .env

# Edit .env with your configuration (optional)
nano .env

# Start all services (auth-service, PostgreSQL, Redis)
docker-compose up -d

# View logs
docker-compose logs -f auth-service

# Stop all services
docker-compose down
```

The service will be available at `http://localhost:8081`

For detailed Docker setup instructions, see [`auth-service-docker/README.md`](auth-service-docker/README.md)

### Running Locally

To run the auth-service locally without Docker:

#### 1. Install Shared Modules

```bash
# Navigate to api-contracts and install
cd shared/api-contracts
mvn clean install

# Navigate to common-utils and install
cd ../common-utils
mvn clean install
```

#### 2. Start PostgreSQL and Redis

You can use Docker for just the databases:

```bash
# Start PostgreSQL
docker run -d \
  --name auth-postgres \
  -e POSTGRES_DB=auth_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:15-alpine

# Start Redis
docker run -d \
  --name auth-redis \
  -p 6379:6379 \
  redis:7-alpine
```

#### 3. Run the Auth Service

```bash
# Navigate to auth-service
cd services/auth-service

# Run with local profile
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Or build and run JAR
mvn clean package
java -jar target/auth-service-1.0.0.jar --spring.profiles.active=local
```

The service will be available at `http://localhost:8080`

---

## Configuration

### Environment Variables

The auth-service can be configured using environment variables:

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `DB_HOST` | PostgreSQL host | `localhost` | No |
| `DB_PORT` | PostgreSQL port | `5432` | No |
| `DB_NAME` | Database name | `auth_db` | No |
| `DB_USERNAME` | Database username | `postgres` | No |
| `DB_PASSWORD` | Database password | `postgres` | No |
| `REDIS_HOST` | Redis host | `localhost` | No |
| `REDIS_PORT` | Redis port | `6379` | No |
| `REDIS_PASSWORD` | Redis password | `` | No |
| `JWT_SECRET` | JWT signing secret | `your-secret-key-change-this-in-production` | **Yes** |
| `JWT_EXPIRATION` | Access token expiration (ms) | `3600000` (1 hour) | No |
| `JWT_REFRESH_EXPIRATION` | Refresh token expiration (ms) | `604800000` (7 days) | No |
| `LOG_LEVEL` | Logging level | `DEBUG` | No |
| `SHOW_SQL` | Show SQL queries | `false` | No |

### Application Profiles

The auth-service supports multiple Spring profiles:

#### Default Profile ([`application.yml`](src/main/resources/application.yml))
- Port: `8080`
- Database: `localhost:5432`
- Redis: `localhost:6379`
- Use for: Local development without Docker

#### Docker Profile ([`application-docker.yml`](src/main/resources/application-docker.yml))
- Port: `8081`
- Database: `postgres:5432` (Docker service name)
- Redis: `redis:6379` (Docker service name)
- Use for: Running in Docker Compose

#### Local Profile ([`application-local.yml`](src/main/resources/application-local.yml))
- Port: `8080`
- Database: `localhost:5432`
- Redis: `localhost:6379`
- Enhanced logging
- Use for: Local development with external databases

### JWT Token Configuration

**Access Token:**
- Default expiration: 1 hour (3600000 ms)
- Used for API authentication
- Short-lived for security
- Include in `Authorization: Bearer {token}` header

**Refresh Token:**
- Default expiration: 7 days (604800000 ms)
- Used to obtain new access tokens
- Stored in database
- Rotated on each use

**JWT Secret:**
- **CRITICAL:** Change the default secret in production
- Use a strong, random string (minimum 256 bits)
- Keep secret secure and never commit to version control
- Example: `openssl rand -base64 32`

---

## Database Schema

The auth-service uses PostgreSQL with Flyway migrations for schema management.

### Tables

#### `users` Table

Stores user account information.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PRIMARY KEY | Unique user identifier |
| `email` | VARCHAR(255) | UNIQUE, NOT NULL | User email (used for login) |
| `password_hash` | VARCHAR(255) | NOT NULL | BCrypt hashed password |
| `role` | VARCHAR(50) | NOT NULL | User role (USER, ADMIN, PROVIDER) |
| `created_at` | TIMESTAMP | NOT NULL | Account creation timestamp |
| `updated_at` | TIMESTAMP | NOT NULL | Last update timestamp |

**Indexes:**
- `idx_users_email` - Fast email lookups for login
- `idx_users_role` - Role-based queries

#### `refresh_tokens` Table

Stores refresh tokens for JWT authentication.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PRIMARY KEY | Unique token identifier |
| `user_id` | UUID | FOREIGN KEY, NOT NULL | Reference to users table |
| `token` | VARCHAR(500) | UNIQUE, NOT NULL | Refresh token string |
| `expires_at` | TIMESTAMP | NOT NULL | Token expiration time |
| `created_at` | TIMESTAMP | NOT NULL | Token creation timestamp |

**Indexes:**
- `idx_refresh_tokens_user_id` - Find all tokens for a user
- `idx_refresh_tokens_token` - Fast token lookups
- `idx_refresh_tokens_expires_at` - Cleanup expired tokens

**Foreign Keys:**
- `fk_refresh_tokens_user` - CASCADE DELETE (tokens deleted when user is deleted)

### Migrations

#### V1: Create Auth Schema ([`V1__create_auth_schema.sql`](src/main/resources/db/migration/V1__create_auth_schema.sql))
- Creates `users` table
- Creates `refresh_tokens` table
- Adds indexes for performance
- Adds table and column comments

#### V2: Seed Test Data ([`V2__seed_test_data.sql`](src/main/resources/db/migration/V2__seed_test_data.sql))
- Inserts test user accounts
- **WARNING:** Only for development/testing environments
- Should NOT be applied in production

### Test Data

The V2 migration creates three test accounts:

| Email | Password | Role | Use Case |
|-------|----------|------|----------|
| `admin@booking.com` | `Admin123!` | ADMIN | Testing admin operations |
| `user@booking.com` | `User123!` | USER | Testing standard user operations |
| `provider@booking.com` | `Provider123!` | PROVIDER | Testing service provider operations |

---

## Security

### Password Security

- **Hashing Algorithm:** BCrypt with cost factor 12
- **Salt:** Automatically generated per password
- **Validation:** Minimum 8 characters, uppercase, lowercase, digit, special character
- **Storage:** Only hashed passwords stored, never plaintext
- **Utility:** [`PasswordUtil`](../../shared/common-utils/src/main/java/com/booking/utils/PasswordUtil.java) from `common-utils`

### JWT Token Security

**Token Structure:**
```
Header:
{
  "alg": "HS256",
  "typ": "JWT"
}

Payload:
{
  "sub": "user-id",
  "email": "user@example.com",
  "role": "USER",
  "iat": 1234567890,
  "exp": 1234571490
}

Signature:
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret
)
```

**Security Features:**
- HMAC SHA-256 signature
- Configurable expiration times
- Claims include user ID, email, and role
- Signature verification on every request
- Utility: [`JwtUtil`](../../shared/common-utils/src/main/java/com/booking/utils/JwtUtil.java) from `common-utils`

### Token Blacklisting

- **Storage:** Redis for fast lookups
- **TTL:** Matches token expiration (automatic cleanup)
- **Scope:** Access tokens only (refresh tokens deleted from database)
- **Use Case:** Immediate logout, compromised token invalidation
- **Performance:** O(1) lookup time

### Token Rotation

- **Mechanism:** Refresh token is deleted and replaced on each use
- **Security Benefit:** Prevents refresh token reuse attacks
- **Implementation:** Atomic operation (delete old, create new)
- **Failure Handling:** Transaction rollback if rotation fails

### CORS Configuration

- **Allowed Origins:** Configurable (default: all origins in development)
- **Allowed Methods:** GET, POST, PUT, DELETE, OPTIONS
- **Allowed Headers:** Authorization, Content-Type
- **Exposed Headers:** Authorization
- **Credentials:** Allowed

### Spring Security Configuration

- **Public Endpoints:** `/auth/register`, `/auth/login`, `/swagger-ui/**`, `/v3/api-docs/**`
- **Protected Endpoints:** `/auth/logout`, `/auth/verify`
- **Authentication:** JWT-based (stateless)
- **Filter:** [`JwtAuthenticationFilter`](src/main/java/com/booking/auth/config/JwtAuthenticationFilter.java)
- **Configuration:** [`SecurityConfig`](src/main/java/com/booking/auth/config/SecurityConfig.java)

---

## Testing

### Swagger UI

The auth-service includes interactive API documentation via Swagger UI.

**Access Swagger UI:**
```
http://localhost:8081/swagger-ui.html
```

**Features:**
- Interactive API testing
- Request/response examples
- Schema documentation
- Try-it-out functionality
- Authentication support

### Manual Testing with Test Accounts

Use the test accounts created by the V2 migration:

#### 1. Test Admin Login
```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@booking.com",
    "password": "Admin123!"
  }'
```

#### 2. Test Regular User Login
```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@booking.com",
    "password": "User123!"
  }'
```

#### 3. Test Provider Login
```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "provider@booking.com",
    "password": "Provider123!"
  }'
```

### Testing Workflow

**Complete authentication flow:**

```bash
# 1. Register a new user
curl -X POST http://localhost:8081/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!",
    "role": "USER"
  }'

# 2. Login and save tokens
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!"
  }' | jq -r '.accessToken'

# 3. Verify token (replace YOUR_ACCESS_TOKEN)
curl -X GET http://localhost:8081/auth/verify \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# 4. Refresh token (replace YOUR_REFRESH_TOKEN)
curl -X POST http://localhost:8081/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN"
  }'

# 5. Logout
curl -X POST http://localhost:8081/auth/logout \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### Health Check

Check if the service is running:

```bash
curl http://localhost:8081/actuator/health
```

Expected response:
```json
{
  "status": "UP"
}
```

---

## Development

### Project Structure

```
auth-service/
├── src/
│   ├── main/
│   │   ├── java/com/booking/auth/
│   │   │   ├── config/              # Configuration classes
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   ├── JwtUtilConfig.java
│   │   │   │   ├── RedisConfig.java
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── UtilConfig.java
│   │   │   ├── controller/          # REST controllers
│   │   │   │   └── AuthController.java
│   │   │   ├── domain/              # JPA entities
│   │   │   │   ├── RefreshToken.java
│   │   │   │   ├── Role.java
│   │   │   │   └── User.java
│   │   │   ├── exception/           # Custom exceptions
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── InvalidCredentialsException.java
│   │   │   │   ├── InvalidTokenException.java
│   │   │   │   └── UserAlreadyExistsException.java
│   │   │   ├── repository/          # JPA repositories
│   │   │   │   ├── RefreshTokenRepository.java
│   │   │   │   └── UserRepository.java
│   │   │   ├── service/             # Business logic
│   │   │   │   └── AuthService.java
│   │   │   └── AuthServiceApplication.java
│   │   └── resources/
│   │       ├── db/migration/        # Flyway migrations
│   │       │   ├── V1__create_auth_schema.sql
│   │       │   └── V2__seed_test_data.sql
│   │       ├── application.yml
│   │       ├── application-docker.yml
│   │       └── application-local.yml
│   └── test/                        # Unit and integration tests
├── auth-service-docker/             # Docker Compose setup
│   ├── docker-compose.yml
│   ├── .env.example
│   └── README.md
├── Dockerfile
├── pom.xml
└── README.md
```

### Adding New Endpoints

1. **Define DTOs** in `api-contracts` module
2. **Add endpoint** to [`AuthController`](src/main/java/com/booking/auth/controller/AuthController.java)
3. **Implement logic** in [`AuthService`](src/main/java/com/booking/auth/service/AuthService.java)
4. **Add Swagger annotations** for documentation
5. **Update security config** if needed
6. **Write tests** for the new endpoint

### Database Migrations

Flyway automatically applies migrations on startup. To create a new migration:

1. Create a new SQL file in `src/main/resources/db/migration/`
2. Follow naming convention: `V{version}__{description}.sql`
   - Example: `V3__add_user_profile_table.sql`
3. Write SQL DDL statements
4. Restart the application (migration runs automatically)

**Migration Best Practices:**
- Never modify existing migrations
- Always increment version number
- Test migrations on a copy of production data
- Include rollback plan in comments
- Use transactions where possible

### Logging Configuration

Logging levels can be configured per package:

```yaml
logging:
  level:
    root: INFO
    com.booking.auth: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
```

**Log Locations:**
- Console output (default)
- File output (configure in `application.yml`)

**Logging Best Practices:**
- Use appropriate log levels (DEBUG, INFO, WARN, ERROR)
- Never log sensitive data (passwords, tokens)
- Include context (user ID, request ID)
- Use structured logging for production

### Building for Production

```bash
# Build JAR
mvn clean package

# Build Docker image
docker build -t booking-platform/auth-service:1.0.0 .

# Run Docker image
docker run -d \
  -p 8081:8081 \
  -e JWT_SECRET=your-production-secret \
  -e DB_HOST=your-db-host \
  -e DB_PASSWORD=your-db-password \
  -e REDIS_HOST=your-redis-host \
  booking-platform/auth-service:1.0.0
```

**Production Checklist:**
- [ ] Change JWT_SECRET to a strong random value
- [ ] Use secure database credentials
- [ ] Enable HTTPS/TLS
- [ ] Configure CORS for specific origins
- [ ] Set appropriate log levels (INFO or WARN)
- [ ] Disable Swagger UI in production
- [ ] Do NOT apply V2 migration (test data)
- [ ] Configure Redis password
- [ ] Set up monitoring and alerting
- [ ] Configure backup strategy

---

## Troubleshooting

### Common Issues

#### 1. Database Connection Failed

**Symptoms:**
```
org.postgresql.util.PSQLException: Connection refused
```

**Solutions:**
- Verify PostgreSQL is running: `docker ps | grep postgres`
- Check database credentials in environment variables
- Verify database host and port
- Check firewall rules
- Test connection: `psql -h localhost -U postgres -d auth_db`

#### 2. Redis Connection Failed

**Symptoms:**
```
io.lettuce.core.RedisConnectionException: Unable to connect to localhost:6379
```

**Solutions:**
- Verify Redis is running: `docker ps | grep redis`
- Check Redis host and port configuration
- Test connection: `redis-cli -h localhost -p 6379 ping`
- Check Redis password if configured
- Verify network connectivity

#### 3. JWT Token Invalid

**Symptoms:**
```json
{
  "valid": false,
  "message": "Invalid token signature"
}
```

**Solutions:**
- Verify JWT_SECRET matches between token generation and validation
- Check token expiration time
- Ensure token is not blacklisted (check Redis)
- Verify token format: `Bearer {token}`
- Check for token tampering

#### 4. Flyway Migration Failed

**Symptoms:**
```
FlywayException: Validate failed: Migration checksum mismatch
```

**Solutions:**
- Never modify existing migrations
- Clear Flyway history: `DELETE FROM flyway_schema_history WHERE version = 'X';`
- Repair Flyway: `mvn flyway:repair`
- Drop and recreate database (development only)
- Check migration file encoding (UTF-8)

#### 5. Port Already in Use

**Symptoms:**
```
Web server failed to start. Port 8081 was already in use.
```

**Solutions:**
- Find process using port: `netstat -ano | findstr :8081` (Windows) or `lsof -i :8081` (Linux/Mac)
- Kill process or change port in `application.yml`
- Use different profile with different port

#### 6. Shared Module Not Found

**Symptoms:**
```
Could not resolve dependencies for project com.booking:auth-service:jar:1.0.0
```

**Solutions:**
- Install shared modules first:
  ```bash
  cd shared/api-contracts && mvn clean install
  cd ../common-utils && mvn clean install
  ```
- Verify Maven local repository: `~/.m2/repository/com/booking/`
- Check module versions in `pom.xml`

#### 7. Docker Compose Issues

**Symptoms:**
- Services not starting
- Network errors
- Volume mount issues

**Solutions:**
- Check Docker daemon is running
- Verify `.env` file exists and is configured
- Check Docker logs: `docker-compose logs -f`
- Rebuild images: `docker-compose build --no-cache`
- Clean up: `docker-compose down -v` (removes volumes)
- Check disk space: `docker system df`

### Debug Mode

Enable debug logging for troubleshooting:

```bash
# Set environment variable
export LOG_LEVEL=DEBUG
export SHOW_SQL=true

# Or in application.yml
logging:
  level:
    com.booking.auth: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
```

### Getting Help

If you encounter issues not covered here:

1. Check application logs for error messages
2. Review Swagger UI for API documentation
3. Verify environment variables and configuration
4. Test with curl commands from [API Examples](#api-examples)
5. Check Docker container logs: `docker logs auth-service`
6. Review database state: `psql -h localhost -U postgres -d auth_db`
7. Check Redis state: `redis-cli -h localhost -p 6379`

---

## API Examples

Complete curl examples for all endpoints with realistic data.

### 1. Register New User

```bash
curl -X POST http://localhost:8081/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newuser@example.com",
    "password": "SecurePass123!",
    "role": "USER"
  }'
```

**Expected Response:**
```json
{
  "id": "c3d4e5f6-7890-1234-5678-90abcdef1234",
  "email": "newuser@example.com",
  "role": "USER",
  "createdAt": "2026-03-09T01:00:00Z",
  "updatedAt": "2026-03-09T01:00:00Z"
}
```

### 2. Login with Admin Account

```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@booking.com",
    "password": "Admin123!"
  }'
```

**Expected Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhMGVlYmM5OS05YzBiLTRlZjgtYmI2ZC02YmI5YmQzODBhMTEiLCJlbWFpbCI6ImFkbWluQGJvb2tpbmcuY29tIiwicm9sZSI6IkFETUlOIiwiaWF0IjoxNzA5OTQ3MjAwLCJleHAiOjE3MDk5NTA4MDB9.signature",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhMGVlYmM5OS05YzBiLTRlZjgtYmI2ZC02YmI5YmQzODBhMTEiLCJpYXQiOjE3MDk5NDcyMDAsImV4cCI6MTcxMDU1MjAwMH0.signature",
  "expiresIn": 3600000,
  "user": {
    "id": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
    "email": "admin@booking.com",
    "role": "ADMIN",
    "createdAt": "2026-03-09T01:00:00Z",
    "updatedAt": "2026-03-09T01:00:00Z"
  }
}
```

### 3. Refresh Access Token

```bash
# Save refresh token from login response
REFRESH_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -X POST http://localhost:8081/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{
    \"refreshToken\": \"$REFRESH_TOKEN\"
  }"
```

**Expected Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.new-token.signature",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.new-refresh-token.signature",
  "expiresIn": 3600000
}
```

### 4. Verify Token

```bash
# Save access token from login response
ACCESS_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -X GET http://localhost:8081/auth/verify \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

**Expected Response:**
```json
{
  "valid": true,
  "userId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
  "email": "admin@booking.com",
  "role": "ADMIN",
  "message": "Token is valid"
}
```

### 5. Logout

```bash
# Use access token from login
ACCESS_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -X POST http://localhost:8081/auth/logout \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

**Expected Response:**
```json
{
  "message": "Logged out successfully"
}
```

### Complete Workflow Example

```bash
#!/bin/bash

# 1. Register a new user
echo "=== Registering new user ==="
REGISTER_RESPONSE=$(curl -s -X POST http://localhost:8081/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "workflow@example.com",
    "password": "Workflow123!",
    "role": "USER"
  }')
echo $REGISTER_RESPONSE | jq

# 2. Login with the new user
echo -e "\n=== Logging in ==="
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "workflow@example.com",
    "password": "Workflow123!"
  }')
echo $LOGIN_RESPONSE | jq

# Extract tokens
ACCESS_TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.accessToken')
REFRESH_TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.refreshToken')

# 3. Verify the access token
echo -e "\n=== Verifying token ==="
curl -s -X GET http://localhost:8081/auth/verify \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq

# 4. Refresh the access token
echo -e "\n=== Refreshing token ==="
REFRESH_RESPONSE=$(curl -s -X POST http://localhost:8081/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\": \"$REFRESH_TOKEN\"}")
echo $REFRESH_RESPONSE | jq

# Extract new access token
NEW_ACCESS_TOKEN=$(echo $REFRESH_RESPONSE | jq -r '.accessToken')

# 5. Verify the new token
echo -e "\n=== Verifying new token ==="
curl -s -X GET http://localhost:8081/auth/verify \
  -H "Authorization: Bearer $NEW_ACCESS_TOKEN" | jq

# 6. Logout
echo -e "\n=== Logging out ==="
curl -s -X POST http://localhost:8081/auth/logout \
  -H "Authorization: Bearer $NEW_ACCESS_TOKEN" | jq

# 7. Try to use token after logout (should fail)
echo -e "\n=== Verifying token after logout (should be invalid) ==="
curl -s -X GET http://localhost:8081/auth/verify \
  -H "Authorization: Bearer $NEW_ACCESS_TOKEN" | jq
```

---

## License

This project is licensed under the MIT License - see the [LICENSE](../../LICENSE) file for details.

---

## Contact & Contribution

**Project:** Booking Platform  
**Service:** Auth Service  
**Version:** 1.0.0  
**Maintainer:** Booking Platform Team

### Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Standards

- Follow Java coding conventions
- Write comprehensive tests
- Update documentation
- Use meaningful commit messages
- Keep methods small and focused
- Add Javadoc for public APIs

### Reporting Issues

If you find a bug or have a feature request:

1. Check existing issues first
2. Create a new issue with detailed description
3. Include steps to reproduce (for bugs)
4. Provide environment details
5. Add relevant logs or screenshots

---

**Built with ❤️ by the Booking Platform Team**
