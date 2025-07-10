-- Sample data and performance optimizations
-- This migration adds sample data for development and testing

-- Additional performance indexes
CREATE INDEX idx_step_records_guardian_timestamp ON step_records(guardian_id, timestamp DESC);
CREATE INDEX idx_energy_transactions_guardian_timestamp ON energy_transactions(guardian_id, timestamp DESC);

-- Add composite index for common query patterns
CREATE INDEX idx_step_records_date_range ON step_records(guardian_id, DATE(timestamp), timestamp);

-- Cleanup job for old rate limiting records (older than 24 hours)
-- This can be run as a scheduled job in production
CREATE EVENT IF NOT EXISTS cleanup_old_rate_limits
ON SCHEDULE EVERY 1 HOUR
DO
  DELETE FROM submission_rate_limits 
  WHERE window_end < DATE_SUB(NOW(), INTERVAL 24 HOUR);

-- Sample development data (only insert if not exists)
INSERT IGNORE INTO guardians (id, username, display_name, email, age) VALUES
(1, 'test_guardian', 'Test Guardian', 'test@guardianes.com', 10),
(2, 'demo_guardian', 'Demo Guardian', 'demo@guardianes.com', 8),
(3, 'sample_guardian', 'Sample Guardian', 'sample@guardianes.com', 12);

-- Initialize energy balances for all guardians
INSERT IGNORE INTO energy_balances (guardian_id, current_balance) VALUES
(1, 0),
(2, 500),
(3, 1000);

-- Sample step records for testing (last 7 days)
INSERT IGNORE INTO step_records (guardian_id, step_count, timestamp) VALUES
-- Test Guardian data (Guardian ID 1)
(1, 3000, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(1, 4500, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(1, 2800, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(1, 5200, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(1, 3700, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1, 4100, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, 2900, NOW()),

-- Demo Guardian data (Guardian ID 2)
(2, 2500, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(2, 3800, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2, 4200, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(2, 3100, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 4700, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 3300, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 3900, NOW()),

-- Sample Guardian data (Guardian ID 3)
(3, 4800, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(3, 5500, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(3, 3200, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(3, 4900, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(3, 5100, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, 4600, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 4300, NOW());

-- Build daily aggregates for the sample data
INSERT IGNORE INTO daily_step_aggregates (guardian_id, date, total_steps, energy_earned)
SELECT 
    guardian_id,
    DATE(timestamp) as date,
    SUM(step_count) as total_steps,
    FLOOR(SUM(step_count) / 10) as energy_earned
FROM step_records
WHERE DATE(timestamp) >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
GROUP BY guardian_id, DATE(timestamp);

-- Sample energy transactions
INSERT IGNORE INTO energy_transactions (guardian_id, transaction_type, amount, source, timestamp) VALUES
-- Energy earned from steps
(1, 'EARNED', 300, 'DAILY_STEPS', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(1, 'EARNED', 450, 'DAILY_STEPS', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(1, 'EARNED', 280, 'DAILY_STEPS', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(1, 'EARNED', 520, 'DAILY_STEPS', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(1, 'SPENT', 100, 'BATTLE', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(1, 'EARNED', 370, 'DAILY_STEPS', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1, 'SPENT', 50, 'CHALLENGE', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1, 'EARNED', 410, 'DAILY_STEPS', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, 'EARNED', 290, 'DAILY_STEPS', NOW()),

-- Demo Guardian transactions
(2, 'EARNED', 250, 'DAILY_STEPS', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(2, 'EARNED', 380, 'DAILY_STEPS', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2, 'EARNED', 420, 'DAILY_STEPS', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(2, 'SPENT', 75, 'SHOP', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(2, 'EARNED', 310, 'DAILY_STEPS', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 'EARNED', 470, 'DAILY_STEPS', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 'SPENT', 125, 'BATTLE', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 'EARNED', 330, 'DAILY_STEPS', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 'EARNED', 390, 'DAILY_STEPS', NOW());

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
CREATE OR REPLACE VIEW v_guardian_stats AS
SELECT 
    g.id,
    g.username,
    g.display_name,
    eb.current_balance,
    COALESCE(das.total_steps_today, 0) as steps_today,
    COALESCE(das.energy_earned_today, 0) as energy_earned_today,
    (SELECT COUNT(*) FROM step_records sr WHERE sr.guardian_id = g.id AND DATE(sr.timestamp) = CURDATE()) as submissions_today
FROM guardians g
LEFT JOIN energy_balances eb ON g.id = eb.guardian_id
LEFT JOIN (
    SELECT guardian_id, total_steps as total_steps_today, energy_earned as energy_earned_today
    FROM daily_step_aggregates
    WHERE date = CURDATE()
) das ON g.id = das.guardian_id;

-- Database health monitoring view
CREATE OR REPLACE VIEW v_database_health AS
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