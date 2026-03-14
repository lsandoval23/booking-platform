-- ============================================
-- Flyway Migration V2: Seed Test Data
-- ============================================
-- Description: Insert test users for development and testing
-- This migration creates test accounts with known credentials
-- WARNING: This should NOT be applied in production environments
-- Author: Booking Platform Team
-- Date: 2026-03-09
-- ============================================

-- ============================================
-- Insert test admin user
-- ============================================
-- Email: admin@test.com
-- Password: Admin123!
-- BCrypt hash generated with cost factor 12
-- Role: ADMIN
INSERT INTO users (id, email, password_hash, role, created_at, updated_at)
VALUES (
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'::uuid,
    'admin@test.com',
    '$2a$12$qRjT0snpKIcIPAiqadhYXubHYeKWlKQl/YXTbS6LRHoJby2OBF7z.',
    'ADMIN',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- ============================================
-- Insert test regular user
-- ============================================
-- Email: user@test.com
-- Password: User123!
-- BCrypt hash generated with cost factor 12
-- Role: USER
INSERT INTO users (id, email, password_hash, role, created_at, updated_at)
VALUES (
    'b1ffcd99-9c0b-4ef8-bb6d-6bb9bd380a22'::uuid,
    'user@test.com',
    '$2a$12$FoVmXuIwG5P7ZH2ZUoCJFuuAdO6YlBCcSGdSlom9CKQWL66JiIpeO',
    'USER',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- ============================================
-- Insert test provider user
-- ============================================
-- Email: provider@test.com
-- Password: Provider123!
-- BCrypt hash generated with cost factor 12
-- Role: PROVIDER
INSERT INTO users (id, email, password_hash, role, created_at, updated_at)
VALUES (
    '9c1c6b2a-3e7f-4a6e-b2c5-8d4f1e9a7b63'::uuid,
    'provider@test.com',
    '$2a$12$6cM1DZYBre5HzVK0Rmxqau5Xv2ZpYXj5Z/BQb3nlFdr96r4jqU25G',
    'PROVIDER',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- ============================================
-- Notes
-- ============================================
-- Test credentials summary:
-- 1. Admin User:
--    - Email: admin@test.com
--    - Password: Admin123!
--    - Role: ADMIN
--    - Use for: Testing admin-level operations
--
-- 2. Regular User:
--    - Email: user@test.com
--    - Password: User123!
--    - Role: USER
--    - Use for: Testing standard user operations
--
-- 3. Provider User:
--    - Email: provider@test.com
--    - Password: Provider123!
--    - Role: PROVIDER
--    - Use for: Testing service provider operations
--
-- IMPORTANT: These test accounts should only be used in development
-- and testing environments. Do not apply this migration in production.
