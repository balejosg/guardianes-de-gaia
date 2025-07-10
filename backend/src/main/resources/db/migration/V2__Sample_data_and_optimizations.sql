-- Sample data and performance optimizations
-- This migration adds sample data for development and testing

-- Additional performance indexes
CREATE INDEX idx_step_records_guardian_timestamp ON step_records(guardian_id, timestamp DESC);
CREATE INDEX idx_energy_transactions_guardian_timestamp ON energy_transactions(guardian_id, timestamp DESC);

-- Add composite index for common query patterns (H2 compatible)
-- Note: Removed DATE(timestamp) index as it's not supported in H2

-- Sample development data (only insert if not exists)
-- H2 compatible: conditional INSERT statements
INSERT INTO guardians (username, display_name, email, age)
SELECT 'test_guardian', 'Test Guardian', 'test@guardianes.com', 10
WHERE NOT EXISTS (SELECT 1 FROM guardians WHERE username = 'test_guardian');

INSERT INTO guardians (username, display_name, email, age)
SELECT 'demo_guardian', 'Demo Guardian', 'demo@guardianes.com', 8
WHERE NOT EXISTS (SELECT 1 FROM guardians WHERE username = 'demo_guardian');

INSERT INTO guardians (username, display_name, email, age)
SELECT 'sample_guardian', 'Sample Guardian', 'sample@guardianes.com', 12
WHERE NOT EXISTS (SELECT 1 FROM guardians WHERE username = 'sample_guardian');

-- Initialize energy balances for all guardians
-- Use guardian IDs from the inserted test data
INSERT INTO energy_balances (guardian_id, current_balance)
SELECT g.id, 0
FROM guardians g
WHERE g.username = 'test_guardian'
AND NOT EXISTS (SELECT 1 FROM energy_balances eb WHERE eb.guardian_id = g.id);

INSERT INTO energy_balances (guardian_id, current_balance)
SELECT g.id, 500
FROM guardians g
WHERE g.username = 'demo_guardian'
AND NOT EXISTS (SELECT 1 FROM energy_balances eb WHERE eb.guardian_id = g.id);

INSERT INTO energy_balances (guardian_id, current_balance)
SELECT g.id, 1000
FROM guardians g
WHERE g.username = 'sample_guardian'
AND NOT EXISTS (SELECT 1 FROM energy_balances eb WHERE eb.guardian_id = g.id);

-- Sample step records for testing (last 7 days)
-- H2 compatible: using conditional INSERT with guardian lookups
-- Test Guardian data
INSERT INTO step_records (guardian_id, step_count, timestamp)
SELECT g.id, 3000, DATEADD('DAY', -6, NOW())
FROM guardians g
WHERE g.username = 'test_guardian';

INSERT INTO step_records (guardian_id, step_count, timestamp)
SELECT g.id, 4500, DATEADD('DAY', -5, NOW())
FROM guardians g
WHERE g.username = 'test_guardian';

INSERT INTO step_records (guardian_id, step_count, timestamp)
SELECT g.id, 2800, DATEADD('DAY', -4, NOW())
FROM guardians g
WHERE g.username = 'test_guardian';

INSERT INTO step_records (guardian_id, step_count, timestamp)
SELECT g.id, 5200, DATEADD('DAY', -3, NOW())
FROM guardians g
WHERE g.username = 'test_guardian';

INSERT INTO step_records (guardian_id, step_count, timestamp)
SELECT g.id, 3700, DATEADD('DAY', -2, NOW())
FROM guardians g
WHERE g.username = 'test_guardian';

INSERT INTO step_records (guardian_id, step_count, timestamp)
SELECT g.id, 4100, DATEADD('DAY', -1, NOW())
FROM guardians g
WHERE g.username = 'test_guardian';

INSERT INTO step_records (guardian_id, step_count, timestamp)
SELECT g.id, 2900, NOW()
FROM guardians g
WHERE g.username = 'test_guardian';

-- Demo Guardian data
INSERT INTO step_records (guardian_id, step_count, timestamp)
SELECT g.id, 2500, DATEADD('DAY', -6, NOW())
FROM guardians g
WHERE g.username = 'demo_guardian';

INSERT INTO step_records (guardian_id, step_count, timestamp)
SELECT g.id, 3800, DATEADD('DAY', -5, NOW())
FROM guardians g
WHERE g.username = 'demo_guardian';

INSERT INTO step_records (guardian_id, step_count, timestamp)
SELECT g.id, 4200, DATEADD('DAY', -4, NOW())
FROM guardians g
WHERE g.username = 'demo_guardian';

