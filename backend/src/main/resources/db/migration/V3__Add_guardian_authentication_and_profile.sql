-- Migration to enhance guardians table for authentication and profile management
-- Adds password authentication, birth dates, experience system, and proper profile fields

-- Add new columns to guardians table
ALTER TABLE guardians 
ADD COLUMN password_hash VARCHAR(255) NOT NULL DEFAULT '';

ALTER TABLE guardians 
ADD COLUMN name VARCHAR(50) NOT NULL DEFAULT '';

ALTER TABLE guardians 
ADD COLUMN birth_date DATE NOT NULL DEFAULT '2010-01-01';

ALTER TABLE guardians 
ADD COLUMN level VARCHAR(20) NOT NULL DEFAULT 'INITIATE';

ALTER TABLE guardians 
ADD COLUMN experience_points INTEGER NOT NULL DEFAULT 0;

ALTER TABLE guardians 
ADD COLUMN total_steps INTEGER NOT NULL DEFAULT 0;

ALTER TABLE guardians 
ADD COLUMN total_energy_generated INTEGER NOT NULL DEFAULT 0;

ALTER TABLE guardians 
ADD COLUMN last_active_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE guardians 
ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;

-- Modify existing columns (H2 uses ALTER COLUMN syntax)
ALTER TABLE guardians ALTER COLUMN username SET DATA TYPE VARCHAR(20);
ALTER TABLE guardians ALTER COLUMN email SET DATA TYPE VARCHAR(100);
ALTER TABLE guardians DROP COLUMN display_name;
ALTER TABLE guardians DROP COLUMN age;

-- Update existing test guardian with proper data
UPDATE guardians 
SET 
    password_hash = '$2a$10$dummyHashForTestingPurposes.OnlyForDevelopment',
    name = 'Test Guardian',
    birth_date = '2012-06-15',
    level = 'INITIATE',
    experience_points = 0,
    total_steps = 0,
    total_energy_generated = 0,
    last_active_at = CURRENT_TIMESTAMP,
    active = TRUE
WHERE id = 1;

-- Add constraints for data validation (H2 compatible syntax)
ALTER TABLE guardians ADD CONSTRAINT chk_username_length CHECK (LENGTH(username) >= 3 AND LENGTH(username) <= 20);
ALTER TABLE guardians ADD CONSTRAINT chk_username_format CHECK (username REGEXP '^[a-zA-Z0-9_]+$');
ALTER TABLE guardians ADD CONSTRAINT chk_email_format CHECK (email REGEXP '^[A-Za-z0-9+_.-]+@(.+)$');
ALTER TABLE guardians ADD CONSTRAINT chk_experience_non_negative CHECK (experience_points >= 0);
ALTER TABLE guardians ADD CONSTRAINT chk_total_steps_non_negative CHECK (total_steps >= 0);
ALTER TABLE guardians ADD CONSTRAINT chk_total_energy_non_negative CHECK (total_energy_generated >= 0);
ALTER TABLE guardians ADD CONSTRAINT chk_birth_date_valid CHECK (birth_date <= CURRENT_DATE AND birth_date >= DATEADD('YEAR', -100, CURRENT_DATE));
ALTER TABLE guardians ADD CONSTRAINT chk_level_valid CHECK (level IN ('INITIATE', 'APPRENTICE', 'PROTECTOR', 'KEEPER', 'GUARDIAN', 'ELDER', 'SAGE', 'MASTER', 'LEGEND', 'CHAMPION'));

-- Add indexes for authentication and profile queries
CREATE INDEX idx_guardians_active ON guardians(active);
CREATE INDEX idx_guardians_level ON guardians(level);
CREATE INDEX idx_guardians_last_active ON guardians(last_active_at);
CREATE INDEX idx_guardians_experience ON guardians(experience_points);

-- Comments
-- 
-- This migration adds comprehensive guardian profile and authentication support:
-- 1. Password authentication with bcrypt hash storage
-- 2. Birth date for age calculation and child/adult distinction
-- 3. Experience points and leveling system (10 levels: INITIATE to CHAMPION)
-- 4. Activity tracking for user engagement metrics
-- 5. Profile statistics (total steps, energy generated)
-- 
-- Security considerations:
-- - Password hashes use bcrypt algorithm (implemented in application layer)
-- - Email and username validation through constraints
-- - Birth date validation prevents invalid ages
-- 
-- Performance considerations:
-- - Indexes on commonly queried fields (active, level, last_active_at)
-- - Username length constraint prevents excessive storage
--
-- Business logic:
-- - Level progression based on experience points (implemented in domain layer)
-- - Child vs Adult distinction based on age calculation from birth_date
-- - Active flag for soft deletion and account management