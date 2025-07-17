-- Sample data and performance optimizations
-- This migration adds sample data for development and testing

-- Additional performance indexes
CREATE INDEX idx_step_records_guardian_recorded_at_desc ON step_records(guardian_id, recorded_at DESC);
CREATE INDEX idx_energy_transactions_guardian_recorded_at ON energy_transactions(guardian_id, recorded_at DESC);

-- Composite indexes for common query patterns are already defined in V1

-- Note: Cleanup job for old rate limiting records should be handled by application scheduler
-- H2 does not support CREATE EVENT, so this is commented out for tests
-- In production with MySQL, this would be:
-- CREATE EVENT IF NOT EXISTS cleanup_old_rate_limits
-- ON SCHEDULE EVERY 1 HOUR
-- DO DELETE FROM submission_rate_limits WHERE window_end < DATE_SUB(NOW(), INTERVAL 24 HOUR);

-- Sample development data (using INSERT for H2 compatibility)
-- Skip if already exists (handled by unique constraints)
INSERT INTO guardians (username, display_name, email, age) VALUES
('demo_guardian', 'Demo Guardian', 'demo@guardianes.com', 8),
('sample_guardian', 'Sample Guardian', 'sample@guardianes.com', 12);

-- Initialize energy balances for all guardians
INSERT INTO energy_balances (guardian_id, current_balance) VALUES
(2, 500),
(3, 1000);

-- Sample step records for testing (last 7 days)
INSERT INTO step_records (guardian_id, step_count, recorded_at) VALUES
-- Test Guardian data (Guardian ID 1)
(1, 3000, DATEADD('DAY', -6, CURRENT_TIMESTAMP)),
(1, 4500, DATEADD('DAY', -5, CURRENT_TIMESTAMP)),
(1, 2800, DATEADD('DAY', -4, CURRENT_TIMESTAMP)),
(1, 5200, DATEADD('DAY', -3, CURRENT_TIMESTAMP)),
(1, 3700, DATEADD('DAY', -2, CURRENT_TIMESTAMP)),
(1, 4100, DATEADD('DAY', -1, CURRENT_TIMESTAMP)),
(1, 2900, CURRENT_TIMESTAMP),

-- Demo Guardian data (Guardian ID 2)
(2, 2500, DATEADD('DAY', -6, CURRENT_TIMESTAMP)),
(2, 3800, DATEADD('DAY', -5, CURRENT_TIMESTAMP)),
(2, 4200, DATEADD('DAY', -4, CURRENT_TIMESTAMP)),
(2, 3100, DATEADD('DAY', -3, CURRENT_TIMESTAMP)),
(2, 4700, DATEADD('DAY', -2, CURRENT_TIMESTAMP)),
(2, 3300, DATEADD('DAY', -1, CURRENT_TIMESTAMP)),
(2, 3900, CURRENT_TIMESTAMP),

-- Sample Guardian data (Guardian ID 3)
(3, 4800, DATEADD('DAY', -6, CURRENT_TIMESTAMP)),
(3, 5500, DATEADD('DAY', -5, CURRENT_TIMESTAMP)),
(3, 3200, DATEADD('DAY', -4, CURRENT_TIMESTAMP)),
(3, 4900, DATEADD('DAY', -3, CURRENT_TIMESTAMP)),
(3, 5100, DATEADD('DAY', -2, CURRENT_TIMESTAMP)),
(3, 4600, DATEADD('DAY', -1, CURRENT_TIMESTAMP)),
(3, 4300, CURRENT_TIMESTAMP);

-- Build daily aggregates for the sample data
INSERT INTO daily_step_aggregates (guardian_id, date, total_steps, energy_earned)
SELECT 
    guardian_id,
    CAST(recorded_at AS DATE) as date,
    SUM(step_count) as total_steps,
    CAST(SUM(step_count) / 10 AS INT) as energy_earned
FROM step_records
WHERE CAST(recorded_at AS DATE) >= DATEADD('DAY', -7, CURRENT_DATE)
GROUP BY guardian_id, CAST(recorded_at AS DATE);

