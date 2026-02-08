package mk.dmt.wms.monitoring;

import mk.dmt.wms.config.MonitoringConfig;
import mk.dmt.wms.event.MeasurementEventBus;
import mk.dmt.wms.model.SensorMeasurement;
import mk.dmt.wms.model.SensorType;
import mk.dmt.wms.service.MeasurementPersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CentralMonitoringService threshold logic.
 */
class CentralMonitoringServiceTest {

    private CentralMonitoringService monitoringService;
    private MeasurementEventBus eventBus;
    private MonitoringConfig config;
    private MeasurementPersistenceService persistenceService;

    @BeforeEach
    void setUp() {
        eventBus = new MeasurementEventBus();
        config = new MonitoringConfig();
        config.setTemperatureThreshold(35.0);
        config.setHumidityThreshold(50.0);
        persistenceService = mock(MeasurementPersistenceService.class);
        monitoringService = new CentralMonitoringService(eventBus, config, persistenceService);
    }

    @Test
    @DisplayName("Should not exceed threshold when temperature is below limit")
    void shouldNotExceedThresholdWhenTemperatureBelowLimit() {
        SensorMeasurement measurement = SensorMeasurement.of("t1", SensorType.TEMPERATURE, 30.0);

        assertFalse(monitoringService.exceedsThreshold(measurement));
    }

    @Test
    @DisplayName("Should not exceed threshold when temperature equals limit")
    void shouldNotExceedThresholdWhenTemperatureEqualsLimit() {
        SensorMeasurement measurement = SensorMeasurement.of("t1", SensorType.TEMPERATURE, 35.0);

        assertFalse(monitoringService.exceedsThreshold(measurement));
    }

    @Test
    @DisplayName("Should exceed threshold when temperature is above limit")
    void shouldExceedThresholdWhenTemperatureAboveLimit() {
        SensorMeasurement measurement = SensorMeasurement.of("t1", SensorType.TEMPERATURE, 36.0);

        assertTrue(monitoringService.exceedsThreshold(measurement));
    }

    @Test
    @DisplayName("Should not exceed threshold when humidity is below limit")
    void shouldNotExceedThresholdWhenHumidityBelowLimit() {
        SensorMeasurement measurement = SensorMeasurement.of("h1", SensorType.HUMIDITY, 45.0);

        assertFalse(monitoringService.exceedsThreshold(measurement));
    }

    @Test
    @DisplayName("Should exceed threshold when humidity is above limit")
    void shouldExceedThresholdWhenHumidityAboveLimit() {
        SensorMeasurement measurement = SensorMeasurement.of("h1", SensorType.HUMIDITY, 55.0);

        assertTrue(monitoringService.exceedsThreshold(measurement));
    }

    @ParameterizedTest
    @DisplayName("Should correctly identify threshold violations for various values")
    @CsvSource({
        "TEMPERATURE, 34.9, false",
        "TEMPERATURE, 35.0, false",
        "TEMPERATURE, 35.1, true",
        "TEMPERATURE, 40.0, true",
        "HUMIDITY, 49.9, false",
        "HUMIDITY, 50.0, false",
        "HUMIDITY, 50.1, true",
        "HUMIDITY, 75.0, true"
    })
    void shouldCorrectlyIdentifyThresholdViolations(SensorType type, double value, boolean expected) {
        SensorMeasurement measurement = SensorMeasurement.of("sensor1", type, value);

        assertEquals(expected, monitoringService.exceedsThreshold(measurement));
    }

    @Test
    @DisplayName("Should use custom threshold from config")
    void shouldUseCustomThresholdFromConfig() {
        config.setTemperatureThreshold(40.0);

        SensorMeasurement belowThreshold = SensorMeasurement.of("t1", SensorType.TEMPERATURE, 38.0);
        SensorMeasurement aboveThreshold = SensorMeasurement.of("t1", SensorType.TEMPERATURE, 42.0);

        assertFalse(monitoringService.exceedsThreshold(belowThreshold));
        assertTrue(monitoringService.exceedsThreshold(aboveThreshold));
    }
}