INSERT INTO step_records (guardian_id, step_count, timestamp)
SELECT g.id, 3100, DATEADD('DAY', -3, NOW())
FROM guardians g
WHERE g.username = 'demo_guardian';

INSERT INTO step_records (guardian_id, step_count, timestamp)
SELECT g.id, 4700, DATEADD('DAY', -2, NOW())
FROM guardians g
WHERE g.username = 'demo_guardian';

INSERT INTO step_records (guardian_id, step_count, timestamp)
SELECT g.id, 3300, DATEADD('DAY', -1, NOW())
FROM guardians g
WHERE g.username = 'demo_guardian';

INSERT INTO step_records (guardian_id, step_count, timestamp)
SELECT g.id, 3900, NOW()
FROM guardians g
WHERE g.username = 'demo_guardian';

-- Sample Guardian data
INSERT INTO step_records (guardian_id, step_count, timestamp)
SELECT g.id, 4800, DATEADD('DAY', -6, NOW())
FROM guardians g
WHERE g.username = 'sample_guardian';

INSERT INTO step_records (guardian_id, step_count, timestamp)
SELECT g.id, 5500, DATEADD('DAY', -5, NOW())
FROM guardians g
WHERE g.username = 'sample_guardian';

INSERT INTO step_records (guardian_id, step_count, timestamp)
SELECT g.id, 3200, DATEADD('DAY', -4, NOW())
FROM guardians g
WHERE g.username = 'sample_guardian';

INSERT INTO step_records (guardian_id, step_count, timestamp)
SELECT g.id, 4900, DATEADD('DAY', -3, NOW())
FROM guardians g
WHERE g.username = 'sample_guardian';

INSERT INTO step_records (guardian_id, step_count, timestamp)
SELECT g.id, 5100, DATEADD('DAY', -2, NOW())
FROM guardians g
WHERE g.username = 'sample_guardian';

INSERT INTO step_records (guardian_id, step_count, timestamp)
SELECT g.id, 4600, DATEADD('DAY', -1, NOW())
FROM guardians g
WHERE g.username = 'sample_guardian';

INSERT INTO step_records (guardian_id, step_count, timestamp)
SELECT g.id, 4300, NOW()
FROM guardians g
WHERE g.username = 'sample_guardian';

-- Build daily aggregates for the sample data
-- H2 compatible: conditional INSERT for aggregates
INSERT INTO daily_step_aggregates (guardian_id, date, total_steps, energy_earned)
SELECT 
    guardian_id,
    CAST(timestamp AS DATE) as date,
    SUM(step_count) as total_steps,
    FLOOR(SUM(step_count) / 10) as energy_earned
FROM step_records
WHERE CAST(timestamp AS DATE) >= DATEADD('DAY', -7, CURRENT_DATE)
GROUP BY guardian_id, CAST(timestamp AS DATE)
HAVING NOT EXISTS (
    SELECT 1 FROM daily_step_aggregates dsa 
    WHERE dsa.guardian_id = step_records.guardian_id 
    AND dsa.date = CAST(step_records.timestamp AS DATE)
);

-- Sample energy transactions
-- H2 compatible: conditional INSERT for transactions
-- Test Guardian transactions
INSERT INTO energy_transactions (guardian_id, transaction_type, amount, source, timestamp)
SELECT g.id, 'EARNED', 300, 'DAILY_STEPS', DATEADD('DAY', -6, NOW())
FROM guardians g WHERE g.username = 'test_guardian';

INSERT INTO energy_transactions (guardian_id, transaction_type, amount, source, timestamp)
SELECT g.id, 'EARNED', 450, 'DAILY_STEPS', DATEADD('DAY', -5, NOW())
FROM guardians g WHERE g.username = 'test_guardian';

INSERT INTO energy_transactions (guardian_id, transaction_type, amount, source, timestamp)
SELECT g.id, 'EARNED', 280, 'DAILY_STEPS', DATEADD('DAY', -4, NOW())
FROM guardians g WHERE g.username = 'test_guardian';

INSERT INTO energy_transactions (guardian_id, transaction_type, amount, source, timestamp)
SELECT g.id, 'EARNED', 520, 'DAILY_STEPS', DATEADD('DAY', -3, NOW())
FROM guardians g WHERE g.username = 'test_guardian';

INSERT INTO energy_transactions (guardian_id, transaction_type, amount, source, timestamp)
SELECT g.id, 'SPENT', 100, 'BATTLE', DATEADD('DAY', -3, NOW())
FROM guardians g WHERE g.username = 'test_guardian';