-- Sample energy transactions
INSERT INTO energy_transactions (guardian_id, transaction_type, amount, source, recorded_at) VALUES
-- Energy earned from steps
(1, 'EARNED', 300, 'DAILY_STEPS', DATEADD('DAY', -6, CURRENT_TIMESTAMP)),
(1, 'EARNED', 450, 'DAILY_STEPS', DATEADD('DAY', -5, CURRENT_TIMESTAMP)),
(1, 'EARNED', 280, 'DAILY_STEPS', DATEADD('DAY', -4, CURRENT_TIMESTAMP)),
(1, 'EARNED', 520, 'DAILY_STEPS', DATEADD('DAY', -3, CURRENT_TIMESTAMP)),
(1, 'SPENT', 100, 'BATTLE', DATEADD('DAY', -3, CURRENT_TIMESTAMP)),
(1, 'EARNED', 370, 'DAILY_STEPS', DATEADD('DAY', -2, CURRENT_TIMESTAMP)),
(1, 'SPENT', 50, 'CHALLENGE', DATEADD('DAY', -2, CURRENT_TIMESTAMP)),
(1, 'EARNED', 410, 'DAILY_STEPS', DATEADD('DAY', -1, CURRENT_TIMESTAMP)),
(1, 'EARNED', 290, 'DAILY_STEPS', CURRENT_TIMESTAMP),

-- Demo Guardian transactions
(2, 'EARNED', 250, 'DAILY_STEPS', DATEADD('DAY', -6, CURRENT_TIMESTAMP)),
(2, 'EARNED', 380, 'DAILY_STEPS', DATEADD('DAY', -5, CURRENT_TIMESTAMP)),
(2, 'EARNED', 420, 'DAILY_STEPS', DATEADD('DAY', -4, CURRENT_TIMESTAMP)),
(2, 'SPENT', 75, 'SHOP', DATEADD('DAY', -4, CURRENT_TIMESTAMP)),
(2, 'EARNED', 310, 'DAILY_STEPS', DATEADD('DAY', -3, CURRENT_TIMESTAMP)),
(2, 'EARNED', 470, 'DAILY_STEPS', DATEADD('DAY', -2, CURRENT_TIMESTAMP)),
(2, 'SPENT', 125, 'BATTLE', DATEADD('DAY', -1, CURRENT_TIMESTAMP)),
(2, 'EARNED', 330, 'DAILY_STEPS', DATEADD('DAY', -1, CURRENT_TIMESTAMP)),
(2, 'EARNED', 390, 'DAILY_STEPS', CURRENT_TIMESTAMP);

-- Update energy balances based on transactions
UPDATE energy_balances eb
SET current_balance = (
    SELECT COALESCE(SUM(
        CASE 
            WHEN et.transaction_type = 'EARNED' THEN et.amount
            WHEN et.transaction_type = 'SPENT' THEN -et.amount
            ELSE 0
        END
    ), 0)
    FROM energy_transactions et
    WHERE et.guardian_id = eb.guardian_id
);

-- Add database performance monitoring views
-- Note: H2 doesn't support CREATE OR REPLACE VIEW, so we use DROP IF EXISTS + CREATE
DROP VIEW IF EXISTS v_guardian_stats;
CREATE VIEW v_guardian_stats AS
SELECT 
    g.id,
    g.username,
    g.display_name,
    eb.current_balance,
    COALESCE(das.total_steps_today, 0) as steps_today,
    COALESCE(das.energy_earned_today, 0) as energy_earned_today,
    (SELECT COUNT(*) FROM step_records sr WHERE sr.guardian_id = g.id AND CAST(sr.recorded_at AS DATE) = CURRENT_DATE) as submissions_today
FROM guardians g
LEFT JOIN energy_balances eb ON g.id = eb.guardian_id
LEFT JOIN (
    SELECT guardian_id, total_steps as total_steps_today, energy_earned as energy_earned_today
    FROM daily_step_aggregates
    WHERE date = CURRENT_DATE
) das ON g.id = das.guardian_id;

-- Database health monitoring view
DROP VIEW IF EXISTS v_database_health;
CREATE VIEW v_database_health AS
SELECT 
    'guardians' as table_name,
    COUNT(*) as record_count,
    MAX(created_at) as last_updated
FROM guardians
UNION ALL
SELECT 
    'step_records' as table_name,
    COUNT(*) as record_count,
    MAX(submitted_at) as last_updated
FROM step_records
UNION ALL
SELECT 
    'energy_transactions' as table_name,
    COUNT(*) as record_count,
    MAX(created_at) as last_updated
FROM energy_transactions;

-- Comments
-- This migration provides:
-- 1. Performance optimizations with additional indexes
-- 2. Sample data for development and testing
-- 3. Automated cleanup for rate limiting data
-- 4. Monitoring views for application health
-- 5. Proper energy balance calculations