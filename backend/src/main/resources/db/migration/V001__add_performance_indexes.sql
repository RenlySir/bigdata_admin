-- Performance Optimization Indexes for BigData Admin
-- This script adds indexes to optimize frequently used queries

-- data_record table indexes
-- Primary query pattern: WHERE collection_id = ? ORDER BY created_at DESC
CREATE INDEX IF NOT EXISTS idx_data_record_collection_created
ON data_record(collection_id, created_at DESC);

-- For filtering by data type
CREATE INDEX IF NOT EXISTS idx_data_record_data_type
ON data_record(data_type);

-- For text search optimization (partial index for JSON data)
CREATE INDEX IF NOT EXISTS idx_data_record_json_text
ON data_record(collection_id, id) WHERE text_content IS NOT NULL;

-- data_source table indexes
-- For filtering by type
CREATE INDEX IF NOT EXISTS idx_data_source_type
ON data_source(type);

-- For filtering by status
CREATE INDEX IF NOT EXISTS idx_data_source_status
ON data_source(status);

-- Composite index for type and status
CREATE INDEX IF NOT EXISTS idx_data_source_type_status
ON data_source(type, status);

-- data_collection table indexes
-- For joining with data_source
CREATE INDEX IF NOT EXISTS idx_data_collection_datasource
ON data_collection(data_source_id);

-- For filtering by status
CREATE INDEX IF NOT EXISTS idx_data_collection_status
ON data_collection(status);

-- For ordering by creation date
CREATE INDEX IF NOT EXISTS idx_data_collection_created
ON data_collection(created_at DESC);

-- import_task table indexes
-- For filtering by collection_id
CREATE INDEX IF NOT EXISTS idx_import_task_collection
ON import_task(collection_id);

-- For filtering by status
CREATE INDEX IF NOT EXISTS idx_import_task_status
ON import_task(status);

-- For monitoring and cleanup queries
CREATE INDEX IF NOT EXISTS idx_import_task_created
ON import_task(created_at DESC);

-- etl_execution table indexes
-- For filtering by transformation_id
CREATE INDEX IF NOT EXISTS idx_etl_execution_transformation
ON etl_execution(transformationId);

-- For filtering by status
CREATE INDEX IF NOT EXISTS idx_etl_execution_status
ON etl_execution(status);

-- For monitoring queries
CREATE INDEX IF NOT EXISTS idx_etl_execution_created
ON etl_execution(created_at DESC);

-- system_metric table indexes
-- For time-series queries
CREATE INDEX IF NOT EXISTS idx_system_metric_timestamp
ON system_metric(timestamp DESC);

-- For filtering by metric name
CREATE INDEX IF NOT EXISTS idx_system_metric_name
ON system_metric(metric_name);

-- Composite index for time-series by metric
CREATE INDEX IF NOT EXISTS idx_system_metric_name_timestamp
ON system_metric(metric_name, timestamp DESC);

-- alert_rule and alert_history indexes
-- For filtering active rules
CREATE INDEX IF NOT EXISTS idx_alert_rule_status
ON alert_rule(status);

-- For querying alert history
CREATE INDEX IF NOT EXISTS idx_alert_history_rule
ON alert_history(ruleId);

-- For time-based alert history queries
CREATE INDEX IF NOT EXISTS idx_alert_history_created
ON alert_history(created_at DESC);

-- Analyze tables to update statistics after index creation
ANALYZE TABLE data_record;
ANALYZE TABLE data_source;
ANALYZE TABLE data_collection;
ANALYZE TABLE import_task;
ANALYZE TABLE etl_execution;
ANALYZE TABLE system_metric;
ANALYZE TABLE alert_rule;
ANALYZE TABLE alert_history;
