package mk.dmt.wms.event;

import mk.dmt.wms.model.SensorMeasurement;
import mk.dmt.wms.model.SensorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

/**
 * Tests for MeasurementEventBus using StepVerifier.
 */
class MeasurementEventBusTest {

    private MeasurementEventBus eventBus;

    @BeforeEach
    void setUp() {
        eventBus = new MeasurementEventBus();
    }

    @Test
    @DisplayName("Should deliver measurement to subscriber")
    void shouldDeliverMeasurementToSubscriber() {
        SensorMeasurement measurement = SensorMeasurement.of("t1", SensorType.TEMPERATURE, 25.0);

        StepVerifier.create(eventBus.subscribe().take(1))
                .then(() -> eventBus.publish(measurement))
                .expectNext(measurement)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should deliver multiple measurements in order")
    void shouldDeliverMultipleMeasurementsInOrder() {
        SensorMeasurement m1 = SensorMeasurement.of("t1", SensorType.TEMPERATURE, 25.0);
        SensorMeasurement m2 = SensorMeasurement.of("h1", SensorType.HUMIDITY, 45.0);
        SensorMeasurement m3 = SensorMeasurement.of("t2", SensorType.TEMPERATURE, 30.0);

        StepVerifier.create(eventBus.subscribe().take(3))
                .then(() -> {
                    eventBus.publish(m1);
                    eventBus.publish(m2);
                    eventBus.publish(m3);
                })
                .expectNext(m1)
                .expectNext(m2)
                .expectNext(m3)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should complete when complete is called")
    void shouldCompleteWhenCompleteIsCalled() {
        StepVerifier.create(eventBus.subscribe())
                .then(() -> eventBus.complete())
                .verifyComplete();
    }
}
