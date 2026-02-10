-- ==========================================
-- Warehouse Monitoring System Database Schema
-- Initial migration (V1)
-- ==========================================

-- Table for storing sensor measurements
create table if not exists sensor_measurements (
    id                 bigserial primary key,
    sensor_id          text,
    sensor_type        text,
    measurement_value  numeric(10,2),
    updated_at         timestamp,
    warehouse_id       text,
    created_at         timestamp
);

-- Indexes for sensor_measurements
create index idx_sensor_measurements_sensor_id on sensor_measurements(sensor_id);
create index idx_sensor_measurements_timestamp on sensor_measurements(updated_at);
create index idx_sensor_measurements_sensor_type on sensor_measurements(sensor_type);

-- Table for storing alarm events
create table if not exists alarm_events (
    id                 bigserial primary key,
    sensor_id          text,
    sensor_type        text,
    measurement_value  numeric(10,2),
    threshold          numeric(10,2),
    severity           text,
    updated_at         timestamp,
    warehouse_id       text,
    created_at         timestamp
);

-- Indexes for alarm_events
create index idx_alarm_events_sensor_id on alarm_events(sensor_id);
create index idx_alarm_events_timestamp on alarm_events(updated_at);
create index idx_alarm_events_severity on alarm_events(severity);
