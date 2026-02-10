package mk.dmt.wms.service;

import mk.dmt.wms.event.AlarmEvent;
import mk.dmt.wms.model.SensorMeasurement;
import mk.dmt.wms.model.SensorType;
import mk.dmt.wms.repository.AlarmEventRepository;
import mk.dmt.wms.repository.SensorMeasurementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for MeasurementPersistenceService.
 * Tests the complete persistence flow with real database.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:application-test.properties")
class MeasurementPersistenceServiceTest {

    @Autowired
    private MeasurementPersistenceService persistenceService;

    @Autowired
    private SensorMeasurementRepository measurementRepository;

    @Autowired
    private AlarmEventRepository alarmRepository;

    @BeforeEach
    void setUp() {
        // Clean up before each test - commented out to avoid errors before Flyway runs
        measurementRepository.deleteAll().block();
        alarmRepository.deleteAll().block();
    }

    @Test
    void shouldPersistTemperatureMeasurement() {
        // Given
        SensorMeasurement measurement = new SensorMeasurement(
            "zone_a",
            SensorType.TEMPERATURE,
            25.5,
            Instant.now(),
            "warehouse-1"
        );

        // When & Then
        StepVerifier.create(persistenceService.saveMeasurement(measurement))
            .assertNext(entity -> {
                assertThat(entity.getId()).isNotNull();
                assertThat(entity.getSensorId()).isEqualTo("zone_a");
                assertThat(entity.getSensorType()).isEqualTo("TEMPERATURE");
                assertThat(entity.getValue()).isEqualTo(25.5);
                assertThat(entity.getWarehouseId()).isEqualTo("warehouse-1");
                assertThat(entity.getCreatedAt()).isNotNull();
            })
            .verifyComplete();

        // Verify it's actually in the database
        StepVerifier.create(measurementRepository.count())
            .expectNext(1L)
            .verifyComplete();
    }

    @Test
    void shouldPersistHumidityMeasurement() {
        // Given
        SensorMeasurement measurement = new SensorMeasurement(
            "zone_b",
            SensorType.HUMIDITY,
            45.0,
            Instant.now(),
            "warehouse-1"
        );

        // When & Then
        StepVerifier.create(persistenceService.saveMeasurement(measurement))
            .assertNext(entity -> {
                assertThat(entity.getSensorId()).isEqualTo("zone_b");
                assertThat(entity.getSensorType()).isEqualTo("HUMIDITY");
                assertThat(entity.getValue()).isEqualTo(45.0);
            })
            .verifyComplete();
    }

    @Test
    void shouldPersistAlarmEvent() {
        // Given
        SensorMeasurement measurement = new SensorMeasurement(
            "zone_a",
            SensorType.TEMPERATURE,
            36.5,
            Instant.now(),
            "warehouse-1"
        );
        AlarmEvent alarm = new AlarmEvent(measurement, 35.0, Instant.now(), AlarmEvent.Severity.HIGH);

        // When & Then
        StepVerifier.create(persistenceService.saveAlarm(alarm))
            .assertNext(entity -> {
                assertThat(entity.getId()).isNotNull();
                assertThat(entity.getSensorId()).isEqualTo("zone_a");
                assertThat(entity.getValue()).isEqualTo(36.5);
                assertThat(entity.getThreshold()).isEqualTo(35.0);
                assertThat(entity.getSeverity()).isEqualTo("HIGH");
            })
            .verifyComplete();

        // Verify it's actually in the database
        StepVerifier.create(alarmRepository.count())
            .expectNext(1L)
            .verifyComplete();
    }

    @Test
    void shouldRetrieveMeasurementHistory() {
        // Given - Save multiple measurements
        SensorMeasurement m1 = new SensorMeasurement("zone_a", SensorType.TEMPERATURE, 25.0, Instant.now(), "warehouse-1");
        SensorMeasurement m2 = new SensorMeasurement("zone_a", SensorType.TEMPERATURE, 26.0, Instant.now(), "warehouse-1");
        SensorMeasurement m3 = new SensorMeasurement("zone_b", SensorType.HUMIDITY, 45.0, Instant.now(), "warehouse-1");

        persistenceService.saveMeasurement(m1).block();
        persistenceService.saveMeasurement(m2).block();
        persistenceService.saveMeasurement(m3).block();

        // When & Then
        StepVerifier.create(persistenceService.getMeasurementHistory("zone_a", 10))
            .expectNextCount(2)
            .verifyComplete();
    }

