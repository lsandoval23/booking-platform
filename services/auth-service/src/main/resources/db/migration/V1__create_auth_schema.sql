-- ============================================
-- Flyway Migration V1: Create Auth Schema
-- ============================================
-- Description: Initial database schema for authentication service
-- Creates users and refresh_tokens tables with appropriate constraints and indexes
-- Author: Booking Platform Team
-- Date: 2026-03-09
-- ============================================

-- ============================================
-- Create users table
-- ============================================
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- ============================================
-- Create indexes on users table
-- ============================================
-- Index for email lookups (login, registration checks)
CREATE INDEX idx_users_email ON users(email);

-- Index for role-based queries (admin operations, user filtering)
CREATE INDEX idx_users_role ON users(role);

-- ============================================
-- Add comments to users table
-- ============================================
COMMENT ON TABLE users IS 'User accounts for authentication and authorization';
COMMENT ON COLUMN users.id IS 'Unique user identifier (UUID)';
COMMENT ON COLUMN users.email IS 'User email address (unique, used for login)';
COMMENT ON COLUMN users.password_hash IS 'BCrypt hashed password (cost factor 12)';
COMMENT ON COLUMN users.role IS 'User role: USER, ADMIN, or PROVIDER';
COMMENT ON COLUMN users.created_at IS 'Timestamp when user account was created';
COMMENT ON COLUMN users.updated_at IS 'Timestamp when user account was last updated';

-- ============================================
-- Create refresh_tokens table
-- ============================================
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token VARCHAR(500) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================
-- Create indexes on refresh_tokens table
-- ============================================
-- Index for user_id lookups (finding all tokens for a user)
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);

-- Index for token lookups (token validation)
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);

-- Index for expiration queries (cleanup of expired tokens)
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);

-- ============================================
-- Add comments to refresh_tokens table
-- ============================================
COMMENT ON TABLE refresh_tokens IS 'Refresh tokens for JWT authentication';
COMMENT ON COLUMN refresh_tokens.id IS 'Unique token identifier (UUID)';
COMMENT ON COLUMN refresh_tokens.user_id IS 'Reference to user who owns this token';
COMMENT ON COLUMN refresh_tokens.token IS 'Refresh token string (unique, used to obtain new access tokens)';
COMMENT ON COLUMN refresh_tokens.expires_at IS 'Token expiration timestamp (tokens are invalid after this time)';
COMMENT ON COLUMN refresh_tokens.created_at IS 'Timestamp when token was created';
