-- ==========================================
-- Warehouse Monitoring System Database Schema
-- Initial migration (V1)
-- ==========================================

-- Table for storing sensor measurements
CREATE TABLE sensor_measurements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sensor_id VARCHAR(100) NOT NULL,
    sensor_type VARCHAR(20) NOT NULL,
    value DOUBLE NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    warehouse_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for sensor_measurements
CREATE INDEX idx_sensor_measurements_sensor_id ON sensor_measurements(sensor_id);
CREATE INDEX idx_sensor_measurements_timestamp ON sensor_measurements(timestamp);
CREATE INDEX idx_sensor_measurements_sensor_type ON sensor_measurements(sensor_type);

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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for alarm_events
CREATE INDEX idx_alarm_events_sensor_id ON alarm_events(sensor_id);
CREATE INDEX idx_alarm_events_timestamp ON alarm_events(timestamp);
CREATE INDEX idx_alarm_events_severity ON alarm_events(severity);

