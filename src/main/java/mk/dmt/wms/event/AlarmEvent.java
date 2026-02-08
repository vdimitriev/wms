package mk.dmt.wms.event;

import mk.dmt.wms.model.SensorMeasurement;
import mk.dmt.wms.model.SensorType;

import java.time.Instant;

/**
 * Record representing an alarm event triggered when a measurement exceeds threshold.
 *
 * @param measurement The sensor measurement that triggered the alarm
 * @param threshold   The threshold that was exceeded
 * @param timestamp   The time when the alarm was triggered
 * @param severity    The severity level of the alarm
 */
public record AlarmEvent(
        SensorMeasurement measurement,
        double threshold,
        Instant timestamp,
        Severity severity
) {
    /**
     * Severity levels for alarms.
     */
    public enum Severity {
        WARNING, HIGH, CRITICAL
    }

    /**
     * Creates an AlarmEvent with current timestamp.
     */
    public static AlarmEvent of(SensorMeasurement measurement, double threshold) {
        Severity severity = calculateSeverity(measurement.value(), threshold);
        return new AlarmEvent(measurement, threshold, Instant.now(), severity);
    }

    /**
     * Creates an AlarmEvent with specified severity.
     */
    public static AlarmEvent of(SensorMeasurement measurement, double threshold, Severity severity) {
        return new AlarmEvent(measurement, threshold, Instant.now(), severity);
    }

    /**
     * Calculates severity based on how much the threshold was exceeded.
     */
    private static Severity calculateSeverity(double value, double threshold) {
        double exceededBy = value - threshold;
        double percentage = (exceededBy / threshold) * 100;

        if (percentage > 20) {
            return Severity.CRITICAL;
        } else if (percentage > 10) {
            return Severity.HIGH;
        } else {
            return Severity.WARNING;
        }
    }

    /**
     * Returns the severity level based on how much the threshold was exceeded.
     */
    public String getSeverity() {
        return severity.name();
    }

    /**
     * Returns a formatted alarm message for console output.
     */
    public String toAlarmMessage() {
        SensorType type = measurement.sensorType();
        return String.format(
                "🚨 ALARM [%s] - %s threshold exceeded! Sensor: %s, Value: %.2f%s, Threshold: %.2f%s, Warehouse: %s",
                getSeverity(),
                type.getDisplayName().toUpperCase(),
                measurement.sensorId(),
                measurement.value(),
                type.getUnit(),
                threshold,
                type.getUnit(),
                measurement.warehouseId()
        );
    }
}
