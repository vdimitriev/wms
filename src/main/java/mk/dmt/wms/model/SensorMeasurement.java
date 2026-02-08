package mk.dmt.wms.model;

import java.time.Instant;

/**
 * Record representing a measurement from a sensor.
 *
 * @param sensorId   The unique identifier of the sensor (e.g., "t1", "h1")
 * @param sensorType The type of sensor (TEMPERATURE or HUMIDITY)
 * @param value      The measured value
 * @param timestamp  The time when the measurement was recorded
 * @param warehouseId The identifier of the warehouse (optional, for multi-warehouse support)
 */
public record SensorMeasurement(
        String sensorId,
        SensorType sensorType,
        double value,
        Instant timestamp,
        String warehouseId
) {
    /**
     * Creates a SensorMeasurement with current timestamp and default warehouse.
     */
    public static SensorMeasurement of(String sensorId, SensorType sensorType, double value) {
        return new SensorMeasurement(sensorId, sensorType, value, Instant.now(), "warehouse-1");
    }

    /**
     * Creates a SensorMeasurement with current timestamp.
     */
    public static SensorMeasurement of(String sensorId, SensorType sensorType, double value, String warehouseId) {
        return new SensorMeasurement(sensorId, sensorType, value, Instant.now(), warehouseId);
    }

    /**
     * Returns a formatted string representation of the measurement.
     */
    public String toDisplayString() {
        return String.format("[%s] Sensor %s (%s): %.2f%s",
                warehouseId, sensorId, sensorType.getDisplayName(), value, sensorType.getUnit());
    }
}
