package mk.dmt.wms.event;

import mk.dmt.wms.model.SensorMeasurement;
import mk.dmt.wms.model.SensorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AlarmEvent.
 */
class AlarmEventTest {

    @Test
    @DisplayName("Should create alarm event with current timestamp")
    void shouldCreateAlarmEventWithCurrentTimestamp() {
        SensorMeasurement measurement = SensorMeasurement.of("t1", SensorType.TEMPERATURE, 40.0);

        AlarmEvent alarm = AlarmEvent.of(measurement, 35.0);

        assertNotNull(alarm.timestamp());
        assertEquals(measurement, alarm.measurement());
        assertEquals(35.0, alarm.threshold());
    }

    @ParameterizedTest
    @DisplayName("Should determine correct severity based on threshold exceeded percentage")
    @CsvSource({
        "35.0, 36.0, WARNING",      // ~2.8% over
        "35.0, 39.0, HIGH",          // ~11.4% over
        "35.0, 45.0, CRITICAL",      // ~28.5% over
        "50.0, 52.0, WARNING",       // 4% over
        "50.0, 56.0, HIGH",          // 12% over
        "50.0, 65.0, CRITICAL"       // 30% over
    })
    void shouldDetermineCorrectSeverity(double threshold, double value, String expectedSeverity) {
        SensorMeasurement measurement = SensorMeasurement.of("t1", SensorType.TEMPERATURE, value);
        AlarmEvent alarm = AlarmEvent.of(measurement, threshold);

        assertEquals(expectedSeverity, alarm.getSeverity());
    }

    @Test
    @DisplayName("Should format alarm message correctly")
    void shouldFormatAlarmMessageCorrectly() {
        SensorMeasurement measurement = SensorMeasurement.of("t1", SensorType.TEMPERATURE, 40.0);
        AlarmEvent alarm = AlarmEvent.of(measurement, 35.0);

        String message = alarm.toAlarmMessage();

        assertTrue(message.contains("ALARM"));
        assertTrue(message.contains("TEMPERATURE"));
        assertTrue(message.contains("t1"));
        assertTrue(message.contains("40.00"));
        assertTrue(message.contains("35.00"));
    }
}
