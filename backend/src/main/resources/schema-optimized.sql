-- Big Data Admin Platform - Optimized Schema with Indexes
-- Performance optimizations for common query patterns

-- Drop existing indexes if they exist (for re-running this script)
DROP INDEX IF EXISTS idx_records_collection_created ON data_record;
DROP INDEX IF EXISTS idx_records_type_created ON data_record;
DROP INDEX IF EXISTS idx_records_collection_type ON data_record;
DROP INDEX IF EXISTS idx_collections_source_status ON data_collection;
DROP INDEX IF EXISTS idx_collections_status_updated ON data_collection;
DROP INDEX IF EXISTS idx_sources_type_status ON data_source;

-- Optimized indexes for data_record table
-- 1. Composite index for collection queries with ordering by creation time
CREATE INDEX idx_records_collection_created ON data_record(collection_id, created_at DESC);

-- 2. Composite index for type filtering with time ordering
CREATE INDEX idx_records_type_created ON data_record(data_type, created_at DESC);

-- 3. Composite index for collection + type queries
CREATE INDEX idx_records_collection_type ON data_record(collection_id, data_type);

-- 4. Covering index for common list queries (reduces table lookups)
CREATE INDEX idx_records_covering ON data_record(collection_id, created_at DESC, id, data_type);

-- Optimized indexes for data_collection table
-- 1. Composite index for source + status queries
CREATE INDEX idx_collections_source_status ON data_collection(data_source_id, status);

-- 2. Index for status filtering with update time ordering
CREATE INDEX idx_collections_status_updated ON data_collection(status, updated_at DESC);

-- 3. Covering index for statistics queries
CREATE INDEX idx_collections_stats_covering ON data_collection(status, record_count, size_in_bytes);

-- Optimized indexes for data_source table
-- 1. Composite index for type + status queries
CREATE INDEX idx_sources_type_status ON data_source(type, status);

-- 2. Index for name searches (already exists as FULLTEXT, adding regular index for prefix searches)
CREATE INDEX idx_sources_name ON data_source(name(50));

-- Optimized indexes for sys_user table
-- 1. Composite index for status + role queries
CREATE INDEX idx_users_status_role ON sys_user(status, role);

-- Analyze tables for query optimizer
ANALYZE TABLE data_source;
ANALYZE TABLE data_collection;
ANALYZE TABLE data_record;
ANALYZE TABLE sys_user;

-- Display index information for verification
SELECT
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME,
    SEQ_IN_INDEX
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'bigdata_admin'
ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;
