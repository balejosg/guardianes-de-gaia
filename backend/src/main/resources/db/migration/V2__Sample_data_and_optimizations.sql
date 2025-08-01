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

-- Sample development data (using MySQL/H2 compatible syntax)
-- Skip if already exists (handled by unique constraints)
INSERT IGNORE INTO guardians (username, display_name, email, age) VALUES
('demo_guardian', 'Demo Guardian', 'demo@guardianes.com', 8),
('sample_guardian', 'Sample Guardian', 'sample@guardianes.com', 12);

-- Initialize energy balances for all guardians  
INSERT IGNORE INTO energy_balances (guardian_id, current_balance) VALUES
(2, 500),
(3, 1000);

-- Sample step records for testing (last 7 days) - Universal SQL compatibility
INSERT IGNORE INTO step_records (guardian_id, step_count, recorded_at) VALUES
-- Test Guardian data (Guardian ID 1) - using basic string timestamps
(1, 3000, '2024-07-26 09:00:00'),
(1, 4500, '2024-07-27 10:30:00'),
(1, 2800, '2024-07-28 08:15:00'),
(1, 5200, '2024-07-29 11:45:00'),
(1, 3700, '2024-07-30 14:20:00'),
(1, 4100, '2024-07-31 16:10:00'),
(1, 2900, NOW()),

-- Demo Guardian data (Guardian ID 2)
(2, 2500, '2024-07-26 08:30:00'),
(2, 3800, '2024-07-27 09:45:00'),
(2, 4200, '2024-07-28 12:20:00'),
(2, 3100, '2024-07-29 15:10:00'),
(2, 4700, '2024-07-30 07:55:00'),
(2, 3300, '2024-07-31 13:25:00'),
(2, 3900, NOW()),

-- Sample Guardian data (Guardian ID 3)
(3, 4800, '2024-07-26 07:20:00'),
(3, 5500, '2024-07-27 11:15:00'),
(3, 3200, '2024-07-28 14:40:00'),
(3, 4900, '2024-07-29 09:30:00'),
(3, 5100, '2024-07-30 16:45:00'),
(3, 4600, '2024-07-31 12:05:00'),
(3, 4300, NOW());

-- Build daily aggregates for the sample data - H2/MySQL compatible using simple date logic
INSERT IGNORE INTO daily_step_aggregates (guardian_id, date, total_steps, energy_earned)
SELECT 
    guardian_id,
    CAST(recorded_at AS DATE) as date,
    SUM(step_count) as total_steps,
    CAST(SUM(step_count) / 10 AS INTEGER) as energy_earned
FROM step_records
WHERE recorded_at >= DATE '2024-07-25'  -- Simple date filter for last week of test data
GROUP BY guardian_id, CAST(recorded_at AS DATE);

-- Sample energy transactions - H2/MySQL compatible using simple timestamps
INSERT IGNORE INTO energy_transactions (guardian_id, transaction_type, amount, source, recorded_at) VALUES
-- Energy earned from steps
(1, 'EARNED', 300, 'DAILY_STEPS', TIMESTAMP '2024-07-26 09:05:00'),
(1, 'EARNED', 450, 'DAILY_STEPS', TIMESTAMP '2024-07-27 10:35:00'),
(1, 'EARNED', 280, 'DAILY_STEPS', TIMESTAMP '2024-07-28 08:20:00'),
(1, 'EARNED', 520, 'DAILY_STEPS', TIMESTAMP '2024-07-29 11:50:00'),
(1, 'SPENT', 100, 'BATTLE', TIMESTAMP '2024-07-29 15:30:00'),
(1, 'EARNED', 370, 'DAILY_STEPS', TIMESTAMP '2024-07-30 14:25:00'),
(1, 'SPENT', 50, 'CHALLENGE', TIMESTAMP '2024-07-30 18:45:00'),
(1, 'EARNED', 410, 'DAILY_STEPS', TIMESTAMP '2024-07-31 16:15:00'),
(1, 'EARNED', 290, 'DAILY_STEPS', CURRENT_TIMESTAMP),

-- Demo Guardian transactions
(2, 'EARNED', 250, 'DAILY_STEPS', TIMESTAMP '2024-07-26 08:35:00'),
(2, 'EARNED', 380, 'DAILY_STEPS', TIMESTAMP '2024-07-27 09:50:00'),
(2, 'EARNED', 420, 'DAILY_STEPS', TIMESTAMP '2024-07-28 12:25:00'),
(2, 'SPENT', 75, 'SHOP', TIMESTAMP '2024-07-28 16:10:00'),
(2, 'EARNED', 310, 'DAILY_STEPS', TIMESTAMP '2024-07-29 15:15:00'),
(2, 'EARNED', 470, 'DAILY_STEPS', TIMESTAMP '2024-07-30 08:00:00'),
(2, 'SPENT', 125, 'BATTLE', TIMESTAMP '2024-07-31 10:20:00'),
(2, 'EARNED', 330, 'DAILY_STEPS', TIMESTAMP '2024-07-31 13:30:00'),
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

-- Add database performance monitoring views - MySQL compatible
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

-- Database health monitoring view - MySQL compatible
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