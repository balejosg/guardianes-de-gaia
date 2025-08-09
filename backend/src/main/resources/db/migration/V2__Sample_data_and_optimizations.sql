-- Sample data and performance optimizations
-- This migration adds sample data for development and testing
-- Fixed: Proper dependency management and portable SQL syntax

-- Additional performance indexes
CREATE INDEX IF NOT EXISTS idx_step_records_guardian_recorded_at_desc ON step_records(guardian_id, recorded_at DESC);
CREATE INDEX IF NOT EXISTS idx_energy_transactions_guardian_recorded_at ON energy_transactions(guardian_id, recorded_at DESC);

-- Composite indexes for common query patterns are already defined in V1

-- Note: Cleanup job for old rate limiting records should be handled by application scheduler
-- H2 does not support CREATE EVENT, so this is commented out for tests
-- In production with MySQL, this would be:
-- CREATE EVENT IF NOT EXISTS cleanup_old_rate_limits
-- ON SCHEDULE EVERY 1 HOUR
-- DO DELETE FROM submission_rate_limits WHERE window_end < DATE_SUB(NOW(), INTERVAL 24 HOUR);

-- Sample development data with proper dependency management
-- Step 1: Create sample guardians using INSERT ... SELECT for proper conditional insertion
INSERT INTO guardians (username, display_name, email, age) 
SELECT 'test_guardian', 'Test Guardian', 'test@guardianes.com', 10
WHERE NOT EXISTS (SELECT 1 FROM guardians WHERE username = 'test_guardian');

INSERT INTO guardians (username, display_name, email, age) 
SELECT 'demo_guardian', 'Demo Guardian', 'demo@guardianes.com', 8
WHERE NOT EXISTS (SELECT 1 FROM guardians WHERE username = 'demo_guardian');

INSERT INTO guardians (username, display_name, email, age) 
SELECT 'sample_guardian', 'Sample Guardian', 'sample@guardianes.com', 12
WHERE NOT EXISTS (SELECT 1 FROM guardians WHERE username = 'sample_guardian');

-- Step 2: Get the actual guardian IDs (they may not be 1,2,3 due to auto-increment)
-- Initialize energy balances using actual guardian IDs from the database
INSERT INTO energy_balances (guardian_id, current_balance)
SELECT g.id, 0
FROM guardians g
WHERE g.username IN ('test_guardian', 'demo_guardian', 'sample_guardian')
AND NOT EXISTS (SELECT 1 FROM energy_balances eb WHERE eb.guardian_id = g.id);

-- Step 3: Add sample step records using actual guardian IDs from database
-- Using portable SQL syntax and proper foreign key references
INSERT INTO step_records (guardian_id, step_count, recorded_at)
SELECT g.id, step_data.step_count, step_data.recorded_at
FROM guardians g
CROSS JOIN (
  -- Test Guardian data - recent test data for development
  SELECT 3000 as step_count, TIMESTAMP '2024-07-26 09:00:00' as recorded_at
  UNION ALL SELECT 4500, TIMESTAMP '2024-07-27 10:30:00'
  UNION ALL SELECT 2800, TIMESTAMP '2024-07-28 08:15:00'
  UNION ALL SELECT 5200, TIMESTAMP '2024-07-29 11:45:00'
  UNION ALL SELECT 3700, TIMESTAMP '2024-07-30 14:20:00'
  UNION ALL SELECT 4100, TIMESTAMP '2024-07-31 16:10:00'
  UNION ALL SELECT 2900, CURRENT_TIMESTAMP
) step_data
WHERE g.username = 'test_guardian'
AND NOT EXISTS (
  SELECT 1 FROM step_records sr 
  WHERE sr.guardian_id = g.id 
  AND sr.recorded_at = step_data.recorded_at
);

