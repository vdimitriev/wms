package mk.dmt.wms.repository;

import mk.dmt.wms.model.SensorMeasurementEntity;
import mk.dmt.wms.model.SensorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for SensorMeasurementRepository.
 * Tests database persistence with R2DBC and Flyway migrations.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:application-test.properties")
class SensorMeasurementRepositoryTest {

    @Autowired
    private SensorMeasurementRepository repository;

    @BeforeEach
    void setUp() {
        // Clean up before each test - commented out to avoid errors before Flyway runs
        repository.deleteAll().block();
    }

    @Test
    void shouldSaveMeasurement() {
        // Given
        SensorMeasurementEntity entity = new SensorMeasurementEntity(
            "zone_a",
            SensorType.TEMPERATURE.name(),
            25.5,
            Instant.now(),
            "warehouse-1"
        );

        // When & Then
        StepVerifier.create(repository.save(entity))
            .assertNext(saved -> {
                assertThat(saved.getId()).isNotNull();
                assertThat(saved.getSensorId()).isEqualTo("zone_a");
                assertThat(saved.getSensorType()).isEqualTo("TEMPERATURE");
                assertThat(saved.getValue()).isEqualTo(25.5);
                assertThat(saved.getWarehouseId()).isEqualTo("warehouse-1");
                assertThat(saved.getCreatedAt()).isNotNull();
            })
            .verifyComplete();
    }

    @Test
    void shouldFindBySensorId() {
        // Given
        SensorMeasurementEntity entity1 = new SensorMeasurementEntity(
            "zone_a", SensorType.TEMPERATURE.name(), 25.5, Instant.now(), "warehouse-1"
        );
        SensorMeasurementEntity entity2 = new SensorMeasurementEntity(
            "zone_a", SensorType.TEMPERATURE.name(), 26.0, Instant.now(), "warehouse-1"
        );
        SensorMeasurementEntity entity3 = new SensorMeasurementEntity(
            "zone_b", SensorType.HUMIDITY.name(), 45.0, Instant.now(), "warehouse-1"
        );

        repository.save(entity1).block();
        repository.save(entity2).block();
        repository.save(entity3).block();

        // When & Then
        StepVerifier.create(repository.findBySensorIdOrderByTimestampDesc("zone_a"))
            .expectNextCount(2)
            .verifyComplete();
    }

    @Test
    void shouldFindByTimestampAfter() {
        // Given
        Instant now = Instant.now();
        Instant oneHourAgo = now.minus(1, ChronoUnit.HOURS);
        Instant twoHoursAgo = now.minus(2, ChronoUnit.HOURS);

        SensorMeasurementEntity recent = new SensorMeasurementEntity(
            "zone_a", SensorType.TEMPERATURE.name(), 25.5, now, "warehouse-1"
        );
        SensorMeasurementEntity old = new SensorMeasurementEntity(
            "zone_a", SensorType.TEMPERATURE.name(), 24.0, twoHoursAgo, "warehouse-1"
        );

        repository.save(recent).block();
        repository.save(old).block();

        // When & Then
        StepVerifier.create(repository.findByTimestampAfterOrderByTimestampDesc(oneHourAgo))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    void shouldFindLatestBySensorType() {
        // Given
        SensorMeasurementEntity temp1 = new SensorMeasurementEntity(
            "zone_a", SensorType.TEMPERATURE.name(), 25.5, Instant.now(), "warehouse-1"
        );
        SensorMeasurementEntity temp2 = new SensorMeasurementEntity(
            "zone_b", SensorType.TEMPERATURE.name(), 26.0, Instant.now(), "warehouse-1"
        );
        SensorMeasurementEntity humidity = new SensorMeasurementEntity(
            "zone_c", SensorType.HUMIDITY.name(), 45.0, Instant.now(), "warehouse-1"
        );

        repository.save(temp1).block();
        repository.save(temp2).block();
        repository.save(humidity).block();

        // When & Then
        StepVerifier.create(repository.findLatestBySensorType("TEMPERATURE", 10))
            .expectNextCount(2)
            .verifyComplete();
    }

    @Test
    void shouldCountMeasurements() {
        // Given
        SensorMeasurementEntity entity1 = new SensorMeasurementEntity("zone_a", SensorType.TEMPERATURE.name(), 25.5, Instant.now(), "warehouse-1");
        SensorMeasurementEntity entity2 = new SensorMeasurementEntity("zone_b", SensorType.HUMIDITY.name(), 45.0, Instant.now(), "warehouse-1");

        repository.save(entity1).block();
        repository.save(entity2).block();

        // When & Then
        StepVerifier.create(repository.count())
            .expectNext(2L)
            .verifyComplete();
    }

    @Test
    void shouldHandleInstantTimestamp() {
        // Given
        Instant specificTime = Instant.parse("2026-02-08T10:30:00Z");
        SensorMeasurementEntity entity = new SensorMeasurementEntity("zone_a", SensorType.TEMPERATURE.name(), 25.5, specificTime, "warehouse-1");

        // When & Then
        StepVerifier.create(repository.save(entity))
            .assertNext(saved -> { assertThat(saved.getTimestamp()).isEqualTo(specificTime);})
            .verifyComplete();
    }
}