INSERT INTO energy_transactions (guardian_id, transaction_type, amount, source, timestamp)
SELECT g.id, 'EARNED', 370, 'DAILY_STEPS', DATEADD('DAY', -2, NOW())
FROM guardians g WHERE g.username = 'test_guardian';

INSERT INTO energy_transactions (guardian_id, transaction_type, amount, source, timestamp)
SELECT g.id, 'SPENT', 50, 'CHALLENGE', DATEADD('DAY', -2, NOW())
FROM guardians g WHERE g.username = 'test_guardian';

INSERT INTO energy_transactions (guardian_id, transaction_type, amount, source, timestamp)
SELECT g.id, 'EARNED', 410, 'DAILY_STEPS', DATEADD('DAY', -1, NOW())
FROM guardians g WHERE g.username = 'test_guardian';

INSERT INTO energy_transactions (guardian_id, transaction_type, amount, source, timestamp)
SELECT g.id, 'EARNED', 290, 'DAILY_STEPS', NOW()
FROM guardians g WHERE g.username = 'test_guardian';

-- Demo Guardian transactions
INSERT INTO energy_transactions (guardian_id, transaction_type, amount, source, timestamp)
SELECT g.id, 'EARNED', 250, 'DAILY_STEPS', DATEADD('DAY', -6, NOW())
FROM guardians g WHERE g.username = 'demo_guardian';

INSERT INTO energy_transactions (guardian_id, transaction_type, amount, source, timestamp)
SELECT g.id, 'EARNED', 380, 'DAILY_STEPS', DATEADD('DAY', -5, NOW())
FROM guardians g WHERE g.username = 'demo_guardian';

INSERT INTO energy_transactions (guardian_id, transaction_type, amount, source, timestamp)
SELECT g.id, 'EARNED', 420, 'DAILY_STEPS', DATEADD('DAY', -4, NOW())
FROM guardians g WHERE g.username = 'demo_guardian';

INSERT INTO energy_transactions (guardian_id, transaction_type, amount, source, timestamp)
SELECT g.id, 'SPENT', 75, 'SHOP', DATEADD('DAY', -4, NOW())
FROM guardians g WHERE g.username = 'demo_guardian';

INSERT INTO energy_transactions (guardian_id, transaction_type, amount, source, timestamp)
SELECT g.id, 'EARNED', 310, 'DAILY_STEPS', DATEADD('DAY', -3, NOW())
FROM guardians g WHERE g.username = 'demo_guardian';

INSERT INTO energy_transactions (guardian_id, transaction_type, amount, source, timestamp)
SELECT g.id, 'EARNED', 470, 'DAILY_STEPS', DATEADD('DAY', -2, NOW())
FROM guardians g WHERE g.username = 'demo_guardian';

INSERT INTO energy_transactions (guardian_id, transaction_type, amount, source, timestamp)
SELECT g.id, 'SPENT', 125, 'BATTLE', DATEADD('DAY', -1, NOW())
FROM guardians g WHERE g.username = 'demo_guardian';

INSERT INTO energy_transactions (guardian_id, transaction_type, amount, source, timestamp)
SELECT g.id, 'EARNED', 330, 'DAILY_STEPS', DATEADD('DAY', -1, NOW())
FROM guardians g WHERE g.username = 'demo_guardian';

INSERT INTO energy_transactions (guardian_id, transaction_type, amount, source, timestamp)
SELECT g.id, 'EARNED', 390, 'DAILY_STEPS', NOW()
FROM guardians g WHERE g.username = 'demo_guardian';

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

-- Add database performance monitoring views (H2 compatible)
CREATE VIEW IF NOT EXISTS v_guardian_stats AS
SELECT 
    g.id,
    g.username,
    g.display_name,
    eb.current_balance,
    COALESCE(das.total_steps_today, 0) as steps_today,
    COALESCE(das.energy_earned_today, 0) as energy_earned_today,
    (SELECT COUNT(*) FROM step_records sr WHERE sr.guardian_id = g.id AND CAST(sr.timestamp AS DATE) = CURRENT_DATE) as submissions_today
FROM guardians g
LEFT JOIN energy_balances eb ON g.id = eb.guardian_id
LEFT JOIN (
    SELECT guardian_id, total_steps as total_steps_today, energy_earned as energy_earned_today
    FROM daily_step_aggregates
    WHERE date = CURRENT_DATE
) das ON g.id = das.guardian_id;

-- Database health monitoring view (H2 compatible)
CREATE VIEW IF NOT EXISTS v_database_health AS
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