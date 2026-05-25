-- Big Data Admin Platform - TiDB Schema
-- Multi-modal data storage platform

CREATE DATABASE IF NOT EXISTS bigdata_admin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE bigdata_admin;

-- Data Sources Table
CREATE TABLE IF NOT EXISTS data_source (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL COMMENT 'Data source name',
    type VARCHAR(50) NOT NULL COMMENT 'Source type: mysql, postgres, mongodb, kafka, file, api',
    connection_config JSON COMMENT 'Connection configuration in JSON format',
    description TEXT COMMENT 'Description of the data source',
    status INT DEFAULT 1 COMMENT '1=active, 0=inactive',
    created_by BIGINT COMMENT 'Creator user ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0 COMMENT 'Soft delete flag',
    INDEX idx_type (type),
    INDEX idx_status (status),
    FULLTEXT INDEX idx_name_desc (name, description)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Data sources configuration';

-- Data Collections Table
CREATE TABLE IF NOT EXISTS data_collection (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL COMMENT 'Collection name',
    description TEXT COMMENT 'Collection description',
    data_source_id BIGINT COMMENT 'Associated data source ID',
    schema_definition JSON COMMENT 'Schema definition for validation',
    record_count BIGINT DEFAULT 0 COMMENT 'Number of records in collection',
    size_in_bytes BIGINT DEFAULT 0 COMMENT 'Total size in bytes',
    tags VARCHAR(500) COMMENT 'Comma-separated tags',
    status INT DEFAULT 1 COMMENT '1=active, 0=inactive',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0 COMMENT 'Soft delete flag',
    INDEX idx_source (data_source_id),
    INDEX idx_status (status),
    FULLTEXT INDEX idx_tags (tags),
    FOREIGN KEY (data_source_id) REFERENCES data_source(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Data collections for organizing records';

-- Data Records Table (Multi-modal storage)
CREATE TABLE IF NOT EXISTS data_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    collection_id BIGINT NOT NULL COMMENT 'Parent collection ID',
    data_type VARCHAR(50) DEFAULT 'json' COMMENT 'Data type: json, text, binary, document',
    json_data JSON COMMENT 'Structured data in JSON format',
    text_content TEXT COMMENT 'Text content for full-text search',
    metadata JSON COMMENT 'Metadata about the record',
    version BIGINT DEFAULT 1 COMMENT 'Version for optimistic locking',
    checksum VARCHAR(64) COMMENT 'SHA-256 checksum for integrity',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0 COMMENT 'Soft delete flag',
    INDEX idx_collection (collection_id),
    INDEX idx_type (data_type),
    INDEX idx_created (created_at),
    FULLTEXT INDEX idx_text_search (text_content),
    FOREIGN KEY (collection_id) REFERENCES data_collection(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Multi-modal data records';

-- System Users Table
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL COMMENT 'Login username',
    password VARCHAR(255) NOT NULL COMMENT 'BCrypt hashed password',
    email VARCHAR(255) UNIQUE COMMENT 'User email',
    nickname VARCHAR(100) COMMENT 'Display name',
    avatar VARCHAR(500) COMMENT 'Avatar URL',
    role INT DEFAULT 0 COMMENT '0=user, 1=admin, 2=super_admin',
    status INT DEFAULT 1 COMMENT '1=active, 0=inactive',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0 COMMENT 'Soft delete flag',
    INDEX idx_role (role),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='System users';

-- Import Tasks Table (for async data import)
CREATE TABLE IF NOT EXISTS import_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    collection_id BIGINT NOT NULL COMMENT 'Target collection',
    source_type VARCHAR(50) NOT NULL COMMENT 'csv, json, excel, database',
    source_config JSON COMMENT 'Source configuration',
    status VARCHAR(20) DEFAULT 'pending' COMMENT 'pending, running, completed, failed',
    total_records INT DEFAULT 0 COMMENT 'Total records to import',
    processed_records INT DEFAULT 0 COMMENT 'Processed records count',
    failed_records INT DEFAULT 0 COMMENT 'Failed records count',
    error_message TEXT COMMENT 'Error details if failed',
    progress INT DEFAULT 0 COMMENT 'Progress percentage 0-100',
    started_at TIMESTAMP NULL COMMENT 'Task start time',
    completed_at TIMESTAMP NULL COMMENT 'Task completion time',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_collection (collection_id),
    INDEX idx_status (status),
    FOREIGN KEY (collection_id) REFERENCES data_collection(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Async import task tracking';

-- Sample data for testing
INSERT INTO sys_user (username, password, email, nickname, role, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'admin@bigdata.com', 'Administrator', 2, 1),
('user', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'user@bigdata.com', 'Demo User', 0, 1)
ON DUPLICATE KEY UPDATE username=username;
