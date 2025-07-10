-- Initial database schema for Guardianes de Gaia
-- Domain-driven design implementation for walking and energy tracking

-- Guardians table (core entity)
CREATE TABLE guardians (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    age INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Indexes for guardians table
CREATE INDEX idx_guardians_username ON guardians(username);
CREATE INDEX idx_guardians_email ON guardians(email);

-- Step records table (event store pattern)
CREATE TABLE step_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    guardian_id BIGINT NOT NULL,
    step_count INTEGER NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (guardian_id) REFERENCES guardians(id) ON DELETE CASCADE,
    
    CONSTRAINT chk_step_count_positive CHECK (step_count >= 0),
    CONSTRAINT chk_step_count_reasonable CHECK (step_count <= 100000)
);

-- Indexes for step_records table
CREATE INDEX idx_step_records_guardian_date ON step_records(guardian_id, timestamp);
CREATE INDEX idx_step_records_timestamp ON step_records(timestamp);
CREATE INDEX idx_step_records_submitted_at ON step_records(submitted_at);

-- Daily step aggregates table (materialized view pattern)
CREATE TABLE daily_step_aggregates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    guardian_id BIGINT NOT NULL,
    date DATE NOT NULL,
    total_steps INTEGER NOT NULL DEFAULT 0,
    energy_earned INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (guardian_id) REFERENCES guardians(id) ON DELETE CASCADE,
    
    CONSTRAINT chk_total_steps_non_negative CHECK (total_steps >= 0),
    CONSTRAINT chk_energy_earned_non_negative CHECK (energy_earned >= 0)
);

-- Indexes for daily_step_aggregates table
CREATE UNIQUE INDEX uk_guardian_date ON daily_step_aggregates(guardian_id, date);
CREATE INDEX idx_daily_aggregates_date ON daily_step_aggregates(date);

-- Energy transactions table (event sourcing for energy)
CREATE TABLE energy_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    guardian_id BIGINT NOT NULL,
    transaction_type VARCHAR(10) NOT NULL,
    amount INTEGER NOT NULL,
    source VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (guardian_id) REFERENCES guardians(id) ON DELETE CASCADE,
    
    CONSTRAINT chk_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_transaction_type CHECK (transaction_type IN ('EARNED', 'SPENT'))
);

-- Indexes for energy_transactions table
CREATE INDEX idx_energy_transactions_guardian_id ON energy_transactions(guardian_id);
CREATE INDEX idx_energy_transactions_timestamp ON energy_transactions(timestamp);
CREATE INDEX idx_energy_transactions_type ON energy_transactions(transaction_type);
CREATE INDEX idx_energy_transactions_source ON energy_transactions(source);

-- Energy balances table (current state projection)
CREATE TABLE energy_balances (
    guardian_id BIGINT PRIMARY KEY,
    current_balance INTEGER NOT NULL DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (guardian_id) REFERENCES guardians(id) ON DELETE CASCADE,
    
    CONSTRAINT chk_balance_non_negative CHECK (current_balance >= 0)
);

-- Rate limiting table (for step submission throttling)
CREATE TABLE submission_rate_limits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    guardian_id BIGINT NOT NULL,
    submission_count INTEGER NOT NULL DEFAULT 1,
    window_start TIMESTAMP NOT NULL,
    window_end TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (guardian_id) REFERENCES guardians(id) ON DELETE CASCADE,
    
    CONSTRAINT chk_submission_count_positive CHECK (submission_count > 0)
);

-- Indexes for submission_rate_limits table
CREATE INDEX idx_rate_limits_guardian_window ON submission_rate_limits(guardian_id, window_start, window_end);
CREATE INDEX idx_rate_limits_window_end ON submission_rate_limits(window_end);

-- Anomaly detection events table (audit trail)
CREATE TABLE anomaly_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    guardian_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    step_count INTEGER,
    reason VARCHAR(255) NOT NULL,
    detected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved BOOLEAN DEFAULT FALSE,
    resolved_at TIMESTAMP NULL,
    
    FOREIGN KEY (guardian_id) REFERENCES guardians(id) ON DELETE CASCADE
);

-- Indexes for anomaly_events table
CREATE INDEX idx_anomaly_events_guardian_id ON anomaly_events(guardian_id);
CREATE INDEX idx_anomaly_events_detected_at ON anomaly_events(detected_at);
CREATE INDEX idx_anomaly_events_resolved ON anomaly_events(resolved);

-- Insert default test guardian for development
INSERT INTO guardians (username, display_name, email, age) VALUES
('test_guardian', 'Test Guardian', 'test@guardianes.com', 10);

-- Initialize energy balance for test guardian
INSERT INTO energy_balances (guardian_id, current_balance) VALUES
(1, 0);

-- Comments for future developers
-- 
-- This schema implements Domain-Driven Design principles:
-- 1. Aggregates: Guardian is the main aggregate root
-- 2. Event Sourcing: step_records and energy_transactions store all events
-- 3. CQRS: daily_step_aggregates and energy_balances are read projections
-- 4. Bounded Context: Clear separation between walking and energy domains
-- 
-- Performance considerations:
-- - Indexes on frequently queried columns (guardian_id, dates, timestamps)
-- - Partitioning strategy should be considered for large datasets
-- - Regular cleanup of old rate limiting data recommended
-- 
-- Security considerations:
-- - Foreign key constraints ensure data integrity
-- - Check constraints prevent invalid data
-- - Consider adding row-level security for multi-tenant scenarios