INSERT INTO step_records (guardian_id, step_count, recorded_at)  
SELECT g.id, step_data.step_count, step_data.recorded_at
FROM guardians g
CROSS JOIN (
  -- Demo Guardian data - different pattern for testing
  SELECT 2500 as step_count, TIMESTAMP '2024-07-26 08:30:00' as recorded_at
  UNION ALL SELECT 3800, TIMESTAMP '2024-07-27 09:45:00'
  UNION ALL SELECT 4200, TIMESTAMP '2024-07-28 12:20:00'
  UNION ALL SELECT 3100, TIMESTAMP '2024-07-29 15:10:00'
  UNION ALL SELECT 4700, TIMESTAMP '2024-07-30 07:55:00'
  UNION ALL SELECT 3300, TIMESTAMP '2024-07-31 13:25:00'
  UNION ALL SELECT 3900, CURRENT_TIMESTAMP
) step_data
WHERE g.username = 'demo_guardian'
AND NOT EXISTS (
  SELECT 1 FROM step_records sr 
  WHERE sr.guardian_id = g.id 
  AND sr.recorded_at = step_data.recorded_at
);

INSERT INTO step_records (guardian_id, step_count, recorded_at)
SELECT g.id, step_data.step_count, step_data.recorded_at
FROM guardians g
CROSS JOIN (
  -- Sample Guardian data - high activity pattern
  SELECT 4800 as step_count, TIMESTAMP '2024-07-26 07:20:00' as recorded_at
  UNION ALL SELECT 5500, TIMESTAMP '2024-07-27 11:15:00'
  UNION ALL SELECT 3200, TIMESTAMP '2024-07-28 14:40:00'
  UNION ALL SELECT 4900, TIMESTAMP '2024-07-29 09:30:00'
  UNION ALL SELECT 5100, TIMESTAMP '2024-07-30 16:45:00'
  UNION ALL SELECT 4600, TIMESTAMP '2024-07-31 12:05:00'
  UNION ALL SELECT 4300, CURRENT_TIMESTAMP
) step_data
WHERE g.username = 'sample_guardian'
AND NOT EXISTS (
  SELECT 1 FROM step_records sr 
  WHERE sr.guardian_id = g.id 
  AND sr.recorded_at = step_data.recorded_at
);

-- Step 4: Build daily aggregates from the actual step records data
-- Using portable SQL syntax compatible with both MySQL and H2
INSERT INTO daily_step_aggregates (guardian_id, date, total_steps, energy_earned)
SELECT 
    guardian_id,
    CAST(recorded_at AS DATE) as date,
    SUM(step_count) as total_steps,
    CAST(SUM(step_count) / 10 AS INTEGER) as energy_earned
FROM step_records
WHERE recorded_at >= DATE '2024-07-25'  -- Filter for recent test data
AND NOT EXISTS (
    SELECT 1 FROM daily_step_aggregates das 
    WHERE das.guardian_id = step_records.guardian_id 
    AND das.date = CAST(step_records.recorded_at AS DATE)
)
GROUP BY guardian_id, CAST(recorded_at AS DATE);

-- Step 5: Add sample energy transactions using actual guardian IDs
-- Test Guardian transactions
INSERT INTO energy_transactions (guardian_id, transaction_type, amount, source, recorded_at)
SELECT g.id, trans_data.transaction_type, trans_data.amount, trans_data.source, trans_data.recorded_at
FROM guardians g
CROSS JOIN (
  -- Test Guardian transaction history
  SELECT 'EARNED' as transaction_type, 300 as amount, 'DAILY_STEPS' as source, TIMESTAMP '2024-07-26 09:05:00' as recorded_at
  UNION ALL SELECT 'EARNED', 450, 'DAILY_STEPS', TIMESTAMP '2024-07-27 10:35:00'
  UNION ALL SELECT 'EARNED', 280, 'DAILY_STEPS', TIMESTAMP '2024-07-28 08:20:00'
  UNION ALL SELECT 'EARNED', 520, 'DAILY_STEPS', TIMESTAMP '2024-07-29 11:50:00'
  UNION ALL SELECT 'SPENT', 100, 'BATTLE', TIMESTAMP '2024-07-29 15:30:00'
  UNION ALL SELECT 'EARNED', 370, 'DAILY_STEPS', TIMESTAMP '2024-07-30 14:25:00'
  UNION ALL SELECT 'SPENT', 50, 'CHALLENGE', TIMESTAMP '2024-07-30 18:45:00'
  UNION ALL SELECT 'EARNED', 410, 'DAILY_STEPS', TIMESTAMP '2024-07-31 16:15:00'
  UNION ALL SELECT 'EARNED', 290, 'DAILY_STEPS', CURRENT_TIMESTAMP
) trans_data
WHERE g.username = 'test_guardian'
AND NOT EXISTS (
  SELECT 1 FROM energy_transactions et 
  WHERE et.guardian_id = g.id 
  AND et.recorded_at = trans_data.recorded_at
  AND et.source = trans_data.source
);

