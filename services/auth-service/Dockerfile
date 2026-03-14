# Multi-stage Dockerfile for Auth Service
# Stage 1: Build the application with Maven
# Stage 2: Run the application with JRE

# ============================================
# Stage 1: Build Stage
# ============================================
FROM maven:3.9-eclipse-temurin-21 AS builder

# Set working directory
WORKDIR /app

# Copy shared modules first (for dependency resolution)
COPY ../../shared/api-contracts /app/shared/api-contracts
COPY ../../shared/common-utils /app/shared/common-utils

# Install shared modules to local Maven repository
WORKDIR /app/shared/api-contracts
RUN mvn clean install -DskipTests

WORKDIR /app/shared/common-utils
RUN mvn clean install -DskipTests

# Copy auth-service pom.xml and download dependencies
WORKDIR /app/auth-service
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# ============================================
# Stage 2: Runtime Stage
# ============================================
FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Create a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Copy the JAR file from builder stage
COPY --from=builder /app/auth-service/target/*.jar app.jar

# Change ownership to non-root user
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring:spring

# Expose port 8080
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Set JVM options for containerized environment
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
