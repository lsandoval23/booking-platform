# Auth Service Docker Setup

This directory contains the Docker Compose configuration for running the Auth Service in an isolated environment for development and testing.

## Services

The setup includes three services:

1. **auth-service** - Spring Boot application (port 8081)
2. **postgres** - PostgreSQL database (port 5432)
3. **redis** - Redis cache for token blacklist (port 6379)

## Prerequisites

- Docker Engine 20.10+
- Docker Compose 2.0+

## Quick Start

### 1. Configure Environment Variables

Copy the example environment file and update the values:

```bash
cp .env.example .env
```

Edit `.env` and update the following:
- `JWT_SECRET` - **IMPORTANT**: Change this to a strong secret in production
- `DB_PASSWORD` - Change the default database password
- `REDIS_PASSWORD` - Set a password for Redis (optional)

To generate a strong JWT secret:
```bash
openssl rand -base64 64
```

### 2. Start the Services

```bash
docker-compose up -d
```

This will:
- Build the auth-service Docker image
- Start PostgreSQL and Redis
- Wait for health checks to pass
- Start the auth-service

### 3. Check Service Status

```bash
docker-compose ps
```

All services should show as "healthy" after a minute.

### 4. View Logs

View logs for all services:
```bash
docker-compose logs -f
```

View logs for a specific service:
```bash
docker-compose logs -f auth-service
docker-compose logs -f postgres
docker-compose logs -f redis
```

## Testing the Endpoints

### Health Check

```bash
curl http://localhost:8081/actuator/health
```

### Register a User

```bash
curl -X POST http://localhost:8081/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "SecurePassword123!",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

### Login

```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "SecurePassword123!"
  }'
```

### Verify Token

```bash
curl -X POST http://localhost:8081/auth/verify \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Refresh Token

```bash
curl -X POST http://localhost:8081/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN"
  }'
```

### Logout

```bash
curl -X POST http://localhost:8081/auth/logout \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Stopping the Services

Stop all services:
```bash
docker-compose down
```

Stop and remove volumes (WARNING: This will delete all data):
```bash
docker-compose down -v
```

## Rebuilding the Service

If you make changes to the code, rebuild the auth-service:

```bash
docker-compose up -d --build auth-service
```

## Accessing the Database

Connect to PostgreSQL:
```bash
docker exec -it auth-postgres psql -U postgres -d auth_db
```

## Accessing Redis

Connect to Redis CLI:
```bash
docker exec -it auth-redis redis-cli
```

If Redis has a password:
```bash
docker exec -it auth-redis redis-cli -a YOUR_PASSWORD
```

## Troubleshooting

### Service won't start

1. Check logs:
   ```bash
   docker-compose logs auth-service
   ```

2. Verify dependencies are healthy:
   ```bash
   docker-compose ps
   ```

3. Check if ports are already in use:
   ```bash
   # Windows
   netstat -ano | findstr :8081
   netstat -ano | findstr :5432
   netstat -ano | findstr :6379
   
   # Linux/Mac
   lsof -i :8081
   lsof -i :5432
   lsof -i :6379
   ```

### Database connection issues

1. Ensure PostgreSQL is healthy:
   ```bash
   docker-compose ps postgres
   ```

2. Check database logs:
   ```bash
   docker-compose logs postgres
   ```

3. Verify database credentials in `.env` file

### Redis connection issues

1. Ensure Redis is healthy:
   ```bash
   docker-compose ps redis
   ```

2. Test Redis connection:
   ```bash
   docker exec -it auth-redis redis-cli ping
   ```

### Build failures

1. Clean Docker build cache:
   ```bash
   docker-compose build --no-cache auth-service
   ```

2. Ensure shared modules are built:
   ```bash
   cd ../../shared/api-contracts
   mvn clean install
   cd ../common-utils
   mvn clean install
   ```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_NAME` | PostgreSQL database name | `auth_db` |
| `DB_USERNAME` | PostgreSQL username | `postgres` |
| `DB_PASSWORD` | PostgreSQL password | `postgres` |
| `REDIS_PASSWORD` | Redis password (optional) | _(empty)_ |
| `JWT_SECRET` | JWT signing secret | _(change in production)_ |
| `JWT_EXPIRATION` | Access token expiration (ms) | `3600000` (1 hour) |
| `JWT_REFRESH_EXPIRATION` | Refresh token expiration (ms) | `604800000` (7 days) |
| `LOG_LEVEL` | Application log level | `INFO` |
| `SHOW_SQL` | Show SQL queries in logs | `false` |

## Network

All services are connected to the `auth-network` bridge network, allowing them to communicate using service names as hostnames.

## Volumes

- `auth-postgres-data` - PostgreSQL data persistence
- `auth-redis-data` - Redis data persistence

To remove volumes:
```bash
docker volume rm auth-postgres-data auth-redis-data
```

## Production Considerations

This setup is for **development and testing only**. For production:

1. Use strong, randomly generated secrets
2. Enable Redis password authentication
3. Use environment-specific configuration
4. Set up proper backup strategies
5. Configure resource limits
6. Use Docker secrets for sensitive data
7. Enable TLS/SSL for all connections
8. Set up monitoring and alerting
9. Use a reverse proxy (nginx, traefik)
10. Implement proper logging aggregation

## Support

For issues or questions, refer to the main project documentation or contact the development team.
