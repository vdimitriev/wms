-- ==========================================
-- Warehouse Monitoring System Database Schema
-- ==========================================

-- Drop tables if they exist (for clean restart)
DROP TABLE IF EXISTS sensor_measurements;
DROP TABLE IF EXISTS alarm_events;

-- Table for storing sensor measurements
CREATE TABLE sensor_measurements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sensor_id VARCHAR(100) NOT NULL,
    sensor_type VARCHAR(20) NOT NULL,
    value DOUBLE NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    warehouse_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_sensor_id (sensor_id),
    INDEX idx_timestamp (timestamp),
    INDEX idx_sensor_type (sensor_type)
);

-- Table for storing alarm events
CREATE TABLE alarm_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sensor_id VARCHAR(100) NOT NULL,
    sensor_type VARCHAR(20) NOT NULL,
    value DOUBLE NOT NULL,
    threshold DOUBLE NOT NULL,
    severity VARCHAR(20) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    warehouse_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_sensor_id (sensor_id),
    INDEX idx_timestamp (timestamp),
    INDEX idx_severity (severity)
);