-- Demo Guardian transactions
INSERT INTO energy_transactions (guardian_id, transaction_type, amount, source, recorded_at)
SELECT g.id, trans_data.transaction_type, trans_data.amount, trans_data.source, trans_data.recorded_at
FROM guardians g
CROSS JOIN (
  -- Demo Guardian transaction history  
  SELECT 'EARNED' as transaction_type, 250 as amount, 'DAILY_STEPS' as source, TIMESTAMP '2024-07-26 08:35:00' as recorded_at
  UNION ALL SELECT 'EARNED', 380, 'DAILY_STEPS', TIMESTAMP '2024-07-27 09:50:00'
  UNION ALL SELECT 'EARNED', 420, 'DAILY_STEPS', TIMESTAMP '2024-07-28 12:25:00'
  UNION ALL SELECT 'SPENT', 75, 'SHOP', TIMESTAMP '2024-07-28 16:10:00'
  UNION ALL SELECT 'EARNED', 310, 'DAILY_STEPS', TIMESTAMP '2024-07-29 15:15:00'
  UNION ALL SELECT 'EARNED', 470, 'DAILY_STEPS', TIMESTAMP '2024-07-30 08:00:00'
  UNION ALL SELECT 'SPENT', 125, 'BATTLE', TIMESTAMP '2024-07-31 10:20:00'
  UNION ALL SELECT 'EARNED', 330, 'DAILY_STEPS', TIMESTAMP '2024-07-31 13:30:00'
  UNION ALL SELECT 'EARNED', 390, 'DAILY_STEPS', CURRENT_TIMESTAMP
) trans_data
WHERE g.username = 'demo_guardian'
AND NOT EXISTS (
  SELECT 1 FROM energy_transactions et 
  WHERE et.guardian_id = g.id 
  AND et.recorded_at = trans_data.recorded_at
  AND et.source = trans_data.source
);

-- Step 6: Update energy balances based on actual transactions
-- Calculate energy balance for each guardian based on their transaction history
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
)
WHERE EXISTS (SELECT 1 FROM energy_transactions et WHERE et.guardian_id = eb.guardian_id);

-- Step 7: Create database monitoring views with proper error handling
-- Guardian stats view for application monitoring
DROP VIEW IF EXISTS v_guardian_stats;
CREATE VIEW v_guardian_stats AS
SELECT 
    g.id,
    g.username,
    g.display_name,
    COALESCE(eb.current_balance, 0) as current_balance,
    COALESCE(das.total_steps_today, 0) as steps_today,
    COALESCE(das.energy_earned_today, 0) as energy_earned_today,
    (SELECT COUNT(*) FROM step_records sr 
     WHERE sr.guardian_id = g.id 
     AND CAST(sr.recorded_at AS DATE) = CURRENT_DATE) as submissions_today
FROM guardians g
LEFT JOIN energy_balances eb ON g.id = eb.guardian_id
LEFT JOIN (
    SELECT guardian_id, 
           total_steps as total_steps_today, 
           energy_earned as energy_earned_today
    FROM daily_step_aggregates
    WHERE date = CURRENT_DATE
) das ON g.id = das.guardian_id;

-- Database health monitoring view for system diagnostics  
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
    MAX(recorded_at) as last_updated
FROM energy_transactions
UNION ALL
SELECT 
    'daily_step_aggregates' as table_name,
    COUNT(*) as record_count,
    MAX(updated_at) as last_updated
FROM daily_step_aggregates;

-- Migration Summary
-- This migration provides comprehensive sample data and performance optimizations:
-- 1. Performance indexes with IF NOT EXISTS for idempotency
-- 2. Sample guardian data with proper dependency management
-- 3. Step records using actual guardian IDs (not hardcoded values)
-- 4. Energy transactions linked to proper guardian references
-- 5. Daily aggregates calculated from actual step data
-- 6. Energy balance calculations based on transaction history
-- 7. Monitoring views for application health and diagnostics
-- 8. Portable SQL syntax compatible with MySQL and H2
-- 9. Proper error handling with NOT EXISTS checks for idempotency
-- 10. Foreign key integrity maintained throughout the migration