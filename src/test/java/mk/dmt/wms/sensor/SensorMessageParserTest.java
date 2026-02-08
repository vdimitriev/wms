package mk.dmt.wms.sensor;

import mk.dmt.wms.model.SensorMeasurement;
import mk.dmt.wms.model.SensorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SensorMessageParser.
 */
class SensorMessageParserTest {

    private SensorMessageParser parser;

    @BeforeEach
    void setUp() {
        parser = new SensorMessageParser();
    }

    @Test
    @DisplayName("Should parse valid temperature sensor message")
    void shouldParseValidTemperatureMessage() {
        String message = "sensor_id=t1; value=30";

        Optional<SensorMeasurement> result = parser.parseTemperature(message);

        assertTrue(result.isPresent());
        SensorMeasurement measurement = result.get();
        assertEquals("t1", measurement.sensorId());
        assertEquals(SensorType.TEMPERATURE, measurement.sensorType());
        assertEquals(30.0, measurement.value());
    }

    @Test
    @DisplayName("Should parse valid humidity sensor message")
    void shouldParseValidHumidityMessage() {
        String message = "sensor_id=h1; value=40";

        Optional<SensorMeasurement> result = parser.parseHumidity(message);

        assertTrue(result.isPresent());
        SensorMeasurement measurement = result.get();
        assertEquals("h1", measurement.sensorId());
        assertEquals(SensorType.HUMIDITY, measurement.sensorType());
        assertEquals(40.0, measurement.value());
    }

    @ParameterizedTest
    @DisplayName("Should parse messages with various spacing")
    @CsvSource({
        "'sensor_id=t1; value=30', t1, 30.0",
        "'sensor_id = t1 ; value = 30', t1, 30.0",
        "'sensor_id=t1;value=30', t1, 30.0",
        "'sensor_id=temp_sensor_001; value=25.5', temp_sensor_001, 25.5"
    })
    void shouldParseMessagesWithVariousSpacing(String message, String expectedId, double expectedValue) {
        Optional<SensorMeasurement> result = parser.parseTemperature(message);

        assertTrue(result.isPresent());
        assertEquals(expectedId, result.get().sensorId());
        assertEquals(expectedValue, result.get().value());
    }

    @Test
    @DisplayName("Should parse decimal values")
    void shouldParseDecimalValues() {
        String message = "sensor_id=t1; value=35.75";

        Optional<SensorMeasurement> result = parser.parseTemperature(message);

        assertTrue(result.isPresent());
        assertEquals(35.75, result.get().value());
    }

    @ParameterizedTest
    @DisplayName("Should return empty for invalid messages")
    @ValueSource(strings = {
        "",
        "invalid message",
        "sensor_id=t1",
        "value=30",
        "sensor_id=; value=30",
        "sensor_id=t1; value=abc"
    })
    void shouldReturnEmptyForInvalidMessages(String message) {
        Optional<SensorMeasurement> result = parser.parseTemperature(message);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty for null message")
    void shouldReturnEmptyForNullMessage() {
        Optional<SensorMeasurement> result = parser.parseTemperature(null);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle case-insensitive parsing")
    void shouldHandleCaseInsensitiveParsing() {
        String message = "SENSOR_ID=T1; VALUE=30";

        Optional<SensorMeasurement> result = parser.parseTemperature(message);

        assertTrue(result.isPresent());
        assertEquals("T1", result.get().sensorId());
        assertEquals(30.0, result.get().value());
    }
}