    @Test
    void shouldRetrieveMeasurementsBySensorType() {
        // Given
        SensorMeasurement temp1 = new SensorMeasurement("zone_a", SensorType.TEMPERATURE, 25.0, Instant.now(), "warehouse-1");
        SensorMeasurement temp2 = new SensorMeasurement("zone_b", SensorType.TEMPERATURE, 26.0, Instant.now(), "warehouse-1");
        SensorMeasurement humidity = new SensorMeasurement("zone_c", SensorType.HUMIDITY, 45.0, Instant.now(), "warehouse-1");

        persistenceService.saveMeasurement(temp1).block();
        persistenceService.saveMeasurement(temp2).block();
        persistenceService.saveMeasurement(humidity).block();

        // When & Then
        StepVerifier.create(persistenceService.getMeasurementsBySensorType("TEMPERATURE", 10))
            .expectNextCount(2)
            .verifyComplete();
    }

    @Test
    void shouldRetrieveLatestMeasurements() {
        // Given
        SensorMeasurement m1 = new SensorMeasurement("zone_a", SensorType.TEMPERATURE, 25.0, Instant.now(), "warehouse-1");
        SensorMeasurement m2 = new SensorMeasurement("zone_b", SensorType.HUMIDITY, 45.0, Instant.now(), "warehouse-1");

        persistenceService.saveMeasurement(m1).block();
        persistenceService.saveMeasurement(m2).block();

        // When & Then
        StepVerifier.create(persistenceService.getLatestMeasurements(10))
            .expectNextCount(2)
            .verifyComplete();
    }

    @Test
    void shouldGetTotalMeasurementCount() {
        // Given
        SensorMeasurement m1 = new SensorMeasurement("zone_a", SensorType.TEMPERATURE, 25.0, Instant.now(), "warehouse-1");
        SensorMeasurement m2 = new SensorMeasurement("zone_b", SensorType.HUMIDITY, 45.0, Instant.now(), "warehouse-1");

        persistenceService.saveMeasurement(m1).block();
        persistenceService.saveMeasurement(m2).block();

        // When & Then
        StepVerifier.create(persistenceService.getTotalMeasurementCount())
            .expectNext(2L)
            .verifyComplete();
    }

    @Test
    void shouldGetTotalAlarmCount() {
        // Given
        SensorMeasurement measurement = new SensorMeasurement("zone_a", SensorType.TEMPERATURE, 36.5, Instant.now(), "warehouse-1");
        AlarmEvent alarm = new AlarmEvent(measurement, 35.0, Instant.now(), AlarmEvent.Severity.HIGH);

        persistenceService.saveAlarm(alarm).block();

        // When & Then
        StepVerifier.create(persistenceService.getTotalAlarmCount())
            .expectNext(1L)
            .verifyComplete();
    }

    @Test
    void shouldHandleMultipleMeasurementsConcurrently() {
        // Given - Create multiple measurements
        SensorMeasurement[] measurements = new SensorMeasurement[10];
        for (int i = 0; i < 10; i++) {
            measurements[i] = new SensorMeasurement(
                "zone_" + i,
                i % 2 == 0 ? SensorType.TEMPERATURE : SensorType.HUMIDITY,
                20.0 + i,
                Instant.now(),
                "warehouse-1"
            );
        }

        // When - Save all measurements
        for (SensorMeasurement measurement : measurements) {
            persistenceService.saveMeasurement(measurement).subscribe();
        }

        // Wait a bit for async operations
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Then - Verify all were saved
        StepVerifier.create(persistenceService.getTotalMeasurementCount())
            .expectNext(10L)
            .verifyComplete();
    }
}

