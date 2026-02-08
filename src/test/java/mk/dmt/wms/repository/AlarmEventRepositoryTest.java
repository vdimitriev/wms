package mk.dmt.wms.repository;

import mk.dmt.wms.model.AlarmEventEntity;
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
 * Integration test for AlarmEventRepository.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:application-test.properties")
class AlarmEventRepositoryTest {

    @Autowired
    private AlarmEventRepository repository;

    @BeforeEach
    void setUp() {
        // Clean up before each test - commented out to avoid errors before Flyway runs
        // repository.deleteAll().block();
    }

    @Test
    void shouldSaveAlarmEvent() {
        // Given
        AlarmEventEntity entity = new AlarmEventEntity(
            "zone_a",
            SensorType.TEMPERATURE.name(),
            36.5,
            35.0,
            "HIGH",
            Instant.now(),
            "warehouse-1"
        );

        // When & Then
        StepVerifier.create(repository.save(entity))
            .assertNext(saved -> {
                assertThat(saved.getId()).isNotNull();
                assertThat(saved.getSensorId()).isEqualTo("zone_a");
                assertThat(saved.getSensorType()).isEqualTo("TEMPERATURE");
                assertThat(saved.getValue()).isEqualTo(36.5);
                assertThat(saved.getThreshold()).isEqualTo(35.0);
                assertThat(saved.getSeverity()).isEqualTo("HIGH");
                assertThat(saved.getWarehouseId()).isEqualTo("warehouse-1");
            })
            .verifyComplete();
    }

    @Test
    void shouldFindBySensorId() {
        // Given
        AlarmEventEntity alarm1 = new AlarmEventEntity(
            "zone_a", SensorType.TEMPERATURE.name(), 36.5, 35.0, "HIGH", Instant.now(), "warehouse-1"
        );
        AlarmEventEntity alarm2 = new AlarmEventEntity(
            "zone_b", SensorType.HUMIDITY.name(), 55.0, 50.0, "HIGH", Instant.now(), "warehouse-1"
        );

        repository.save(alarm1).block();
        repository.save(alarm2).block();

        // When & Then
        StepVerifier.create(repository.findBySensorIdOrderByTimestampDesc("zone_a"))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    void shouldFindByTimestampAfter() {
        // Given
        Instant now = Instant.now();
        Instant oneHourAgo = now.minus(1, ChronoUnit.HOURS);
        Instant twoHoursAgo = now.minus(2, ChronoUnit.HOURS);

        AlarmEventEntity recent = new AlarmEventEntity(
            "zone_a", SensorType.TEMPERATURE.name(), 36.5, 35.0, "HIGH", now, "warehouse-1"
        );
        AlarmEventEntity old = new AlarmEventEntity(
            "zone_a", SensorType.TEMPERATURE.name(), 36.0, 35.0, "HIGH", twoHoursAgo, "warehouse-1"
        );

        repository.save(recent).block();
        repository.save(old).block();

        // When & Then
        StepVerifier.create(repository.findByTimestampAfterOrderByTimestampDesc(oneHourAgo))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    void shouldCountAlarms() {
        // Given
        AlarmEventEntity alarm1 = new AlarmEventEntity(
            "zone_a", SensorType.TEMPERATURE.name(), 36.5, 35.0, "HIGH", Instant.now(), "warehouse-1"
        );
        AlarmEventEntity alarm2 = new AlarmEventEntity(
            "zone_b", SensorType.HUMIDITY.name(), 55.0, 50.0, "HIGH", Instant.now(), "warehouse-1"
        );

        repository.save(alarm1).block();
        repository.save(alarm2).block();

        // When & Then
        StepVerifier.create(repository.count())
            .expectNext(2L)
            .verifyComplete();
    }
}

