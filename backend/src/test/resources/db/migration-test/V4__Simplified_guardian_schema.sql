-- Simplified Guardian schema that works around MySQL keyword issues
-- This migration adds Guardian authentication without complex indexes

-- Drop views first (they depend on tables)
DROP VIEW IF EXISTS v_database_health;
DROP VIEW IF EXISTS v_guardian_stats;

-- Drop existing problematic tables if they exist
DROP TABLE IF EXISTS step_records;
DROP TABLE IF EXISTS energy_transactions;
DROP TABLE IF EXISTS daily_step_aggregates;
DROP TABLE IF EXISTS energy_balances;
DROP TABLE IF EXISTS submission_rate_limits;
DROP TABLE IF EXISTS anomaly_events;

-- Recreate guardians table with Guardian domain fields (H2 compatible)
DROP TABLE IF EXISTS guardians;
CREATE TABLE guardians (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    birth_date DATE NOT NULL,
    level VARCHAR(20) NOT NULL DEFAULT 'INITIATE',
    experience_points INTEGER NOT NULL DEFAULT 0,
    total_steps INTEGER NOT NULL DEFAULT 0,
    total_energy_generated INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_active_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Constraints
    CONSTRAINT chk_username_length CHECK (LENGTH(username) >= 3 AND LENGTH(username) <= 20),
    CONSTRAINT chk_experience_non_negative CHECK (experience_points >= 0),
    CONSTRAINT chk_total_steps_non_negative CHECK (total_steps >= 0),
    CONSTRAINT chk_total_energy_non_negative CHECK (total_energy_generated >= 0),
    CONSTRAINT chk_birth_date_valid CHECK (birth_date <= CURRENT_DATE AND birth_date >= DATEADD('YEAR', -100, CURRENT_DATE)),
    CONSTRAINT chk_level_valid CHECK (level IN ('INITIATE', 'APPRENTICE', 'PROTECTOR', 'KEEPER', 'GUARDIAN', 'ELDER', 'SAGE', 'MASTER', 'LEGEND', 'CHAMPION'))
);

-- Create indexes separately for H2 compatibility
CREATE INDEX idx_guardians_username ON guardians(username);
CREATE INDEX idx_guardians_email ON guardians(email);
CREATE INDEX idx_guardians_active ON guardians(active);
CREATE INDEX idx_guardians_level ON guardians(level);

-- Recreate essential tables for walking domain (simplified)
CREATE TABLE step_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    guardian_id BIGINT NOT NULL,
    step_count INTEGER NOT NULL,
    recorded_at TIMESTAMP NOT NULL,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (guardian_id) REFERENCES guardians(id) ON DELETE CASCADE,
    
    CONSTRAINT chk_step_count_positive CHECK (step_count >= 0),
    CONSTRAINT chk_step_count_reasonable CHECK (step_count <= 100000)
);

CREATE INDEX idx_step_records_guardian_id ON step_records(guardian_id);
CREATE INDEX idx_step_records_recorded_at ON step_records(recorded_at);

CREATE TABLE energy_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    guardian_id BIGINT NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    amount INTEGER NOT NULL,
    source VARCHAR(50) NOT NULL,
    recorded_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (guardian_id) REFERENCES guardians(id) ON DELETE CASCADE,
    
    CONSTRAINT chk_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_transaction_type CHECK (transaction_type IN ('EARNED', 'SPENT'))
);

CREATE INDEX idx_energy_transactions_guardian_id ON energy_transactions(guardian_id);
CREATE INDEX idx_energy_transactions_recorded_at ON energy_transactions(recorded_at);

CREATE TABLE energy_balances (
    guardian_id BIGINT PRIMARY KEY,
    current_balance INTEGER NOT NULL DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (guardian_id) REFERENCES guardians(id) ON DELETE CASCADE,
    CONSTRAINT chk_balance_non_negative CHECK (current_balance >= 0)
);

-- Insert test Guardian for development
INSERT INTO guardians (username, email, password_hash, name, birth_date, level, experience_points) VALUES
('test_guardian', 'test@guardianes.com', '$2a$10$dummyHashForTestingPurposes.OnlyForDevelopment', 'Test Guardian', '2012-06-15', 'INITIATE', 0);

-- Initialize energy balance for test guardian
INSERT INTO energy_balances (guardian_id, current_balance) VALUES
(1, 0);

-- Comments
-- This simplified schema focuses on Guardian authentication and basic functionality
-- Complex indexes and optimizations can be added later once the core system works