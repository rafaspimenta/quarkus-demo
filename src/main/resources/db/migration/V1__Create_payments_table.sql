-- Create payments table with optimized structure for write performance
CREATE TABLE IF NOT EXISTS payments (
    id UUID PRIMARY KEY,
    correlation_id UUID NOT NULL UNIQUE,
    amount DECIMAL(10,2) NOT NULL,
    requested_at TIMESTAMP NOT NULL,
    processor_type VARCHAR(10) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT payments_correlation_id_unique UNIQUE (correlation_id)
);

-- Create indexes for optimal query performance
-- This composite index perfectly supports the getPaymentSummary query:
-- - FILTER clauses on processor_type 
-- - WHERE clauses on requested_at
CREATE INDEX IF NOT EXISTS idx_payments_processor_type_requested_at ON payments (processor_type, requested_at);

-- Note: correlation_id index is automatically created by the UNIQUE constraint
-- Note: created_at index removed as no queries filter by this column

-- Add table-level optimizations for PostgreSQL
ALTER TABLE payments SET (
    fillfactor = 90,  -- Leave space for updates
    autovacuum_vacuum_threshold = 100,
    autovacuum_analyze_threshold = 100,
    autovacuum_vacuum_scale_factor = 0.1,
    autovacuum_analyze_scale_factor = 0.05
); 